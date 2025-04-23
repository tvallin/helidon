package io.helidon.integrations.mcp.server;

import java.util.Map;
import java.util.function.Function;

import io.modelcontextprotocol.spec.McpSchema;

public record ToolComponent(
		McpSchema.Tool tool,
		Function<Map<String, Object>, McpSchema.CallToolResult> fx) {
}
