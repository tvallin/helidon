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
import io.helidon.integrations.mcp.server.PromptContents;
import io.helidon.integrations.mcp.server.ResourceContent;
import io.helidon.integrations.mcp.server.ResourceContents;
import io.helidon.integrations.mcp.server.Role;
import io.helidon.integrations.mcp.server.ToolContent;
import io.helidon.integrations.mcp.server.ToolContents;

class SharedComponentDeclarative {

    @Mcp.Tool
    ToolContent tool() {
        return ToolContents.textContent("I am a nice tool");
    }

    @Mcp.Prompt
    PromptContent prompt() {
        return PromptContents.textContent("I am a nice prompt", Role.USER);
    }

    @Mcp.Resource
    @Mcp.URI("resource://nice/resource")
    ResourceContent resource() {
        return ResourceContents.textContent("I am a nice resource");
    }
}
