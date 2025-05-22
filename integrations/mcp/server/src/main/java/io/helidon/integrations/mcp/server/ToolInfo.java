package io.helidon.integrations.mcp.server;

public interface ToolInfo {
    String name();
    String description();
    InputSchema schema();

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        String name;
        String description;
        InputSchema.Builder schemaBuilder = InputSchema.builder();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String version) {
            this.description = version;
            return this;
        }

        public Builder properties(String name, String type, boolean required) {
            this.schemaBuilder.properties(name, type);
            if (required) {
                this.schemaBuilder.required(name);
            }
            return this;
        }

        public Builder properties(String name, String type) {
            this.schemaBuilder.properties(name, type);
            return this;
        }

        public Builder requiredProperties(String... properties) {
            for (String property : properties) {
                this.schemaBuilder.required(property);
            }
            return this;
        }

        public ToolInfo build() {
            return new ToolInfo() {

                @Override
                public String name() {
                    return name;
                }

                @Override
                public String description() {
                    return description;
                }

                @Override
                public InputSchema schema() {
                    return schemaBuilder.build();
                }
            };
        }

    }
}
