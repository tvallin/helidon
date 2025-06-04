package io.helidon.integrations.mcp.server;

import java.util.function.Function;

class ToolImpl implements Tool {
    private final String name;
    private final JsonSchema schema;
    private final String description;
    private final Function<McpParameters, ToolContent> process;

    ToolImpl(String name, String description, JsonSchema schema, Function<McpParameters, ToolContent> process) {
        this.name = name;
        this.description = description;
        this.schema = schema;
        this.process = process;
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public JsonSchema schema() {
        return schema;
    }

    @Override
    public ToolContent process(McpParameters parameters) {
        return process.apply(parameters);
    }
}
