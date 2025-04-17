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

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import io.helidon.http.Status;
import io.helidon.http.sse.SseEvent;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.helidon.webserver.sse.SseSink;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;

import static io.helidon.http.HeaderNames.MCP_SESSION_ID;

public class McpServerImpl implements McpServer {

	private final WebServer server;
	private final McpServerConfig config;
	private final ObjectMapper mapper = new ObjectMapper();
	private final Map<String, ClientRequestHandler<McpSchema.JSONRPCMessage>> sseRouting = new HashMap<>();
	private final Map<String, McpServerSession> sessions = new ConcurrentHashMap<>();

	McpServerImpl(McpServerConfig config) {
		this.config = config;
		this.server = WebServerConfig.builder()
				.routing(routing -> routing
						.get("/sse", this::get)
						.post("/endpoint", this::post))
				.build();
		sseRouting();
	}

	/**
	 * Handler to create a server session.
	 *
	 * @param request  server request
	 * @param response server response
	 */
	private void get(ServerRequest request, ServerResponse response) {
		String sessionId = UUID.randomUUID().toString();
		SseEvent initialEvent = SseEvent.builder()
				.name("endpoint")
				.data(String.format("/endpoint?sessionId=%s", sessionId))
				.build();

		sessions.put(sessionId, new McpServerSession(sessionId, response.outputStream(), sseRouting, this::initialize));
		response.sink(SseSink.TYPE).emit(initialEvent).close();
	}

	/**
	 * Handler to forward client payload to proper session.
	 *
	 * @param request  server request
	 * @param response server response
	 */
	private void post(ServerRequest request, ServerResponse response) {
		AtomicReference<String> sessionId = new AtomicReference<>();

		try {
			sessionId.set(request.query().get("sessionId"));
		} catch (NoSuchElementException exception) {
			request.headers()
					.find(MCP_SESSION_ID)
					.ifPresentOrElse(header -> sessionId.set(header.values()),
							() -> response.status(Status.BAD_REQUEST_400).send());
		}

		McpServerSession session = sessions.get(sessionId.get());
		if (session == null) {
			response.status(Status.NOT_FOUND_404);
			response.send();
			return;
		}

		String content = request.content().as(String.class);
		McpSchema.JSONRPCMessage message = deserializeJsonRpcMessage(content);
		session.handle(message, response.sink(SseSink.TYPE));

	}

	static void write(OutputStream os, String event, String data) {
		try {
			os.write(("event: " + event + "\ndata: " + data + "\n\n").getBytes());
			os.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void sseRouting() {
		sseRouting.put(McpSchema.METHOD_PING, ClientRequestHandlers::ping);

		// Add tools API handlers if the tool capability is enabled
//		if (this.config.capabilities().tools().listChanged()) {
//			requestHandlers.put(McpSchema.METHOD_TOOLS_LIST, toolsListRequestHandler());
//			requestHandlers.put(McpSchema.METHOD_TOOLS_CALL, toolsCallRequestHandler());
//		}
//
//		// Add resources API handlers if provided
//		if (this.serverCapabilities.resources() != null) {
//			requestHandlers.put(McpSchema.METHOD_RESOURCES_LIST, resourcesListRequestHandler());
//			requestHandlers.put(McpSchema.METHOD_RESOURCES_READ, resourcesReadRequestHandler());
//			requestHandlers.put(McpSchema.METHOD_RESOURCES_TEMPLATES_LIST, resourceTemplateListRequestHandler());
//		}
//
//		// Add prompts API handlers if provider exists
//		if (this.serverCapabilities.prompts() != null) {
//			requestHandlers.put(McpSchema.METHOD_PROMPT_LIST, promptsListRequestHandler());
//			requestHandlers.put(McpSchema.METHOD_PROMPT_GET, promptsGetRequestHandler());
//		}
//
//		// Add logging API handlers if the logging capability is enabled
//		if (this.serverCapabilities.logging() != null) {
//			requestHandlers.put(McpSchema.METHOD_LOGGING_SET_LEVEL, setLoggerRequestHandler());
//		}

	}

	private McpSchema.JSONRPCMessage deserializeJsonRpcMessage(String content) {
		try {
			return McpSchema.deserializeJsonRpcMessage(mapper, content);
		} catch (IOException e) {
			throw new McpException("Failed to deserialize JSON RPC message");
		}
	}



	@Override
	public McpServer start() {
		server.start();
		return this;
	}

	@Override
	public String baseUri() {
		return "http://localhost:" + server.port();
	}

	@Override
	public McpServerConfig prototype() {
		return config;
	}

	public McpSchema.InitializeResult initialize() {
		return McpSchemaMapper.initializeResult(
				McpSchema.LATEST_PROTOCOL_VERSION,
				config.capabilities(),
				config.implementation(),
				config.instructions());
	}

}
