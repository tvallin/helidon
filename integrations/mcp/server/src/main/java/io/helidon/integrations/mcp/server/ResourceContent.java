package io.helidon.integrations.mcp.server;

public interface ResourceContent {
    String data();
    String mimeType();

    static ResourceContent textContent(String text) {
        return new ResourceTextContent(text);
    }

    static ResourceContent binaryContent(String data, String mimeType) {
        return new ResourceBinaryContent(data, mimeType);
    }

}
