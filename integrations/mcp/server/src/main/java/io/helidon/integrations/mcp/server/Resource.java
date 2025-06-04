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

import java.util.function.Supplier;

import io.helidon.common.media.type.MediaType;

/**
 * MCP resource definition.
 */
public interface Resource extends Jsonable {
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
    MediaType mediaType();

    /**
     * Resource reader.
     *
     * @return resource content as a {@link String}
     */
    ResourceContent read();

    static Resource.Builder builder() {
        return new Resource.Builder();
    }

    class Builder implements io.helidon.common.Builder<Resource.Builder, Resource> {
        String name;
        String description;
        MediaType mediaType;
        String uri;
        Supplier<ResourceContent> content;

        public Builder read(Supplier<ResourceContent> content) {
            this.content = content;
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

        public Builder mediaType(MediaType type) {
            this.mediaType = type;
            return this;
        }

        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        @Override
        public Resource build() {
            return new ResourceImpl(uri, name, description, mediaType, content);
        }
    }
}
