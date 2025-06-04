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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * MCP Prompt definition.
 */
public interface Prompt extends Jsonable {
    /**
     * Prompt name.
     *
     * @return name
     */
    String name();

    /**
     * Prompt description.
     *
     * @return description
     */
    String description();

    /**
     * A {@link Set} of prompt argument.
     *
     * @return {@link Set} of argument
     */
    Set<PromptArgument> arguments();

    /**
     * Create prompt based on parameters.
     *
     * @param parameters client parameters
     * @return prompt as {@link String}
     */
    PromptContent prompt(McpParameters parameters);

    static Prompt.Builder builder() {
        return new Prompt.Builder();
    }

    class Builder implements io.helidon.common.Builder<Prompt.Builder, Prompt> {
        private String name;
        private String description;
        private Function<McpParameters, PromptContent> prompt;
        private final Set<PromptArgument> arguments = new HashSet<>();

        public Builder prompt(Function<McpParameters, PromptContent> process) {
            this.prompt = process;
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

        public Builder argument(Consumer<PromptArgument.Builder> builder) {
            PromptArgument.Builder argumentBuilder = PromptArgument.builder();
            builder.accept(argumentBuilder);
            arguments.add(argumentBuilder.build());
            return this;
        }

        @Override
        public Prompt build() {
            return new PromptImpl(name, description, arguments, prompt);
        }
    }
}
