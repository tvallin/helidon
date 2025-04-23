package io.helidon.integrations.mcp.server;

import java.util.function.Function;

import io.modelcontextprotocol.spec.McpSchema;

public record ResourceComponent(McpSchema.Resource resource,
		Function<McpSchema.ReadResourceRequest, McpSchema.ReadResourceResult> reader) {
}
