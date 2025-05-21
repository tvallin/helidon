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

import io.modelcontextprotocol.spec.McpSchema;

public record ResourceComponent(McpSchema.Resource resource) {

	public ResourceComponent(Builder builder) {
		this(new McpSchema.Resource(builder.uri,
				builder.name,
				builder.description,
				builder.mimeType,
				null));
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String name;
		private String description;
		private String uri;
		private String mimeType;

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

		public ResourceComponent build() {
			return new ResourceComponent(this);
		}
	}
}
