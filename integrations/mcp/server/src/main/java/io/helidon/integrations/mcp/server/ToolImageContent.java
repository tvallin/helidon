package io.helidon.integrations.mcp.server;

class ToolImageContent implements ImageContent {
    private final String data;
    private final String mimeType;

    ToolImageContent(String data, String mimeType) {
        this.data = data;
        this.mimeType = mimeType;
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
