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

import io.helidon.integrations.mcp.server.McpSession;


public interface McpTransportProvider {
	/**
	 * Sets the session factory that will be used to create sessions for new clients. An
	 * implementation of the MCP server MUST call this method before any MCP interactions
	 * take place.
	 * @param factory the session factory to be used for initiating client sessions
	 */
	void setSessionFactory(McpSession.Factory factory);

	/**
	 * Sends a notification to all connected clients.
	 * @param method the name of the notification method to be called on the clients
	 * @param params parameters to be sent with the notification
	 * @return a Mono that completes when the notification has been broadcast
	 */
	void notifyClients(String method, Object params);

	/**
	 * Close transport once all operations are processed.
	 */
	void closeGracefully();

	/**
	 * Immediately closes all the transports with connected clients and releases any
	 * associated resources.
	 */
	void close();
}
