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
import io.helidon.common.media.type.MediaType;

/**
 * MCP resource definition.
 */
@RuntimeType.PrototypedBy(ResourceConfig.class)
public interface Resource extends Jsonable, RuntimeType.Api<ResourceConfig> {

    static Resource create(ResourceConfig config) {
        return new ResourceImpl(config.uri(), config.name(), config.description(), config.mediaType(), config.ressource());
    }

    static Resource create(Consumer<ResourceConfig.Builder> consumer) {
        return ResourceConfig.builder().update(consumer).build();
    }

    static ResourceConfig.Builder builder() {
        return ResourceConfig.builder();
    }

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

    @Override
    default ResourceConfig prototype() {
        return ResourceConfig.create();
    }
}
