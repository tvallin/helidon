package io.helidon.integrations.mcp.server;

public interface PromptArgument {
    String name();
    String description();
    boolean required();

    static PromptArgument create(String name, String description) {
        return create(name, description, true);
    }

    static PromptArgument create(String name, String description, boolean required) {
        return new PromptArgument() {

            @Override
            public String name() {
                return name;
            }

            @Override
            public String description() {
                return description;
            }

            @Override
            public boolean required() {
                return required;
            }
        };
    }
}
