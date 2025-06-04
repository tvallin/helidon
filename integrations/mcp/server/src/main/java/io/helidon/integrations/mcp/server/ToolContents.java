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

/**
 * {@link ToolContent} factory class.
 */
public class ToolContents {
    private ToolContents() {
    }

    /**
     * Create a list of contents.
     *
     * @param content nested content
     * @return nested content
     */
    public static ToolContent list(ToolContent content) {
        return new ToolContentList(content);
    }

    public static ToolContent list(ToolContent content, ToolContent content1) {
        return new ToolContentList(content, content1);
    }

    public static ToolContent list(ToolContent content, ToolContent content1, ToolContent content2) {
        return new ToolContentList(content, content1, content2);
    }

    public static ToolContent textContent(String text) {
        return new TextContentImpl(text);
    }

    public static ToolContent imageContent(String data, MediaType type) {
        return new ImageContentImpl(data, type);
    }

    public static ToolContent resourceContent(String uri) {
        return new ToolResourceContent(uri);
    }
}
