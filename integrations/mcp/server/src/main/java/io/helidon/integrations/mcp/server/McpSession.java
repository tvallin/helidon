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

import io.helidon.integrations.mcp.server.spi.McpTransport;

public interface McpSession {

	<T> T sendRequest(String method, Object request, Class<T> clazz);

	void sendNotification(String method, Object params);

	/**
	 * Handle incoming client message.
	 *
	 * @param message client message
	 */
	<T> void handle(T message);

	void closeGracefully();

	void close();

	@FunctionalInterface
	interface Factory {
		McpSession create(McpTransport transport);
	}
}
