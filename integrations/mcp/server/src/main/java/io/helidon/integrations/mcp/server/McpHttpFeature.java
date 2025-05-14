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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

import io.helidon.config.Config;
import io.helidon.http.Status;
import io.helidon.http.sse.SseEvent;
import io.helidon.webserver.http.HttpFeature;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.helidon.webserver.sse.SseSink;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;

public class McpHttpFeature implements HttpFeature {

	private static final System.Logger LOGGER = System.getLogger(McpHttpFeature.class.getName());

	private final McpServer server;
	private final ObjectMapper mapper = new ObjectMapper();
	private final Map<String, McpSession> sessions = new ConcurrentHashMap<>();

	public McpHttpFeature(Builder builder) {
		this.server = builder.server;
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public void setup(HttpRouting.Builder routing) {
		routing.get("/sse", this::sse)
				.post("/mcp/message", this::message)
				.post("/disconnect", this::disconnect);
	}

	private void disconnect(ServerRequest request, ServerResponse response) {
		String sessionId = request.query().get("sessionId");
		McpSession session = sessions.remove(sessionId);
		session.disonnect();
	}

	private void message(ServerRequest request, ServerResponse response) {
		String sessionId = request.query().get("sessionId");

		McpSession session = sessions.get(sessionId);
		if (session == null) {
			response.status(Status.NOT_FOUND_404);
			response.send();
			return;
		}

		String content = request.content().as(String.class);
		McpSchema.JSONRPCMessage message = deserializeJsonRpcMessage(content);
		LOGGER.log(System.Logger.Level.INFO, "Message received : {0}", message.toString());
		session.send(message);
		response.status(Status.OK_200);
		response.send();
	}

	private void sse(ServerRequest request, ServerResponse response) {
		String sessionId = UUID.randomUUID().toString();
		McpSession session = McpSession.create(server.handlers());
		sessions.put(sessionId, session);

		try (SseSink sink = response.sink(SseSink.TYPE)) {
			sink.emit(SseEvent.builder()
					.name("endpoint")
					.data("/mcp/message?sessionId=" + sessionId)
					.build());
			session.poll(message -> sink.emit(SseEvent.builder()
					.name("message")
					.data(message)
					.build()));
		}
	}

	private McpSchema.JSONRPCMessage deserializeJsonRpcMessage(String content) {
		try {
			return McpSchema.deserializeJsonRpcMessage(mapper, content);
		} catch (IOException e) {
			throw new McpException("Failed to deserialize JSON RPC message");
		}
	}

	public static class Builder {

		private McpServer server;

		public Builder mcpServer(Consumer<McpServerConfig.Builder> builderConsumer) {
			McpServerConfig.Builder builder = McpServer.builder();
			builderConsumer.accept(builder);
			this.server = builder.build();
			return this;
		}

		public McpHttpFeature build() {
			return new McpHttpFeature(this);
		}
	}

}
