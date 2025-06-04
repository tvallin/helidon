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

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;

class ResourceImpl implements Resource {

    private final String uri;
    private final String name;
    private final String description;
    private final MediaType type;
    private final Supplier<ResourceContent> content;

    ResourceImpl(String uri, String name, String description, MediaType mediaType, Supplier<ResourceContent> content) {
        this.uri = uri;
        this.name = name;
        this.description = description;
        this.type = mediaType;
        this.content = content;
    }

    @Override
    public String uri() {
        return uri;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public MediaType mediaType() {
        return type;
    }

    @Override
    public ResourceContent read() {
        return content.get();
    }

    @Override
    public JsonObjectBuilder json() {
        return Json.createObjectBuilder()
                .add("uri", uri)
                .add("name", name)
                .add("description", description)
                .add("mimeType", type.text());
    }
}
