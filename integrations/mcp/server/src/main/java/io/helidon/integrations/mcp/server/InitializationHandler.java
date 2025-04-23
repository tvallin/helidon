package io.helidon.integrations.mcp.server;

import io.modelcontextprotocol.spec.McpSchema;

@FunctionalInterface
public interface InitializationHandler {

	McpSchema.InitializeResult handle(McpSchema.InitializeRequest request);

	default McpSchema.InitializeResult empty(McpSchema.InitializeRequest request) {
		return null;
	}
}
