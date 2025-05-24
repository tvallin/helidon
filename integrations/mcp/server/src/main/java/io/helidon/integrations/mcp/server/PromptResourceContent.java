package io.helidon.integrations.mcp.server;

class PromptResourceContent implements PromptContent {
    private final Role role;
    private final String uri;

    PromptResourceContent(String uri, Role role) {
        this.uri = uri;
        this.role = role;
    }

    String uri() {
        return uri;
    }

    @Override
    public Role role() {
        return role;
    }

    @Override
    public Content content() {
        throw new UnsupportedOperationException("This is only a resource reference");
    }
}
