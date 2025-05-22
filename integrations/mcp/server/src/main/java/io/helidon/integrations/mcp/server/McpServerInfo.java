package io.helidon.integrations.mcp.server;

import java.util.Set;

public interface McpServerInfo {
    String name();
    String version();
    Set<CapabilitiesTemp> capabilities();

    static McpServerInfo create(String name, String version, CapabilitiesTemp... capabilities) {
        return new McpServerInfo() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public String version() {
                return version;
            }

            @Override
            public Set<CapabilitiesTemp> capabilities() {
                return Set.of(capabilities);
            }
        };
    }
}
