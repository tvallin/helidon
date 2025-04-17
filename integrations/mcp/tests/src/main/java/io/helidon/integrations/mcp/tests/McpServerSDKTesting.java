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

package io.helidon.integrations.mcp.tests;

import java.util.List;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;

class McpServerSDKTesting {

	public static void main(String[] args) {
		McpSyncServer server = McpServer.sync(new StdioServerTransportProvider())
				.serverInfo("Helidon MCP Server", "0.0.1")
				.capabilities(McpSchema.ServerCapabilities.builder()
						.resources(true, true)
						.tools(true)
						.prompts(true)
						.logging()
						.build())
				.build();

		server.addTool(createTool());
		server.addResource(createResource());
		server.addPrompt(createPrompt());

		server.loggingNotification(McpSchema.LoggingMessageNotification.builder()
				.level(McpSchema.LoggingLevel.INFO)
				.logger("custom-logger")
				.data("Server initialized")
				.build());

//		server.close();
	}

	static McpServerFeatures.SyncToolSpecification createTool() {
		var schema = """
            {
              "type" : "object",
              "id" : "urn:jsonschema:Operation",
              "properties" : {
                "operation" : {
                  "type" : "string"
                },
                "a" : {
                  "type" : "number"
                },
                "b" : {
                  "type" : "number"
                }
              }
            }
            """;
		return new McpServerFeatures.SyncToolSpecification(
				new McpSchema.Tool("calculator", "Basic calculator", schema),
				(exchange, arguments) -> {
					// Tool implementation
					StringBuilder sb = new StringBuilder();
					arguments.forEach((k,v) -> sb.append("key - " + k)
							.append("Value - " + v)
							.append("\n"));
					return new McpSchema.CallToolResult(List.of(
							new McpSchema.TextContent(List.of(McpSchema.Role.USER), 2.0, sb.toString())
					), false);
				}
		);
	}

	static McpServerFeatures.SyncResourceSpecification createResource() {
		return new McpServerFeatures.SyncResourceSpecification(
				new McpSchema.Resource("custom://resource", "name", "description", "mime-type", null),
				(exchange, request) -> {
					// Resource read implementation
					return new McpSchema.ReadResourceResult(List.of(
							new McpSchema.TextResourceContents("custom://resource", "plain/text", "Resource Text content")));
				}
		);
	}

	static McpServerFeatures.SyncPromptSpecification createPrompt() {
		return new McpServerFeatures.SyncPromptSpecification(
				new McpSchema.Prompt("greeting", "A nice greeting prompt", List.of(
						new McpSchema.PromptArgument("name", "Person to be greeted", true)
				)),
				(exchange, request) -> {
					String name = (String) request.arguments().getOrDefault("name", "Anonymous");
					// Prompt implementation
					return new McpSchema.GetPromptResult("Desription",
							List.of(new McpSchema.PromptMessage(McpSchema.Role.USER,
									new McpSchema.TextContent(List.of(McpSchema.Role.USER), 2.0, "Say Hello to " + name))));
				}
		);
	}
}
