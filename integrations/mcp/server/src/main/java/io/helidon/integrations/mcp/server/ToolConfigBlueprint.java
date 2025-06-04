package io.helidon.integrations.mcp.server;

import io.helidon.builder.api.Prototype;

@Prototype.Blueprint
interface ToolConfigBlueprint extends Prototype.Factory<Tool>{
    /**
     * Tool name.
     *
     * @return name
     */
    String name();

    /**
     * Tool description.
     *
     * @return description
     */
    String description();

    /**
     * Tool {@link JsonSchema}.
     *
     * @return schema
     */
    JsonSchema schema();
}
