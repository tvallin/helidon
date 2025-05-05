package io.helidon.integrations.mcp.server;

import java.util.Map;
import java.util.function.Function;

import io.modelcontextprotocol.spec.McpSchema;

public record ToolComponent(
		McpSchema.Tool tool,
		Function<Map<String, Object>, String> handler) {

	public ToolComponent(Builder builder) {
		this(new McpSchema.Tool(builder.name, builder.description, builder.schema), builder.handler);
	}

	public static ToolComponent.Builder builder() {
		return new ToolComponent.Builder();
	}

	public static class Builder {
		private String name;
		private String description;
		private String schema;
		private Function<Map<String, Object>, String> handler;

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder schema(String schema) {
			this.schema = schema;
			return this;
		}

		public Builder handler(Function<Map<String, Object>, String> handler) {
			this.handler = handler;
			return this;
		}

		public ToolComponent build() {
			return new ToolComponent(this);
		}
	}
}
