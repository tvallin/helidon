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

public class PromptContents {
    private PromptContents() {
    }

    public static PromptContent textContent(String prompt, Role role) {
        return new PromptTextContent(prompt, role);
    }

    public static PromptContent imageContent(String data, MediaType type, Role role) {
        return new PromptImageContent(data, type, role);
    }

    public static PromptContent resourceContent(String uri, Role role) {
        return new PromptResourceContent(uri, role);
    }

    public static PromptContent list(PromptContent content) {
        return new PromptListContent(content);
    }

    public static PromptContent list(PromptContent content, PromptContent content1) {
        return new PromptListContent(content, content1);
    }

    public static PromptContent list(PromptContent content, PromptContent content1, PromptContent content2) {
        return new PromptListContent(content, content1, content2);
    }
}
