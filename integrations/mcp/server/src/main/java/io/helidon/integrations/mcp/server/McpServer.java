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

import java.util.List;
import java.util.Map;

import io.helidon.builder.api.RuntimeType;

import io.modelcontextprotocol.spec.McpSchema;

@RuntimeType.PrototypedBy(McpServerConfig.class)
public interface McpServer extends RuntimeType.Api<McpServerConfig> {

	static final String PROTOCOLE_VERSION = "2024-11-05";

	static McpServer create(McpServerConfig serverConfig) {
		return new McpServerImpl(serverConfig);
	}

	static McpServer create(java.util.function.Consumer<McpServerConfig.Builder> consumer) {
		return builder().update(consumer).build();
	}

	/**
	 * A new builder to set up server.
	 *
	 * @return builder
	 */
	static McpServerConfig.Builder builder() {
		return McpServerConfig.builder();
	}

	Capabilities capabilities();

	Implementation serverInfo();

	Map<String, RequestHandler<?>> handlers();

	void addTool(ToolComponent tool);

	void removeTool(ToolComponent tool);

	void addResourceTemplate(McpSchema.ResourceTemplate resourceTemplate);

	void removeResourceTemplate(McpSchema.ResourceTemplate resourceTemplate);

	void addResource(ResourceComponent resource);

	void removeResource(String resourceName);

	void addPrompt(PromptComponent prompt);

	void removePrompt(String name);

	interface RequestHandler<T> {
		/**
		 * Handles a request from the client.
		 *
		 * @param params the parameters of the request.
		 */
		T handle(Object params);
	}

}
