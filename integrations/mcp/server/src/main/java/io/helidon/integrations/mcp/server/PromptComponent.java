package io.helidon.integrations.mcp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.modelcontextprotocol.spec.McpSchema;

public record PromptComponent(McpSchema.Prompt prompt,
							  Function<Map<String, Object>, String>  handler) {

	public PromptComponent(Builder builder) {
		this(new McpSchema.Prompt(
				builder.name,
				builder.description,
				builder.promptArguments),
				builder.handler);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final List<McpSchema.PromptArgument> promptArguments = new ArrayList<>();

		private String name;
		private String description;
		private Function<Map<String, Object>, String>  handler;

		public Builder name(String name) {
			this.name = name;
			return this;
		}
		public Builder description(String description) {
			this.description = description;
			return this;
		}
		public Builder promptArgument(McpSchema.PromptArgument argument) {
			this.promptArguments.add(argument);
			return this;
		}
		public Builder handler(Function<Map<String, Object>, String> handler) {
			this.handler = handler;
			return this;
		}
		public PromptComponent build() {
			return new PromptComponent(this);
		}
	}
}
