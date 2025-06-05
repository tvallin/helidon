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

package io.helidon.integrations.mcp.tests.se;

import java.util.Set;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.integrations.mcp.server.McpHttpFeatureConfig;
import io.helidon.integrations.mcp.server.McpParameters;
import io.helidon.integrations.mcp.server.Prompt;
import io.helidon.integrations.mcp.server.PromptArgument;
import io.helidon.integrations.mcp.server.PromptContent;
import io.helidon.integrations.mcp.server.PromptContents;
import io.helidon.integrations.mcp.server.Role;
import io.helidon.webserver.WebServer;

class MultiplePrompt {
    static WebServer server;

    static WebServer start() {
        server = WebServer.builder()
                .routing(routing -> routing.addFeature(
                        McpHttpFeatureConfig.builder()
                                .addPrompt(prompt -> prompt.name("prompt1")
                                        .description("Prompt 1")
                                        .addArgument(argument -> argument.name("argument1")
                                                .description("Argument 1")
                                                .required(true))
                                        .prompt(param -> PromptContents.textContent("text", Role.USER)))

                                .addPrompt(prompt -> prompt.name("prompt2")
                                        .description("Prompt 2")
                                        .prompt(param -> PromptContents.imageContent("base64", MediaTypes.APPLICATION_OCTET_STREAM, Role.ASSISTANT)))

                                .addPrompt(prompt -> prompt.name("prompt3")
                                        .description("Prompt 3")
                                        .prompt(param -> PromptContents.resourceContent("resource://resource", Role.ASSISTANT)))

                                .addPrompt(new MyPrompt())))
                .build()
                .start();
        return server;
    }

    static void stop() {
        server.stop();
    }

    private static class MyPrompt implements Prompt {

        @Override
        public String name() {
            return "prompt4";
        }

        @Override
        public String description() {
            return "Prompt 4";
        }

        @Override
        public Set<PromptArgument> arguments() {
            return Set.of();
        }

        @Override
        public PromptContent prompt(McpParameters parameters) {
            return PromptContents.list(
                    PromptContents.textContent("text", Role.USER),
                    PromptContents.resourceContent("resource://resource", Role.USER),
                    PromptContents.imageContent("base64", MediaTypes.APPLICATION_OCTET_STREAM, Role.USER));
        }
    }

}
