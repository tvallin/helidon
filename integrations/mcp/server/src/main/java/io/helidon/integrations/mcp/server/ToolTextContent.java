package io.helidon.integrations.mcp.server;

class ToolTextContent implements TextContent {
    private final String text;

    public ToolTextContent(String text) {
        this.text = text;
    }

    @Override
    public String text() {
        return text;
    }
}
