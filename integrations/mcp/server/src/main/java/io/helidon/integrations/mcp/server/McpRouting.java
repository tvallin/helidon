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
import java.util.Arrays;
import java.util.List;

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

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        List<Tool> tools = new ArrayList<>();
        List<Prompt> prompts = new ArrayList<>();
        List<Resource> resources = new ArrayList<>();

        public Builder register(Tool... tool) {
            this.tools.addAll(Arrays.asList(tool));
            return this;
        }

        public Builder register(Resource... resource) {
            this.resources.addAll(Arrays.asList(resource));
            return this;
        }

        public Builder register(Prompt... prompt) {
            this.prompts.addAll(Arrays.asList(prompt));
            return this;
        }

        public McpRouting build() {
            return new McpRouting() {

                @Override
                public List<Tool> tools() {
                    return tools;
                }

                @Override
                public List<Prompt> prompts() {
                    return prompts;
                }

                @Override
                public List<Resource> resources() {
                    return resources;
                }
            };
        }
    }
}
