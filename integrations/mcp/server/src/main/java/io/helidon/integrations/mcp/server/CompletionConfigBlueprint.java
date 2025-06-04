package io.helidon.integrations.mcp.server;

import java.util.function.Function;

import io.helidon.builder.api.Prototype;

@Prototype.Blueprint
interface CompletionConfigBlueprint extends Prototype.Factory<Completion>{
    /**
     * Resource completion uri
     *
     * @return uri
     */
    String uri();

    /**
     * Prompt completion name.
     *
     * @return name
     */
    String name();

    /**
     * Completion content.
     *
     * @return content
     */
    Function<McpParameters, CompletionContent> completion();
}
