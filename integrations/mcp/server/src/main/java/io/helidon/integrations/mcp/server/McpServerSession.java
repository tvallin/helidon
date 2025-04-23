/*
 * Copyright (c) 2025 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.integrations.mcp.server;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import io.helidon.integrations.mcp.server.spi.McpTransport;

import io.modelcontextprotocol.spec.McpSchema;

import static io.helidon.integrations.mcp.server.McpServerSession.State.INITIALIZED;
import static io.helidon.integrations.mcp.server.McpServerSession.State.INITIALIZING;
import static io.helidon.integrations.mcp.server.McpServerSession.State.UNINITIALIZED;

class McpServerSession implements McpSession {

	private static final System.Logger LOGGER = System.getLogger(McpServerSession.class.getName());

	private final McpTransport transport;
	private final InitializationHandler initializationHandler;
	private final AtomicLong requestCounter = new AtomicLong(0);
	private final Map<String, RequestHandler<?>> handlers;
	private final AtomicReference<McpSchema.Implementation> clientInfo = new AtomicReference<>();
	private final AtomicReference<McpSchema.ClientCapabilities> clientCapabilities = new AtomicReference<>();
	private final ConcurrentHashMap<String, Class<?>> pendingResponses = new ConcurrentHashMap<>();


	private State state = UNINITIALIZED;

	McpServerSession(McpTransport transport,
					 InitializationHandler initializationHandler,
					 Map<String, RequestHandler<?>> handlers) {
		this.transport = transport;
		this.handlers = handlers;
		this.initializationHandler = initializationHandler;
	}

	@Override
	public void handle(Object message) {
		if (message instanceof McpSchema.JSONRPCResponse response) {
			handleResponse(response);
			return;
		}
		if (message instanceof McpSchema.JSONRPCRequest request) {
			handleRequest(request);
			return;
		}
		if (message instanceof McpSchema.JSONRPCNotification notification) {
			handleNotification(notification);
			return;
		}
		LOGGER.log(System.Logger.Level.DEBUG, "Unexpected message type: " + message.getClass());
//		throw new McpException("Unexpected message type: " + message.getClass());
	}

	private void handleNotification(McpSchema.JSONRPCNotification notification) {
		if (McpSchema.METHOD_NOTIFICATION_INITIALIZED.equals(notification.method())) {
			state = INITIALIZED;
		}
	}

	private void handleRequest(McpSchema.JSONRPCRequest request) {
		if (McpSchema.METHOD_INITIALIZE.equals(request.method())) {
			if (state == UNINITIALIZED) {
				state = INITIALIZING;
				McpSchema.InitializeRequest initializeRequest = transport.unmarshall(request.params(), McpSchema.InitializeRequest.class);
				this.clientCapabilities.lazySet(initializeRequest.capabilities());
				this.clientInfo.lazySet(initializeRequest.clientInfo());
				McpSchema.InitializeResult result = initializationHandler.handle(initializeRequest);
				McpSchema.JSONRPCResponse response = new McpSchema.JSONRPCResponse(
						request.jsonrpc(),
						request.id(),
						result,
						null);
				transport.sendMessage(response);
				return;
			}
		}
		var handler = handlers.get(request.method());
		if (handler == null) {
			//TODO - send error
			McpSchema.JSONRPCResponse.JSONRPCError error = McpException.toError("");
			transport.sendMessage(new McpSchema.JSONRPCResponse(request.jsonrpc(), request.id(), null, error));
		}
		var result = handler.handle(request.params());
		transport.sendMessage(new McpSchema.JSONRPCResponse(request.jsonrpc(), request.id(), result, null));
	}

	private void handleResponse(McpSchema.JSONRPCResponse response) {
		//TODO - Can this throw a NPE ?
		Class<?> clazz = pendingResponses.remove(response.id());
		if (clazz == null) {
			LOGGER.log(System.Logger.Level.DEBUG, "Unexpected response type: " + response.id());
			return;
		}
		//handle(transport.unmarshall(response.result(), clazz));
	}

	@Override
	public <T> T sendRequest(String method, Object request, Class<T> clazz) {
		String requestId = generateId();
		this.pendingResponses.put(requestId, clazz);
		McpSchema.JSONRPCRequest jsonrpcRequest = new McpSchema.JSONRPCRequest(McpSchema.JSONRPC_VERSION, method,
				requestId, request);
		transport.sendMessage(jsonrpcRequest);
		return null;
	}

	@Override
	public void sendNotification(String method, Object params) {
		//TODO - Shall we send the params as map ?
		McpSchema.JSONRPCNotification jsonrpcNotification = new McpSchema.JSONRPCNotification(McpSchema.JSONRPC_VERSION,
				method, null);
		this.transport.sendMessage(jsonrpcNotification);
	}

	@Override
	public void closeGracefully() {
		this.transport.closeGracefully();
	}

	@Override
	public void close() {
		this.transport.close();
	}

	private String generateId() {
		return UUID.randomUUID().toString() + this.requestCounter.getAndIncrement();
	}

	enum State {
		INITIALIZED,
		INITIALIZING,
		UNINITIALIZED
	}
}
