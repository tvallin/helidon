package io.helidon.integrations.mcp.server;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import io.helidon.http.Status;
import io.helidon.http.sse.SseEvent;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.helidon.webserver.sse.SseSink;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;

import static io.helidon.http.HeaderNames.MCP_SESSION_ID;

public class McpServletTransportProvider {


	/**
	 * Handler to create a server session.
	 *
	 * @param request  server request
	 * @param response server response
	 */
	private void get(ServerRequest request, ServerResponse response) {
		String sessionId = UUID.randomUUID().toString();
		SseEvent initialEvent = SseEvent.builder()
				.name("endpoint")
				.data(String.format("/endpoint?sessionId=%s", sessionId))
				.build();

//		sessions.put(sessionId, new McpServerSession(sessionId, response.outputStream(), sseRouting, this::initialize));
		response.sink(SseSink.TYPE).emit(initialEvent).close();
	}

	/**
	 * Handler to forward client payload to proper session.
	 *
	 * @param request  server request
	 * @param response server response
	 */
	private void post(ServerRequest request, ServerResponse response) {
		AtomicReference<String> sessionId = new AtomicReference<>();

		try {
			sessionId.set(request.query().get("sessionId"));
		} catch (NoSuchElementException exception) {
			request.headers()
					.find(MCP_SESSION_ID)
					.ifPresentOrElse(header -> sessionId.set(header.values()),
							() -> response.status(Status.BAD_REQUEST_400).send());
		}

//		McpServerSession session = sessions.get(sessionId.get());
//		if (session == null) {
//			response.status(Status.NOT_FOUND_404);
//			response.send();
//			return;
//		}

		String content = request.content().as(String.class);
		McpSchema.JSONRPCMessage message = deserializeJsonRpcMessage(content);
//		session.handle(message, response.sink(SseSink.TYPE));

	}

	private McpSchema.JSONRPCMessage deserializeJsonRpcMessage(String content) {
		try {
			return McpSchema.deserializeJsonRpcMessage(new ObjectMapper(), content);
		} catch (IOException e) {
			throw new McpException("Failed to deserialize JSON RPC message");
		}
	}

}
