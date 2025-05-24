package io.helidon.integrations.mcp.server;

interface ImageContent extends ToolContent, Content {

    default String type() {
        return "image";
    }

    String data();

    String mimeType();

    static ImageContent create(String data, String mimeType) {
        return new ImageContent() {

            @Override
            public String data() {
                return data;
            }

            @Override
            public String mimeType() {
                return mimeType;
            }
        };
    }
}
