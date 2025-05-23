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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

class McpJsonRPC {
    private static final System.Logger LOGGER = System.getLogger(McpJsonRPC.class.getName());

    private McpJsonRPC() {
    }

    static final String LATEST_PROTOCOL_VERSION = "2024-11-05";

    static final String JSONRPC_VERSION = "2.0";

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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // ---------------------------
    // JSON-RPC Error Codes
    // ---------------------------

    /**
     * Standard error codes used in MCP JSON-RPC responses.
     */
    static final class ErrorCodes {

        /**
         * Invalid JSON was received by the server.
         */
        static final int PARSE_ERROR = -32700;

        /**
         * The JSON sent is not a valid Request object.
         */
        static final int INVALID_REQUEST = -32600;

        /**
         * The method does not exist / is not available.
         */
        static final int METHOD_NOT_FOUND = -32601;

        /**
         * Invalid method parameter(s).
         */
        static final int INVALID_PARAMS = -32602;

        /**
         * Internal JSON-RPC error.
         */
        static final int INTERNAL_ERROR = -32603;

    }

    sealed interface Request
            permits InitializeRequest, CallToolRequest, CreateMessageRequest, CompleteRequest, GetPromptRequest {

    }

    private static final TypeReference<HashMap<String, Object>> MAP_TYPE_REF = new TypeReference<>() {
    };

    /**
     * Deserializes a JSON string into a JSONRPCMessage object.
     *
     * @param objectMapper The ObjectMapper instance to use for deserialization
     * @param jsonText     The JSON string to deserialize
     * @return A JSONRPCMessage instance using either the {@link JSONRPCRequest},
     * {@link JSONRPCNotification}, or {@link JSONRPCResponse} classes.
     * @throws IOException              If there's an error during deserialization
     * @throws IllegalArgumentException If the JSON structure doesn't match any known
     *                                  message type
     */
    static JSONRPCMessage deserializeJsonRpcMessage(ObjectMapper objectMapper, String jsonText)
            throws IOException {

        LOGGER.log(System.Logger.Level.DEBUG, "Received JSON message: %s", jsonText);

        var map = objectMapper.readValue(jsonText, MAP_TYPE_REF);

        // Determine message type based on specific JSON structure
        if (map.containsKey("method") && map.containsKey("id")) {
            return objectMapper.convertValue(map, JSONRPCRequest.class);
        } else if (map.containsKey("method") && !map.containsKey("id")) {
            return objectMapper.convertValue(map, JSONRPCNotification.class);
        } else if (map.containsKey("result") || map.containsKey("error")) {
            return objectMapper.convertValue(map, JSONRPCResponse.class);
        }

        throw new IllegalArgumentException("Cannot deserialize JSONRPCMessage: " + jsonText);
    }

    // ---------------------------
    // JSON-RPC Message Types
    // ---------------------------
    interface JSONRPCMessage {

        String jsonrpc();

    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record JSONRPCRequest( // @formatter:off
                                  @JsonProperty("jsonrpc") String jsonrpc,
                                  @JsonProperty("method") String method,
                                  @JsonProperty("id") Object id,
                                  @JsonProperty("params") Object params) implements JSONRPCMessage {
    } // @formatter:on

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record JSONRPCNotification( // @formatter:off
                                       @JsonProperty("jsonrpc") String jsonrpc,
                                       @JsonProperty("method") String method,
                                       @JsonProperty("params") Map<String, Object> params) implements JSONRPCMessage {
    } // @formatter:on

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record JSONRPCResponse( // @formatter:off
                                   @JsonProperty("jsonrpc") String jsonrpc,
                                   @JsonProperty("id") Object id,
                                   @JsonProperty("result") Object result,
                                   @JsonProperty("error") JSONRPCResponse.JSONRPCError error) implements JSONRPCMessage {

        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        @JsonIgnoreProperties(ignoreUnknown = true)
        record JSONRPCError(
                @JsonProperty("code") int code,
                @JsonProperty("message") String message,
                @JsonProperty("data") Object data) {
        }
    }// @formatter:on

    // ---------------------------
    // Initialization
    // ---------------------------
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record InitializeRequest( // @formatter:off
                             @JsonProperty("protocolVersion") String protocolVersion,
                             @JsonProperty("capabilities") ClientCapabilities capabilities,
                             @JsonProperty("clientInfo") Implementation clientInfo) implements Request {
    } // @formatter:on

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record InitializeResult( // @formatter:off
                            @JsonProperty("protocolVersion") String protocolVersion,
                            @JsonProperty("capabilities") ServerCapabilities capabilities,
                            @JsonProperty("serverInfo") Implementation serverInfo,
                            @JsonProperty("instructions") String instructions) {
    } // @formatter:on

    /**
     * Clients can implement additional features to enrich connected MCP servers with
     * additional capabilities. These capabilities can be used to extend the functionality
     * of the server, or to provide additional information to the server about the
     * client's capabilities.
     *
     * @param experimental WIP
     * @param roots        define the boundaries of where servers can operate within the
     *                     filesystem, allowing them to understand which directories and files they have
     *                     access to.
     * @param sampling     Provides a standardized way for servers to request LLM sampling
     *                     (“completions” or “generations”) from language models via clients.
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ClientCapabilities( // @formatter:off
                              @JsonProperty("experimental") Map<String, Object> experimental,
                              @JsonProperty("roots") ClientCapabilities.RootCapabilities roots,
                              @JsonProperty("sampling") ClientCapabilities.Sampling sampling) {

        /**
         * Roots define the boundaries of where servers can operate within the filesystem,
         * allowing them to understand which directories and files they have access to.
         * Servers can request the list of roots from supporting clients and
         * receive notifications when that list changes.
         *
         * @param listChanged Whether the client would send notification about roots
         * 		  has changed since the last time the server checked.
         */
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        @JsonIgnoreProperties(ignoreUnknown = true)
        record RootCapabilities(
                @JsonProperty("listChanged") Boolean listChanged) {
        }

        /**
         * Provides a standardized way for servers to request LLM
         * sampling ("completions" or "generations") from language
         * models via clients. This flow allows clients to maintain
         * control over model access, selection, and permissions
         * while enabling servers to leverage AI capabilities—with
         * no server API keys necessary. Servers can request text or
         * image-based interactions and optionally include context
         * from MCP servers in their prompts.
         */
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        record Sampling() {
        }

        static ClientCapabilities.Builder builder() {
            return new ClientCapabilities.Builder();
        }

        static class Builder {
            private Map<String, Object> experimental;
            private ClientCapabilities.RootCapabilities roots;
            private ClientCapabilities.Sampling sampling;

            ClientCapabilities.Builder experimental(Map<String, Object> experimental) {
                this.experimental = experimental;
                return this;
            }

            ClientCapabilities.Builder roots(Boolean listChanged) {
                this.roots = new ClientCapabilities.RootCapabilities(listChanged);
                return this;
            }

            ClientCapabilities.Builder sampling() {
                this.sampling = new ClientCapabilities.Sampling();
                return this;
            }

            ClientCapabilities build() {
                return new ClientCapabilities(experimental, roots, sampling);
            }
        }
    }// @formatter:on

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ServerCapabilities( // @formatter:off
                                      @JsonProperty("experimental") Map<String, Object> experimental,
                                      @JsonProperty("logging") ServerCapabilities.LoggingCapabilities logging,
                                      @JsonProperty("prompts") ServerCapabilities.PromptCapabilities prompts,
                                      @JsonProperty("resources") ServerCapabilities.ResourceCapabilities resources,
                                      @JsonProperty("tools") ServerCapabilities.ToolCapabilities tools) {


        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        record LoggingCapabilities() {
        }

        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        record PromptCapabilities(
                @JsonProperty("listChanged") Boolean listChanged) {
        }

        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        record ResourceCapabilities(
                @JsonProperty("subscribe") Boolean subscribe,
                @JsonProperty("listChanged") Boolean listChanged) {
        }

        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        record ToolCapabilities(
                @JsonProperty("listChanged") Boolean listChanged) {
        }

        static ServerCapabilities.Builder builder() {
            return new ServerCapabilities.Builder();
        }

        static class Builder {

            private Map<String, Object> experimental;
            private ServerCapabilities.LoggingCapabilities logging = new ServerCapabilities.LoggingCapabilities();
            private ServerCapabilities.PromptCapabilities prompts;
            private ServerCapabilities.ResourceCapabilities resources;
            private ServerCapabilities.ToolCapabilities tools;

            ServerCapabilities.Builder experimental(Map<String, Object> experimental) {
                this.experimental = experimental;
                return this;
            }

            ServerCapabilities.Builder logging() {
                this.logging = new ServerCapabilities.LoggingCapabilities();
                return this;
            }

            ServerCapabilities.Builder prompts(Boolean listChanged) {
                this.prompts = new ServerCapabilities.PromptCapabilities(listChanged);
                return this;
            }

            ServerCapabilities.Builder resources(Boolean subscribe, Boolean listChanged) {
                this.resources = new ServerCapabilities.ResourceCapabilities(subscribe, listChanged);
                return this;
            }

            ServerCapabilities.Builder tools(Boolean listChanged) {
                this.tools = new ServerCapabilities.ToolCapabilities(listChanged);
                return this;
            }

            ServerCapabilities build() {
                return new ServerCapabilities(experimental, logging, prompts, resources, tools);
            }
        }
    } // @formatter:on

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Implementation(// @formatter:off
                                 @JsonProperty("name") String name,
                                 @JsonProperty("version") String version) {
    } // @formatter:on

    // Existing Enums and Base Types (from previous implementation)
    enum Role {// @formatter:off

        @JsonProperty("user") USER,
        @JsonProperty("assistant") ASSISTANT
    }// @formatter:on

    // ---------------------------
    // Resource Interfaces
    // ---------------------------

    /**
     * Base for objects that include optional annotations for the client. The client can
     * use annotations to inform how objects are used or displayed
     */
    interface Annotated {

        Annotations annotations();

    }

    /**
     * Optional annotations for the client. The client can use annotations to inform how
     * objects are used or displayed.
     *
     * @param audience Describes who the intended customer of this object or data is. It
     *                 can include multiple entries to indicate content useful for multiple audiences
     *                 (e.g., `["user", "assistant"]`).
     * @param priority Describes how important this data is for operating the server. A
     *                 value of 1 means "most important," and indicates that the data is effectively
     *                 required, while 0 means "least important," and indicates that the data is entirely
     *                 optional. It is a number between 0 and 1.
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Annotations( // @formatter:off
                               @JsonProperty("audience") List<Role> audience,
                               @JsonProperty("priority") Double priority) {
    } // @formatter:on

    /**
     * A known resource that the server is capable of reading.
     *
     * @param uri         the URI of the resource.
     * @param name        A human-readable name for this resource. This can be used by clients to
     *                    populate UI elements.
     * @param description A description of what this resource represents. This can be used
     *                    by clients to improve the LLM's understanding of available resources. It can be
     *                    thought of like a "hint" to the model.
     * @param mimeType    The MIME type of this resource, if known.
     * @param annotations Optional annotations for the client. The client can use
     *                    annotations to inform how objects are used or displayed.
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Resource( // @formatter:off
                            @JsonProperty("uri") String uri,
                            @JsonProperty("name") String name,
                            @JsonProperty("description") String description,
                            @JsonProperty("mimeType") String mimeType,
                            @JsonProperty("annotations") Annotations annotations) implements Annotated {
    } // @formatter:on

    /**
     * Resource templates allow servers to expose parameterized resources using URI
     * templates.
     *
     * @param uriTemplate A URI template that can be used to generate URIs for this
     *                    resource.
     * @param name        A human-readable name for this resource. This can be used by clients to
     *                    populate UI elements.
     * @param description A description of what this resource represents. This can be used
     *                    by clients to improve the LLM's understanding of available resources. It can be
     *                    thought of like a "hint" to the model.
     * @param mimeType    The MIME type of this resource, if known.
     * @param annotations Optional annotations for the client. The client can use
     *                    annotations to inform how objects are used or displayed.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc6570">RFC 6570</a>
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ResourceTemplate( // @formatter:off
                                    @JsonProperty("uriTemplate") String uriTemplate,
                                    @JsonProperty("name") String name,
                                    @JsonProperty("description") String description,
                                    @JsonProperty("mimeType") String mimeType,
                                    @JsonProperty("annotations") Annotations annotations) implements Annotated {
    } // @formatter:on

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ListResourcesResult( // @formatter:off
                                       @JsonProperty("resources") List<Resource> resources,
                                       @JsonProperty("nextCursor") String nextCursor) {
    } // @formatter:on

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ListResourceTemplatesResult( // @formatter:off
                                               @JsonProperty("resourceTemplates") List<ResourceTemplate> resourceTemplates,
                                               @JsonProperty("nextCursor") String nextCursor) {
    } // @formatter:on

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ReadResourceRequest( // @formatter:off
                                       @JsonProperty("uri") String uri){
    } // @formatter:on

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ReadResourceResult( // @formatter:off
                                      @JsonProperty("contents") List<ResourceContents> contents){
    } // @formatter:on

    /**
     * Sent from the client to request resources/updated notifications from the server
     * whenever a particular resource changes.
     *
     * @param uri the URI of the resource to subscribe to. The URI can use any protocol;
     *            it is up to the server how to interpret it.
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record SubscribeRequest( // @formatter:off
                                    @JsonProperty("uri") String uri){
    } // @formatter:on

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record UnsubscribeRequest( // @formatter:off
                                      @JsonProperty("uri") String uri){
    } // @formatter:on

    /**
     * The contents of a specific resource or sub-resource.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, include = JsonTypeInfo.As.PROPERTY)
    @JsonSubTypes({@JsonSubTypes.Type(value = TextResourceContents.class, name = "text"),
            @JsonSubTypes.Type(value = BlobResourceContents.class, name = "blob")})
    sealed interface ResourceContents permits TextResourceContents, BlobResourceContents {

        /**
         * The URI of this resource.
         *
         * @return the URI of this resource.
         */
        String uri();

        /**
         * The MIME type of this resource.
         *
         * @return the MIME type of this resource.
         */
        String mimeType();

    }

    /**
     * Text contents of a resource.
     *
     * @param uri      the URI of this resource.
     * @param mimeType the MIME type of this resource.
     * @param text     the text of the resource. This must only be set if the resource can
     *                 actually be represented as text (not binary data).
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record TextResourceContents( // @formatter:off
                                        @JsonProperty("uri") String uri,
                                        @JsonProperty("mimeType") String mimeType,
                                        @JsonProperty("text") String text) implements ResourceContents {
    } // @formatter:on

    /**
     * Binary contents of a resource.
     *
     * @param uri      the URI of this resource.
     * @param mimeType the MIME type of this resource.
     * @param blob     a base64-encoded string representing the binary data of the resource.
     *                 This must only be set if the resource can actually be represented as binary data
     *                 (not text).
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record BlobResourceContents( // @formatter:off
                                        @JsonProperty("uri") String uri,
                                        @JsonProperty("mimeType") String mimeType,
                                        @JsonProperty("blob") String blob) implements ResourceContents {
    } // @formatter:on

    // ---------------------------
    // Prompt Interfaces
    // ---------------------------

    /**
     * A prompt or prompt template that the server offers.
     *
     * @param name        The name of the prompt or prompt template.
     * @param description An optional description of what this prompt provides.
     * @param arguments   A list of arguments to use for templating the prompt.
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Prompt( // @formatter:off
                          @JsonProperty("name") String name,
                          @JsonProperty("description") String description,
                          @JsonProperty("arguments") List<PromptArgument> arguments) {
    } // @formatter:on

    /**
     * Describes an argument that a prompt can accept.
     *
     * @param name        The name of the argument.
     * @param description A human-readable description of the argument.
     * @param required    Whether this argument must be provided.
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record PromptArgument( // @formatter:off
                                  @JsonProperty("name") String name,
                                  @JsonProperty("description") String description,
                                  @JsonProperty("required") Boolean required) {
    }// @formatter:on

    /**
     * Describes a message returned as part of a prompt.
     * <p>
     * This is similar to `SamplingMessage`, but also supports the embedding of resources
     * from the MCP server.
     *
     * @param role    The sender or recipient of messages and data in a conversation.
     * @param content The content of the message of type {@link Content}.
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record PromptMessage( // @formatter:off
                                 @JsonProperty("role") Role role,
                                 @JsonProperty("content") Content content) {
    } // @formatter:on

    /**
     * The server's response to a prompts/list request from the client.
     *
     * @param prompts    A list of prompts that the server provides.
     * @param nextCursor An optional cursor for pagination. If present, indicates there
     *                   are more prompts available.
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ListPromptsResult( // @formatter:off
                                     @JsonProperty("prompts") List<Prompt> prompts,
                                     @JsonProperty("nextCursor") String nextCursor) {
    }// @formatter:on

    /**
     * Used by the client to get a prompt provided by the server.
     *
     * @param name      The name of the prompt or prompt template.
     * @param arguments Arguments to use for templating the prompt.
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GetPromptRequest(// @formatter:off
                                   @JsonProperty("name") String name,
                                   @JsonProperty("arguments") Map<String, Object> arguments) implements Request {
    }// @formatter:off

    /**
     * The server's response to a prompts/get request from the client.
     *
     * @param description An optional description for the prompt.
     * @param messages A list of messages to display as part of the prompt.
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GetPromptResult( // @formatter:off
                                   @JsonProperty("description") String description,
                                   @JsonProperty("messages") List<PromptMessage> messages) {
    } // @formatter:on

    // ---------------------------
    // Tool Interfaces
    // ---------------------------

    /**
     * The server's response to a tools/list request from the client.
     *
     * @param tools      A list of tools that the server provides.
     * @param nextCursor An optional cursor for pagination. If present, indicates there
     *                   are more tools available.
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ListToolsResult( // @formatter:off
                                   @JsonProperty("tools") List<Tool> tools,
                                   @JsonProperty("nextCursor") String nextCursor) {
    }// @formatter:on

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record JsonSchema( // @formatter:off
                              @JsonProperty("type") String type,
                              @JsonProperty("properties") Map<String, Object> properties,
                              @JsonProperty("required") List<String> required,
                              @JsonProperty("additionalProperties") Boolean additionalProperties) {
    } // @formatter:on

    /**
     * Represents a tool that the server provides. Tools enable servers to expose
     * executable functionality to the system. Through these tools, you can interact with
     * external systems, perform computations, and take actions in the real world.
     *
     * @param name        A unique identifier for the tool. This name is used when calling the
     *                    tool.
     * @param description A human-readable description of what the tool does. This can be
     *                    used by clients to improve the LLM's understanding of available tools.
     * @param inputSchema A JSON Schema object that describes the expected structure of
     *                    the arguments when calling this tool. This allows clients to validate tool
     *                    arguments before sending them to the server.
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Tool( // @formatter:off
                        @JsonProperty("name") String name,
                        @JsonProperty("description") String description,
                        @JsonProperty("inputSchema") JsonSchema inputSchema) {

        Tool(String name, String description, String schema) {
            this(name, description, parseSchema(schema));
        }

    } // @formatter:on

    private static JsonSchema parseSchema(String schema) {
        try {
            return OBJECT_MAPPER.readValue(schema, JsonSchema.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid schema: " + schema, e);
        }
    }

    /**
     * Used by the client to call a tool provided by the server.
     *
     * @param name      The name of the tool to call. This must match a tool name from
     *                  tools/list.
     * @param arguments Arguments to pass to the tool. These must conform to the tool's
     *                  input schema.
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record CallToolRequest(// @formatter:off
                                  @JsonProperty("name") String name,
                                  @JsonProperty("arguments") Map<String, Object> arguments) implements Request {
    }// @formatter:off

    /**
     * The server's response to a tools/call request from the client.
     *
     * @param content A list of content items representing the tool's output. Each item can be text, an image,
     *                or an embedded resource.
     * @param isError If true, indicates that the tool execution failed and the content contains error information.
     *                If false or absent, indicates successful execution.
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record CallToolResult( // @formatter:off
                                  @JsonProperty("content") List<Content> content,
                                  @JsonProperty("isError") Boolean isError) {
    } // @formatter:on

    // ---------------------------
    // Sampling Interfaces
    // ---------------------------
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ModelPreferences(// @formatter:off
                                   @JsonProperty("hints") List<ModelHint> hints,
                                   @JsonProperty("costPriority") Double costPriority,
                                   @JsonProperty("speedPriority") Double speedPriority,
                                   @JsonProperty("intelligencePriority") Double intelligencePriority) {

        static ModelPreferences.Builder builder() {
            return new ModelPreferences.Builder();
        }

        static class Builder {
            private List<ModelHint> hints;
            private Double costPriority;
            private Double speedPriority;
            private Double intelligencePriority;

            ModelPreferences.Builder hints(List<ModelHint> hints) {
                this.hints = hints;
                return this;
            }

            ModelPreferences.Builder addHint(String name) {
                if (this.hints == null) {
                    this.hints = new ArrayList<>();
                }
                this.hints.add(new ModelHint(name));
                return this;
            }

            ModelPreferences.Builder costPriority(Double costPriority) {
                this.costPriority = costPriority;
                return this;
            }

            ModelPreferences.Builder speedPriority(Double speedPriority) {
                this.speedPriority = speedPriority;
                return this;
            }

            ModelPreferences.Builder intelligencePriority(Double intelligencePriority) {
                this.intelligencePriority = intelligencePriority;
                return this;
            }

            ModelPreferences build() {
                return new ModelPreferences(hints, costPriority, speedPriority, intelligencePriority);
            }
        }
    } // @formatter:on

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ModelHint(@JsonProperty("name") String name) {
        static ModelHint of(String name) {
            return new ModelHint(name);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record SamplingMessage(// @formatter:off
                                  @JsonProperty("role") Role role,
                                  @JsonProperty("content") Content content) {
    } // @formatter:on

    // Sampling and Message Creation
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record CreateMessageRequest(// @formatter:off
                                       @JsonProperty("messages") List<SamplingMessage> messages,
                                       @JsonProperty("modelPreferences") ModelPreferences modelPreferences,
                                       @JsonProperty("systemPrompt") String systemPrompt,
                                       @JsonProperty("includeContext") CreateMessageRequest.ContextInclusionStrategy includeContext,
                                       @JsonProperty("temperature") Double temperature,
                                       @JsonProperty("maxTokens") int maxTokens,
                                       @JsonProperty("stopSequences") List<String> stopSequences,
                                       @JsonProperty("metadata") Map<String, Object> metadata) implements Request {

        enum ContextInclusionStrategy {
            @JsonProperty("none") NONE,
            @JsonProperty("thisServer") THIS_SERVER,
            @JsonProperty("allServers") ALL_SERVERS
        }

        static CreateMessageRequest.Builder builder() {
            return new CreateMessageRequest.Builder();
        }

        static class Builder {
            private List<SamplingMessage> messages;
            private ModelPreferences modelPreferences;
            private String systemPrompt;
            private CreateMessageRequest.ContextInclusionStrategy includeContext;
            private Double temperature;
            private int maxTokens;
            private List<String> stopSequences;
            private Map<String, Object> metadata;

            CreateMessageRequest.Builder messages(List<SamplingMessage> messages) {
                this.messages = messages;
                return this;
            }

            CreateMessageRequest.Builder modelPreferences(ModelPreferences modelPreferences) {
                this.modelPreferences = modelPreferences;
                return this;
            }

            CreateMessageRequest.Builder systemPrompt(String systemPrompt) {
                this.systemPrompt = systemPrompt;
                return this;
            }

            CreateMessageRequest.Builder includeContext(CreateMessageRequest.ContextInclusionStrategy includeContext) {
                this.includeContext = includeContext;
                return this;
            }

            CreateMessageRequest.Builder temperature(Double temperature) {
                this.temperature = temperature;
                return this;
            }

            CreateMessageRequest.Builder maxTokens(int maxTokens) {
                this.maxTokens = maxTokens;
                return this;
            }

            CreateMessageRequest.Builder stopSequences(List<String> stopSequences) {
                this.stopSequences = stopSequences;
                return this;
            }

            CreateMessageRequest.Builder metadata(Map<String, Object> metadata) {
                this.metadata = metadata;
                return this;
            }

            CreateMessageRequest build() {
                return new CreateMessageRequest(messages, modelPreferences, systemPrompt,
                        includeContext, temperature, maxTokens, stopSequences, metadata);
            }
        }
    }// @formatter:on

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record CreateMessageResult(// @formatter:off
                                      @JsonProperty("role") Role role,
                                      @JsonProperty("content") Content content,
                                      @JsonProperty("model") String model,
                                      @JsonProperty("stopReason") CreateMessageResult.StopReason stopReason) {

        enum StopReason {
            @JsonProperty("endTurn") END_TURN,
            @JsonProperty("stopSequence") STOP_SEQUENCE,
            @JsonProperty("maxTokens") MAX_TOKENS
        }

        static CreateMessageResult.Builder builder() {
            return new CreateMessageResult.Builder();
        }

        static class Builder {
            private Role role = Role.ASSISTANT;
            private Content content;
            private String model;
            private CreateMessageResult.StopReason stopReason = CreateMessageResult.StopReason.END_TURN;

            CreateMessageResult.Builder role(Role role) {
                this.role = role;
                return this;
            }

            CreateMessageResult.Builder content(Content content) {
                this.content = content;
                return this;
            }

            CreateMessageResult.Builder model(String model) {
                this.model = model;
                return this;
            }

            CreateMessageResult.Builder stopReason(CreateMessageResult.StopReason stopReason) {
                this.stopReason = stopReason;
                return this;
            }

            CreateMessageResult.Builder message(String message) {
                this.content = new TextContent(message);
                return this;
            }

            CreateMessageResult build() {
                return new CreateMessageResult(role, content, model, stopReason);
            }
        }
    }// @formatter:on

    // ---------------------------
    // Pagination Interfaces
    // ---------------------------
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record PaginatedRequest(@JsonProperty("cursor") String cursor) {
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record PaginatedResult(@JsonProperty("nextCursor") String nextCursor) {
    }

    // ---------------------------
    // Progress and Logging
    // ---------------------------
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ProgressNotification(// @formatter:off
                                       @JsonProperty("progressToken") String progressToken,
                                       @JsonProperty("progress") double progress,
                                       @JsonProperty("total") Double total) {
    }// @formatter:on

    /**
     * The Model Context Protocol (MCP) provides a standardized way for servers to send
     * structured log messages to clients. Clients can control logging verbosity by
     * setting minimum log levels, with servers sending notifications containing severity
     * levels, optional logger names, and arbitrary JSON-serializable data.
     *
     * @param level  The severity levels. The mimimum log level is set by the client.
     * @param logger The logger that generated the message.
     * @param data   JSON-serializable logging data.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record LoggingMessageNotification(// @formatter:off
                                             @JsonProperty("level") LoggingLevel level,
                                             @JsonProperty("logger") String logger,
                                             @JsonProperty("data") String data) {

        static LoggingMessageNotification.Builder builder() {
            return new LoggingMessageNotification.Builder();
        }

        static class Builder {
            private LoggingLevel level = LoggingLevel.INFO;
            private String logger = "server";
            private String data;

            LoggingMessageNotification.Builder level(LoggingLevel level) {
                this.level = level;
                return this;
            }

            LoggingMessageNotification.Builder logger(String logger) {
                this.logger = logger;
                return this;
            }

            LoggingMessageNotification.Builder data(String data) {
                this.data = data;
                return this;
            }

            LoggingMessageNotification build() {
                return new LoggingMessageNotification(level, logger, data);
            }
        }
    }// @formatter:on

    enum LoggingLevel {// @formatter:off
        @JsonProperty("debug") DEBUG(0),
        @JsonProperty("info") INFO(1),
        @JsonProperty("notice") NOTICE(2),
        @JsonProperty("warning") WARNING(3),
        @JsonProperty("error") ERROR(4),
        @JsonProperty("critical") CRITICAL(5),
        @JsonProperty("alert") ALERT(6),
        @JsonProperty("emergency") EMERGENCY(7);

        private final int level;

        LoggingLevel(int level) {
            this.level = level;
        }

        int level() {
            return level;
        }

    } // @formatter:on

    // ---------------------------
    // Autocomplete
    // ---------------------------
    record CompleteRequest(CompleteRequest.PromptOrResourceReference ref,
                           CompleteRequest.CompleteArgument argument) implements Request {
        sealed interface PromptOrResourceReference permits CompleteRequest.PromptReference, CompleteRequest.ResourceReference {

            String type();

        }

        record PromptReference(// @formatter:off
                                      @JsonProperty("type") String type,
                                      @JsonProperty("name") String name) implements CompleteRequest.PromptOrResourceReference {
        }// @formatter:on

        record ResourceReference(// @formatter:off
                                        @JsonProperty("type") String type,
                                        @JsonProperty("uri") String uri) implements CompleteRequest.PromptOrResourceReference {
        }// @formatter:on

        record CompleteArgument(// @formatter:off
                                       @JsonProperty("name") String name,
                                       @JsonProperty("value") String value) {
        }// @formatter:on
    }

    record CompleteResult(CompleteResult.CompleteCompletion completion) {
        record CompleteCompletion(// @formatter:off
                                         @JsonProperty("values") List<String> values,
                                         @JsonProperty("total") Integer total,
                                         @JsonProperty("hasMore") Boolean hasMore) {
        }// @formatter:on
    }

    // ---------------------------
    // Content Types
    // ---------------------------
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({@JsonSubTypes.Type(value = TextContent.class, name = "text"),
            @JsonSubTypes.Type(value = ImageContent.class, name = "image"),
            @JsonSubTypes.Type(value = EmbeddedResource.class, name = "resource")})
    sealed interface Content permits TextContent, ImageContent, EmbeddedResource {

        default String type() {
            if (this instanceof TextContent) {
                return "text";
            } else if (this instanceof ImageContent) {
                return "image";
            } else if (this instanceof EmbeddedResource) {
                return "resource";
            }
            throw new IllegalArgumentException("Unknown content type: " + this);
        }

    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record TextContent( // @formatter:off
                               @JsonProperty("audience") List<Role> audience,
                               @JsonProperty("priority") Double priority,
                               @JsonProperty("text") String text) implements Content { // @formatter:on

        TextContent(String content) {
            this(null, null, content);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ImageContent( // @formatter:off
                                @JsonProperty("audience") List<Role> audience,
                                @JsonProperty("priority") Double priority,
                                @JsonProperty("data") String data,
                                @JsonProperty("mimeType") String mimeType) implements Content { // @formatter:on
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record EmbeddedResource( // @formatter:off
                                    @JsonProperty("audience") List<Role> audience,
                                    @JsonProperty("priority") Double priority,
                                    @JsonProperty("resource") ResourceContents resource) implements Content { // @formatter:on
    }

    // ---------------------------
    // Roots
    // ---------------------------

    /**
     * Represents a root directory or file that the server can operate on.
     *
     * @param uri  The URI identifying the root. This *must* start with file:// for now.
     *             This restriction may be relaxed in future versions of the protocol to allow other
     *             URI schemes.
     * @param name An optional name for the root. This can be used to provide a
     *             human-readable identifier for the root, which may be useful for display purposes or
     *             for referencing the root in other parts of the application.
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Root( // @formatter:off
                        @JsonProperty("uri") String uri,
                        @JsonProperty("name") String name) {
    } // @formatter:on

    /**
     * The client's response to a roots/list request from the server. This result contains
     * an array of Root objects, each representing a root directory or file that the
     * server can operate on.
     *
     * @param roots An array of Root objects, each representing a root directory or file
     *              that the server can operate on.
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ListRootsResult( // @formatter:off
                                   @JsonProperty("roots") List<Root> roots) {
    } // @formatter:on

}
