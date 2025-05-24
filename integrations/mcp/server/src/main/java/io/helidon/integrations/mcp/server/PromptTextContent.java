package io.helidon.integrations.mcp.server;

class PromptTextContent implements PromptContent {

    private final Role role;
    private final Content content;

    PromptTextContent(String text, Role role) {
        this.role = role;
        this.content = TextContent.create(text);
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
