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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

final class McpServerImpl {

	private static final String PROTOCOLE_VERSION = "2024-11-05";

	private final McpRouting routing;
	private final McpServerInfo info;
	private final ObjectMapper mapper = new ObjectMapper();
	private final List<String> protocolVersions = new ArrayList<>();
	private final Map<String, JsonRPCHandler<?>> handlers = new HashMap<>();

	public McpServerImpl(McpServer server) {
		McpRouting.Builder routing = McpRouting.builder();
		Set<Capability> capabilities = server.info().capabilities();

		this.info = server.info();
		this.protocolVersions.add(PROTOCOLE_VERSION);

		handlers.put(McpJsonRPC.METHOD_PING, ping());
		handlers.put(McpJsonRPC.METHOD_INITIALIZE, initialize());

		if (capabilities.contains(Capability.TOOL_LIST_CHANGED)) {
			handlers.put(McpJsonRPC.METHOD_TOOLS_LIST, toolsList());
			handlers.put(McpJsonRPC.METHOD_TOOLS_CALL, toolsCall());
		}

		if (capabilities.contains(Capability.RESOURCE_LIST_CHANGED)) {
			handlers.put(McpJsonRPC.METHOD_RESOURCES_LIST, resourcesList());
			handlers.put(McpJsonRPC.METHOD_RESOURCES_READ, resourcesRead());
			handlers.put(McpJsonRPC.METHOD_RESOURCES_TEMPLATES_LIST, resourceTemplateList());
		}

		if (capabilities.contains(Capability.RESOURCE_SUBSCRIBE)) {
			handlers.put(McpJsonRPC.METHOD_RESOURCES_SUBSCRIBE, resourceSubscribe());
			handlers.put(McpJsonRPC.METHOD_RESOURCES_UNSUBSCRIBE, resourceUnsubscribe());
		}

		if (capabilities.contains(Capability.PROMPT_LIST_CHANGED)) {
			handlers.put(McpJsonRPC.METHOD_PROMPT_LIST, promptsList());
			handlers.put(McpJsonRPC.METHOD_PROMPT_GET, promptsGet());
		}

		if (capabilities.contains(Capability.LOGGING)) {
			handlers.put(McpJsonRPC.METHOD_LOGGING_SET_LEVEL, logging());
		}

		server.setup(routing);
		this.routing = routing.build();
	}

	Map<String, JsonRPCHandler<?>> handlers() {
		return this.handlers;
	}

	//TODO - How to maintain list of client subscription ?
	private JsonRPCHandler<?> resourceUnsubscribe() {
		return null;
	}

	private JsonRPCHandler<?> resourceSubscribe() {
		return null;
	}

	JsonRPCHandler<Object> ping() {
		return object -> "pong";
	}

	//Return only the reponse payload
	JsonRPCHandler<JsonObject> toolsList() {
		return cursor -> {
			JsonArrayBuilder builder = Json.createArrayBuilder();
			this.routing.tools().stream()
					.map(Tool::info)
					.map(ToolInfo::json)
					.forEach(builder::add);
			return Json.createObjectBuilder()
					.add("tools", builder.build())
					.build();
		};
	}

	JsonRPCHandler<JsonObject> toolsCall() {
		return params -> {
			McpJsonRPC.CallToolRequest callToolRequest = (McpJsonRPC.CallToolRequest) params;

			Optional<Tool> tool = this.routing.tools().stream()
					.filter(tr -> callToolRequest.name().equals(tr.info().name()))
					.findAny();

			if (tool.isEmpty()) {
				return new McpJsonRPC.CallToolResult(List.of(), true);
			}

			return tool.get()
					.process(Parameters.toParameters(callToolRequest.arguments()))
					.json();
		};
	}

	JsonRPCHandler<JsonObject> resourcesList() {
		return params -> {
			JsonArrayBuilder builder = Json.createArrayBuilder();
			this.routing.resources().stream()
					.map(Resource::info)
					.map(ResourceInfo::json)
					.forEach(builder::add);
			return Json.createObjectBuilder()
					.add("resources", builder.build())
					.build();
		};
	}

	JsonRPCHandler<JsonObject> resourcesRead() {
		return params -> {
			McpJsonRPC.ReadResourceRequest resourceRequest = (McpJsonRPC.ReadResourceRequest) params;

			String resourceUri = resourceRequest.uri();
			Optional<Resource> resource = this.routing.resources().stream()
					.filter(it -> Objects.equals(it.info().uri(), resourceUri))
					.findFirst();
			if (resource.isEmpty()) {
				//Return JSON-RPC error
				return Json.createObjectBuilder();
			}
			return resource.get().read().json();
		};
	}

	JsonRPCHandler<JsonObject> resourceTemplateList() {
		return param -> {
			List<JsonObject> templates = this.routing.resources().stream()
					.map(Resource::info)
					.filter(ResourceInfo::isTemplate)
					.map(ResourceInfo::json)
					.toList();
			return Json.createObjectBuilder().add("resourceTemplates", Json.createArrayBuilder(templates)).build();
		};
	}

	private JsonRPCHandler<JsonObject> promptsList() {
		return object -> {
			JsonArrayBuilder builder = Json.createArrayBuilder();
			this.routing.prompts().stream()
					.map(Prompt::info)
					.map(PromptInfo::json)
					.forEach(builder::add);
			return Json.createObjectBuilder()
					.add("prompts", builder.build())
					.build();
		};
	}

	JsonRPCHandler<JsonObject> promptsGet() {
		return params -> {
			McpJsonRPC.GetPromptRequest promptRequest = (McpJsonRPC.GetPromptRequest) params;
			var prompt = this.routing.prompts().stream()
					.filter(p -> Objects.equals(p.info().name(), promptRequest.name()))
					.findFirst();
			if (prompt.isEmpty()) {
				//Return JSON-RPC error
				return Json.createObjectBuilder();
			}
			return prompt.get()
					.prompt(Parameters.toParameters(promptRequest.arguments()))
					.json();
		};
	}

	//Todo - Change the logging level in the sessions
	JsonRPCHandler<McpJsonRPC.LoggingMessageNotification> logging() {
		return param -> new McpJsonRPC.LoggingMessageNotification(McpJsonRPC.LoggingLevel.INFO, "", "");
	}

	private JsonRPCHandler<McpJsonRPC.InitializeResult> initialize() {
		return param -> {
			McpJsonRPC.InitializeRequest request = mapper.convertValue(param, new TypeReference<>() {});
			String protocoleVersion = this.protocolVersions.getLast();

			if (this.protocolVersions.contains(request.protocolVersion())) {
				protocoleVersion = request.protocolVersion();
			}

			return null;
		};
	}

	interface JsonRPCHandler<T> {
		/**
		 * Handles a request from the client.
		 *
		 * @param params the parameters of the request.
		 */
		T handle(Object params);
	}
}
