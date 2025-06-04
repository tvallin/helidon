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
import java.util.function.Supplier;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

/**
 * MCP tool information.
 */
public interface ToolInfo extends Jsonable {
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

    class Builder implements Supplier<ToolInfo> {
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

        public Builder schema(String schema) {
            this.schema = JsonSchema.builder()
                    .schema(schema)
                    .build();
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

                @Override
                public JsonObjectBuilder json() {
                    JsonObjectBuilder builder = Json.createObjectBuilder()
                            .add("name", name)
                            .add("description", description);
                    if (schema.schema() != null) {
                        builder.add("inputSchema", schema.schema());
                        return builder;
                    }
                    builder.add("inputSchema", schema.json());
                    return builder;
                }
            };
        }

        @Override
        public ToolInfo get() {
            return this.build();
        }
    }
}
