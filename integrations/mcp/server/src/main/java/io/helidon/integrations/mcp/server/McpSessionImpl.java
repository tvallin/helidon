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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import io.helidon.integrations.mcp.server.spi.McpTransport;
import io.helidon.integrations.mcp.server.McpServer.RequestHandler;

import io.modelcontextprotocol.spec.McpSchema;

import static io.helidon.integrations.mcp.server.McpSessionImpl.State.INITIALIZED;
import static io.helidon.integrations.mcp.server.McpSessionImpl.State.INITIALIZING;
import static io.helidon.integrations.mcp.server.McpSessionImpl.State.UNINITIALIZED;

class McpSessionImpl implements McpSession {

	private static final System.Logger LOGGER = System.getLogger(McpSessionImpl.class.getName());

	private final McpTransport transport;
	private final AtomicLong requestCounter = new AtomicLong(0);
	private final Map<String, RequestHandler<?>> handlers;
	private final AtomicReference<McpSchema.Implementation> clientInfo = new AtomicReference<>();
	private final AtomicReference<McpSchema.ClientCapabilities> clientCapabilities = new AtomicReference<>();
	private final List<String> pendingResponses = new ArrayList<>();

	private State state = UNINITIALIZED;

	McpSessionImpl(McpTransport transport,
				   Map<String, RequestHandler<?>> handlers) {
		this.transport = transport;
		this.handlers = handlers;
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
				var initializeRequest = transport.unmarshall(request.params(), McpSchema.InitializeRequest.class);
				this.clientCapabilities.lazySet(initializeRequest.capabilities());
				this.clientInfo.lazySet(initializeRequest.clientInfo());
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
		boolean expected = pendingResponses.remove(response.id().toString());
		if (expected) {
			return;
		}
		LOGGER.log(System.Logger.Level.DEBUG, "Unexpected response type: " + response.id());
		//handle(transport.unmarshall(response.result(), clazz));
	}

	@Override
	public void sendRequest(String method, Object params) {
		String requestId = generateId();
		this.pendingResponses.add(requestId);
		McpSchema.JSONRPCRequest jsonrpcRequest = new McpSchema.JSONRPCRequest(McpSchema.JSONRPC_VERSION, method,
				requestId, params);
		transport.sendMessage(jsonrpcRequest);
	}

	@Override
	public void sendNotification(String method, Map<String, Object> params) {
		McpSchema.JSONRPCNotification jsonrpcNotification = new McpSchema.JSONRPCNotification(McpSchema.JSONRPC_VERSION,
				method, params);
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
