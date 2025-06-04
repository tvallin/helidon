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

import java.util.LinkedList;
import java.util.List;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;

class PromptListContent implements PromptContent {

    List<PromptContent> prompts = new LinkedList<>();

    PromptListContent(PromptContent content) {
        prompts.add(content);
    }

    PromptListContent(PromptContent content, PromptContent content1) {
        prompts.add(content);
        prompts.add(content1);
    }

    PromptListContent(PromptContent content, PromptContent content1, PromptContent content2) {
        prompts.add(content);
        prompts.add(content1);
        prompts.add(content2);
    }

    @Override
    public Role role() {
        return null;
    }

    @Override
    public Content content() {
        return null;
    }

    @Override
    public JsonObjectBuilder json() {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (PromptContent prompt : prompts) {
            builder.add(prompt.json());
        }
        return Json.createObjectBuilder().add("messages", builder);
    }
}
