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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.helidon.integrations.mcp.server.spi.McpTransport;
import io.helidon.integrations.mcp.server.spi.McpTransportProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;

public class StdioTransportProvider implements McpTransportProvider {

	private static final System.Logger LOGGER = System.getLogger(StdioTransportProvider.class.getName());

	private final AtomicBoolean closing;
	private final InputStream inputStream;
	private final OutputStream outputStream;

	private McpSession session;

	public StdioTransportProvider() {
		this.inputStream = System.in;
		this.outputStream = System.out;
		this.closing = new AtomicBoolean(false);
	}

	StdioTransportProvider(InputStream is, OutputStream os) {
		this.inputStream = is;
		this.outputStream = os;
		this.closing = new AtomicBoolean(false);
	}

	@Override
	public void setSessionFactory(McpSession.Factory factory) {
		var transport = new StdioTransport();
		this.session = factory.create(transport);
		transport.initProcessing();
	}

	@Override
	public void notifyClients(String method, Object params) {
		this.session.sendNotification(method, params);
	}

	@Override
	public void closeGracefully() {
		this.session.closeGracefully();
	}

	@Override
	public void close() {
		this.session.close();
	}

	private class StdioTransport implements McpTransport {

		private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		private final ObjectMapper mapper = new ObjectMapper();

		StdioTransport() {
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

				LOGGER.log(System.Logger.Level.INFO, "Message to send: " + jsonMessage);

				outputStream.write(jsonMessage.getBytes(StandardCharsets.UTF_8));
				outputStream.write("\n".getBytes(StandardCharsets.UTF_8));
				outputStream.flush();
			} catch (IOException e) {
				LOGGER.log(System.Logger.Level.ERROR, "Error writing to stdout", e);
			}
		}

		@Override
		public void close() {
			executor.shutdownNow();
			closing.set(true);
		}

		@Override
		public void closeGracefully() {
			executor.shutdown();
			closing.set(true);
		}

		public void initProcessing() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				while (!closing.get()) {
					try {
						String line = reader.readLine();
						if (line == null || closing.get()) {
							break;
						}

						if (LOGGER.isLoggable(System.Logger.Level.DEBUG)) {
							LOGGER.log(System.Logger.Level.DEBUG, "Received JSON message:" + line);
						}

						try {
							McpSchema.JSONRPCMessage message = McpSchema.deserializeJsonRpcMessage(mapper, line);
							session.handle(message);
						} catch (Exception e) {
							LOGGER.log(System.Logger.Level.ERROR,"Error processing inbound message", e);
							break;
						}
					} catch (IOException e) {
						LOGGER.log(System.Logger.Level.ERROR, "Error reading from stdin", e);
						break;
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("Error in inbound processing", e);
			} finally {
				closing.set(true);
				if (session != null) {
					session.close();
				}
				executor.shutdown();
				LOGGER.log(System.Logger.Level.INFO, "Session closed.");
			}
		}
	}
}
