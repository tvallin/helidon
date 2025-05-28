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

import jakarta.json.Json;
import jakarta.json.JsonObject;

/**
 * MCP resource information.
 */
public interface ResourceInfo {

    /**
     * Resource URI.
     *
     * @return uri
     */
    String uri();

    /**
     * Resource name.
     *
     * @return name
     */
    String name();

    /**
     * Resource description.
     *
     * @return description
     */
    String description();

    /**
     * Resource mime type.
     *
     * @return type
     */
    String mimeType();

    /**
     * Wether this ressource is a template.
     *
     * @return {@code true} if this resource is a resource template, {@code false} otherwise
     */
    boolean isTemplate();

    /**
     * Serialize to json.
     *
     * @return json
     */
    JsonObject json();

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private String uri;
        private String name;
        private String mimeType;
        private String description;

        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public ResourceInfo build() {
            return new ResourceInfo() {
                @Override
                public String uri() {
                    return uri;
                }

                @Override
                public String name() {
                    return name;
                }

                @Override
                public String mimeType() {
                    return mimeType;
                }

                @Override
                public boolean isTemplate() {
                    return uri.contains("{") && uri.contains("}");
                }

                @Override
                public String description() {
                    return description;
                }

                @Override
                public JsonObject json() {
                    return Json.createObjectBuilder()
                            .add("uri", uri)
                            .add("name", name)
                            .add("description", description)
                            .add("mimeType", mimeType)
                            .build();
                }
            };
        }
    }
}
