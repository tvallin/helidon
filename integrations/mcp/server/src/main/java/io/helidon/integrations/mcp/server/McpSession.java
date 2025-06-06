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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import io.helidon.common.UncheckedException;
import io.helidon.http.Status;
import io.helidon.webserver.jsonrpc.JsonRpcResponse;

import jakarta.json.Json;
import jakarta.json.JsonObject;

import static io.helidon.integrations.mcp.server.McpSession.State.UNINITIALIZED;

class McpSession {

    private static final System.Logger LOGGER = System.getLogger(McpSession.class.getName());

    private final List<String> pendingResponses = new ArrayList<>();
    private final AtomicBoolean active = new AtomicBoolean(true);
    private final BlockingQueue<JsonObject> queue = new LinkedBlockingQueue<>();
    private final AtomicReference<JsonObject> clientInfo = new AtomicReference<>();
    private final AtomicReference<JsonObject> clientCapabilities = new AtomicReference<>();

    private State state = UNINITIALIZED;

    void poll(Consumer<JsonObject> consumer) {
        while (active.get()) {
            try {
                JsonObject message = queue.take();
                if (message.getBoolean("disconnect", false)) {
                    break;
                }
                consumer.accept(message);
            } catch (InterruptedException e) {
                throw new UncheckedException(e);
            }
        }
    }

    // TODO: Not used but we need to resolve notification response handling
    void send(JsonObject event) {
        try {
            if (event.containsKey("method") && event.containsKey("id")) {
                // event = handleRequest(event);
            } else if (event.containsKey("method") && !event.containsKey("id")) {
                // handleNotification(event);
            } else if (event.containsKey("result") || event.containsKey("error")) {
                handleResponse(event);
            }
            queue.put(event);
        } catch (InterruptedException e) {
            throw new UncheckedException(e);
        }
    }

    void disconnect() {
        LOGGER.log(System.Logger.Level.INFO, "Disconnecting session");
        if (active.compareAndSet(true, false)) {
            queue.offer(Json.createObjectBuilder().add("disconnect", "true").build());
            return;
        }
        LOGGER.log(System.Logger.Level.DEBUG, "Session is already disconnected.");
    }

    // TODO: Response to server notifications?
    void handleResponse(JsonObject response) {
        pendingResponses.remove(response.getString("id"));
    }

    enum State {
        INITIALIZED,
        INITIALIZING,
        UNINITIALIZED
    }

    public State state() {
        return state;
    }

    public void state(State state) {
        this.state = state;
    }

    public AtomicReference<JsonObject> clientInfo() {
        return clientInfo;
    }

    public AtomicReference<JsonObject> clientCapabilities() {
        return clientCapabilities;
    }

    void enqueue(JsonRpcResponse res) {
        try {
            queue.put(res.status(Status.ACCEPTED_202).asJsonObject());
        } catch (InterruptedException e) {
            throw new UncheckedException(e);
        }
    }
}
