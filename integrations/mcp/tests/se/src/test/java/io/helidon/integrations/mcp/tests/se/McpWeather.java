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

package io.helidon.integrations.mcp.tests.se;

import io.helidon.integrations.mcp.server.McpHttpFeature;
import io.helidon.integrations.mcp.server.McpParameter;
import io.helidon.integrations.mcp.server.McpRouting;
import io.helidon.integrations.mcp.server.McpServer;
import io.helidon.integrations.mcp.server.McpServerInfo;
import io.helidon.integrations.mcp.server.PromptContent;
import io.helidon.integrations.mcp.server.ResourceContent;
import io.helidon.integrations.mcp.server.Role;
import io.helidon.integrations.mcp.server.ToolContent;
import io.helidon.webserver.WebServer;

import jakarta.json.Json;
import jakarta.json.JsonObject;

class McpWeather {

    public static final String PROTOCOL_VERSION = "2024-11-05";
    public static final String SERVER_VERSION = "0.0.1";
    public static final String SERVER_NAME = "helidon-mcp-server";

    public static final String TOOL_NAME = "weather-alerts";
    public static final String TOOL_DESCRIPTION = "Get weather from town";

    public static final String PROMPT_ARGUMENT_NAME = "town";
    public static final String PROMPT_NAME = "weather-in-town";
    public static final String PROMPT_ARGUMENT_DESCRIPTION = "town's name";
    public static final String PROMPT_DESCRIPTION = "Get the weather in a specific town";

    public static final String RESOURCE_NAME = "alerts-list";
    public static final String RESOURCE_MIME_TYPE = "text/plain";
    public static final String RESOURCE_URI = "file:///Users/tvallin/Documents/alerts.txt";
    public static final String RESOURCE_DESCRIPTION = "Get the list of all weather alerts";

    static WebServer server;

    static void start() {
        server = WebServer.builder()
                .routing(routing -> routing.addFeature(
                        McpHttpFeature.builder().server(new McpWeatherServer())))
                .build()
                .start();
    }

    static int port() {
        return server.port();
    }

    static void stop() {
        server.stop();
    }

    static class McpWeatherServer implements McpServer {

        @Override
        public McpServerInfo info() {
            return McpServerInfo.builder()
                    .name(SERVER_NAME)
                    .version(SERVER_VERSION)
                    .build();
        }

        @Override
        public void setup(McpRouting.Builder routing) {
            JsonObject json = Json.createObjectBuilder()
                    .add("type", "object")
                    .add("id", "urn:jsonschema:weather")
                    .add("required", Json.createArrayBuilder().add("town"))
                    .add("properties", Json.createObjectBuilder()
                            .add("town", Json.createObjectBuilder()
                                    .add("type", "string")))
                    .build();
            routing.tool(tool -> tool.name(TOOL_NAME)
                            .description(TOOL_DESCRIPTION)
                            .schema(schema -> schema.json(json)), this::process)
                    .resource(resource -> resource.name(RESOURCE_NAME)
                            .description(RESOURCE_DESCRIPTION)
                            .uri(RESOURCE_URI)
                            .mimeType(RESOURCE_MIME_TYPE), this::read)
                    .prompt(prompt -> prompt.name(PROMPT_NAME)
                            .description(PROMPT_DESCRIPTION)
                            .argument(arg -> arg.name(PROMPT_ARGUMENT_NAME)
                                    .description(PROMPT_ARGUMENT_DESCRIPTION)
                                    .required(true)), this::prompt);
        }

        ToolContent process(McpParameter parameters) {
            String town = parameters.getString("town");
            return ToolContent.textContent("There is a hurricane in " + town);
        }

        PromptContent prompt(McpParameter parameters) {
            String town = parameters.getString("town");
            return PromptContent.textContent("What is the weather like in " + town + " ?", Role.USER);
        }

        ResourceContent read() {
            return ResourceContent.textContent("There are severe weather alerts in Praha");
        }
    }
}
