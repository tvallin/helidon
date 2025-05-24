package io.helidon.integrations.mcp.server;

/**
 * Prompt Role
 */
public enum Role {
    USER,
    ASSISTANT;

    public String getName() {
        return toString().toLowerCase();
    }
}
