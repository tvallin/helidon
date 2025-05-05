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

import io.helidon.webserver.spi.ServerFeature;

import static io.helidon.webserver.WebServer.DEFAULT_SOCKET_NAME;

/**
 * MCP Server Feature.
 */
public class McpServerFeature implements ServerFeature {

	private static final String TYPE = "mcp-server";
	private final McpServerConfig configuration;

	public McpServerFeature(McpServerConfig configuration) {
		this.configuration = configuration;
	}

	public static McpServerFeature create(McpServerConfig configuration) {
		return new McpServerFeature(configuration);
	}

	@Override
	public void setup(ServerFeatureContext featureContext) {
//		featureContext.socket(DEFAULT_SOCKET_NAME)
//				.httpRouting()
//				.addFeature(new McpHttpFeature(configuration));
	}

	@Override
	public String name() {
		return configuration.implementation().name();
	}

	@Override
	public String type() {
		return TYPE;
	}
}
