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

package io.helidon.integrations.mcp;

import io.helidon.integrations.mcp.server.Mcp;

import static io.helidon.integrations.mcp.server.Capabilities.PROMPT_LIST_CHANGED;
import static io.helidon.integrations.mcp.server.Capabilities.RESOURCE_LIST_CHANGED;
import static io.helidon.integrations.mcp.server.Capabilities.RESOURCE_SUBSCRIBE;
import static io.helidon.integrations.mcp.server.Capabilities.TOOL_LIST_CHANGED;

@Mcp.Server(
        name = "mcp-weather-server",
        version = "1.0.0",
        capabilities = {
                TOOL_LIST_CHANGED,
                RESOURCE_LIST_CHANGED,
                RESOURCE_SUBSCRIBE,
                PROMPT_LIST_CHANGED
        })
//@Mcp.Capabilities(TOOL_LIST_CHANGED, RESOURCE_LIST_CHANGED)
class McpWeatherServerDeclarative {

    @Mcp.Tool(
            name = "Weather Alert",
            description = "Get weather alert from state")
    String weatherAlert(@Mcp.Param("state's name") String state) {
        return "Hurricane in " + state;
    }

    @Mcp.Prompt(
            name = "Weather in town",
            description = "Get the weather in a specific town")
    String weatherInTown(@Mcp.Param("town's name") String town) {
        return "What is the weather like in {{town}}";
    }

    @Mcp.Resource(
            uri = "https://api.weather.gov/{path}",
            name = "weather-report",
            description = "Get the list of all alerts")
    void weatherAlerts() {
    }

}
