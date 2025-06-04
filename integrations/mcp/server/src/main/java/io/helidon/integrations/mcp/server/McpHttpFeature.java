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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.helidon.builder.api.Prototype;
import io.helidon.builder.api.RuntimeType;
import io.helidon.http.Status;
import io.helidon.http.sse.SseEvent;
import io.helidon.service.registry.Service;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.http.HttpFeature;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.helidon.webserver.sse.SseSink;

import jakarta.json.JsonObject;

@RuntimeType.PrototypedBy(McpHttpFeatureConfig.class)
public class McpHttpFeature implements HttpFeature, RuntimeType.Api<McpHttpFeatureConfig> {

    private static final System.Logger LOGGER = System.getLogger(McpHttpFeature.class.getName());

    //TODO - Move the server implementation under here. Routing + ServerImpl must disappear!
    private final McpServerImpl server;
    private final McpHttpFeatureConfig config;
    private final Map<String, McpSession> sessions = new ConcurrentHashMap<>();

    public McpHttpFeature(McpHttpFeatureConfig config) {
        server = null;
        this.config = config;
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
        McpSession session = new McpSession(server.handlers());
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
}
