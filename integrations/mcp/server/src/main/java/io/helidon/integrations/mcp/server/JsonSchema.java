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

import java.util.UUID;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

/**
 * Json schema.
 */
public class JsonSchema {

    private final JsonObject json;
    private final String schema;

    private JsonSchema(JsonObject json, String schema) {
        this.json = json;
        this.schema = schema;
    }

    public static Builder builder() {
        return new Builder();
    }

    JsonObject json() {
        return json;
    }

    String schema() {
        return schema;
    }

    public static class Builder {
        private final String id = "jsonSchema-" + UUID.randomUUID();
        private final String type = "object";
        private final JsonObjectBuilder properties = Json.createObjectBuilder();
        private final JsonArrayBuilder required = Json.createArrayBuilder();
        private String schema;

        private Builder() {
        }

        public Builder schema(String schema) {
            this.schema = schema;
            return this;
        }

        public Builder addString(String key, boolean required) {
            properties.add(key, Json.createObjectBuilder()
                    .add("type", "string"));
            if (required) {
                this.required.add(key);
            }
            return this;
        }

        public Builder addNumber(String key, boolean required) {
            properties.add(key, Json.createObjectBuilder()
                    .add("type", "number"));
            if (required) {
                this.required.add(key);
            }
            return this;
        }

        public Builder addBoolean(String key, boolean required) {
            properties.add(key, Json.createObjectBuilder()
                    .add("type", "boolean"));
            if (required) {
                this.required.add(key);
            }
            return this;
        }

        public Builder addBooleanArray(String key, boolean required) {
            properties.add(key, Json.createObjectBuilder()
                    .add("type", "boolean"));
            if (required) {
                this.required.add(key);
            }
            return this;
        }

        public Builder addNumberArray(String key, boolean required) {
            properties.add(key, Json.createObjectBuilder()
                    .add("type", "number"));
            if (required) {
                this.required.add(key);
            }
            return this;
        }

        public Builder addStringArray(String key, boolean required) {
            properties.add(key, Json.createObjectBuilder()
                    .add("type", "string"));
            if (required) {
                this.required.add(key);
            }
            return this;
        }

        public Builder addString(String key) {
            addString(key, true);
            return this;
        }

        public Builder addNumber(String key) {
            addNumber(key, true);
            return this;
        }

        public Builder addBoolean(String key) {
            addBoolean(key, true);
            return this;
        }

        public Builder addBooleanArray(String key) {
            addBooleanArray(key, true);
            return this;
        }

        public Builder addNumberArray(String key) {
            addNumberArray(key, true);
            return this;
        }

        public Builder addStringArray(String key) {
            addStringArray(key, true);
            return this;
        }

        public JsonSchema build() {
            return new JsonSchema(Json.createObjectBuilder()
                    .add("id", id)
                    .add("type", type)
                    .add("properties", properties)
                    .add("required", required)
                    .build(), schema);
        }
    }
}
