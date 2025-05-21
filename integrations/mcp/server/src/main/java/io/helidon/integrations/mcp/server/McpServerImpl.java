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
import java.util.concurrent.CopyOnWriteArrayList;

import io.helidon.common.media.type.MediaTypes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;

public class McpServerImpl implements McpServer {

	private final McpServerConfig config;
	private final ObjectMapper mapper = new ObjectMapper();
	private final List<String> protocoleVersions = new ArrayList<>();
	private final Map<String, RequestHandler<?>> handlers = new HashMap<>();
	private final CopyOnWriteArrayList<PromptComponent> prompts = new CopyOnWriteArrayList<>();
	private final CopyOnWriteArrayList<ResourceComponent> resources = new CopyOnWriteArrayList<>();
	private final CopyOnWriteArrayList<McpSchema.ResourceTemplate> resourceTemplates = new CopyOnWriteArrayList<>();
	private final CopyOnWriteArrayList<ToolComponent> tools = new CopyOnWriteArrayList<>();

	public McpServerImpl(McpServerConfig config) {
		this.config = config;
		this.protocoleVersions.add(PROTOCOLE_VERSION);

		handlers.put(McpSchema.METHOD_PING, ping());
		handlers.put(McpSchema.METHOD_INITIALIZE, initialize());

		if (config.capabilities().tools().listChanged()) {
			handlers.put(McpSchema.METHOD_TOOLS_LIST, toolsList());
			handlers.put(McpSchema.METHOD_TOOLS_CALL, toolsCall());
		}

		if (config.capabilities().resources().listChanged()) {
			handlers.put(McpSchema.METHOD_RESOURCES_LIST, resourcesList());
			handlers.put(McpSchema.METHOD_RESOURCES_READ, resourcesRead());
			handlers.put(McpSchema.METHOD_RESOURCES_TEMPLATES_LIST, resourceTemplateList());
		}

		if (config.capabilities().resources().subscribe()) {
			handlers.put(McpSchema.METHOD_RESOURCES_SUBSCRIBE, resourceSubscribe());
			handlers.put(McpSchema.METHOD_RESOURCES_UNSUBSCRIBE, resourceUnsubscribe());
		}

		if (config.capabilities().prompts().listChanged()) {
			handlers.put(McpSchema.METHOD_PROMPT_LIST, promptsList());
			handlers.put(McpSchema.METHOD_PROMPT_GET, promptsGet());
		}

		if (config.capabilities().logging()) {
			handlers.put(McpSchema.METHOD_LOGGING_SET_LEVEL, logging());
		}

		this.tools.addAll(config.tools());
		this.resources.addAll(config.resources());
		this.prompts.addAll(config.prompts());
	}

	@Override
	public Map<String, RequestHandler<?>> handlers() {
		return this.handlers;
	}

	public void addTool(ToolComponent tool) {
		this.tools.add(tool);
	}

	public void removeTool(ToolComponent tool) {
		this.tools.remove(tool);
	}

	public void addResourceTemplate(McpSchema.ResourceTemplate resourceTemplate) {
		this.resourceTemplates.add(resourceTemplate);
	}

	public void removeResourceTemplate(McpSchema.ResourceTemplate resourceTemplate) {
		this.resourceTemplates.remove(resourceTemplate);
	}

	public void addResource(ResourceComponent resource) {
		this.resources.add(resource);
	}

	public void removeResource(String resourceName) {
		Optional<ResourceComponent> resource = this.resources.stream()
				.filter(component -> component.resource().name().equals(resourceName))
				.findFirst();
		resource.ifPresent(this.resources::remove);
	}

	public void addPrompt(PromptComponent prompt) {
		this.prompts.add(prompt);
	}

	public void removePrompt(String name) {
		Optional<PromptComponent> prompt = this.prompts.stream()
				.filter(component -> component.prompt().name().equals(name))
				.findFirst();
		prompt.ifPresent(this.prompts::remove);
	}

	@Override
	public McpServerConfig prototype() {
		return this.config;
	}

	//TODO - How to maintain list of client subscription ?
	private RequestHandler<?> resourceUnsubscribe() {
		return null;
	}

	private RequestHandler<?> resourceSubscribe() {
		return null;
	}

	RequestHandler<Object> ping() {
		return object -> "pong";
	}

	RequestHandler<McpSchema.ListToolsResult> toolsList() {
		return (object) -> {
			List<McpSchema.Tool> toolz = this.tools.stream().filter(Objects::nonNull).map(ToolComponent::tool).toList();
			return new McpSchema.ListToolsResult(toolz, null);
		};
	}

	/**
	 * Params is the arguments provided by the client to the tool (method signature).
	 * McpSchema.Tool represent the tool definition and is selected by name.
	 * Create an object that run the method with the params and return the result of it.
	 * It is defined in McpSchema by Features with specification.
	 *
	 * @return Call tool result
	 */
	RequestHandler<McpSchema.CallToolResult> toolsCall() {
		return (params) -> {
			McpSchema.CallToolRequest callToolRequest = mapper.convertValue(params, new TypeReference<>() {});

			Optional<ToolComponent> tool = this.tools.stream()
					.filter(tr -> callToolRequest.name().equals(tr.tool().name()))
					.findAny();

			if (tool.isEmpty()) {
				return new McpSchema.CallToolResult(List.of(), true);
			}
			String result = tool.orElseThrow().handler().apply(callToolRequest.arguments());
			List<McpSchema.Content> content = List.of(
					new McpSchema.TextContent(List.of(McpSchema.Role.USER), 2.0, result));
			return new McpSchema.CallToolResult(content, false);
		};
	}

	RequestHandler<McpSchema.ListResourcesResult> resourcesList() {
		return (params) -> {
			var resources = this.resources.stream().map(ResourceComponent::resource).toList();
			return new McpSchema.ListResourcesResult(resources, null);
		};
	}

	RequestHandler<McpSchema.ReadResourceResult> resourcesRead() {
		return (params) -> {
			McpSchema.ReadResourceRequest resourceRequest = mapper.convertValue(params, new TypeReference<>() {});
			String resourceUri = resourceRequest.uri();
			Optional<ResourceComponent> resource = this.resources.stream()
					.filter(it -> Objects.equals(it.resource().uri(), resourceUri))
					.findFirst();
			if (resource.isEmpty()) {
				return new McpSchema.ReadResourceResult(List.of());
			}
			McpSchema.ResourceContents content = ResourceReader.get(resourceUri).read();
			return new McpSchema.ReadResourceResult(List.of(content));
		};
	}

	RequestHandler<McpSchema.ListResourceTemplatesResult> resourceTemplateList() {
		return (param) -> new McpSchema.ListResourceTemplatesResult(this.resourceTemplates, null);
	}

	private RequestHandler<McpSchema.ListPromptsResult> promptsList() {
		return (object) -> {
			var promptsList = this.prompts.stream().map(PromptComponent::prompt).toList();
			return new McpSchema.ListPromptsResult(promptsList, null);
		};
	}

	/**
	 * Same as tools, feature that process it and return prompt messages.
	 * @return prompt result
	 */
	RequestHandler<McpSchema.GetPromptResult> promptsGet() {
		return (params) -> {
			McpSchema.GetPromptRequest promptRequest = mapper.convertValue(params, new TypeReference<>() {});
			var prompt = this.prompts.stream()
					.filter(component -> Objects.equals(component.prompt().name(), promptRequest.name()))
					.findFirst();
			if (prompt.isEmpty()) {
				//TODO - return an error
				return new McpSchema.GetPromptResult("Error", List.of());
			}
			String content = prompt.orElseThrow(() -> new McpException("Prompt not found"))
					.handler()
					.apply(promptRequest.arguments());
			return new McpSchema.GetPromptResult(prompt.orElseThrow().prompt().description(), List.of(
					new McpSchema.PromptMessage(McpSchema.Role.USER, new McpSchema.TextContent(content))));
		};
	}

	//Todo - Change the logging level in the sessions
	RequestHandler<McpSchema.LoggingMessageNotification> logging() {
		return (param) -> new McpSchema.LoggingMessageNotification(McpSchema.LoggingLevel.INFO, "", "");
	}

	private RequestHandler<McpSchema.InitializeResult> initialize() {
		return (param) -> {
			McpSchema.InitializeRequest request = mapper.convertValue(param, new TypeReference<>() {});
			String protocoleVersion = this.protocoleVersions.getLast();

			if (this.protocoleVersions.contains(request.protocolVersion())) {
				protocoleVersion = request.protocolVersion();
			}

			return McpSchemaMapper.initializeResult(
					protocoleVersion,
					config.capabilities(),
					config.implementation(),
					config.instructions());
		};
	}
}
