package io.helidon.integrations.mcp.server;

import io.helidon.http.Status;
import io.helidon.webserver.http.ErrorHandler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

public class McpExceptionHandler implements ErrorHandler<Exception> {

	static McpExceptionHandler create() {
		return new McpExceptionHandler();
	}

	@Override
	public void handle(ServerRequest req, ServerResponse res, Exception throwable) {
		System.out.println("McpExceptionHandler: " + throwable.getMessage());
		//Todo - Return a verbosed error message with HTTP 500 Internal Error
	}
}
