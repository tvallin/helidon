package io.helidon.integrations.mcp.server;

public interface ResourceInfo {
    String uri();
    String name();
    String description();

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private String uri;
        private String name;
        private String description;

        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public ResourceInfo build() {
            return new ResourceInfo() {
                @Override
                public String uri() {
                    return uri;
                }

                @Override
                public String name() {
                    return name;
                }

                @Override
                public String description() {
                    return description;
                }
            };
        }
    }
}
