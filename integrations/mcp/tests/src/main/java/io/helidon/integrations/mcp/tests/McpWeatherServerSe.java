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

    public static final String SERVER_NAME = "Helidon MCP Server";
    public static final String SERVER_VERSION = "0.0.1";

    public static final String TOOL_NAME = "weather-alerts";
    public static final String TOOL_DESCRIPTION = "Get weather from town";

    public static final String PROMPT_NAME = "Weather in town";
    public static final String PROMPT_DESCRIPTION = "Get the weather in a specific town";
    public static final String PROMPT_ARGUMENT_NAME = "town";
    public static final String PROMPT_ARGUMENT_DESCRIPTION = "town's name";

    public static final String RESOURCE_URI = "file:///Users/tvallin/Documents/alerts.txt";
    public static final String RESOURCE_NAME = "alerts-list";
    public static final String RESOURCE_DESCRIPTION = "Get the list of all weather alerts";

    private static WebServer server;

    public static void main(String[] args) {
        server = WebServer.builder()
                .port(8081)
                .routing(routing -> routing.addFeature(McpHttpFeature.builder()
                        .mcpServer(builder -> builder
                                .implementation(Implementation.builder()
                                        //TODO - check wether it is server version or "API" version ?
                                        .name(SERVER_NAME)
                                        .version(SERVER_VERSION)
                                        .build())
                                .capabilities(Capabilities.builder()
                                        .resources(Resource.builder()
                                                //TODO - Double check that.
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

    public static WebServer server() {
        return server;
    }

    private static List<ResourceComponent> createResources() {
        return List.of(ResourceComponent.builder()
                .uri(RESOURCE_URI)
                .name(RESOURCE_NAME)
                .description(RESOURCE_DESCRIPTION)
                .reader(uri -> "There are severe weather alerts in Praha")
                .build());
    }

    private static List<ToolComponent> createTools() {
        return List.of(createWeatherTool(), createCoffeShopTool());
    }

    private static ToolComponent createWeatherTool() {
        String schema = """
                {
                  "type" : "object",
                  "id" : "urn:jsonschema:Weather",
                  "required": ["town"],
                  "properties" : {
                    "town" : {
                      "type" : "string"
                    }
                  }
                }
                """;
        return ToolComponent.builder()
                .name(TOOL_NAME)
                .description(TOOL_DESCRIPTION)
                .schema(schema)
                .handler(arguments -> {
                    String town = arguments.get("town").toString();
                    return "There is a hurricane in " + town;
                })
                .build();
    }

    private static ToolComponent createCoffeShopTool() {
        String schema = """
                {
                	"type": "object",
                	"required": ["day"],
                	"properties" : {
                		"day" : {
                			"type" : "string"
                		}
                	}
                }
                """;
        return ToolComponent.builder()
                .name("opening-hours")
                .description("Get the coffee shop opening hours")
                .schema(schema)
                .handler(arguments -> {
                    String day = arguments.get("day").toString();
                    return """
                            The coffee shop opening hours for %s:
                            Open: 10:00 AM
                            Closes: 21:00 PM
                            """.formatted(day);
                })
                .build();
    }

    private static List<PromptComponent> createPrompts() {
        return List.of(
                PromptComponent.builder()
                        .name(PROMPT_NAME)
                        .description(PROMPT_DESCRIPTION)
                        .promptArgument(new McpSchema.PromptArgument(PROMPT_ARGUMENT_NAME, PROMPT_ARGUMENT_DESCRIPTION, true))
                        .handler(request -> {
                            String town = request.get("town").toString();
                            return "What is the weather like in " + town + " ?";
                        })
                        .build());
    }
}
