package io.helidon.integrations.mcp.server;

class ResourceBinaryContent implements ResourceContent {
    String mimeType;
    String data;

    ResourceBinaryContent(String mimeType, String data) {
        this.mimeType = mimeType;
        this.data = data;
    }

    @Override
    public String data() {
        return data;
    }

    @Override
    public String mimeType() {
        return mimeType;
    }
}
