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

import jakarta.json.Json;
import jakarta.json.JsonObject;

import static io.helidon.integrations.mcp.server.McpJsonRPC.METHOD_NOTIFICATION_CANCELED;
import static io.helidon.integrations.mcp.server.McpSession.State.INITIALIZED;
import static io.helidon.integrations.mcp.server.McpSession.State.INITIALIZING;
import static io.helidon.integrations.mcp.server.McpSession.State.UNINITIALIZED;

class McpSession {

	private static final System.Logger LOGGER = System.getLogger(McpSession.class.getName());

	private final Map<String, McpServerImpl.JsonRPCHandler> handlers;
	private final List<String> pendingResponses = new ArrayList<>();
	private final AtomicBoolean active = new AtomicBoolean(true);
	private final BlockingQueue<JsonObject> queue = new LinkedBlockingQueue<>();
	private final AtomicReference<JsonObject> clientInfo = new AtomicReference<>();
	private final AtomicReference<JsonObject> clientCapabilities = new AtomicReference<>();

	private State state = UNINITIALIZED;

	McpSession(Map<String, McpServerImpl.JsonRPCHandler> handlers) {
		this.handlers = handlers;
	}

	void poll(Consumer<JsonObject> consumer) {
		while (active.get()) {
			try {
				JsonObject message = queue.take();
				if (message.getBoolean("disconnect", false)) {
					break;
				}
				consumer.accept(message);
			} catch (InterruptedException e) {
				throw new UncheckedException(e);
			}
		}
	}

	void send(JsonObject event) {
		try {
			if (event.containsKey("method") && event.containsKey("id")) {
				event = handleRequest(event);
			} else if (event.containsKey("method") && !event.containsKey("id")) {
				handleNotification(event);
			} else if (event.containsKey("result") || event.containsKey("error")) {
				handleResponse(event);
			}
			queue.put(event);
		} catch (InterruptedException e) {
			throw new UncheckedException(e);
		}
	}

	void disonnect() {
		LOGGER.log(System.Logger.Level.INFO, "Disconnecting session");
		if (active.compareAndSet(true, false)) {
			queue.offer(Json.createObjectBuilder().add("disconnect", "true").build());
			return;
		}
		LOGGER.log(System.Logger.Level.DEBUG, "Session is already disconnected.");
	}

	private void handleNotification(JsonObject notification) {
		String method = notification.getString("method");
		if (McpJsonRPC.METHOD_NOTIFICATION_INITIALIZED.equals(method)) {
			state = INITIALIZED;
		}
		if (METHOD_NOTIFICATION_CANCELED.equals(method)) {
			this.disonnect();
		}
	}

	private JsonObject handleRequest(JsonObject request) {
		String method = request.getString("method");
		if (McpJsonRPC.METHOD_INITIALIZE.equals(method)) {
			if (state == UNINITIALIZED) {
				state = INITIALIZING;
				JsonObject param = request.getJsonObject("params");
				this.clientCapabilities.lazySet(param.getJsonObject("capabilities"));
				this.clientInfo.lazySet(param.getJsonObject("clientInfo"));
			}
		}
		var handler = handlers.get(method);
		if (handler == null) {
			return Json.createObjectBuilder()
					.add("jsonrpc", request.getString("jsonrpc"))
					.add("id", request.get("id"))
					.add("error", "Required method is not supported: " + method)
					.build();
		}
		var result = handler.handle(request.getJsonObject("params"));
		if (result.containsKey("error")) {
			return Json.createObjectBuilder()
					.add("jsonrpc", request.getString("jsonrpc"))
					.add("id", request.get("id"))
					.add("error", Json.createObjectBuilder(result))
					.build();
		}
		return Json.createObjectBuilder()
				.add("jsonrpc", request.getString("jsonrpc"))
				.add("id", request.get("id"))
				.add("result", Json.createObjectBuilder(result))
				.build();
	}

	private void handleResponse(JsonObject response) {
		pendingResponses.remove(response.getString("id"));
	}

	enum State {
		INITIALIZED,
		INITIALIZING,
		UNINITIALIZED
	}
}
