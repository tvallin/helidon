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
import jakarta.json.JsonObjectBuilder;

class TextContentImpl implements TextContent {
    private final String text;

    public TextContentImpl(String text) {
        //Due to Anthropic failing test -> Find a better way
        this.text = text.replace("\"", "");
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    public JsonObjectBuilder json() {
        return Json.createObjectBuilder()
                .add("type", type())
                .add("text", text);
    }
}
