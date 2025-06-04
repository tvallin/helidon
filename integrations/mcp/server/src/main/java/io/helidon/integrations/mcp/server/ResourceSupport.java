package io.helidon.integrations.mcp.server;

import java.util.function.Supplier;

import io.helidon.builder.api.Prototype;

class ResourceSupport {

    @Prototype.BuilderMethod
    static void read(ResourceConfig.BuilderBase<?, ?> builder, Supplier<ResourceContent> supplier) {
        ResourceContent content = supplier.get();
        builder.read(content);
    }
}
