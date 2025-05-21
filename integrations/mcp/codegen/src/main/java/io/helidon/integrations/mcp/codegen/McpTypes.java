package io.helidon.integrations.mcp.codegen;

import io.helidon.common.types.TypeName;

final class McpTypes {

	//Annotations
	static final TypeName MCP_SERVER = TypeName.create("io.helidon.integrations.mcp.server.Mcp.Server");
	static final TypeName MCP_TOOL = TypeName.create("io.helidon.integrations.mcp.server.Mcp.Tool");
	static final TypeName MCP_TOOL_PARAM = TypeName.create("io.helidon.integrations.mcp.server.Mcp.ToolParam");
	static final TypeName MCP_RESOURCE = TypeName.create("io.helidon.integrations.mcp.server.Mcp.Resource");
	static final TypeName MCP_PROMPT = TypeName.create("io.helidon.integrations.mcp.server.Mcp.Prompt");
	static final TypeName MCP_PROMT_PARAM = TypeName.create("io.helidon.integrations.mcp.server.Mcp.PromptParam");
	static final TypeName MCP_NOTIFICATION = TypeName.create("io.helidon.integrations.mcp.server.Mcp.Notification");
	static final TypeName MCP_SUBSCRIBE = TypeName.create("io.helidon.integrations.mcp.server.Mcp.Subscribe");

	//Implementations
	static final TypeName MCPSERVER = TypeName.create("io.helidon.integrations.mcp.server.McpServer");
	static final TypeName MCPIMPLEMENTATION = TypeName.create("io.helidon.integrations.mcp.server.Implementation");
	static final TypeName MCPCAPABILITIES = TypeName.create("io.helidon.integrations.mcp.server.Capabilities");
	static final TypeName MCP_TOOL_COMPONENT = TypeName.create("io.helidon.integrations.mcp.server.ToolComponent");
	static final TypeName MCP_RESOURCE_COMPONENT = TypeName.create("io.helidon.integrations.mcp.server.ResourceComponent");
	static final TypeName MCP_PROMPT_COMPONENT = TypeName.create("io.helidon.integrations.mcp.server.PromptComponent");

}
