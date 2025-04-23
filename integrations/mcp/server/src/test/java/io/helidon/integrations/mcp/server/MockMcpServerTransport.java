/*
 * Copyright 2024-2024 the original author or authors.
 */

package io.helidon.integrations.mcp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import io.helidon.integrations.mcp.server.spi.McpTransport;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCNotification;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCRequest;

/**
 * A mock implementation of the {@link McpTransport} interfaces.
 */
public class MockMcpServerTransport implements McpTransport {

	private final List<McpSchema.JSONRPCMessage> sent = new ArrayList<>();

	private final BiConsumer<MockMcpServerTransport, McpSchema.JSONRPCMessage> interceptor;

	public MockMcpServerTransport() {
		this((t, msg) -> {
		});
	}

	public MockMcpServerTransport(BiConsumer<MockMcpServerTransport, McpSchema.JSONRPCMessage> interceptor) {
		this.interceptor = interceptor;
	}

	@Override
	public void sendMessage(Object object) {
		McpSchema.JSONRPCMessage message = (McpSchema.JSONRPCMessage) object;
		sent.add(message);
		interceptor.accept(this, message);
	}

	public JSONRPCRequest getLastSentMessageAsRequest() {
		return (JSONRPCRequest) getLastSentMessage();
	}

	public JSONRPCNotification getLastSentMessageAsNotification() {
		return (JSONRPCNotification) getLastSentMessage();
	}

	public McpSchema.JSONRPCMessage getLastSentMessage() {
		return !sent.isEmpty() ? sent.get(sent.size() - 1) : null;
	}

	@Override
	public <T> T unmarshall(Object params, Class<T> clazz) {
		return new ObjectMapper().convertValue(params, clazz);
	}

	@Override
	public void close() {
	}

	@Override
	public void closeGracefully() {
	}

}
