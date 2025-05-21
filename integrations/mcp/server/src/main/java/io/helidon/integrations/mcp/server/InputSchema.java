package io.helidon.integrations.mcp.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Tool input schema description.
 */
public class InputSchema {
    private final ObjectNode schema = new ObjectMapper().createObjectNode();

    private InputSchema(Builder builder) {
        schema.put("id", builder.id);
        schema.put("type", builder.type);
        ArrayNode array = schema.putArray("required");
        builder.required.forEach(array::add);
        ObjectNode props = schema.putObject("properties");
        for (Map.Entry<String,String> entry : builder.properties.entrySet()) {
            ObjectNode node = props.putObject(entry.getKey());
            node.put("type", entry.getValue());
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public String asString() {
        return schema.toString();
    }

    public static class Builder {

        private final String type;
        private final String id;
        private final List<String> required;
        private final Map<String, String> properties;

        private Builder() {
            this.id = "jsonSchema-" + UUID.randomUUID();
            this.type = "object";
            this.required = new ArrayList<>();
            this.properties = new HashMap<>();
        }

        public Builder properties(String key, String value) {
            this.properties.put(key, value);
            return this;
        }

        public Builder required(String value) {
            this.required.add(value);
            return this;
        }

        public InputSchema build() {
            return new InputSchema(this);
        }

    }

}
