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

import java.util.Set;
import java.util.function.Function;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;

class PromptImpl implements Prompt {

    private final String name;
    private final String description;
    private final Set<PromptArgument> arguments;
    private final Function<McpParameters, PromptContent> prompt;

    PromptImpl(String name, String description, Set<PromptArgument> arguments, Function<McpParameters, PromptContent> prompt) {
        this.name = name;
        this.description = description;
        this.arguments = arguments;
        this.prompt = prompt;
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
    public Set<PromptArgument> arguments() {
        return arguments;
    }

    @Override
    public PromptContent prompt(McpParameters parameters) {
        return prompt.apply(parameters);
    }

    @Override
    public JsonObjectBuilder json() {
        JsonArrayBuilder array = Json.createArrayBuilder();
        arguments.stream()
                .map(PromptArgument::json)
                .forEach(array::add);
        return Json.createObjectBuilder()
                .add("name", name)
                .add("description", description)
                .add("arguments", array);
    }
}
