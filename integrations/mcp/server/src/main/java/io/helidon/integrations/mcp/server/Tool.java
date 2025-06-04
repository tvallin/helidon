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

package io.helidon.integrations.mcp.server;

import java.util.function.Consumer;

import io.helidon.builder.api.RuntimeType;

/**
 * MCP tool definition.
 */
@RuntimeType.PrototypedBy(ToolConfig.class)
public interface Tool extends Jsonable, RuntimeType.Api<ToolConfig> {
    /**
     * Tool name.
     *
     * @return name
     */
    String name();

    /**
     * Tool description.
     *
     * @return description
     */
    String description();

    /**
     * Tool {@link JsonSchema}.
     *
     * @return schema
     */
    JsonSchema schema();

    /**
     * Tool execution logic with client provided parameters.
     *
     * @param parameters client parameters
     * @return tool execution result as a {@link String}
     */
    ToolContent process(McpParameters parameters);

    static Tool create(ToolConfig config) {
        return config.build();
    }

    static Tool create(Consumer<ToolConfig.Builder> consumer) {
        return ToolConfig.builder().update(consumer).build();
    }

    static ToolConfig.Builder builder() {
        return ToolConfig.builder();
    }

    @Override
    default ToolConfig prototype() {
        return ToolConfig.create();
    }
}
