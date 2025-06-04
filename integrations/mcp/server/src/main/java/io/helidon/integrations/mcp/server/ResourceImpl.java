package io.helidon.integrations.mcp.server;

import java.util.function.Supplier;

import io.helidon.common.media.type.MediaType;

public class ResourceImpl implements Resource {
    private final String uri;
    private final String name;
    private final String description;
    private final MediaType type;
    private final Supplier<ResourceContent> content;


    ResourceImpl(String uri, String name, String description, MediaType type, Supplier<ResourceContent> content) {
        this.uri = uri;
        this.name = name;
        this.description = description;
        this.type = type;
        this.content = content;
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
    public String description() {
        return description;
    }

    @Override
    public MediaType mediaType() {
        return type;
    }

    @Override
    public ResourceContent read() {
        return content.get();
    }
}
