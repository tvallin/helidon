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
 * Prompt argument information.
 */
public interface PromptArgument {
    /**
     * Prompt argument name.
     *
     * @return name
     */
    String name();

    /**
     * Prompt argument description.
     *
     * @return description
     */
    String description();

    /**
     * Wether this prompt argument is required.
     *
     * @return {@code true} if is required, {@code false} otherwise
     */
    boolean required();

    static PromptArgument create(String name, String description) {
        return create(name, description, true);
    }

    static PromptArgument create(String name, String description, boolean required) {
        return new PromptArgument() {

            @Override
            public String name() {
                return name;
            }

            @Override
            public String description() {
                return description;
            }

            @Override
            public boolean required() {
                return required;
            }
        };
    }
}
