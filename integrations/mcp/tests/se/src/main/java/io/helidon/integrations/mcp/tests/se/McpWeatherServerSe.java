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

import java.util.Optional;

import io.helidon.common.config.Config;
import io.helidon.integrations.mcp.server.Capability;
import io.helidon.integrations.mcp.server.McpHttpFeature;
import io.helidon.integrations.mcp.server.McpRouting;
import io.helidon.integrations.mcp.server.McpServerDetails;
import io.helidon.integrations.mcp.server.McpServerInfo;
import io.helidon.integrations.mcp.server.Parameters;
import io.helidon.integrations.mcp.server.Prompt;
import io.helidon.integrations.mcp.server.PromptArgument;
import io.helidon.integrations.mcp.server.PromptContent;
import io.helidon.integrations.mcp.server.PromptInfo;
import io.helidon.integrations.mcp.server.Resource;
import io.helidon.integrations.mcp.server.ResourceInfo;
import io.helidon.integrations.mcp.server.ResourceContent;
import io.helidon.integrations.mcp.server.Role;
import io.helidon.integrations.mcp.server.Tool;
import io.helidon.integrations.mcp.server.ToolContent;
import io.helidon.integrations.mcp.server.ToolInfo;
import io.helidon.service.registry.Services;
import io.helidon.webserver.WebServer;

class McpWeatherServerSe {

    /*
         FEEDBACK LIST:

        - Cannot be dependent on jackson.
        - ! Capability to build JSON schema from classes !
        - ! Capability to read it in the tool !
        - JsonSchema support
        - JsonRPC support
        - Post/Sse will not work in a distributed environment
        - https://raz.sh/blog/2025-05-02_a_critical_look_at_mcp
     */

    //No contstructor
    public static void main(String[] args) {
        var config = Services.get(Config.class);

        WebServer.builder()
                .config(config.get("server"))
                .routing(routing -> routing.addFeature(McpHttpFeature.builder()
                        .server(new McpWeatherDetails())
                        .build()))
                .build()
                .start();
    }

    //config is from blueprint - change naming - maybe builder
    //check capabilities default value, if yes builder.
    static class McpWeatherDetails implements McpServerDetails {

        @Override
        public McpServerInfo info() {
            return McpServerInfo.builder()
                    .name("weather-mcp-server")
                    .version("0.0.1")
                    .capability(Capability.TOOL_LIST_CHANGED)
                    .build();
        }

        @Override
        public void setup(McpRouting.Builder routing) {
            routing.register(new WeatherTool())
                    .register(new WeatherResource())
                    .register(new WeatherPrompt());
        }
    }

    static class WeatherTool implements Tool {

        @Override
        public ToolInfo info() {
            return ToolInfo.builder()
                    .name("tool-weater")
                    .description("Get the weather in a specific town")
                    .schema(schema -> schema
                            .object("town", Town.class, false))
                    .build();
        }

        @Override
        public ToolContent process(Parameters parameters) {

            Optional<Town> town = parameters.object("town", Town.class);

            ToolContent resource = ToolContent.resourceContent("uri");
            ToolContent image = ToolContent.imageContent("data", "text/plain");
            return ToolContent.textContent("data");
        }

        static class Town {
            String name;
            double latitude;
            double longitude;

            Town() {
                name = "Prague";
                latitude = 50.09;
                longitude = 14.40;
            }
        }
    }

    static class WeatherPrompt implements Prompt {

        @Override
        public PromptInfo info() {
            return PromptInfo.builder()
                    .name("prompt-weather")
                    .description("Get the weather in a specific town")
                    .arguments(PromptArgument.create("town", "The name of the town", false))
                    .build();
        }

        @Override
        public PromptContent prompt(Parameters parameters) {
            PromptContent resource = PromptContent.resourceContent("uri", Role.ASSISTANT);
            PromptContent image = PromptContent.imageContent("data", "text/plain", Role.ASSISTANT);
            return PromptContent.textContent("It is sunny in " + parameters.get("town"), Role.USER);
        }
    }

    static class WeatherResource implements Resource {

        @Override
        public ResourceInfo info() {
//            return ResourceInfo.create("resource://weather-report", "resource-weather", "This is a weather report");
            return ResourceInfo.builder()
                    .name("resource-weather")
                    .uri("resource://weather-report")
                    .description("This is a weather report")
                    .build();
        }

        @Override
        public ResourceContent read() {
            ResourceContent text = ResourceContent.textContent("data");
            return ResourceContent.binaryContent("data", "text/plain");
        }
    }
}
