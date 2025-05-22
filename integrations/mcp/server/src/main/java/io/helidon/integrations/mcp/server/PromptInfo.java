package io.helidon.integrations.mcp.server;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public interface PromptInfo {
    String name();
    String description();
    Set<PromptArgument> arguments();

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private String name;
        private String description;
        private final Set<PromptArgument> arguments = new HashSet<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder arguments(PromptArgument... arguments) {
            Collections.addAll(this.arguments, arguments);
            return this;
        }

        public PromptInfo build() {
            return new PromptInfo() {
                @Override
                public String name() {
                    return name;
                }

                @Override
                public String description() {
                    return description;
                }

                @Override
                public Set<PromptArgument> arguments() {
                    return arguments;
                }
            };
        }
    }
}
