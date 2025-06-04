package io.helidon.integrations.mcp.server;

@FunctionalInterface
interface TextContent extends ToolContent {

    default String type() {
        return "text";
    }

    String text();
}
