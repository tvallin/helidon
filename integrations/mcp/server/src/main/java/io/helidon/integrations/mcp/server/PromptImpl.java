package io.helidon.integrations.mcp.server;

import java.util.Set;
import java.util.function.Function;

import jakarta.json.JsonObjectBuilder;

public class PromptImpl implements Prompt {
    private final String name;
    private final String description;
    private final Set<PromptArgument> arguments;
    private final Function<McpParameters, PromptContent> prompt;

    public PromptImpl(String name, String description, Set<PromptArgument> arguments, Function<McpParameters, PromptContent> prompt) {
        this.name = name;
        this.description = description;
        this.arguments = arguments;
        this.prompt = prompt;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public Set<PromptArgument> arguments() {
        return arguments;
    }

    @Override
    public PromptContent prompt(McpParameters parameters) {
        return prompt.apply(parameters);
    }

    @Override
    public JsonObjectBuilder json() {
        return null;
    }
}
