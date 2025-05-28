package io.helidon.integrations.mcp.server;

@FunctionalInterface
interface TextContent extends Content {

    default String type() {
        return "text";
    }

    String text();

    static TextContent create(String text) {
        return () -> text;
    }
}
