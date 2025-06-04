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
import java.util.function.Function;

/**
 * MCP tool definition.
 */
public interface Tool extends Jsonable{
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

    static Tool.Builder builder() {
        return new Builder();
    }

    class Builder implements io.helidon.common.Builder<Builder, Tool> {
        Function<McpParameters, ToolContent> process;
        private String name;
        private String description;
        private JsonSchema schema;

        public Builder process(Function<McpParameters, ToolContent> process) {
            this.process = process;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder schema(String schema) {
            this.schema = JsonSchema.builder().schema(schema).build();
            return this;
        }

        public Builder schema(Consumer<JsonSchema.Builder> builder) {
            JsonSchema.Builder schemaBuilder = JsonSchema.builder();
            builder.accept(schemaBuilder);
            this.schema = schemaBuilder.build();
            return this;
        }

        @Override
        public Tool build() {
            return new ToolImpl(name, description, schema, process);
        }
    }
}
