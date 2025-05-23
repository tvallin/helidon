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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import io.helidon.common.UncheckedException;

import com.fasterxml.jackson.databind.ObjectMapper;

import static io.helidon.integrations.mcp.server.McpJsonRPC.METHOD_NOTIFICATION_CANCELED;
import static io.helidon.integrations.mcp.server.McpSessionImpl.State.INITIALIZED;
import static io.helidon.integrations.mcp.server.McpSessionImpl.State.INITIALIZING;
import static io.helidon.integrations.mcp.server.McpSessionImpl.State.UNINITIALIZED;

class McpSessionImpl implements McpSession {

	private static final System.Logger LOGGER = System.getLogger(McpSessionImpl.class.getName());

	private final Map<String, McpServer.JsonRPCHandler<?>> handlers;
	private final ObjectMapper mapper = new ObjectMapper();
	private final List<String> pendingResponses = new ArrayList<>();
	private final AtomicBoolean active = new AtomicBoolean(true);
	private final BlockingQueue<McpJsonRPC.JSONRPCMessage> queue = new LinkedBlockingQueue<>();
	private final AtomicReference<McpJsonRPC.Implementation> clientInfo = new AtomicReference<>();
	private final AtomicReference<McpJsonRPC.ClientCapabilities> clientCapabilities = new AtomicReference<>();

	private State state = UNINITIALIZED;

	McpSessionImpl(Map<String, McpServer.JsonRPCHandler<?>> handlers) {
		this.handlers = handlers;
	}

	@Override
	public void poll(Consumer<McpJsonRPC.JSONRPCMessage> consumer) {
		while (active.get()) {
			try {
				McpJsonRPC.JSONRPCMessage message = queue.take();
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
	public void send(McpJsonRPC.JSONRPCMessage event) {
		try {
			if (event instanceof McpJsonRPC.JSONRPCResponse response) {
				handleResponse(response);
				return;
			}
			if (event instanceof McpJsonRPC.JSONRPCNotification notification) {
				handleNotification(notification);
				return;
			}
			if (event instanceof McpJsonRPC.JSONRPCRequest request) {
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

	private void handleNotification(McpJsonRPC.JSONRPCNotification notification) {
		if (McpJsonRPC.METHOD_NOTIFICATION_INITIALIZED.equals(notification.method())) {
			state = INITIALIZED;
		}
		if (METHOD_NOTIFICATION_CANCELED.equals(notification.method())) {
			this.disonnect();
		}
	}

	private McpJsonRPC.JSONRPCResponse handleRequest(McpJsonRPC.JSONRPCRequest request) {
		if (McpJsonRPC.METHOD_INITIALIZE.equals(request.method())) {
			if (state == UNINITIALIZED) {
				state = INITIALIZING;
				var initializeRequest = mapper.convertValue(request.params(), McpJsonRPC.InitializeRequest.class);
				this.clientCapabilities.lazySet(initializeRequest.capabilities());
				this.clientInfo.lazySet(initializeRequest.clientInfo());
			}
		}
		var handler = handlers.get(request.method());
		if (handler == null) {
			var error = McpException.toError("Required method is not supported: " + request.method());
			return new McpJsonRPC.JSONRPCResponse(request.jsonrpc(), request.id(), null, error);
		}
		var result = handler.handle(request.params());
		return new McpJsonRPC.JSONRPCResponse(request.jsonrpc(), request.id(), result, null);
	}

	private void handleResponse(McpJsonRPC.JSONRPCResponse response) {
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
