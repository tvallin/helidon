package io.helidon.integrations.mcp.server;

/**
 * Prompt Role
 */
public enum Role {
    /**
     * User role.
     */
    USER,
    /**
     * Assistant role.
     */
    ASSISTANT;

    String getName() {
        return toString().toLowerCase();
    }
}
