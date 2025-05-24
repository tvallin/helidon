package io.helidon.integrations.mcp.server;

class PromptImageContent implements PromptContent {

    private final Role role;
    private final Content content;

    PromptImageContent(String data, String mimeType, Role role) {
        this.role = role;
        this.content = ImageContent.create(data, mimeType);
    }

    @Override
    public Role role() {
        return role;
    }

    @Override
    public Content content() {
        return content;
    }
}
