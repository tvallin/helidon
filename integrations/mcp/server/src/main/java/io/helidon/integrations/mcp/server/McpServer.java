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
import java.util.Map;

import io.helidon.builder.api.RuntimeType;

import io.modelcontextprotocol.spec.McpSchema;

@RuntimeType.PrototypedBy(McpServerConfig.class)
public interface McpServer extends RuntimeType.Api<McpServerConfig> {

    String PROTOCOLE_VERSION = "2024-11-05";

    static McpServer create(McpServerConfig serverConfig) {
        return new McpServerImpl(serverConfig);
    }

    static McpServer create(java.util.function.Consumer<McpServerConfig.Builder> consumer) {
        return builder().update(consumer).build();
    }

    /**
     * A new builder to set up server.
     *
     * @return builder
     */
    static McpServerConfig.Builder builder() {
        return McpServerConfig.builder();
    }

    static McpServer.Builder fluentBuilder() {
        return new McpServer.Builder();
    }

    Map<String, RequestHandler<?>> handlers();

    void addTool(ToolComponent tool);

    void removeTool(ToolComponent tool);

    void addResourceTemplate(McpSchema.ResourceTemplate resourceTemplate);

    void removeResourceTemplate(McpSchema.ResourceTemplate resourceTemplate);

    void addResource(ResourceComponent resource);

    void removeResource(String resourceName);

    void addPrompt(PromptComponent prompt);

    void removePrompt(String name);

    interface RequestHandler<T> {
        /**
         * Handles a request from the client.
         *
         * @param params the parameters of the request.
         */
        T handle(Object params);
    }

    class Builder {
        String name = "mcp-server";
        String version = "0.0.1";
        boolean toolChange = false;
        boolean promptChange = false;
        boolean resourceChange = false;
        boolean resourceSubscribe = false;
        final List<ToolComponent> tools = new ArrayList<>();
        final List<PromptComponent> prompts = new ArrayList<>();
        final List<ResourceComponent> resources = new ArrayList<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder toolChange(boolean toolChange) {
            this.toolChange = toolChange;
            return this;
        }

        public Builder promptChange(boolean promptChange) {
            this.promptChange = promptChange;
            return this;
        }

        public Builder resourceChange(boolean resourceChange) {
            this.resourceChange = resourceChange;
            return this;
        }

        public Builder resourceSubscribe(boolean resourceSubscribe) {
            this.resourceSubscribe = resourceSubscribe;
            return this;
        }

        public Builder addTool(ToolComponent tool) {
            tools.add(tool);
            return this;
        }

        public Builder addResource(ResourceComponent resource) {
            resources.add(resource);
            return this;
        }

        public Builder addPrompt(PromptComponent prompt) {
            prompts.add(prompt);
            return this;
        }

        public McpServer build() {
            return new McpServerImpl(McpServerConfig.builder()
                    .implementation(Implementation.builder()
                            .name(this.name)
                            .version(this.version)
                            .build())
                    .capabilities(Capabilities.builder()
                            .resources(Resource.builder()
                                    .listChanged(this.resourceChange)
                                    .subscribe(this.resourceSubscribe)
                                    .build())
                            .prompts(Prompt.builder()
                                    .listChanged(this.promptChange))
                            .tools(Tool.builder()
                                    .listChanged(this.toolChange)
                                    .build()))
                    .tools(this.tools)
                    .resources(this.resources)
                    .prompts(this.prompts)
                    .buildPrototype());
        }
    }

}
