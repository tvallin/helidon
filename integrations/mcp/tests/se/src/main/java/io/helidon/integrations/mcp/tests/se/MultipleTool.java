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

import io.helidon.common.media.type.MediaTypes;
import io.helidon.integrations.mcp.server.JsonSchema;
import io.helidon.integrations.mcp.server.McpHttpFeatureConfig;
import io.helidon.integrations.mcp.server.McpParameters;
import io.helidon.integrations.mcp.server.Tool;
import io.helidon.integrations.mcp.server.ToolContent;
import io.helidon.integrations.mcp.server.ToolContents;
import io.helidon.webserver.WebServer;

class MultipleTool {

    static WebServer server;

    static WebServer start() {
        server = WebServer.builder()
                .routing(routing -> routing.addFeature(
                        McpHttpFeatureConfig.builder()
                                .addTool(tool -> tool.name("tool1")
                                        .description("Tool 1")
                                        .schema(schema -> schema.addString("schema"))
                                        .tool(param -> ToolContents.imageContent("base64", MediaTypes.APPLICATION_OCTET_STREAM)))

                                .addTool(tool -> tool.name("tool2")
                                        .description("Tool 2")
                                        .schema(schema -> schema.addString("schema"))
                                        .tool(param -> ToolContents.resourceContent("resource://resource")))

                                .addTool(tool -> tool.name("tool3")
                                        .description("Tool 3")
                                        .schema(schema -> schema.addString("schema"))
                                        .tool(param -> ToolContents.list(
                                                ToolContents.imageContent("base64", MediaTypes.APPLICATION_OCTET_STREAM),
                                                ToolContents.resourceContent("resource://resource"),
                                                ToolContents.textContent("text"))))

                                .addTool(new TownTool())))
                .build()
                .start();
        return server;
    }

    static void stop() {
        server.stop();
    }

    static final class TownTool implements Tool {
        @Override
        public String name() {
            return "town4";
        }

        @Override
        public String description() {
            return "Town 4";
        }

        @Override
        public JsonSchema schema() {
            return JsonSchema.builder()
                    .schema("""
                            {
                                "type": "object",
                                "properties": {
                                    "name": {
                                        "type": "string",
                                        "description": "City name"
                                    },
                                    "population": {
                                        "type": "integer",
                                        "description": "Population of the town"
                                    }
                                },
                                "required": ["name", "population"]
                            }
                            """)
                    .build();
        }

        @Override
        public ToolContent process(McpParameters parameters) {
            return ToolContents.textContent("text");
        }
    }

    record Town(String name, int population) {
        @SuppressWarnings("NullableProblems")
        @Override
        public String toString() {
            return "Town{name='"
                    + name
                    + '\''
                    + ", population="
                    + population
                    + '}';
        }
    }

}
