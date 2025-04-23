package io.helidon.integrations.mcp.server;

import java.io.PrintWriter;

import io.helidon.integrations.mcp.server.spi.McpTransport;
import io.helidon.integrations.mcp.server.spi.McpTransportProvider;

public class HelidonTransportProvider implements McpTransportProvider {

	private McpSession.Factory factory;

	@Override
	public void setSessionFactory(McpSession.Factory factory) {
		this.factory = factory;
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

	private static class HelidonTransport implements McpTransport {

		private final String sessionId;
		private final PrintWriter writer;

		HelidonTransport(String sessionId, PrintWriter writer) {
			this.sessionId = sessionId;
			this.writer = writer;
		}

		@Override
		public <T> T unmarshall(Object params, Class<T> initializeRequestClass) {
			return null;
		}

		@Override
		public void sendMessage(Object message) {

		}

		@Override
		public void close() {

		}

		@Override
		public void closeGracefully() {

		}
	}
}
