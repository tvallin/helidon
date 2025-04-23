package io.helidon.integrations.mcp.codegen;

import java.util.Set;

import io.helidon.codegen.CodegenContext;
import io.helidon.codegen.spi.CodegenExtension;
import io.helidon.codegen.spi.CodegenExtensionProvider;
import io.helidon.common.types.TypeName;

import static io.helidon.integrations.mcp.codegen.McpTypes.MCP_SERVER;

public class McpCodegenProvider implements CodegenExtensionProvider {
	/**
	 * Public no-arg constructor required by {@link java.util.ServiceLoader}.
	 */
	public McpCodegenProvider() {
	}

	@Override
	public Set<TypeName> supportedAnnotations() {
		return Set.of(MCP_SERVER);
	}

	@Override
	public CodegenExtension create(CodegenContext ctx, TypeName generator) {
		return new McpCodegen(ctx);
	}
}

