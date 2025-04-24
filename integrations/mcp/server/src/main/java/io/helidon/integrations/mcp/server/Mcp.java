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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.helidon.integrations.mcp.server.spi.McpTransportProvider;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This interface contains a set of annotations for defining MCP declarative server.
 */
public final class Mcp {

	@Target(METHOD)
	@Retention(RUNTIME)
	public @interface Resource {
		String uri();
		String name();
		String description() default "none"; 	//optional
	}

	//TODO - Remove this
	@Target(METHOD)
	@Retention(RUNTIME)
	public @interface ResourceTemplate {
		String uriTemplate(); // "resource://{param}/test"
		String name();
		String description() default "none"; 	//optional
		String mimeType() default "unknown";	//optional
	}

	@Target(METHOD)
	@Retention(RUNTIME)
	public @interface Prompt {
		String name();
		String description() default "none"; //optional
	}

	@Target(PARAMETER)
	@Retention(RUNTIME)
	public @interface PromptParam {
		String value();
	}

	@Target(METHOD)
	@Retention(RUNTIME)
	public @interface Tool {
		String name();
		String description();
		String annotations() default "";  //optional
	}

	@Target(PARAMETER)
	@Retention(RUNTIME)
	public @interface ToolTParam {
		String value();
	}

	@Target(ElementType.TYPE)
	@Retention(RUNTIME)
	public @interface Server {
		Class<? extends McpTransportProvider> value();
	}

}
