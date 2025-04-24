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

import io.helidon.integrations.mcp.server.Implementation;
import io.helidon.integrations.mcp.server.ListChanged;
import io.helidon.integrations.mcp.server.McpServerConfig;
import io.helidon.integrations.mcp.server.McpServerImpl;
import io.helidon.integrations.mcp.server.PromptComponent;
import io.helidon.integrations.mcp.server.Resource;
import io.helidon.integrations.mcp.server.ResourceComponent;
import io.helidon.integrations.mcp.server.ServerCapabilities;
import io.helidon.integrations.mcp.server.transport.StdioTransportProvider;
import io.helidon.integrations.mcp.server.ToolComponent;

import io.modelcontextprotocol.spec.McpSchema;

class McpServerSDKTesting {

	public static void main(String[] args) {
		McpServerImpl server = new McpServerImpl(McpServerConfig.builder()
				.transport("stdio")
				.capabilities(ServerCapabilities.builder()
						.promts(ListChanged.builder()
								.listChanged(true)
								.build())
						.resource(Resource.builder()
								.listChanged(true)
								.subscribe(true)
								.build())
						.tools(ListChanged.builder()
								.listChanged(true)
								.build())
						.build())
				.implementation(Implementation.builder()
						.name("Helidon MCP")
						.version("0.0.1")
						.build())
				.instructions("")
				.buildPrototype(), new StdioTransportProvider());
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
		server.addTool(new ToolComponent(new McpSchema.Tool("calculator", "Basic calculator", schema),
				(arguments) -> {
					int a = Integer.parseInt(arguments.get("a").toString());
					int b = Integer.parseInt(arguments.get("b").toString());
					int result = a + b;
					return new McpSchema.CallToolResult(List.of(
							new McpSchema.TextContent(List.of(McpSchema.Role.USER), 2.0, String.valueOf(result))
					), false);
				}));

		server.addPrompt(new PromptComponent(
				new McpSchema.Prompt("greet", "A simple greeting prompt",
						List.of(new McpSchema.PromptArgument("name", "name to be greeted", true))),
				(request) -> {
					String name = request.arguments().get("name").toString();
					return new McpSchema.GetPromptResult("Hello", List.of(
							new McpSchema.PromptMessage(McpSchema.Role.USER,
									new McpSchema.TextContent("Can you nicely greet " + name + "?"))));
				}));

		server.addResource("Helidon quickstart", new ResourceComponent(
				new McpSchema.Resource("http://helidon.io/starter",
						"start",
						"Helidon starter",
						"application/zip",
						null),
				(request) -> new McpSchema.ReadResourceResult(List.of(
						new McpSchema.BlobResourceContents(request.uri(), "application/zip", "unknown")))));

		server.start();
	}
}
