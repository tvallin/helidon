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

@SuppressWarnings("unused")
@Mcp.Server
class McpWeatherServerDeclarative {

	@Mcp.Tool(
			name = "Weather Alert",
			description = "Get weather alert from state")
	String weatherAlert(@Mcp.ToolParam("state") String state) {
		//Fetch alert for mentioned state
		return "Hurricane in " + state;
	}

	@Mcp.Prompt(
			name = "Weather in town",
			description = "Get the weather in a specific town")
	String weatherInTown(@Mcp.PromptParam("town") String town) {
		return "What is the weather like in {{town}}";
	}

	@Mcp.Resource(
			uri = "https://api.weather.gov/alerts",
			name = "weather-report",
			description = "Get the list of all alerts")
	void weatherAlerts() {
	}

}
