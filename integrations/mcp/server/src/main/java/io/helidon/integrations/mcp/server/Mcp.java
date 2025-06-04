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

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This interface contains a set of annotations to define an MCP declarative server.
 */
public final class Mcp {

	/**
	 * Annotation to define a MCP server.
	 */
	@Target(TYPE)
	@Retention(RUNTIME)
	public @interface Server {
		/**
		 * Name of the server.
		 *
		 * @return server name
		 */
		String value() default "helidon-mcp-server";
	}

	/**
	 * Annotation to define the {@link Server} capabilities.
	 */
	@Target(TYPE)
	@Retention(RUNTIME)
	@Repeatable(Capabilities.class)
	public @interface Capability {
		/**
		 * Capability of the server.
		 *
		 * @return server capability
		 */
		io.helidon.integrations.mcp.server.Capability value();
	}

	/**
	 * Set of {@link Capability}.
	 */
	@Target(TYPE)
	@Retention(RUNTIME)
	public @interface Capabilities {
		/**
		 * Capabilities of the server.
		 *
		 * @return server capabilities
		 */
		Capability[] value();
	}

	/**
	 * Annotation to describe an MCP component such as {@link Tool}, {@link Prompt} and {@link Resource}.
	 */
	@Target({TYPE, METHOD})
	@Retention(RUNTIME)
	public @interface Description {
		/**
		 * Component description.
		 *
		 * @return description
		 */
		String value();
	}

	/**
	 * Annotation to define the {@link Server} version.
	 */
	@Target(TYPE)
	@Retention(RUNTIME)
	public @interface Version {
		/**
		 * Version of the server.
		 *
		 * @return server version
		 */
		String value();
	}

	/**
	 * Annotation to define an MCP Tool.
	 */
	@Target(METHOD)
	@Retention(RUNTIME)
	public @interface Tool {
		/**
		 * Name of the tool.
		 *
		 * @return tool name
		 */
		String value() default "mcp-tool";
	}

	/**
	 * Annotation to manually register classes containing {@link Tool}.
	 */
	@Target(TYPE)
	@Retention(RUNTIME)
	public @interface Tools {
		/**
		 * List of classes containing {@link Tool}.
		 *
		 * @return classes
		 */
		Class<?>[] value();
	}

	/**
	 * Annotation to define an MCP Prompt.
	 */
	@Target(METHOD)
	@Retention(RUNTIME)
	public @interface Prompt {
		/**
		 * Name of the Prompt
		 *
		 * @return name
		 */
		String value() default "mcp-prompt";
	}

	/**
	 * Annotation to manually register classes containing {@link Prompt}.
	 */
	@Target(TYPE)
	@Retention(RUNTIME)
	public @interface Prompts {
		/**
		 * List of classes containing {@link Prompt}.
		 *
		 * @return classes
		 */
		Class<?>[] value();
	}

	/**
	 * Annotation to define an MCP resource.
	 */
	@Target(METHOD)
	@Retention(RUNTIME)
	public @interface Resource {
		/**
		 * Name of the resource
		 *
		 * @return name
		 */
		String value() default "mcp-resource";
	}

	/**
	 * Annotation to manually register classes containing {@link Resource}.
	 */
	@Target(TYPE)
	@Retention(RUNTIME)
	public @interface Resources {
		/**
		 * List of classes containing {@link Resource}.
		 *
		 * @return classes
		 */
		Class<?>[] value();
	}

	/**
	 * Annotation to define a {@link Resource} URI
	 */
	@Target(METHOD)
	@Retention(RUNTIME)
	public @interface URI {
		/**
		 * Value of the URI.
		 *
		 * @return URI
		 */
		String value();
	}

	/**
	 * Annotation to define a {@link Tool} and {@link Prompt} argument.
	 */
	@Target(PARAMETER)
	@Retention(RUNTIME)
	public @interface Param {
		/**
		 * Description of the argument.
		 *
		 * @return description
		 */
		String value();
	}

	/**
	 * Annotation to define a completion for {@link Prompt} name and {@link Resource} uri.
	 */
	@Target(METHOD)
	@Retention(RUNTIME)
	public @interface Completion {
		/**
		 * Resource URI or Prompt name.
		 *
		 * @return uri or prompt name
		 */
		String value() default "";
	}
}
