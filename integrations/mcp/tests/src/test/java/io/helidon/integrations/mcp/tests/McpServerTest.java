package io.helidon.integrations.mcp.tests;

import io.helidon.integrations.mcp.server.Implementation;
import io.helidon.integrations.mcp.server.ListChanged;
import io.helidon.integrations.mcp.server.McpServer;
import io.helidon.integrations.mcp.server.McpServerConfig;
import io.helidon.integrations.mcp.server.Resource;
import io.helidon.integrations.mcp.server.ServerCapabilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class McpServerTest {

	private static final McpServer server;
	private static final ObjectMapper mapper = new ObjectMapper();

	static {

		server = McpServerConfig.builder()
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
				.build();
	}

	@Test
	void testMcpJdkClient() throws JsonProcessingException {
//		McpSyncClient client = io.modelcontextprotocol.client.McpClient.sync(new HttpClientSseClientTransport(server.baseUri()))
//				.capabilities(new McpSchema.ClientCapabilities(
//						Map.of(),
//						new McpSchema.ClientCapabilities.RootCapabilities(true),
//						new McpSchema.ClientCapabilities.Sampling()))
//				.clientInfo(new McpSchema.Implementation("MCP Client", "1.0.0"))
//				.build();
//		McpSchema.InitializeResult result = client.initialize();
//		System.out.println(mapper.writeValueAsString(result));
	}

	@Test
	void testLangchain4jClient() throws Exception {
//		McpTransport transport = new HttpMcpTransport.Builder()
//				.sseUrl(server.baseUri() + "/sse")
//				.logRequests(true)
//				.logResponses(true)
//				.timeout(Duration.ofSeconds(5))
//				.build();
//		dev.langchain4j.mcp.client.McpClient mcpClient = new DefaultMcpClient.Builder()
//				.transport(transport)
//				.build();
//		mcpClient.close();
	}

}
