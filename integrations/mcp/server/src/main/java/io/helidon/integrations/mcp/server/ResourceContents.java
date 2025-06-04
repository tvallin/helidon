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

import io.helidon.common.media.type.MediaType;

public class ResourceContents {
    private ResourceContents() {
    }

    public static ResourceContent textContent(String text) {
        return new ResourceTextContent(text);
    }

    public static ResourceContent binaryContent(String data, MediaType type) {
        return new ResourceBinaryContent(type, data);
    }

    public static ResourceContent list(ResourceContent content) {
        return new ResourceListContent(content);
    }

    public static ResourceContent list(ResourceContent content, ResourceContent content1) {
        return new ResourceListContent(content, content1);
    }

    public static ResourceContent list(ResourceContent content, ResourceContent content1, ResourceContent content2) {
        return new ResourceListContent(content, content1, content2);
    }
}
