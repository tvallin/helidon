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

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

final class McpServerImpl {

	private static final String PROTOCOLE_VERSION = "2024-11-05";

	private final McpRouting routing;
	private final McpServerInfo info;
	private final List<String> protocolVersions = new ArrayList<>();
	private final Map<String, JsonRPCHandler> handlers = new HashMap<>();

	public McpServerImpl(McpServer server) {
		McpRouting.Builder routing = McpRouting.builder();
		Set<Capability> capabilities = server.info().capabilities();

		this.info = server.info();
		this.protocolVersions.add(PROTOCOLE_VERSION);

		handlers.put(McpJsonRPC.METHOD_PING, ping());
		handlers.put(McpJsonRPC.METHOD_INITIALIZE, initialize());

		if (!capabilities.contains(Capability.TOOL_LIST_CHANGED)) {
			handlers.put(McpJsonRPC.METHOD_TOOLS_LIST, toolsList());
			handlers.put(McpJsonRPC.METHOD_TOOLS_CALL, toolsCall());
		}

		if (!capabilities.contains(Capability.RESOURCE_LIST_CHANGED)) {
			handlers.put(McpJsonRPC.METHOD_RESOURCES_LIST, resourcesList());
			handlers.put(McpJsonRPC.METHOD_RESOURCES_READ, resourcesRead());
			handlers.put(McpJsonRPC.METHOD_RESOURCES_TEMPLATES_LIST, resourceTemplateList());
		}

		if (!capabilities.contains(Capability.RESOURCE_SUBSCRIBE)) {
			handlers.put(McpJsonRPC.METHOD_RESOURCES_SUBSCRIBE, resourceSubscribe());
			handlers.put(McpJsonRPC.METHOD_RESOURCES_UNSUBSCRIBE, resourceUnsubscribe());
		}

		if (!capabilities.contains(Capability.PROMPT_LIST_CHANGED)) {
			handlers.put(McpJsonRPC.METHOD_PROMPT_LIST, promptsList());
			handlers.put(McpJsonRPC.METHOD_PROMPT_GET, promptsGet());
		}

		if (!capabilities.contains(Capability.LOGGING)) {
			handlers.put(McpJsonRPC.METHOD_LOGGING_SET_LEVEL, logging());
		}

		server.setup(routing);
		this.routing = routing.build();
	}

	Map<String, JsonRPCHandler> handlers() {
		return this.handlers;
	}

	//TODO - How to maintain list of client subscription ?
	private JsonRPCHandler resourceUnsubscribe() {
		return null;
	}

	private JsonRPCHandler resourceSubscribe() {
		return null;
	}

	JsonRPCHandler ping() {
		return object -> Json.createObjectBuilder().add("ping", "pong").build();
	}

	//Return only the reponse payload
	JsonRPCHandler toolsList() {
		return cursor -> {
			JsonArrayBuilder builder = Json.createArrayBuilder();
			this.routing.tools().stream()
					.map(Tool::info)
					.map(McpJsonRPC::json)
					.forEach(builder::add);
			return Json.createObjectBuilder()
					.add("tools", builder.build())
					.build();
		};
	}

	JsonRPCHandler toolsCall() {
		return params -> {
			Optional<Tool> tool = this.routing.tools().stream()
					.filter(t -> params.getString("name").equals(t.info().name()))
					.findAny();
			McpParameter parameters = new McpParameter(params.getJsonObject("arguments"));
            return tool.map(value -> McpJsonRPC.json(value.process(parameters)))
					.map(result -> Json.createObjectBuilder()
							.add("content", Json.createArrayBuilder()
									.add(result))
							.build())
					.orElse(null);
        };
	}

	JsonRPCHandler resourcesList() {
		return params -> {
			JsonArrayBuilder builder = Json.createArrayBuilder();
			this.routing.resources().stream()
					.map(Resource::info)
					.map(McpJsonRPC::json)
					.forEach(builder::add);
			return Json.createObjectBuilder()
					.add("resources", builder.build())
					.build();
		};
	}

	JsonRPCHandler resourcesRead() {
		return params -> {
			String resourceUri = params.getString("uri");
			Optional<Resource> resource = this.routing.resources().stream()
					.filter(it -> Objects.equals(it.info().uri(), resourceUri))
					.findFirst();

			return resource.map(value -> Json.createObjectBuilder(McpJsonRPC.json(value.read()))
					.add("uri", resourceUri))
					.map(result -> Json.createObjectBuilder()
							.add("contents", Json.createArrayBuilder()
									.add(result))
							.build())
					.orElse(null);
		};
	}

	JsonRPCHandler resourceTemplateList() {
		return param -> {
			List<JsonObject> templates = this.routing.resources().stream()
					.map(Resource::info)
					.filter(this::isTemplate)
					.map(McpJsonRPC::json)
					.toList();
			return Json.createObjectBuilder()
					.add("resourceTemplates", Json.createArrayBuilder(templates))
					.build();
		};
	}

	private JsonRPCHandler promptsList() {
		return object -> {
			JsonArrayBuilder builder = Json.createArrayBuilder();
			this.routing.prompts().stream()
					.map(Prompt::info)
					.map(McpJsonRPC::json)
					.forEach(builder::add);
			return Json.createObjectBuilder()
					.add("prompts", builder.build())
					.build();
		};
	}

	JsonRPCHandler promptsGet() {
		return params -> {
			var prompt = this.routing.prompts().stream()
					.filter(p -> Objects.equals(p.info().name(), params.getString("name")))
					.findFirst();

			McpParameter parameters = new McpParameter(params.getJsonObject("arguments"));
			return prompt.map(value -> Json.createObjectBuilder()
							.add("description", value.info().description())
							.add("messages", Json.createArrayBuilder()
									.add(McpJsonRPC.json(value.prompt(parameters))))
							.build())
					.orElse(null);
		};
	}

	//Todo - Change the logging level in the sessions
	JsonRPCHandler logging() {
		return param -> Json.createObjectBuilder().build();
	}

	private JsonRPCHandler initialize() {
		return param -> {
			String protocoleVersion = this.protocolVersions.getLast();
			return Json.createObjectBuilder()
					.add("protocolVersion", protocoleVersion)
					.add("capabilities", Json.createObjectBuilder()
							.add("logging", Json.createObjectBuilder())
							.add("prompts", Json.createObjectBuilder()
									.add("listChanged", !info.capabilities().contains(Capability.PROMPT_LIST_CHANGED)))
							.add("tools", Json.createObjectBuilder()
									.add("listChanged", !info.capabilities().contains(Capability.TOOL_LIST_CHANGED)))
							.add("resources", Json.createObjectBuilder()
									.add("listChanged", !info.capabilities().contains(Capability.RESOURCE_LIST_CHANGED))
									.add("subscribe", !info.capabilities().contains(Capability.RESOURCE_SUBSCRIBE))))
					.add("serverInfo", McpJsonRPC.json(info))
					.add("instructions", "")
					.build();
		};
	}

	boolean isTemplate(ResourceInfo info) {
		String uri = info.uri();
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
