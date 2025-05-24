package io.helidon.integrations.mcp.server;

public interface ToolContent {

    static ToolContent textContent(String text) {
        return new ToolTextContent(text);
    }

    static ToolContent imageContent(String data, String mimeType) {
        return new ToolImageContent(data, mimeType);
    }

    static ToolContent resourceContent(String uri) {
        return new ToolResourceContent(uri);
    }
}
