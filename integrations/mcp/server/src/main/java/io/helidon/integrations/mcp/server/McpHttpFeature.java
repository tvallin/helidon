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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import io.helidon.builder.api.RuntimeType;
import io.helidon.http.Status;
import io.helidon.http.sse.SseEvent;
import io.helidon.webserver.http.HttpFeature;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.helidon.webserver.sse.SseSink;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

@RuntimeType.PrototypedBy(McpHttpFeatureConfig.class)
public class McpHttpFeature implements HttpFeature, RuntimeType.Api<McpHttpFeatureConfig> {

    private static final String PROTOCOLE_VERSION = "2024-11-05";
    private static final System.Logger LOGGER = System.getLogger(McpHttpFeature.class.getName());

    private final McpHttpFeatureConfig config;
    private final Set<Capability> capabilities = new HashSet<>();
    private final Map<String, McpSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, JsonRPCHandler> handlers = new HashMap<>();

    public McpHttpFeature(McpHttpFeatureConfig config) {
        this.config = config;
        handlers.put(McpJsonRPC.METHOD_PING, this::ping);
        handlers.put(McpJsonRPC.METHOD_INITIALIZE, this::initialize);

        if (!config.tools().isEmpty()) {
            capabilities.add(Capability.TOOL_LIST_CHANGED);
            handlers.put(McpJsonRPC.METHOD_TOOLS_LIST, this::toolsList);
            handlers.put(McpJsonRPC.METHOD_TOOLS_CALL, this::toolsCall);
        }

        if (!config.resources().isEmpty()) {
            capabilities.add(Capability.RESOURCE_LIST_CHANGED);
            capabilities.add(Capability.RESOURCE_SUBSCRIBE);
            handlers.put(McpJsonRPC.METHOD_RESOURCES_LIST, this::resourcesList);
            handlers.put(McpJsonRPC.METHOD_RESOURCES_READ, this::resourcesRead);
            handlers.put(McpJsonRPC.METHOD_RESOURCES_TEMPLATES_LIST, this::resourceTemplateList);
            handlers.put(McpJsonRPC.METHOD_RESOURCES_SUBSCRIBE, this::resourceSubscribe);
            handlers.put(McpJsonRPC.METHOD_RESOURCES_UNSUBSCRIBE, this::resourceUnsubscribe);
        }

        if (!config.prompts().isEmpty()) {
            capabilities.add(Capability.PROMPT_LIST_CHANGED);
            handlers.put(McpJsonRPC.METHOD_PROMPT_LIST, this::promptsList);
            handlers.put(McpJsonRPC.METHOD_PROMPT_GET, this::promptsGet);
        }

        if (config.logging()) {
            capabilities.add(Capability.LOGGING);
            handlers.put(McpJsonRPC.METHOD_LOGGING_SET_LEVEL, this::logging);
        }

        if (!config.completions().isEmpty()) {
            capabilities.add(Capability.COMPLETION);
            handlers.put(McpJsonRPC.METHOD_COMPLETION_COMPLETE, this::completion);
        }
    }

    static McpHttpFeature create(McpHttpFeatureConfig config) {
        return new McpHttpFeature(config);
    }

    static McpHttpFeature create(Consumer<McpHttpFeatureConfig.Builder> consumer) {
        McpHttpFeatureConfig.Builder builder = McpHttpFeatureConfig.builder();
        consumer.accept(builder);
        return builder.build();
    }

    static McpHttpFeatureConfig.Builder builder() {
        return McpHttpFeatureConfig.builder();
    }

    @Override
    public void setup(HttpRouting.Builder routing) {
        routing.get("/sse", this::sse)
                .post("/mcp/message", this::message)
                .post("/disconnect", this::disconnect);
    }

    @Override
    public McpHttpFeatureConfig prototype() {
        return config;
    }

    private void disconnect(ServerRequest request, ServerResponse response) {
        String sessionId = request.query().get("sessionId");
        McpSession session = sessions.remove(sessionId);
        session.disonnect();
    }

    private void sse(ServerRequest request, ServerResponse response) {
        String sessionId = UUID.randomUUID().toString();
        McpSession session = new McpSession(handlers);
        sessions.put(sessionId, session);

        try (SseSink sink = response.sink(SseSink.TYPE)) {
            sink.emit(SseEvent.builder()
                    .name("endpoint")
                    .data("/mcp/message?sessionId=" + sessionId)
                    .build());
            session.poll(message -> sink.emit(SseEvent.builder()
                    .name("message")
                    .data(message)
                    .build()));
        }
    }

    private void message(ServerRequest request, ServerResponse response) {
        String sessionId = request.query().get("sessionId");

        McpSession session = sessions.get(sessionId);
        if (session == null) {
            response.status(Status.NOT_FOUND_404);
            response.send();
            return;
        }

        JsonObject jsonRpc = request.content().as(JsonObject.class);
        if (LOGGER.isLoggable(System.Logger.Level.DEBUG)) {
            LOGGER.log(System.Logger.Level.DEBUG, "Message received : %s", jsonRpc.toString());
        }
        session.send(jsonRpc);
        response.status(Status.OK_200);
        response.send();
    }


    private JsonObject ping(JsonObject ping) {
        return Json.createObjectBuilder()
                .add("ping", "pong")
                .build();
    }

    private JsonObject toolsList(JsonObject list) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        this.config.tools().stream()
                .map(Jsonable::json)
                .forEach(builder::add);
        return Json.createObjectBuilder()
                .add("tools", builder.build())
                .build();
    }

    private JsonObject toolsCall(JsonObject call) {
        Optional<Tool> tool = this.config.tools().stream()
                .filter(t -> call.getString("name").equals(t.name()))
                .findAny();
        McpParameters parameters = new McpParameters(call.getJsonObject("arguments"));
        return tool.map(value -> value.process(parameters))
                .map(Jsonable::json)
                .map(result -> Json.createObjectBuilder()
                        .add("content", Json.createArrayBuilder()
                                .add(result))
                        .build())
                .orElse(null);
    }

    private JsonObject resourcesList(JsonObject list) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        this.config.resources().stream()
                .map(Jsonable::json)
                .forEach(builder::add);
        return Json.createObjectBuilder()
                .add("resources", builder.build())
                .build();
    }

    private JsonObject resourcesRead(JsonObject read) {
        String resourceUri = read.getString("uri");
        Optional<Resource> resource = this.config.resources().stream()
                .filter(it -> Objects.equals(it.uri(), resourceUri))
                .findFirst();

        return resource.map(value -> value.read().json())
                .map(result -> Json.createObjectBuilder()
                        .add("contents", Json.createArrayBuilder()
                                .add(result.add("uri", resourceUri)))
                        .build())
                .orElse(null);
    }

    private JsonObject resourceTemplateList(JsonObject list) {
        List<JsonObject> templates = this.config.resources().stream()
                .filter(this::isTemplate)
                .map(Jsonable::json)
                .map(JsonObjectBuilder::build)
                .toList();
        return Json.createObjectBuilder()
                .add("resourceTemplates", Json.createArrayBuilder(templates))
                .build();
    }

    private JsonObject promptsList(JsonObject list) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        this.config.prompts().stream()
                .map(Jsonable::json)
                .forEach(builder::add);
        return Json.createObjectBuilder()
                .add("prompts", builder.build())
                .build();
    }

    private JsonObject promptsGet(JsonObject params) {
        var prompt = this.config.prompts().stream()
                .filter(p -> Objects.equals(p.name(), params.getString("name")))
                .findFirst();

        McpParameters parameters = new McpParameters(params.getJsonObject("arguments"));
        return prompt.map(value -> Json.createObjectBuilder()
                        .add("description", value.description())
                        .add("messages", Json.createArrayBuilder()
                                .add(value.prompt(parameters).json()))
                        .build())
                .orElse(null);
    }

    private JsonObject completion(JsonObject parameter) {
        JsonObject reference = parameter.getJsonObject("ref");
        Optional<String> search = parseCompletionName(reference);
        if (search.isEmpty()) {
            return Json.createObjectBuilder()
                    .add("error", Json.createObjectBuilder()
                            .add("code", McpJsonRPC.INVALID_REQUEST)
                            .add("message", "Completion reference not found"))
                    .build();
        }
        String name = search.get();
        Optional<Completion> completion = config.completions().stream()
                .filter(it -> it.name().equals(name))
                .findFirst();
        McpParameters parameters = new McpParameters(parameter.getJsonObject("argument"));
        return completion.map(it -> it.complete(parameters))
                .map(result -> Json.createObjectBuilder()
                        .add("completion", Json.createObjectBuilder()
                                .add("values", Json.createArrayBuilder(result.values()))
                                .add("total", result.total())
                                .add("hasMore", result.hasMore()))
                        .build())
                .orElse(null);
    }

    private Optional<String> parseCompletionName(JsonObject completion) {
        if (completion.containsKey("name")) {
            return Optional.of(completion.getString("name"));
        }
        if (completion.containsKey("uri")) {
            return Optional.of(completion.getString("uri"));
        }
        return Optional.empty();
    }

    //TODO - How to maintain list of client subscription ?
    private JsonObject resourceUnsubscribe(JsonObject unsubscribe) {
        return null;
    }

    private JsonObject resourceSubscribe(JsonObject subscribe) {
        return null;
    }

    //Todo - Change the logging level in the sessions
    private JsonObject logging(JsonObject logging) {
        return Json.createObjectBuilder().build();
    }

    private JsonObject initialize(JsonObject initialize) {
        return Json.createObjectBuilder()
                .add("protocolVersion", PROTOCOLE_VERSION)
                .add("capabilities", Json.createObjectBuilder()
                        .add("logging", Json.createObjectBuilder())
                        .add("prompts", Json.createObjectBuilder()
                                .add("listChanged", capabilities.contains(Capability.PROMPT_LIST_CHANGED)))
                        .add("tools", Json.createObjectBuilder()
                                .add("listChanged", capabilities.contains(Capability.TOOL_LIST_CHANGED)))
                        .add("resources", Json.createObjectBuilder()
                                .add("listChanged", capabilities.contains(Capability.RESOURCE_LIST_CHANGED))
                                .add("subscribe", capabilities.contains(Capability.RESOURCE_SUBSCRIBE))))
                .add("serverInfo", Json.createObjectBuilder()
                        .add("name", config.name())
                        .add("version", config.version()))
                .add("instructions", "")
                .build();
    }

    boolean isTemplate(Resource resource) {
        String uri = resource.uri();
        return uri.contains("{") && uri.contains("}");
    }

    interface JsonRPCHandler {
        /**
         * Handles a request from the client.
         *
         * @param params the parameters of the request.
         */
        JsonObject handle(JsonObject params);
    }
}
