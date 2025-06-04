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

    static final String METHOD_COMPLETION_COMPLETE = "completion/complete";

    // Roots Methods
    static final String METHOD_ROOTS_LIST = "roots/list";

    static final String METHOD_NOTIFICATION_ROOTS_LIST_CHANGED = "notifications/roots/list_changed";

    // Sampling Methods
    static final String METHOD_SAMPLING_CREATE_MESSAGE = "sampling/createMessage";

    public static final int RESOURCE_NOT_FOUND = -32002;
    public static final int INTERNAL_ERROR = -32603;
    public static final int INVALID_PARAMS = -32602;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_REQUEST = -32600;
    public static final int PARSE_ERROR = -32700;
    public static final int SECURITY_ERROR = -32001;

}
