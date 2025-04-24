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

package io.helidon.integrations.mcp.tests;

import io.helidon.integrations.mcp.server.Mcp;
import io.helidon.integrations.mcp.server.transport.StdioTransportProvider;

@Mcp.Server(StdioTransportProvider.class)
class McpTest {

	@Mcp.Tool(
			name = "Weather Tool",
			description = "Get weather from somewhere")
	String getWeather() {
		return "Sunny";
	}

	@Mcp.Prompt(
			name = "Weather in town",
			description = "Get the weather in a specific town")
	String getWeatherInTown(@Mcp.PromptParam("town") String town) {
		return "Sunny in " + town;
	}

	@Mcp.Resource(
			uri = "https://helidon.io/stater",
			name = "weather-report",
			description = "Get a general weather report")
	String getWeatherReport() {
		return "Sunny everywhere";
	}

	@Mcp.ResourceTemplate(
			uriTemplate = "uri",
			name = "RT",
			description = "Resource Template")
	String resourceTemplate() {
		return "resourceTemplate";
	}

}
