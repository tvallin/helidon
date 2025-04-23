/*
 * Copyright 2024-2024 the original author or authors.
 */

package io.helidon.integrations.mcp.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import io.helidon.integrations.mcp.server.spi.McpTransport;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link StdioTransportProvider}.
 *
 * @author Christian Tzolov
 */
class StdioTransportProviderTest {

	private final PrintStream originalOut = System.out;

	private final PrintStream originalErr = System.err;

	private ByteArrayOutputStream testErr;

	private PrintStream testOutPrintStream;

	private StdioTransportProvider transportProvider;

	private ObjectMapper objectMapper;

	private McpSession.Factory sessionFactory;

	private McpSession mockSession;

	@BeforeEach
	void setUp() {
		testErr = new ByteArrayOutputStream();

		testOutPrintStream = new PrintStream(testErr, true);
		System.setOut(testOutPrintStream);
		System.setErr(testOutPrintStream);

		objectMapper = new ObjectMapper();

		// Create mocks for session factory and session
		mockSession = mock(McpSession.class);
		sessionFactory = mock(McpSession.Factory.class);

		// Configure mock behavior
		when(sessionFactory.create(any(McpTransport.class))).thenReturn(mockSession);

		transportProvider = new StdioTransportProvider(System.in, testOutPrintStream);
	}

	@AfterEach
	void tearDown() {
		if (transportProvider != null) {
			//transportProvider.closeGracefully();
		}
		if (testOutPrintStream != null) {
			testOutPrintStream.close();
		}
		System.setOut(originalOut);
		System.setErr(originalErr);
	}

	@Test
	void shouldCreateSessionWhenSessionFactoryIsSet() {
		// Set session factory
//		transportProvider.setSessionFactory(sessionFactory);
//
//		// Verify session was created with a transport
//		assertThat(testErr.toString().contains("Error"), is(false));
	}

	@Test
	void shouldHandleIncomingMessages() {

		String jsonMessage = "{\"jsonrpc\":\"2.0\",\"method\":\"tools/list\",\"params\":{},\"id\":1}\n";
		InputStream stream = new ByteArrayInputStream(jsonMessage.getBytes(StandardCharsets.UTF_8));

		transportProvider = new StdioTransportProvider(stream, testOutPrintStream);
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

		var schema = """
            {
              "type" : "object",
              "id" : "urn:jsonschema:Operation",
              "properties" : {
                "operation" : {
                  "type" : "string"
                },
                "a" : {
                  "type" : "number"
                },
                "b" : {
                  "type" : "number"
                }
              }
            }
            """;
		server.addTool(new ToolComponent(new McpSchema.Tool("calculator", "Basic calculator", schema),
				(object) -> {
					System.out.println(object);
					System.out.println("Class Name: " + object.getClass().getName());
					return new McpSchema.CallToolResult(List.of(
							new McpSchema.TextContent(List.of(McpSchema.Role.USER), 2.0, object.toString())
					), false);
				}));

		server.start();
		server.closeGracefully();
	}

	@Test
	void shouldHandleSessionClose() {
		// Set session factory
//		transportProvider.setSessionFactory(sessionFactory);
//
//		// Close the transport provider
//		transportProvider.close();
//
//		// Verify session was closed
//		verify(mockSession).closeGracefully();
	}

}
