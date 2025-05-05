package io.helidon.integrations.mcp.server;

import java.util.function.Function;

import io.modelcontextprotocol.spec.McpSchema;

public record ResourceComponent(McpSchema.Resource resource,
								Function<String, String> reader) {

	public ResourceComponent(Builder builder) {
		this(new McpSchema.Resource(builder.uri,
				builder.name,
				builder.description,
				builder.mimeType,
				null), builder.reader);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String name;
		private String description;
		private String uri;
		private String mimeType;
		private Function<String, String> reader;

		public Builder name(String name) {
			this.name = name;
			return this;
		}
		public Builder description(String description) {
			this.description = description;
			return this;
		}
		public Builder uri(String URI) {
			this.uri = URI;
			return this;
		}
		public Builder mimeType(String mimeType) {
			this.mimeType = mimeType;
			return this;
		}
		public Builder reader(Function<String, String> reader) {
			this.reader = reader;
			return this;
		}
		public ResourceComponent build() {
			return new ResourceComponent(this);
		}
	}
}
