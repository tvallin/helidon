package io.helidon.integrations.mcp.server;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * McpSchemaMapper convert Helidon MCP configuration into McpSchema.
 */
public class McpSchemaMapper {

	static McpSchema.InitializeResult initializeResult(String latestProtocolVersion,
													   Capabilities capabilities,
													   Implementation implementation,
													   String instructions) {
		return new McpSchema.InitializeResult(
				latestProtocolVersion,
				serverCapabilities(capabilities),
				implementation(implementation),
				instructions);
	}

	private static McpSchema.Implementation implementation(Implementation implementation) {
		return new McpSchema.Implementation(implementation.name(), implementation.version());
	}

	private static McpSchema.ServerCapabilities serverCapabilities(Capabilities capabilities) {
		var loggingCapabilities = capabilities.logging()
				? new McpSchema.ServerCapabilities.LoggingCapabilities()
				: null;
		return new McpSchema.ServerCapabilities(
				capabilities.experimentation(),
				loggingCapabilities,
				prompts(capabilities.prompts()),
				resource(capabilities.resources()),
				tools(capabilities.tools()));
	}

	private static McpSchema.ServerCapabilities.ToolCapabilities tools(Tool tools) {
		return new McpSchema.ServerCapabilities.ToolCapabilities(tools.listChanged());
	}

	private static McpSchema.ServerCapabilities.ResourceCapabilities resource(Resource resource) {
		return new McpSchema.ServerCapabilities.ResourceCapabilities(resource.subscribe(), resource.listChanged());
	}

	static McpSchema.ServerCapabilities.PromptCapabilities prompts(Prompt prompts) {
		return new McpSchema.ServerCapabilities.PromptCapabilities(prompts.listChanged());
	}
}
