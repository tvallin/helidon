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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;

/**
 * Tool input schema description.
 */
public class JsonSchema {

    private final Map<String, Object> schema = new HashMap<>();

    private JsonSchema(Builder builder) {
        schema.put("id", builder.id);
        schema.put("type", builder.type);

        JsonArrayBuilder array = Json.createArrayBuilder();
        builder.required.forEach(array::add);
        schema.put("required", array);

        JsonObjectBuilder properties = Json.createObjectBuilder();
        builder.properties.forEach(properties::add);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final String type;
        private final String id;
        private final List<String> required;
        private final Map<String, String> properties;

        private Builder() {
            this.id = "jsonSchema-" + UUID.randomUUID();
            this.type = "object";
            this.required = new ArrayList<>();
            this.properties = new HashMap<>();
        }

        public Builder object(String key, Class<?> type, boolean required) {
            this.properties.put(key, type.toString());
            if (required) {
                this.required.add(key);
            }
            return this;
        }

        public Builder object(String key, Class<?> type) {
            this.properties.put(key, type.toString());
            this.required.add(key);
            return this;
        }

        public JsonSchema build() {
            return new JsonSchema(this);
        }

    }

}
