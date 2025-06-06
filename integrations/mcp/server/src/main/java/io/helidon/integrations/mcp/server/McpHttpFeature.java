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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
import io.helidon.webserver.jsonrpc.JsonRpcHandlers;
import io.helidon.webserver.jsonrpc.JsonRpcRequest;
import io.helidon.webserver.jsonrpc.JsonRpcResponse;
import io.helidon.webserver.jsonrpc.JsonRpcRouting;
import io.helidon.webserver.sse.SseSink;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

@RuntimeType.PrototypedBy(McpHttpFeatureConfig.class)
public class McpHttpFeature implements HttpFeature, RuntimeType.Api<McpHttpFeatureConfig> {
    private static final System.Logger LOGGER = System.getLogger(McpHttpFeature.class.getName());

    private static final String PROTOCOL_VERSION = "2024-11-05";
    private static final JsonObject PING_PONG = Json.createObjectBuilder()
            .add("ping", "pong")
            .build();

    private final McpHttpFeatureConfig config;
    private final Set<Capability> capabilities = new HashSet<>();
    private final Map<String, McpSession> sessions = new ConcurrentHashMap<>();

    private final JsonRpcHandlers jsonRpcHandlers;

    public McpHttpFeature(McpHttpFeatureConfig config) {
        this.config = config;
        JsonRpcHandlers.Builder builder = JsonRpcHandlers.builder();

        builder.method(McpJsonRPC.METHOD_PING, this::pingRpc);
        builder.method(McpJsonRPC.METHOD_INITIALIZE, this::initializeRpc);

        if (!config.tools().isEmpty()) {
            capabilities.add(Capability.TOOL_LIST_CHANGED);
            builder.method(McpJsonRPC.METHOD_TOOLS_LIST, this::toolsListRpc);
            builder.method(McpJsonRPC.METHOD_TOOLS_CALL, this::toolsCallRpc);
        }

        if (!config.resources().isEmpty()) {
            capabilities.add(Capability.RESOURCE_LIST_CHANGED);
            capabilities.add(Capability.RESOURCE_SUBSCRIBE);
            builder.method(McpJsonRPC.METHOD_RESOURCES_LIST, this::resourcesListRpc);
            builder.method(McpJsonRPC.METHOD_RESOURCES_READ, this::resourcesReadRpc);
            builder.method(McpJsonRPC.METHOD_RESOURCES_TEMPLATES_LIST, this::resourceTemplateListRpc);
            builder.method(McpJsonRPC.METHOD_RESOURCES_SUBSCRIBE, this::resourceSubscribeRpc);
            builder.method(McpJsonRPC.METHOD_RESOURCES_UNSUBSCRIBE, this::resourceUnsubscribeRpc);
        }

        if (!config.prompts().isEmpty()) {
            capabilities.add(Capability.PROMPT_LIST_CHANGED);
            builder.method(McpJsonRPC.METHOD_PROMPT_LIST, this::promptsListRpc);
            builder.method(McpJsonRPC.METHOD_PROMPT_GET, this::promptsGetRpc);
        }

        if (config.logging()) {
            capabilities.add(Capability.LOGGING);
            builder.method(McpJsonRPC.METHOD_LOGGING_SET_LEVEL, this::loggingRpc);
        }

        if (!config.completions().isEmpty()) {
            capabilities.add(Capability.COMPLETION);
            builder.method(McpJsonRPC.METHOD_COMPLETION_COMPLETE, this::completionRpc);
        }

        builder.method(McpJsonRPC.METHOD_NOTIFICATION_INITIALIZED, this::notificationInitRpc);
        builder.method(McpJsonRPC.METHOD_NOTIFICATION_CANCELED, this::notificationCancelRpc);

        jsonRpcHandlers = builder.build();
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
        // add all the JSON-RPC routes first
        JsonRpcRouting jsonRpcRouting = JsonRpcRouting.builder()
                .register("/mcp/message", jsonRpcHandlers)
                .build();
        jsonRpcRouting.toHttpRouting(routing);

        // additional HTTP routes for SSE and session disconnect
        routing.get("/sse", this::sse)
                .post("/disconnect", this::disconnect);
    }

    @Override
    public McpHttpFeatureConfig prototype() {
        return config;
    }

    private void disconnect(ServerRequest request, ServerResponse response) {
        String sessionId = request.query().get("sessionId");
        McpSession session = sessions.remove(sessionId);
        session.disconnect();
    }

    private void sse(ServerRequest request, ServerResponse response) {
        String sessionId = UUID.randomUUID().toString();
        McpSession session = new McpSession();
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

    private McpSession findSession(JsonRpcRequest req) {
        try {
            String sessionId = req.query().get("sessionId");
            return sessions.get(sessionId);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    private void notificationInitRpc(JsonRpcRequest req, JsonRpcResponse res) {
        McpSession session = findSession(req);
        if (session == null) {
            res.status(Status.NOT_FOUND_404).send();
            return;
        }
        session.state(McpSession.State.INITIALIZED);
    }

    private void notificationCancelRpc(JsonRpcRequest req, JsonRpcResponse res) {
        McpSession session = findSession(req);
        if (session == null) {
            res.status(Status.NOT_FOUND_404).send();
            return;
        }
        session.disconnect();
    }

    private void pingRpc(JsonRpcRequest req, JsonRpcResponse res) {
        McpSession session = findSession(req);
        if (session == null) {
            res.status(Status.NOT_FOUND_404).send();
            return;
        }
        session.enqueue(res.result(PING_PONG).asJsonObject());
    }

    private void initializeRpc(JsonRpcRequest req, JsonRpcResponse res) {
        McpSession session = findSession(req);
        if (session == null) {
            res.status(Status.NOT_FOUND_404).send();
            return;
        }

        if (session.state() == McpSession.State.UNINITIALIZED) {
            session.state(McpSession.State.INITIALIZING);
            JsonObject clientCapabilities = req.params().get("capabilities").asJsonObject();
            session.clientCapabilities().set(clientCapabilities);
            JsonObject clientInfo = req.params().get("clientInfo").asJsonObject();
            session.clientInfo().set(clientInfo);
        }

        JsonObject result = Json.createObjectBuilder()
                .add("protocolVersion", PROTOCOL_VERSION)
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

        session.enqueue(res.result(result).asJsonObject());
    }

    private void toolsListRpc(JsonRpcRequest req, JsonRpcResponse res) {
        McpSession session = findSession(req);
        if (session == null) {
            res.status(Status.NOT_FOUND_404).send();
            return;
        }

        JsonArrayBuilder builder = Json.createArrayBuilder();
        config.tools().stream()
                .map(Jsonable::json)
                .forEach(builder::add);
        JsonObject result = Json.createObjectBuilder()
                .add("tools", builder.build())
                .build();

        session.enqueue(res.result(result).asJsonObject());
    }

    private void toolsCallRpc(JsonRpcRequest req, JsonRpcResponse res) {
        McpSession session = findSession(req);
        if (session == null) {
            res.status(Status.NOT_FOUND_404).send();
            return;
        }

        JsonObject call = req.params().asJsonObject();
        Optional<Tool> tool = this.config.tools().stream()
                .filter(t -> call.getString("name").equals(t.name()))
                .findAny();
        McpParameters parameters = new McpParameters(
                call.getJsonObject("arguments"), "arguments");
        JsonObject result = tool.map(value -> value.process(parameters))
                .map(Jsonable::json)
                .map(r -> Json.createObjectBuilder()
                        .add("content", Json.createArrayBuilder()
                                .add(r))
                        .build())
                .orElse(null);

        session.enqueue(res.result(result).asJsonObject());
    }

    private void resourcesListRpc(JsonRpcRequest req, JsonRpcResponse res) {
        McpSession session = findSession(req);
        if (session == null) {
            res.status(Status.NOT_FOUND_404).send();
            return;
        }

        JsonArrayBuilder builder = Json.createArrayBuilder();
        this.config.resources().stream()
                .map(Jsonable::json)
                .forEach(builder::add);
        JsonObject result =  Json.createObjectBuilder()
                .add("resources", builder.build())
                .build();

        session.enqueue(res.result(result).asJsonObject());
    }

    private void resourcesReadRpc(JsonRpcRequest req, JsonRpcResponse res) {
        McpSession session = findSession(req);
        if (session == null) {
            res.status(Status.NOT_FOUND_404).send();
            return;
        }

        JsonObject read = req.params().asJsonObject();
        String resourceUri = read.getString("uri");
        Optional<Resource> resource = this.config.resources().stream()
                .filter(it -> Objects.equals(it.uri(), resourceUri))
                .findFirst();
        JsonObject result = resource.map(value -> value.read().json())
                .map(r -> Json.createObjectBuilder()
                        .add("contents", Json.createArrayBuilder()
                                .add(r.add("uri", resourceUri)))
                        .build())
                .orElse(null);

        session.enqueue(res.result(result).asJsonObject());
    }

    private void resourceSubscribeRpc(JsonRpcRequest req, JsonRpcResponse res) {
        // TODO
    }

    private void resourceUnsubscribeRpc(JsonRpcRequest req, JsonRpcResponse res) {
        // TODO
    }

    private void resourceTemplateListRpc(JsonRpcRequest req, JsonRpcResponse res) {
        McpSession session = findSession(req);
        if (session == null) {
            res.status(Status.NOT_FOUND_404).send();
            return;
        }

        List<JsonObject> templates = this.config.resources().stream()
                .filter(this::isTemplate)
                .map(Jsonable::json)
                .map(JsonObjectBuilder::build)
                .toList();
        JsonObject result = Json.createObjectBuilder()
                .add("resourceTemplates", Json.createArrayBuilder(templates))
                .build();

        session.enqueue(res.result(result).asJsonObject());
    }

    private void promptsListRpc(JsonRpcRequest req, JsonRpcResponse res) {
        McpSession session = findSession(req);
        if (session == null) {
            res.status(Status.NOT_FOUND_404).send();
            return;
        }

        JsonArrayBuilder builder = Json.createArrayBuilder();
        this.config.prompts().stream()
                .map(Jsonable::json)
                .forEach(builder::add);
        JsonObject result =  Json.createObjectBuilder()
                .add("prompts", builder.build())
                .build();

        session.enqueue(res.result(result).asJsonObject());
    }

    private void promptsGetRpc(JsonRpcRequest req, JsonRpcResponse res) {
        McpSession session = findSession(req);
        if (session == null) {
            res.status(Status.NOT_FOUND_404).send();
            return;
        }

        JsonObject params = req.params().asJsonObject();
        var prompt = this.config.prompts().stream()
                .filter(p -> Objects.equals(p.name(), params.getString("name")))
                .findFirst();
        McpParameters parameters = new McpParameters(params.getJsonObject("arguments"), "arguments");
        JsonObject result =  prompt.map(value -> Json.createObjectBuilder()
                        .add("description", value.description())
                        .add("messages", Json.createArrayBuilder()
                                .add(value.prompt(parameters).json()))
                        .build())
                .orElse(null);

        session.enqueue(res.result(result).asJsonObject());
    }

    private void loggingRpc(JsonRpcRequest req, JsonRpcResponse res) {
        // TODO
    }

    private void completionRpc(JsonRpcRequest req, JsonRpcResponse res) {
        McpSession session = findSession(req);
        if (session == null) {
            res.status(Status.NOT_FOUND_404).send();
            return;
        }

        JsonObject params = req.params().asJsonObject();
        JsonObject reference = params.getJsonObject("ref");
        Optional<String> search = parseCompletionName(reference);
        if (search.isEmpty()) {
            JsonObject result =  Json.createObjectBuilder()
                    .add("error", Json.createObjectBuilder()
                            .add("code", McpJsonRPC.INVALID_REQUEST)
                            .add("message", "Completion reference not found"))
                    .build();
            session.enqueue(res.result(result).asJsonObject());
            res.send();
            return;
        }

        String name = search.get();
        Optional<Completion> completion = config.completions().stream()
                .filter(it -> it.name().equals(name))
                .findFirst();
        McpParameters parameters = new McpParameters(params.getJsonObject("argument"), "argument");
        JsonObject result = completion.map(it -> it.complete(parameters))
                .map(r -> Json.createObjectBuilder()
                        .add("completion", Json.createObjectBuilder()
                                .add("values", Json.createArrayBuilder(r.values()))
                                .add("total", r.total())
                                .add("hasMore", r.hasMore()))
                        .build())
                .orElse(null);

        session.enqueue(res.result(result).asJsonObject());
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

    boolean isTemplate(Resource resource) {
        String uri = resource.uri();
        return uri.contains("{") && uri.contains("}");
    }
}
