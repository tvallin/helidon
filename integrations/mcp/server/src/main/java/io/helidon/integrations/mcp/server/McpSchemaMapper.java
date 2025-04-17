package io.helidon.integrations.mcp.server;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * McpSchemaMapper convert Helidon MCP configuration into McpSchema.
 */
class McpSchemaMapper {

	private McpSchemaMapper() {
	}

//	static McpSchema.JSONRPCResponse jsonrpcResponse(String jsonRpc, Object result) {
//		return new McpSchema.JSONRPCResponse(
//
//		)
//	}

	static McpSchema.InitializeResult initializeResult(String latestProtocolVersion,
													   ServerCapabilities capabilities,
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

	static McpSchema.ServerCapabilities serverCapabilities(ServerCapabilities capabilities) {
		return new McpSchema.ServerCapabilities(
				capabilities.experimentation(),
				new McpSchema.ServerCapabilities.LoggingCapabilities(),
				prompts(capabilities.promts()),
				resource(capabilities.resource()),
				tools(capabilities.tools()));
	}

	private static McpSchema.ServerCapabilities.ToolCapabilities tools(ListChanged tools) {
		return new McpSchema.ServerCapabilities.ToolCapabilities(tools.listChanged());
	}

	private static McpSchema.ServerCapabilities.ResourceCapabilities resource(Resource resource) {
		return new McpSchema.ServerCapabilities.ResourceCapabilities(resource.subscribe(), resource.listChanged());
	}

	static McpSchema.ServerCapabilities.PromptCapabilities prompts(ListChanged promts) {
		return new McpSchema.ServerCapabilities.PromptCapabilities(promts.listChanged());
	}


}
