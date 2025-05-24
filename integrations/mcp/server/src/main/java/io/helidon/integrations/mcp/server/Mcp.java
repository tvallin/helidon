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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This interface contains a set of annotations for defining MCP declarative server.
 */
public final class Mcp {

	@Target(TYPE)
	@Retention(RUNTIME)
	public @interface Server {
		String value() default "Helidon MCP Server";
	}

	@Target(TYPE)
	@Retention(RUNTIME)
	public @interface Capability {
		io.helidon.integrations.mcp.server.Capability[] value();
	}

	@Target({TYPE, METHOD})
	@Retention(RUNTIME)
	public @interface Description {
		String value();
	}

	@Target(TYPE)
	@Retention(RUNTIME)
	public @interface Version {
		String value();
	}

	@Target(METHOD)
	@Retention(RUNTIME)
	public @interface Prompt {
		String value() default "";
	}

	@Target(METHOD)
	@Retention(RUNTIME)
	public @interface Tool {
		String value() default "";
	}

	@Target(PARAMETER)
	@Retention(RUNTIME)
	public @interface Param {
		String value();
	}

	@Target(METHOD)
	@Retention(RUNTIME)
	public @interface Resource {
		String value() default "";
	}

	@Target(METHOD)
	@Retention(RUNTIME)
	public @interface URI {
		String value();
	}
}
