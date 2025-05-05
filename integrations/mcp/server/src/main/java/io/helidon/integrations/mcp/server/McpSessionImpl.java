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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;

import io.helidon.common.UncheckedException;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;

import static io.helidon.integrations.mcp.server.McpSessionImpl.State.INITIALIZED;
import static io.helidon.integrations.mcp.server.McpSessionImpl.State.INITIALIZING;
import static io.helidon.integrations.mcp.server.McpSessionImpl.State.UNINITIALIZED;

class McpSessionImpl implements McpSession {

	private static final System.Logger LOGGER = System.getLogger(McpSessionImpl.class.getName());

	private final Map<String, McpServer.RequestHandler<?>> handlers;
	private final ObjectMapper mapper = new ObjectMapper();
	private final List<String> pendingResponses = new ArrayList<>();
	private final AtomicBoolean active = new AtomicBoolean(true);
	private final BlockingQueue<McpSchema.JSONRPCMessage> queue = new LinkedBlockingQueue<>();
	private final AtomicReference<McpSchema.Implementation> clientInfo = new AtomicReference<>();
	private final AtomicReference<McpSchema.ClientCapabilities> clientCapabilities = new AtomicReference<>();

	private State state = UNINITIALIZED;

	McpSessionImpl(Map<String, McpServer.RequestHandler<?>> handlers) {
		this.handlers = handlers;
	}

	@Override
	public void poll(Consumer<McpSchema.JSONRPCMessage> consumer) {
		while (active.get()) {
			try {
				McpSchema.JSONRPCMessage message = queue.take();
//				if (message instanceof ClosingMessage) {
//					break;
//				}
				consumer.accept(message);
			} catch (InterruptedException e) {
				throw new UncheckedException(e);
			}
		}
	}

	@Override
	public void send(McpSchema.JSONRPCMessage event) {
		try {
			if (event instanceof McpSchema.JSONRPCResponse response) {
				handleResponse(response);
				return;
			}
			if (event instanceof McpSchema.JSONRPCNotification notification) {
				handleNotification(notification);
				return;
			}
			if (event instanceof McpSchema.JSONRPCRequest request) {
				event = handleRequest(request);
			}
			queue.put(event);
		} catch (InterruptedException e) {
			throw new UncheckedException(e);
		}
	}

	@Override
	public void disonnect() {
		LOGGER.log(System.Logger.Level.INFO, "Disconnect session");
		if (active.compareAndSet(true, false)) {
			//queue.offer(ClosingMessage)
			return;
		}
		LOGGER.log(System.Logger.Level.DEBUG, "Session is already disconnected.");
	}

	private void handleNotification(McpSchema.JSONRPCNotification notification) {
		if (McpSchema.METHOD_NOTIFICATION_INITIALIZED.equals(notification.method())) {
			state = INITIALIZED;
		}
		if ("notifications/cancelled".equals(notification.method())) {
			this.disonnect();
		}
	}

	private McpSchema.JSONRPCResponse handleRequest(McpSchema.JSONRPCRequest request) {
		if (McpSchema.METHOD_INITIALIZE.equals(request.method())) {
			if (state == UNINITIALIZED) {
				state = INITIALIZING;
				var initializeRequest = mapper.convertValue(request.params(), McpSchema.InitializeRequest.class);
				this.clientCapabilities.lazySet(initializeRequest.capabilities());
				this.clientInfo.lazySet(initializeRequest.clientInfo());
			}
		}
		var handler = handlers.get(request.method());
		if (handler == null) {
			var error = McpException.toError("Required method is not supported: " + request.method());
			return new McpSchema.JSONRPCResponse(request.jsonrpc(), request.id(), null, error);
		}
		var result = handler.handle(request.params());
		return new McpSchema.JSONRPCResponse(request.jsonrpc(), request.id(), result, null);
	}

	private void handleResponse(McpSchema.JSONRPCResponse response) {
		boolean expected = pendingResponses.remove(response.id().toString());
		if (expected) {
			return;
		}
		LOGGER.log(System.Logger.Level.DEBUG, "Unexpected response type: " + response.id());
	}

	enum State {
		INITIALIZED,
		INITIALIZING,
		UNINITIALIZED
	}
}
