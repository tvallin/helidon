package io.helidon.integrations.mcp.server;

import java.util.function.Function;

import io.modelcontextprotocol.spec.McpSchema;

public record PromptComponent(McpSchema.Prompt prompt,
							  Function<McpSchema.GetPromptRequest, McpSchema.GetPromptResult> handler) {
}
