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

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

class McpJsonRPC {
    private static final System.Logger LOGGER = System.getLogger(McpJsonRPC.class.getName());

    private McpJsonRPC() {
    }

    // ---------------------------
    // Method Names
    // ---------------------------

    // Lifecycle Methods
    static final String METHOD_INITIALIZE = "initialize";

    static final String METHOD_NOTIFICATION_INITIALIZED = "notifications/initialized";

    static final String METHOD_PING = "ping";

    // Tool Methods
    static final String METHOD_TOOLS_LIST = "tools/list";

    static final String METHOD_TOOLS_CALL = "tools/call";

    static final String METHOD_NOTIFICATION_TOOLS_LIST_CHANGED = "notifications/tools/list_changed";

    // Resources Methods
    static final String METHOD_RESOURCES_LIST = "resources/list";

    static final String METHOD_RESOURCES_READ = "resources/read";

    static final String METHOD_NOTIFICATION_RESOURCES_LIST_CHANGED = "notifications/resources/list_changed";

    static final String METHOD_RESOURCES_TEMPLATES_LIST = "resources/templates/list";

    static final String METHOD_RESOURCES_SUBSCRIBE = "resources/subscribe";

    static final String METHOD_RESOURCES_UNSUBSCRIBE = "resources/unsubscribe";

    // Prompt Methods
    static final String METHOD_PROMPT_LIST = "prompts/list";

    static final String METHOD_PROMPT_GET = "prompts/get";

    static final String METHOD_NOTIFICATION_PROMPTS_LIST_CHANGED = "notifications/prompts/list_changed";

    // Logging Methods
    static final String METHOD_LOGGING_SET_LEVEL = "logging/setLevel";

    static final String METHOD_NOTIFICATION_MESSAGE = "notifications/message";

    static final String METHOD_NOTIFICATION_CANCELED = "notifications/cancelled";

    // Roots Methods
    static final String METHOD_ROOTS_LIST = "roots/list";

    static final String METHOD_NOTIFICATION_ROOTS_LIST_CHANGED = "notifications/roots/list_changed";

    // Sampling Methods
    static final String METHOD_SAMPLING_CREATE_MESSAGE = "sampling/createMessage";

    static JsonObject json(ToolInfo info) {
        return Json.createObjectBuilder()
                .add("name", info.name())
                .add("description", info.description())
                .add("inputSchema", info.schema().json())
                .build();
    }

    static JsonObject json(ResourceInfo info) {
        return Json.createObjectBuilder()
                .add("uri", info.uri())
                .add("name", info.name())
                .add("description", info.description())
                .add("mimeType", info.mimeType())
                .build();
    }

    static JsonObject json(PromptInfo info) {
        JsonArrayBuilder array = Json.createArrayBuilder();
        info.arguments().stream()
                .map(io.helidon.integrations.mcp.server.PromptArgument::json)
                .forEach(array::add);
        return Json.createObjectBuilder()
                .add("name", info.name())
                .add("description", info.description())
                .add("arguments", array)
                .build();
    }

    static JsonObject json(ToolContent content) {
        if (content instanceof TextContent text) {
            return json(text);
        }
        if (content instanceof ImageContent image) {
            return json(image);
        }
        if (content instanceof ResourceReference resource) {
            return json(resource);
        }
        throw new McpException("Unknown content type: " + content);
    }

    private static JsonObject json(TextContent content) {
        return Json.createObjectBuilder()
                .add("type", content.type())
                .add("text", content.text())
                .build();
    }

    private static JsonObject json(ImageContent content) {
        return Json.createObjectBuilder()
                .add("type", content.type())
                .add("data", content.data())
                .add("mimeType", content.mimeType())
                .build();
    }

    private static JsonObject json(ResourceReference content) {
        return json(content.resource().info());
    }

    static JsonObject json(ResourceContent content) {
        if (content instanceof ResourceTextContent text) {
            return json(text);
        }
        if (content instanceof ResourceBinaryContent binary) {
            return json(binary);
        }
        throw new McpException("Unknown content type: " + content);
    }

    private static JsonObject json(ResourceTextContent text) {
        return Json.createObjectBuilder()
                .add("mimeType", text.mimeType())
                .add("text", text.data())
                .build();
    }

    private static JsonObject json(ResourceBinaryContent binary) {
        return Json.createObjectBuilder()
                .add("mimeType", binary.mimeType())
                .add("blob", binary.data())
                .build();
    }

    static JsonObject json(PromptContent prompt) {
        if (prompt instanceof PromptTextContent text) {
            return json(text);
        }
        if (prompt instanceof PromptImageContent image) {
            return json(image);
        }
        if (prompt instanceof ResourceReference resource) {
            return json(resource);
        }
        throw new McpException("Unknown prompt type: " + prompt);
    }

    static JsonObject json(PromptTextContent text) {
        return Json.createObjectBuilder()
                .add("role", text.role().getName())
                .add("content", json((TextContent) text.content()))
                .build();
    }

    static JsonObject json(PromptImageContent image) {
        return Json.createObjectBuilder()
                .add("role", image.role().getName())
                .add("conten", json((ImageContent) image.content()))
                .build();
    }

    static JsonObject json(McpServerInfo info) {
        return Json.createObjectBuilder()
                .add("name", info.name())
                .add("version", info.version())
                .build();
    }
}
