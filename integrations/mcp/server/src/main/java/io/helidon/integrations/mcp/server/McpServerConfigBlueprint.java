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

import io.helidon.builder.api.Option;
import io.helidon.builder.api.Prototype;

import static io.helidon.integrations.mcp.server.McpServerConfigBlueprint.CONFIG_ROOT;

@Prototype.Configured(CONFIG_ROOT)
@Prototype.Blueprint
interface McpServerConfigBlueprint extends Prototype.Factory<McpServer>{

	/**
	 * The default configuration prefix.
	 */
	String CONFIG_ROOT = "mcp.server";

	@Option.Configured
	@Option.Default("http")
	String transport();

	@Option.Configured
	ServerCapabilities capabilities();

	@Option.Configured
	Implementation implementation();

	@Option.Configured
	@Option.Default("")
	String instructions();

}
