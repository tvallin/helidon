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

package io.helidon.integrations.mcp.server.spi;

import java.util.Map;

import io.helidon.integrations.mcp.server.McpSession;

public interface McpTransportProvider {
	/**
	 * Sets the session factory that will be used to create sessions for new clients.
	 *
	 * @param factory the session factory to be used
	 */
	void setSessionFactory(McpSession.Factory factory);

	/**
	 * Sends a notification to all connected clients.
	 *
	 * @param method the name of the notification method to be called on the clients
	 * @param params parameters to be sent with the notification
	 */
	void notifyClients(String method, Map<String, Object> params);

	/**
	 * Close the sessions.
	 */
	void closeGracefully();

	/**
	 * Close the sessions.
	 */
	void close();
}
