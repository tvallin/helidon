package io.helidon.integrations.mcp.server;

import java.util.function.Consumer;

import io.helidon.builder.api.Prototype;

class McpHttpFeatureConfigSupport {

    static class McpCustomMethods {
        @Prototype.BuilderMethod
        static void tool(McpHttpFeatureConfig.BuilderBase<?, ?> builder, Consumer<Tool.Builder> tool) {
            Tool.Builder toolBuilder = Tool.builder();
            tool.accept(toolBuilder);
            builder.addTool(toolBuilder.build());
        }

        @Prototype.BuilderMethod
        static void prompt(McpHttpFeatureConfig.BuilderBase<?, ?> builder, Consumer<Prompt.Builder> prompt) {
            Prompt.Builder promptBuilder = Prompt.builder();
            prompt.accept(promptBuilder);
            builder.addPrompt(promptBuilder.build());
        }

        @Prototype.BuilderMethod
        static void resource(McpHttpFeatureConfig.BuilderBase<?, ?> builder, Consumer<Resource.Builder> resource) {
            Resource.Builder resourceBuilder = Resource.builder();
            resource.accept(resourceBuilder);
            builder.addResource(resourceBuilder.build());
        }

        @Prototype.BuilderMethod
        static void completion(McpHttpFeatureConfig.BuilderBase<?, ?> builder, Consumer<Completion.Builder> completion) {
            Completion.Builder completionBuilder = Completion.builder();
            completion.accept(completionBuilder);
            builder.addCompletion(completionBuilder.build());
        }
    }
}
