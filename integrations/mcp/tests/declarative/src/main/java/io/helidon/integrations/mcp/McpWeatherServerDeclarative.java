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
import io.helidon.integrations.mcp.server.PromptContent;
import io.helidon.integrations.mcp.server.ResourceContent;
import io.helidon.integrations.mcp.server.Role;
import io.helidon.integrations.mcp.server.ToolContent;

import static io.helidon.integrations.mcp.server.Capability.RESOURCE_LIST_CHANGED;
import static io.helidon.integrations.mcp.server.Capability.TOOL_LIST_CHANGED;

@Mcp.Server("mcp-weather-server")
@Mcp.Version("0.0.1")
@Mcp.Capability(TOOL_LIST_CHANGED)
@Mcp.Capability(RESOURCE_LIST_CHANGED)
@Mcp.Prompts({ SharedComponentDeclarative.class })
@Mcp.Resources({ SharedComponentDeclarative.class })
@Mcp.Tools({ SharedComponentDeclarative.class, SharedToolSe.class })
class McpWeatherServerDeclarative {

    @Mcp.Tool
    @Mcp.Description("Get weather alert from state")
    ToolContent weatherAlert(@Mcp.Param("state's name") String state) {
        return ToolContent.textContent("Hurricane in " + state);
    }

    @Mcp.Prompt
    @Mcp.Description("Get weather alert from state")
    PromptContent weatherInTown(@Mcp.Param("town's name") String town) {
        return PromptContent.textContent("What is the weather like in {{town}}", Role.USER);
    }

    @Mcp.Resource
    @Mcp.URI("resource://api.weather.gov/{path}")
    @Mcp.Description("Get weather alert from state")
    ResourceContent weatherAlerts(@Mcp.Param("path") String path) {
        return ResourceContent.textContent("Resource content");
    }

}
