package io.helidon.integrations.mcp.server;

import java.util.function.Function;

import io.helidon.builder.api.Prototype;

public class ToolSupport {

    @Prototype.BuilderMethod
    static void tool(ToolConfig.BuilderBase<?, ?> builder, Function<McpParameters, ToolContent> supplier) {

    }
}
