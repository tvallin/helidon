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
        List<Tool> tools = new ArrayList<>();
        List<Prompt> prompts = new ArrayList<>();
        List<Resource> resources = new ArrayList<>();
        List<Completion> completions = new ArrayList<>();

        public Builder tool(Consumer<ToolInfo.Builder> info, Function<McpParameters, ToolContent> process) {
            ToolInfo.Builder builder = ToolInfo.builder();
            info.accept(builder);
//            tools.add(Tool.create(builder.build(), process));
            return this;
        }

        public Builder prompt(Consumer<PromptInfo.Builder> info, Function<McpParameters, PromptContent> prompt) {
            PromptInfo.Builder builder = PromptInfo.builder();
            info.accept(builder);
//            this.prompts.add(Prompt.create(builder.build(), prompt));
            return this;
        }

        public Builder resource(Consumer<ResourceInfo.Builder> info, Supplier<ResourceContent> read) {
            ResourceInfo.Builder builder = ResourceInfo.builder();
            info.accept(builder);
//            this.resources.add(Resource.create(builder.build(), read));
            return this;
        }

        public Builder completion(Consumer<CompletionInfo.Builder> info, Function<McpParameters, CompletionContent> complete) {
            CompletionInfo.Builder builder = CompletionInfo.builder();
            info.accept(builder);
//            this.completions.add(Completion.create(builder.build(), complete));
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

                @Override
                public List<Completion> completions() {
                    return completions;
                }
            };
        }
    }
}
