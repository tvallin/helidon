/*
 * Copyright 2024-2024 the original author or authors.
 */

package io.helidon.integrations.mcp.server;

import java.util.Map;
import java.util.UUID;

import io.helidon.config.Config;
import io.helidon.webserver.WebServer;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.instanceOf;

/**
 * Tests for MCP protocol version negotiation and listing tools.
 */
class McpProtocolVersionTests {

	private static McpSyncClient client;

	@BeforeAll
	static void startMcpServer() {
//		WebServer.builder()
//				.port(8080)
//				.routing(routing -> routing.addFeature(McpHttpFeature.builder().build()))
//				.build()
//				.start();
//
//		client = McpClient.sync(new HttpClientSseClientTransport("http://localhost:8080"))
//				.capabilities(new McpSchema.ClientCapabilities(
//						Map.of(),
//						new McpSchema.ClientCapabilities.RootCapabilities(true),
//						new McpSchema.ClientCapabilities.Sampling()))
//				.clientInfo(new McpSchema.Implementation("MCP Client", "1.0.0"))
//				.build();
	}

	@Test
	void initializeRequest() {
//		McpSchema.InitializeResult result = client.initialize();

//		assertThat();

//		client.close();
	}

//	@Test
//	void listTools() {
//		McpSchema.ListToolsResult listToolsResult = client.listTools();
//	}

}
