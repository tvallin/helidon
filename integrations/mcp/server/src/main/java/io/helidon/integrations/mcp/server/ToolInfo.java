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

/**
 * MCP tool information.
 */
public interface ToolInfo {
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

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        String name;
        String description;
        JsonSchema schema;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String version) {
            this.description = version;
            return this;
        }

        public Builder schema(Consumer<JsonSchema.Builder> builder) {
            JsonSchema.Builder schema = JsonSchema.builder();
            builder.accept(schema);
            this.schema = schema.build();
            return this;
        }

        public ToolInfo build() {
            return new ToolInfo() {
                @Override
                public String name() {
                    return name;
                }

                @Override
                public String description() {
                    return description;
                }

                @Override
                public JsonSchema schema() {
                    return schema;
                }
            };
        }

    }
}
