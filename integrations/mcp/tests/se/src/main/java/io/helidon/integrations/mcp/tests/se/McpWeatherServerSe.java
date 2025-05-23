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

import io.helidon.common.config.Config;
import io.helidon.common.parameters.Parameters;
import io.helidon.integrations.mcp.server.Capabilities;
import io.helidon.integrations.mcp.server.McpHttpFeature;
import io.helidon.integrations.mcp.server.McpRouting;
import io.helidon.integrations.mcp.server.McpServerConfig;
import io.helidon.integrations.mcp.server.McpServerInfo;
import io.helidon.integrations.mcp.server.Prompt;
import io.helidon.integrations.mcp.server.PromptArgument;
import io.helidon.integrations.mcp.server.PromptInfo;
import io.helidon.integrations.mcp.server.Resource;
import io.helidon.integrations.mcp.server.ResourceInfo;
import io.helidon.integrations.mcp.server.Tool;
import io.helidon.integrations.mcp.server.ToolInfo;
import io.helidon.service.registry.Services;
import io.helidon.webserver.WebServer;

class McpWeatherServerSe {

    public static void main(String[] args) {
        var config = Services.get(Config.class);

        WebServer.builder()
                .config(config.get("server"))
                .routing(routing -> routing.addFeature(McpHttpFeature.create(new McpWeatherConfig())))
                .build()
                .start();
    }

    static class McpWeatherConfig implements McpServerConfig {

        @Override
        public McpServerInfo info() {
            return McpServerInfo.create("mcp-server", "0.0.1", Capabilities.TOOL_LIST_CHANGED);
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
                    .requiredProperties("town")
                    .properties("town", "string")
                    .build();
        }

        @Override
        public String process(Parameters parameters) {
            return "It is sunny in " + parameters.get("town");
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
        public String prompt(Parameters parameters) {
            return "It is sunny in " + parameters.get("town");
        }
    }

    static class WeatherResource implements Resource {

        @Override
        public ResourceInfo info() {
            return ResourceInfo.create("file:///tmp/", "temp-file", "This is a temporary file");
        }

        @Override
        public String read() {
            return null;
        }
    }
}
