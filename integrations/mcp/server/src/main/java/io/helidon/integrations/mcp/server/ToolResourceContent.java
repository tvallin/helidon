package io.helidon.integrations.mcp.server;

class ToolResourceContent implements ToolContent {
    private final String uri;

    public ToolResourceContent(String uri) {
        this.uri = uri;
    }

    String uri() {
        return uri;
    }
}
