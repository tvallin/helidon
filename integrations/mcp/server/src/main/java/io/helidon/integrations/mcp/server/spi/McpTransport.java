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

public interface McpTransport {

	/**
	 * Cast provided object to the procided class.
	 *
	 * @param object object to cast
	 * @param clazz  class to cast the object to
	 * @return the casted object
	 */
	<T> T unmarshall(Object object, Class<T> clazz);

	/**
	 * Send a message from the server to the client.
	 *
	 * @param message message to be sent
	 */
	void sendMessage(Object message);

	/**
	 * Closes the transport connection.
	 */
	void close();

	/**
	 * Closes the transport connection.
	 */
	void closeGracefully();

}
