package io.helidon.integrations.mcp.server;

import java.util.function.Function;

class CompletionImpl implements Completion {
    private final Function<McpParameters, CompletionContent> complete;
    private final String uri;
    private final String name;

    public CompletionImpl(String uri, String name, Function<McpParameters, CompletionContent> complete) {
        this.uri = uri;
        this.name = name;
        this.complete = complete;
    }

    @Override
    public String uri() {
        return uri;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public CompletionContent complete(McpParameters parameters) {
        return complete.apply(parameters);
    }
}
