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

import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import io.helidon.http.sse.SseEvent;
import io.helidon.webserver.http.ServerResponse;
import io.helidon.webserver.sse.SseSink;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;

import static io.helidon.integrations.mcp.server.McpServerImpl.write;
import static io.helidon.integrations.mcp.server.McpServerSession.State.INITIALIZED;
import static io.helidon.integrations.mcp.server.McpServerSession.State.INITIALIZING;
import static io.helidon.integrations.mcp.server.McpServerSession.State.UNINITIALIZED;

class McpServerSession implements McpSession {

	private final String id;
	private final ObjectMapper mapper = new ObjectMapper();
	private final OutputStream outputStream;
	private final Map<String, ClientRequestHandler<McpSchema.JSONRPCMessage>> messageHandlers;
	private final Supplier<McpSchema.InitializeResult> initRequestHandler;
	private final AtomicReference<McpSchema.Implementation> clientInfo = new AtomicReference<>();
	private final AtomicReference<McpSchema.ClientCapabilities> clientCapabilities = new AtomicReference<>();

	private State state = UNINITIALIZED;

	McpServerSession(String id,
					 OutputStream outputStream,
					 Map<String, ClientRequestHandler<McpSchema.JSONRPCMessage>> messageHandlers,
					 Supplier<McpSchema.InitializeResult> initRequestHandler) {
		this.id = id;
		this.outputStream = outputStream;
		this.messageHandlers = messageHandlers;
		this.initRequestHandler = initRequestHandler;

	}

	@Override
	public SseSink sendRequest(SseEvent event) {
		return null;
	}

	@Override
	public void handle(McpSchema.JSONRPCMessage message, SseSink sink) {
		if (message instanceof McpSchema.JSONRPCResponse response) {
			handleResponse(response, sink);
			return;
		}
		if (message instanceof McpSchema.JSONRPCRequest request) {
			handleRequest(request, sink);
			return;
		}
		if (message instanceof McpSchema.JSONRPCNotification) {
			handleNotification();
			return;
		}
		throw new McpException("Unexpected message type: " + message.getClass());
	}

	private void handleNotification() {
		state = INITIALIZED;
	}

	private void handleRequest(McpSchema.JSONRPCRequest request, SseSink sink) {
		if (McpSchema.METHOD_INITIALIZE.equals(request.method())) {
			if (state == UNINITIALIZED) {
				McpSchema.InitializeRequest initializeRequest = mapper.convertValue(request.params(), McpSchema.InitializeRequest.class);
				this.clientCapabilities.lazySet(initializeRequest.capabilities());
				this.clientInfo.lazySet(initializeRequest.clientInfo());
				state = INITIALIZING;
				McpSchema.InitializeResult result = initRequestHandler.get();
				try {
					McpSchema.JSONRPCResponse response = new McpSchema.JSONRPCResponse(
							request.jsonrpc(),
							request.id(),
							result,
							null);
					String payload = mapper.writeValueAsString(response);
					sink.emit(SseEvent.builder()
							.name("message")
							.data(payload)
							.build())
						.close();
					return;
				} catch (JsonProcessingException exception) {
					throw new RuntimeException(exception);
				}
			}
			sink.emit(SseEvent.builder()
					.id(id)
					.name("Error")
					.data("Client initialization failed")
					.build())
					.close();
		}
		messageHandlers
				.get(request.method())
				.handle(request, request.params(), sink);

	}

	private void handleResponse(McpSchema.JSONRPCResponse response, SseSink sink) {
		String id = (String) response.id();
		if (Objects.equals(this.id, id)) {
			//Todo - build this event
				/*
				{
				  "jsonrpc": "2.0",
				  "id": 1,
				  "result": {
					"protocolVersion": "2024-11-05",
					"capabilities": {
					  "logging": {},
					  "prompts": {
						"listChanged": true
					  },
					  "resources": {
						"subscribe": true,
						"listChanged": true
					  },
					  "tools": {
						"listChanged": true
					  }
					},
					"serverInfo": {
					  "name": "ExampleServer",
					  "version": "1.0.0"
					},
					"instructions": "Optional instructions for the client"
				  }
				}
				 */
			sink.emit(SseEvent.builder().build()).close();
		}
	}

	enum State {
		INITIALIZED,
		INITIALIZING,
		UNINITIALIZED
	}
}
