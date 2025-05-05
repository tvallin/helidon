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

import io.helidon.integrations.mcp.server.Capabilities;
import io.helidon.integrations.mcp.server.Implementation;
import io.helidon.integrations.mcp.server.McpHttpFeature;
import io.helidon.integrations.mcp.server.Prompt;
import io.helidon.integrations.mcp.server.PromptComponent;
import io.helidon.integrations.mcp.server.Resource;
import io.helidon.integrations.mcp.server.ResourceComponent;
import io.helidon.integrations.mcp.server.Tool;
import io.helidon.integrations.mcp.server.ToolComponent;
import io.helidon.webserver.WebServer;

import io.modelcontextprotocol.spec.McpSchema;

class McpWeatherServerSe {

	public static void main(String[] args) {
		WebServer.builder()
				.port(8080)
				.routing(routing -> routing.addFeature(McpHttpFeature.builder()
						.mcpServer(builder -> builder
								.implementation(Implementation.builder()
										.name("Helidon MCP Server")
										.version("0.0.1")
										.build())
								.capabilities(Capabilities.builder()
										.resources(Resource.builder()
												.listChanged(true)
												.subscribe(true)
												.build())
										.prompts(Prompt.builder()
												.listChanged(true))
										.tools(Tool.builder()
												.listChanged(true)
												.build()))
								.addTools(createTools())
								.addPrompts(createPrompts())
								.addResources(createResources()))
						.build()))
				.build()
				.start();
	}

	private static List<ResourceComponent> createResources() {
		return List.of(ResourceComponent.builder()
				.uri("file:///Users/tvallin/Documents/alerts.txt")
				.name("alerts-list")
				.description("Get the list of all alerts")
				.reader(uri -> "Alerts")
				.build());
	}

	private static List<ToolComponent> createTools() {
		String schema = """
				{
				  "type" : "object",
				  "id" : "urn:jsonschema:weather",
				  "properties" : {
				    "town" : {
				      "type" : "string"
				    }
				  }
				}
				""";
		return List.of(
				ToolComponent.builder()
						.name("weather alerts")
						.description("Get weather alert from state")
						.schema(schema)
						.handler(arguments -> {
							String town = arguments.get("town").toString();
							return "Hurricane in " + town;
						})
						.build());
	}

	private static List<PromptComponent> createPrompts() {
		return List.of(
				PromptComponent.builder()
						.name("Weather in town")
						.description("Get the weather in a specific town")
						.promptArgument(new McpSchema.PromptArgument("town", "town's name", true))
						.handler(request -> {
							String town = request.get("town").toString();
							return "What is the weather like in " + town + " ?";
						})
						.build());
	}
}
