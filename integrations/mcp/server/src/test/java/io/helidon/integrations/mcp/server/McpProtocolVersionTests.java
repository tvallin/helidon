/*
 * Copyright 2024-2024 the original author or authors.
 */

package io.helidon.integrations.mcp.server;

import java.util.UUID;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.instanceOf;

/**
 * Tests for MCP protocol version negotiation and compatibility.
 */
class McpProtocolVersionTests {

	private static final McpSchema.Implementation SERVER_INFO = new McpSchema.Implementation("test-server", "1.0.0");

	private static final McpSchema.Implementation CLIENT_INFO = new McpSchema.Implementation("test-client", "1.0.0");

	private McpSchema.JSONRPCRequest jsonRpcInitializeRequest(String requestId, String protocolVersion) {
		return new McpSchema.JSONRPCRequest(McpSchema.JSONRPC_VERSION, McpSchema.METHOD_INITIALIZE, requestId,
				new McpSchema.InitializeRequest(protocolVersion, null, CLIENT_INFO));
	}

	@Test
	void shouldUseLatestVersionByDefault() {
		MockMcpServerTransport serverTransport = new MockMcpServerTransport();
		var transportProvider = new MockMcpServerTransportProvider(serverTransport);
		McpServerImpl server = new McpServerImpl(McpServerConfig.builder()
				.transport("http")
				.capabilities(ServerCapabilities.builder()
						.promts(ListChanged.builder()
								.listChanged(true)
								.build())
						.resource(Resource.builder()
								.listChanged(true)
								.subscribe(true)
								.build())
						.tools(ListChanged.builder()
								.listChanged(true)
								.build())
						.build())
				.implementation(Implementation.builder()
						.name("Helidon MCP Server")
						.version("1.0.0")
						.build())
				.instructions("")
				.buildPrototype(), transportProvider);
		server.start();

		String requestId = UUID.randomUUID().toString();

		transportProvider
			.simulateIncomingMessage(jsonRpcInitializeRequest(requestId, McpSchema.LATEST_PROTOCOL_VERSION));

		McpSchema.JSONRPCMessage response = serverTransport.getLastSentMessage();
		assertThat(response, is(instanceOf(McpSchema.JSONRPCResponse.class)));
		McpSchema.JSONRPCResponse jsonResponse = (McpSchema.JSONRPCResponse) response;
		assertThat(jsonResponse.id(), is(requestId));
		assertThat(jsonResponse.result(), is(instanceOf(McpSchema.InitializeResult.class)));
		McpSchema.InitializeResult result = (McpSchema.InitializeResult) jsonResponse.result();
		assertThat(result.protocolVersion(), is(McpSchema.LATEST_PROTOCOL_VERSION));

		server.closeGracefully();
	}

	@Test
	void shouldNegotiateSpecificVersion() {
//		String oldVersion = "0.1.0";
//		MockMcpServerTransport serverTransport = new MockMcpServerTransport();
//		var transportProvider = new MockMcpServerTransportProvider(serverTransport);
//
//		McpServer server = new McpServerImpl(McpServerConfig.builder()
//				.implementation(Implementation.builder()
//						.name("Helidon MCP Server")
//						.version("1.0.0")
//						.build())
//				.buildPrototype(), transportProvider);
//
//		server.setProtocolVersions(List.of(oldVersion, McpSchema.LATEST_PROTOCOL_VERSION));
//
//		String requestId = UUID.randomUUID().toString();
//
//		transportProvider.simulateIncomingMessage(jsonRpcInitializeRequest(requestId, oldVersion));
//
//		McpSchema.JSONRPCMessage response = serverTransport.getLastSentMessage();
//		assertThat(response).isInstanceOf(McpSchema.JSONRPCResponse.class);
//		McpSchema.JSONRPCResponse jsonResponse = (McpSchema.JSONRPCResponse) response;
//		assertThat(jsonResponse.id()).isEqualTo(requestId);
//		assertThat(jsonResponse.result()).isInstanceOf(McpSchema.InitializeResult.class);
//		McpSchema.InitializeResult result = (McpSchema.InitializeResult) jsonResponse.result();
//		assertThat(result.protocolVersion()).isEqualTo(oldVersion);
//
//		server.closeGracefully();
	}

	@Test
	void shouldSuggestLatestVersionForUnsupportedVersion() {
//		String unsupportedVersion = "999.999.999";
//		MockMcpServerTransport serverTransport = new MockMcpServerTransport();
//		var transportProvider = new MockMcpServerTransportProvider(serverTransport);
//
//		McpServer server = new McpServerImpl(McpServerConfig.builder().buildPrototype(), transportProvider);
//
//		String requestId = UUID.randomUUID().toString();
//
//		transportProvider.simulateIncomingMessage(jsonRpcInitializeRequest(requestId, unsupportedVersion));
//
//		McpSchema.JSONRPCMessage response = serverTransport.getLastSentMessage();
//		assertThat(response).isInstanceOf(McpSchema.JSONRPCResponse.class);
//		McpSchema.JSONRPCResponse jsonResponse = (McpSchema.JSONRPCResponse) response;
//		assertThat(jsonResponse.id()).isEqualTo(requestId);
//		assertThat(jsonResponse.result()).isInstanceOf(McpSchema.InitializeResult.class);
//		McpSchema.InitializeResult result = (McpSchema.InitializeResult) jsonResponse.result();
//		assertThat(result.protocolVersion()).isEqualTo(McpSchema.LATEST_PROTOCOL_VERSION);
//
//		server.closeGracefully();
	}

	@Test
	void shouldUseHighestVersionWhenMultipleSupported() {
//		String oldVersion = "0.1.0";
//		String middleVersion = "0.2.0";
//		String latestVersion = McpSchema.LATEST_PROTOCOL_VERSION;
//
//		MockMcpServerTransport serverTransport = new MockMcpServerTransport();
//		var transportProvider = new MockMcpServerTransportProvider(serverTransport);
//
//		McpServer server = new McpServerImpl(McpServerConfig.builder().buildPrototype(), transportProvider);
//
//		server.setProtocolVersions(List.of(oldVersion, middleVersion, latestVersion));
//
//		String requestId = UUID.randomUUID().toString();
//		transportProvider.simulateIncomingMessage(jsonRpcInitializeRequest(requestId, latestVersion));
//
//		McpSchema.JSONRPCMessage response = serverTransport.getLastSentMessage();
//		assertThat(response).isInstanceOf(McpSchema.JSONRPCResponse.class);
//		McpSchema.JSONRPCResponse jsonResponse = (McpSchema.JSONRPCResponse) response;
//		assertThat(jsonResponse.id()).isEqualTo(requestId);
//		assertThat(jsonResponse.result()).isInstanceOf(McpSchema.InitializeResult.class);
//		McpSchema.InitializeResult result = (McpSchema.InitializeResult) jsonResponse.result();
//		assertThat(result.protocolVersion()).isEqualTo(latestVersion);
//
//		server.closeGracefully();
	}

}
