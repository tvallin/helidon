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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

final class McpServer {

	private static final String PROTOCOLE_VERSION = "2024-11-05";

	private final McpRouting routing;
	private final McpServerInfo info;
	private final ObjectMapper mapper = new ObjectMapper();
	private final List<String> protocolVersions = new ArrayList<>();
	private final Map<String, JsonRPCHandler<?>> handlers = new HashMap<>();

	public McpServer(McpServerConfig server) {
		McpRouting.Builder routing = McpRouting.builder();
		Set<Capabilities> capabilities = server.info().capabilities();

		this.info = server.info();
		this.protocolVersions.add(PROTOCOLE_VERSION);

		handlers.put(McpJsonRPC.METHOD_PING, ping());
		handlers.put(McpJsonRPC.METHOD_INITIALIZE, initialize());

		if (capabilities.contains(Capabilities.TOOL_LIST_CHANGED)) {
			handlers.put(McpJsonRPC.METHOD_TOOLS_LIST, toolsList());
			handlers.put(McpJsonRPC.METHOD_TOOLS_CALL, toolsCall());
		}

		if (capabilities.contains(Capabilities.RESOURCE_LIST_CHANGED)) {
			handlers.put(McpJsonRPC.METHOD_RESOURCES_LIST, resourcesList());
			handlers.put(McpJsonRPC.METHOD_RESOURCES_READ, resourcesRead());
			handlers.put(McpJsonRPC.METHOD_RESOURCES_TEMPLATES_LIST, resourceTemplateList());
		}

		if (capabilities.contains(Capabilities.RESOURCE_SUBSCRIBE)) {
			handlers.put(McpJsonRPC.METHOD_RESOURCES_SUBSCRIBE, resourceSubscribe());
			handlers.put(McpJsonRPC.METHOD_RESOURCES_UNSUBSCRIBE, resourceUnsubscribe());
		}

		if (capabilities.contains(Capabilities.PROMPT_LIST_CHANGED)) {
			handlers.put(McpJsonRPC.METHOD_PROMPT_LIST, promptsList());
			handlers.put(McpJsonRPC.METHOD_PROMPT_GET, promptsGet());
		}

		if (capabilities.contains(Capabilities.LOGGING)) {
			handlers.put(McpJsonRPC.METHOD_LOGGING_SET_LEVEL, logging());
		}

		server.setup(routing);
		this.routing = routing.build();
	}

	static McpServer create(McpServerConfig... server) {
		return new McpServer(server[0]);
	}

	public Map<String, JsonRPCHandler<?>> handlers() {
		return this.handlers;
	}

	public void addTool(Tool tool) {
		this.routing.tools().add(tool);
	}

	public void removeTool(Tool tool) {
		this.routing.tools().remove(tool);
	}

	public void addResource(Resource resource) {
		this.routing.resources().add(resource);
	}

	public void addPrompt(Prompt prompt) {
		this.routing.prompts().add(prompt);
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

	JsonRPCHandler<McpJsonRPC.ListToolsResult> toolsList() {
		return (object) -> {
			List<McpJsonRPC.Tool> toolz = this.routing.tools().stream()
					.filter(Objects::nonNull)
					.map(tool -> tool.info().name())
					.toList();
			return new McpJsonRPC.ListToolsResult(toolz, null);
		};
	}

	/**
	 * Params is the arguments provided by the client to the tool (method signature).
	 * McpJsonRPC.Tool represent the tool definition and is selected by name.
	 * Create an object that run the method with the params and return the result of it.
	 * It is defined in McpJsonRPC by Features with specification.
	 *
	 * @return Call tool result
	 */
	JsonRPCHandler<McpJsonRPC.CallToolResult> toolsCall() {
		return (params) -> {
			McpJsonRPC.CallToolRequest callToolRequest = mapper.convertValue(params, new TypeReference<>() {});

			Optional<Tool> tool = this.routing.tools().stream()
					.filter(tr -> callToolRequest.name().equals(tr.info().name()))
					.findAny();

			if (tool.isEmpty()) {
				return new McpJsonRPC.CallToolResult(List.of(), true);
			}
			String result = tool.get().process(callToolRequest.arguments());
			List<McpJsonRPC.Content> content = List.of(
					new McpJsonRPC.TextContent(List.of(McpJsonRPC.Role.USER), 2.0, result));
			return new McpJsonRPC.CallToolResult(content, false);
		};
	}

	JsonRPCHandler<McpJsonRPC.ListResourcesResult> resourcesList() {
		return (params) -> {
			var resources = this.routing.resources().stream()
					.map(Resource::info)
					.toList();
			return new McpJsonRPC.ListResourcesResult(resources, null);
		};
	}

	JsonRPCHandler<McpJsonRPC.ReadResourceResult> resourcesRead() {
		return (params) -> {
			McpJsonRPC.ReadResourceRequest resourceRequest = mapper.convertValue(params, new TypeReference<>() {});
			String resourceUri = resourceRequest.uri();
			Optional<Resource> resource = this.routing.resources().stream()
					.filter(it -> Objects.equals(it.info().uri(), resourceUri))
					.findFirst();
			if (resource.isEmpty()) {
				return new McpJsonRPC.ReadResourceResult(List.of());
			}
			McpJsonRPC.ResourceContents content = ResourceReader.get(resourceUri).read();
			return new McpJsonRPC.ReadResourceResult(List.of(content));
		};
	}

	JsonRPCHandler<McpJsonRPC.ListResourceTemplatesResult> resourceTemplateList() {
		return (param) -> new McpJsonRPC.ListResourceTemplatesResult(this.routing.resources(), null);
	}

	private JsonRPCHandler<McpJsonRPC.ListPromptsResult> promptsList() {
		return (object) -> new McpJsonRPC.ListPromptsResult(this.routing.prompts(), null);
	}

	/**
	 * Same as tools, feature that process it and return prompt messages.
	 * @return prompt result
	 */
	JsonRPCHandler<McpJsonRPC.GetPromptResult> promptsGet() {
		return (params) -> {
			McpJsonRPC.GetPromptRequest promptRequest = mapper.convertValue(params, new TypeReference<>() {});
			var prompt = this.routing.prompts().stream()
					.filter(p -> Objects.equals(p.info().name(), promptRequest.name()))
					.findFirst();
			if (prompt.isEmpty()) {
				//TODO - return an error
				return new McpJsonRPC.GetPromptResult("Error", List.of());
			}
			String content = prompt.get()
					.prompt(promptRequest.arguments());
			return new McpJsonRPC.GetPromptResult(prompt.get().info().description(), List.of(
					new McpJsonRPC.PromptMessage(McpJsonRPC.Role.USER, new McpJsonRPC.TextContent(content))));
		};
	}

	//Todo - Change the logging level in the sessions
	JsonRPCHandler<McpJsonRPC.LoggingMessageNotification> logging() {
		return (param) -> new McpJsonRPC.LoggingMessageNotification(McpJsonRPC.LoggingLevel.INFO, "", "");
	}

	private JsonRPCHandler<McpJsonRPC.InitializeResult> initialize() {
		return (param) -> {
			McpJsonRPC.InitializeRequest request = mapper.convertValue(param, new TypeReference<>() {});
			String protocoleVersion = this.protocolVersions.getLast();

			if (this.protocolVersions.contains(request.protocolVersion())) {
				protocoleVersion = request.protocolVersion();
			}

			return McpJsonRPCMapper.initializeResult(
					protocoleVersion,
					this.info);
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
