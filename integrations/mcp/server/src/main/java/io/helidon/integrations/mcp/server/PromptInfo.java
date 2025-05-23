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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * MCP Prompt information.
 */
public interface PromptInfo {
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

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private String name;
        private String description;
        private final Set<PromptArgument> arguments = new HashSet<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder arguments(PromptArgument... arguments) {
            Collections.addAll(this.arguments, arguments);
            return this;
        }

        public PromptInfo build() {
            return new PromptInfo() {
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
            };
        }
    }
}
