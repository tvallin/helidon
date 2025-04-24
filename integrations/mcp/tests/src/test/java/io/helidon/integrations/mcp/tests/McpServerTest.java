package io.helidon.integrations.mcp.tests;

import java.util.Map;

import io.helidon.integrations.mcp.server.Implementation;
import io.helidon.integrations.mcp.server.ListChanged;
//import io.helidon.integrations.mcp.server.McpServer;
import io.helidon.integrations.mcp.server.McpServerConfig;
import io.helidon.integrations.mcp.server.Resource;
//import io.helidon.integrations.mcp.server.ServerCapabilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;

class McpServerTest {

//	private static final McpServer server;
	private static final ObjectMapper mapper = new ObjectMapper();

	static {
//		HELIDON MCP

//		server = McpServerConfig.builder()
//				.transport("http")
//				.capabilities(ServerCapabilities.builder()
//						.promts(ListChanged.builder()
//								.listChanged(true)
//								.build())
//						.resource(Resource.builder()
//								.listChanged(true)
//								.subscribe(true)
//								.build())
//						.tools(ListChanged.builder()
//								.listChanged(true)
//								.build())
//						.build())
//				.implementation(Implementation.builder()
//						.name("Helidon MCP Server")
//						.version("1.0.0")
//						.build())
//				.instructions("")
//				.build();
//		server.start();

//		JAVA SDK MCP

		McpSyncServer syncServer = McpServer.sync(new HttpServletSseServerTransportProvider(new ObjectMapper(), "/sse"))
				.serverInfo("my-server", "1.0.0")
				.capabilities(McpSchema.ServerCapabilities.builder()
						.resources(true, true)     // Enable resource support
						.tools(true)         // Enable tool support
						.prompts(true)       // Enable prompt support
						.logging()// Enable logging support
						.build())
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
