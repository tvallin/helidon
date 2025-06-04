package io.helidon.integrations.mcp.server;

import java.util.function.Function;

public class CompletionImpl implements Completion {
    private final CompletionInfo info;
    private final Function<McpParameters, CompletionContent> complete;

    public CompletionImpl(CompletionInfo build, Function<McpParameters, CompletionContent> complete) {
        this.info = build;
        this.complete = complete;
    }

    @Override
    public CompletionInfo info() {
        return info;
    }

    @Override
    public CompletionContent complete(McpParameters parameters) {
        return complete.apply(parameters);
    }
}
