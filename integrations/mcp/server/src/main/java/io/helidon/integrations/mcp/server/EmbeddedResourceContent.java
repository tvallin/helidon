package io.helidon.integrations.mcp.server;

public interface EmbeddedResourceContent extends Content {

    String uri();

    ResourceContent content();

    default String type() {
        return "resource";
    }
}
