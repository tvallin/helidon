package io.helidon.integrations.mcp.server;

public interface PromptContent {

    Role role();

    Content content();

    static PromptContent textContent(String prompt, Role role) {
        return new PromptTextContent(prompt, role);
    }

    static PromptContent imageContent(String data, String mimeType, Role role) {
        return new PromptImageContent(data, mimeType, role);
    }

    static PromptContent resourceContent(String uri, Role role) {
        return new PromptResourceContent(uri, role);
    }
}
