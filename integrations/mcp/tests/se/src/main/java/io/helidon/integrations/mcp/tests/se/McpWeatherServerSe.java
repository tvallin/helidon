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

/*
         FEEDBACK LIST:

        - Cannot be dependent on jackson. ->R Reflection
        - ! Capability to build JSON schema from classes !
        - ! Capability to read it in the tool !
        - JsonSchema support
        - JsonRPC support
        - Post/Sse will not work in a distributed environment
        - https://raz.sh/blog/2025-05-02_a_critical_look_at_mcp
     */

package io.helidon.integrations.mcp.tests.se;

import java.util.List;
import java.util.Set;

import io.helidon.common.media.type.MediaType;
import io.helidon.common.media.type.MediaTypes;
import io.helidon.integrations.mcp.server.Completion;
import io.helidon.integrations.mcp.server.CompletionContent;
import io.helidon.integrations.mcp.server.CompletionContents;
import io.helidon.integrations.mcp.server.JsonSchema;
import io.helidon.integrations.mcp.server.McpHttpFeatureConfig;
import io.helidon.integrations.mcp.server.McpParameters;
import io.helidon.integrations.mcp.server.Prompt;
import io.helidon.integrations.mcp.server.PromptArgument;
import io.helidon.integrations.mcp.server.PromptContent;
import io.helidon.integrations.mcp.server.PromptContents;
import io.helidon.integrations.mcp.server.Resource;
import io.helidon.integrations.mcp.server.ResourceContent;
import io.helidon.integrations.mcp.server.ResourceContents;
import io.helidon.integrations.mcp.server.Role;
import io.helidon.integrations.mcp.server.Tool;
import io.helidon.integrations.mcp.server.ToolContent;
import io.helidon.integrations.mcp.server.ToolContents;
import io.helidon.webserver.WebServer;

import jakarta.json.JsonValue;

class McpWeatherServerSe {

    public static void main(String[] args) {
        WebServer.builder()
                .routing(routing -> routing.addFeature(
                        McpHttpFeatureConfig.builder()
                                .path("/mcp2")
                                .name("weather-mcp-server")
                                .version("0.0.1")

                                .addTool(tool -> tool.name("name")
                                        .description("description")
                                        .schema(schema -> schema.schema("schema"))
                                        .schema(schema -> schema.addString("schema"))
                                        .tool(param -> ToolContents.imageContent("base64", MediaTypes.TEXT_PLAIN)))

                                .addResource(resource -> resource.name("name")
                                        .uri("uri")
                                        .description("description")
                                        .mediaType(MediaTypes.TEXT_PLAIN)
                                        .ressource(() -> ResourceContents.textContent("")))

                                .addPrompt(prompt -> prompt.name("name")
                                        .description("description")
                                        .addArgument(argument -> argument.name("arg-name")
                                                .description("arg-description")
                                                .required(true))
                                        .prompt(param -> PromptContents.textContent("", Role.USER)))

                                .addCompletion(completion -> completion
                                        .name("name")
                                        .uri("uri")
                                        .completion(McpWeatherServerSe::complete))

                                .addTool(new WeatherTool())
                                .addResource(new WeatherResource())
                                .addPrompt(new WeatherPrompt())
                                .addCompletion(new WeatherCompletion())))
                .build()
                .start();
    }

    static CompletionContent complete(McpParameters parameters) {
        return CompletionContents.createResourceCompletion("uri", List.of());
    }

    //TODO Separer les composants - Snippet/Gist
    static final class WeatherTool implements Tool {
        @Override
        public String name() {
            return "tool-weater";
        }

        @Override
        public String description() {
            return "Get the weather in a specific town";
        }

        @Override
        public JsonSchema schema() {
            return JsonSchema.builder()
                    .addString("town")
                    .build();
        }

        @Override
        public ToolContent process(McpParameters parameters) {

            Town paris = parameters.get("town")
                    .as(param -> new Town(
                            param.get("name").asString().get(),
                            param.get("population").asInt().get()))
                    .get();

            ToolContent resource = ToolContents.resourceContent("uri");
            ToolContent image = ToolContents.imageContent("data", MediaTypes.create("image/png"));
            ToolContent text = ToolContents.textContent("The weather is sunny in " + paris);

            return ToolContents.list(text, image, resource);
        }
    }

    record Town(String name, int population) {
        @SuppressWarnings("NullableProblems")
        @Override
        public String toString() {
            return "Town{name='"
                    + name
                    + '\''
                    + ", population="
                    + population
                    + '}';
        }
    }

    // TODO - Faire des gist pour montrer aux gens l'api
    static class WeatherPrompt implements Prompt {

        @Override
        public String name() {
            return "prompt-weather";
        }

        @Override
        public String description() {
            return "Get the weather in a specific town";
        }

        @Override
        public Set<PromptArgument> arguments() {
            return Set.of(PromptArgument.builder()
                    .name("town")
                    .description("The name of the town")
                    .required(true)
                    .build());
        }

        @Override
        public PromptContent prompt(McpParameters parameters) {

            PromptContent resource = PromptContents.resourceContent("uri", Role.ASSISTANT);
            PromptContent image = PromptContents.imageContent("data", MediaTypes.create("image/png"), Role.ASSISTANT);
            PromptContent text = PromptContents.textContent("It is sunny in " + parameters.get("town").asString(), Role.USER);

            return PromptContents.list(text, image, resource);
        }
    }

    static class WeatherResource implements Resource {

        @Override
        public String uri() {
            return "resource://weather-report";
        }

        @Override
        public String name() {
            return "resource-weather";
        }

        @Override
        public String description() {
            return "This is a weather report";
        }

        @Override
        public MediaType mediaType() {
            return MediaTypes.create("image/png");
        }

        @Override
        public ResourceContent read() {

            ResourceContent text = ResourceContents.textContent("data");
            ResourceContent binary = ResourceContents.binaryContent("base64-encoded-data", MediaTypes.create("image/png"));

            return ResourceContents.list(text, binary);
        }
    }

    //TODO - do it ourselves or let the user have some custom one
    static class WeatherCompletion implements Completion {

        @Override
        public String uri() {
            return "uri";
        }

        @Override
        public String name() {
            return "name";
        }

        @Override
        public CompletionContent complete(McpParameters parameters) {
            return CompletionContents.createResourceCompletion("uri", List.of());
        }
    }
}
