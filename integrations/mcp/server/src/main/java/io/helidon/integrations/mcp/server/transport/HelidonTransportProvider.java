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

package io.helidon.integrations.mcp.server.transport;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.helidon.http.Status;
import io.helidon.integrations.mcp.server.McpException;
import io.helidon.integrations.mcp.server.McpSession;
import io.helidon.integrations.mcp.server.spi.McpTransport;
import io.helidon.integrations.mcp.server.spi.McpTransportProvider;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;

public class HelidonTransportProvider implements McpTransportProvider {

	private static final System.Logger LOGGER = System.getLogger(HelidonTransportProvider.class.getName());

	private final WebServer server;
	private final ObjectMapper mapper = new ObjectMapper();
	private final Map<String, McpSession> sessions = new ConcurrentHashMap<>();

	private McpSession.Factory factory;

	public HelidonTransportProvider() {
		server = WebServerConfig.builder()
				.addRouting(HttpRouting.builder()
						.get("/sse", new GetHandler())
						.post("/message", new PostHandler()))
				.build()
				.start();
	}

	@Override
	public void setSessionFactory(McpSession.Factory factory) {
		this.factory = factory;
	}

	@Override
	public void notifyClients(String method, Map<String, Object> params) {
		sessions.values().forEach(session -> session.sendNotification(method, params));
	}

	@Override
	public void closeGracefully() {
		sessions.values().forEach(McpSession::closeGracefully);
		server.stop();
	}

	@Override
	public void close() {
		sessions.values().forEach(McpSession::close);
		server.stop();
	}

	private void sendEvent(OutputStream outputStream, String eventType, String data) throws IOException {
		outputStream.write(("event: " + eventType + "\n").getBytes(StandardCharsets.UTF_8));
		outputStream.write(("data: " + data + "\n\n").getBytes(StandardCharsets.UTF_8));
		outputStream.flush();
	}

	/**
	 * Transport managing SSE stream to client.
	 */
	private class HelidonTransport implements McpTransport {

		private final OutputStream outputStream;

		HelidonTransport(OutputStream outputStream) {
			this.outputStream = outputStream;
		}

		@Override
		public <T> T unmarshall(Object params, Class<T> clazz) {
			return mapper.convertValue(params, clazz);
		}

		@Override
		public void sendMessage(Object message) {
			try {
				String jsonMessage = mapper.writeValueAsString(message);
				// Escape any embedded newlines in the JSON message as per spec
				jsonMessage = jsonMessage.replace("\r\n", "\\n")
						.replace("\n", "\\n")
						.replace("\r", "\\n");
				sendEvent(outputStream, "message", jsonMessage);
			} catch (IOException e) {
				LOGGER.log(System.Logger.Level.ERROR, "Error writing to response outputstream", e);
			}
		}

		@Override
		public void closeGracefully() {
			try {
				outputStream.flush();
				outputStream.close();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		@Override
		public void close() {
			try {
				outputStream.close();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	private class GetHandler implements Handler {

		@Override
		public void handle(ServerRequest request, ServerResponse response) throws Exception {
			String sessionId = UUID.randomUUID().toString();
			McpTransport transport = new HelidonTransport(response.outputStream());
			McpSession session = factory.create(transport);
			sessions.put(sessionId, session);
			sendEvent(response.outputStream(), "endpoint", "/endpoint?sessionId=" + sessionId);
			//TODO keep the connection alive somehow ...
		}
	}

	private class PostHandler implements Handler {

		@Override
		public void handle(ServerRequest request, ServerResponse response) throws Exception {
			//Spec 2024-11-05
			String sessionId = request.query().get("sessionId");

			McpSession session = sessions.get(sessionId);
			if (session == null) {
				response.status(Status.NOT_FOUND_404);
				response.send();
				return;
			}

			String content = request.content().as(String.class);
			McpSchema.JSONRPCMessage message = deserializeJsonRpcMessage(content);
			session.handle(message);
		}

		private McpSchema.JSONRPCMessage deserializeJsonRpcMessage(String content) {
			try {
				return McpSchema.deserializeJsonRpcMessage(mapper, content);
			} catch (IOException e) {
				throw new McpException("Failed to deserialize JSON RPC message");
			}
		}
	}
}
