package io.helidon.integrations.mcp.server;

class ResourceTextContent extends ResourceBinaryContent {

    ResourceTextContent(String data) {
        super(data, "text/plain");
    }
}
