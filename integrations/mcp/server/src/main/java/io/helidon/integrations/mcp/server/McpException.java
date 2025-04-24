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

import io.helidon.http.sse.SseEvent;

import io.modelcontextprotocol.spec.McpSchema;

public class McpException extends RuntimeException {

	public McpException(String message) {
		super(message);
	}

	static McpSchema.JSONRPCResponse.JSONRPCError toError(String message) {
		return new McpSchema.JSONRPCResponse.JSONRPCError(500, message, null);
	}

	SseEvent.Builder sseEventBuilder() {
		return SseEvent.builder()
				.name("Error");
	}
}
