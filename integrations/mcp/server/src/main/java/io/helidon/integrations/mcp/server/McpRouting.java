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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Mcp routing.
 */
public interface McpRouting {
    /**
     * List of {@link Tool}.
     *
     * @return list of {@link Tool}
     */
    List<Tool> tools();

    /**
     * List of {@link Prompt}.
     *
     * @return list of {@link Prompt}
     */
    List<Prompt> prompts();

    /**
     * List of {@link Resource}.
     *
     * @return list of {@link Resource}
     */
    List<Resource> resources();

    /**
     * List of {@link Completion}.
     *
     * @return completions
     */
    List<Completion> completions();

    static Builder builder() {
        return new Builder();
    }

    class Builder {

        public McpRouting build() {
            return null;
        }
    }
}
