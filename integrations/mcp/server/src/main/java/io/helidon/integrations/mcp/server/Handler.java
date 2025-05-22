package io.helidon.integrations.mcp.server;

import io.helidon.common.parameters.Parameters;

@FunctionalInterface
public interface Handler {
    String handle(Parameters arguments);
}
