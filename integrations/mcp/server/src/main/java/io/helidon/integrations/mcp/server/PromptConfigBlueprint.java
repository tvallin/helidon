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

import io.helidon.builder.api.Option;
import io.helidon.builder.api.Prototype;

@Prototype.Blueprint
interface PromptConfigBlueprint extends Prototype.Factory<Prompt> {
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
    @Option.Singular
    Set<PromptArgument> arguments();

    /**
     * Create prompt based on parameters.
     *
     * @param parameters client parameters
     * @return prompt as {@link String}
     */
    PromptContent prompt(McpParameters parameters);
}
