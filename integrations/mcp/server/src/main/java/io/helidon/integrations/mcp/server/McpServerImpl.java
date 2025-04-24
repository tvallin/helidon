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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import io.helidon.integrations.mcp.server.spi.McpTransportProvider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;

public class McpServerImpl implements McpServer {

	private static final System.Logger LOGGER = System.getLogger(McpServerImpl.class.getName());

	private final McpServerConfig config;
	private final McpTransportProvider transportProvider;
	private final ObjectMapper mapper = new ObjectMapper();
	private final Map<String, RequestHandler<?>> handlers = new HashMap<>();
	private final ConcurrentHashMap<String, PromptComponent> prompts = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, ResourceComponent> resources = new ConcurrentHashMap<>();
	private final CopyOnWriteArrayList<McpSchema.ResourceTemplate> resourceTemplates = new CopyOnWriteArrayList<>();
	private final CopyOnWriteArrayList<ToolComponent> tools = new CopyOnWriteArrayList<>();
	private List<String> protocoleVersions = new ArrayList<>();

	public McpServerImpl(McpServerConfig config, McpTransportProvider provider) {
		this.config = config;
		this.transportProvider = provider;

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

		if (config.capabilities().promts().listChanged()) {
			handlers.put(McpSchema.METHOD_PROMPT_LIST, promptsList());
			handlers.put(McpSchema.METHOD_PROMPT_GET, promptsGet());
		}

		if (config.capabilities().logging()) {
			handlers.put(McpSchema.METHOD_LOGGING_SET_LEVEL, logging());
		}
	}

	@Override
	public ServerCapabilities getServerCapabilities() {
		return this.config.capabilities();
	}

	@Override
	public Implementation getServerInfo() {
		return this.config.implementation();
	}

	@Override
	public void start() {
		this.transportProvider.setSessionFactory(
				transport -> new McpSessionImpl(transport, this.handlers));
	}

	@Override
	public void closeGracefully() {
		this.transportProvider.closeGracefully();
	}

	@Override
	public void close() {
		this.transportProvider.close();
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

	public void addResource(String resourceName, ResourceComponent resource) {
		this.resources.put(resourceName, resource);
	}

	public void removeResource(String resourceName) {
		this.resources.remove(resourceName);
	}

	public void addPrompt(PromptComponent prompt) {
		this.prompts.put(prompt.prompt().name(), prompt);
	}

	public void removePrompt(String name) {
		this.prompts.remove(name);
	}

	@Override
	public McpServerConfig prototype() {
		return this.config;
	}

	RequestHandler<Object> ping() {
		return null;
	}

	RequestHandler<McpSchema.ListToolsResult> toolsList() {
		return (object) -> {
			LOGGER.log(System.Logger.Level.INFO, "Tools list size: " + this.tools.size());
			List<McpSchema.Tool> toolz = this.tools.stream().map(ToolComponent::tool).toList();
			return new McpSchema.ListToolsResult(toolz, null);
		};
	}

	//TODO
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
			McpSchema.CallToolRequest callToolRequest = mapper.convertValue(params,
					new TypeReference<McpSchema.CallToolRequest>() {
					});

			Optional<ToolComponent> tool = this.tools.stream()
					.filter(tr -> callToolRequest.name().equals(tr.tool().name()))
					.findAny();

			if (tool.isEmpty()) {
				return new McpSchema.CallToolResult(List.of(), true);
			}
			return tool.orElseThrow().fx().apply(callToolRequest.arguments());
		};
	}

	RequestHandler<McpSchema.ListResourcesResult> resourcesList() {
		return (params) -> {
			var resources = this.resources.values().stream().map(ResourceComponent::resource).toList();
			return new McpSchema.ListResourcesResult(resources, null);
		};
	}

	RequestHandler<McpSchema.ReadResourceResult> resourcesRead() {
		return (params) -> {
			McpSchema.ReadResourceRequest resourceRequest = mapper.convertValue(params,
					new TypeReference<McpSchema.ReadResourceRequest>() {
					});
			String resourceUri = resourceRequest.uri();
			var resource = this.resources.get(resourceUri);
			if (resource == null) {
				//TODO - return an error
				return new McpSchema.ReadResourceResult(List.of());
			}
			return resource.reader().apply(resourceRequest);
		};
	}

	RequestHandler<McpSchema.ListResourceTemplatesResult> resourceTemplateList() {
		return (param) -> new McpSchema.ListResourceTemplatesResult(this.resourceTemplates, null);
	}

	private RequestHandler<McpSchema.ListPromptsResult> promptsList() {
		return (object) -> {
			var promptsList = this.prompts.values().stream().map(PromptComponent::prompt).toList();
			return new McpSchema.ListPromptsResult(promptsList, null);
		};
	}

	/**
	 * Same as tools, feature that process it and return prompt messages.
	 * @return prompt result
	 */
	RequestHandler<McpSchema.GetPromptResult> promptsGet() {
		return (params) -> {
			McpSchema.GetPromptRequest promptRequest = mapper.convertValue(params,
					new TypeReference<McpSchema.GetPromptRequest>() {
					});
			var prompt = prompts.get(promptRequest.name());
			if (prompt == null) {
				//TODO - return an error
				return new McpSchema.GetPromptResult("Error", List.of());
			}
			return prompt.handler().apply(promptRequest);
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
