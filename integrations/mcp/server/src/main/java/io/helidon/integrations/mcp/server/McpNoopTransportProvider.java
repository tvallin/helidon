package io.helidon.integrations.mcp.server;

import io.helidon.integrations.mcp.server.spi.McpTransportProvider;

public class McpNoopTransportProvider implements McpTransportProvider {
	@Override
	public void setSessionFactory(McpSession.Factory factory) {
	}

	@Override
	public void notifyClients(String method, Object params) {
	}

	@Override
	public void closeGracefully() {
	}

	@Override
	public void close() {
	}
}
