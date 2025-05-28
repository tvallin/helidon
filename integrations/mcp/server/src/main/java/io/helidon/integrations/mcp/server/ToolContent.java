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

/**
 * Tool content.
 */
public interface ToolContent {

    /**
     * Chain prompt content.
     *
     * @param content nested content
     * @return nested content
     */
    ToolContent chain(ToolContent content);

    static ToolContent textContent(String text) {
        return new ToolTextContent(text);
    }

    static ToolContent imageContent(String data, String mimeType) {
        return new ToolImageContent(data, mimeType);
    }

    static ToolContent resourceContent(String uri) {
        return new ToolResourceContent(uri);
    }
}
