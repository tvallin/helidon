/*
 * Copyright (c) 2025 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.integrations.mcp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

import io.helidon.common.GenericType;
import io.helidon.common.mapper.MapperException;
import io.helidon.common.mapper.Mappers;
import io.helidon.common.mapper.OptionalValue;
import io.helidon.config.Config;

import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

/**
 * Mcp parameters provided to {@link Tool} and {@link Prompt}.
 */
public class McpParameters {
    private static final Mappers MAPPERS = Mappers.create();
    private static final EmptyValue EMPTY_VALUE = new EmptyValue();
    private static final McpParameters EMPTY = new McpParameters(JsonValue.NULL, "null");

    private final JsonValue value;
    private final String key;

    McpParameters(JsonValue root, String key) {
        this.value = root;
        this.key = key;
    }

    @SuppressWarnings("unchecked")
    private static <T> OptionalValue<T> empty() {
        return (OptionalValue<T>) EMPTY_VALUE;
    }

    public McpParameters get(String key) {
        if (value instanceof JsonObject jsonObject) {
            JsonValue v = jsonObject.get(key);
            if (v != null) {
                return new McpParameters(v, key);
            }
            return EMPTY;
        }
        if (value == JsonValue.NULL) {
            return EMPTY;
        }
        throw new IllegalStateException("Cannot get " + value.getValueType() + " as an object");
    }

    public OptionalValue<String> asString() {
        if (value instanceof JsonString jsonString) {
            return OptionalValue.create(MAPPERS, key, jsonString.getString());
        }
        if (value == JsonValue.NULL) {
            return empty();
        }
        throw new IllegalStateException("Cannot get " + value.getValueType() + " as a string");
    }

    public OptionalValue<Integer> asInt() {
        if (value instanceof JsonNumber number) {
            return OptionalValue.create(MAPPERS, key, number.intValue());
        }
        if (value == JsonValue.NULL) {
            return empty();
        }
        throw new IllegalStateException("Cannot get " + value.getValueType() + "as an integer");
    }

    public OptionalValue<Boolean> asBoolean() {
        if (value == JsonValue.TRUE) {
            return OptionalValue.create(MAPPERS, key, true);
        }
        if (value == JsonValue.FALSE) {
            return OptionalValue.create(MAPPERS, key, false);
        }
        if (value == JsonValue.NULL) {
            return empty();
        }
        throw new IllegalStateException("Cannot get " + value.getValueType() + "as a boolean");
    }

    public OptionalValue<List<McpParameters>> asList() {
        if (value instanceof JsonArray array) {
            List<McpParameters> list = new ArrayList<>();
            int i = 0;
            for (JsonValue value : array) {
                list.add(new McpParameters(value, key + "-" + i++));
            }
            return OptionalValue.create(MAPPERS, key, list);
        }
        if (value == JsonValue.NULL) {
            return empty();
        }
        throw new IllegalStateException("Cannot get " + value.getValueType() + "as a list");
    }

    public <T> OptionalValue<T> as(Function<McpParameters, T> function) {
        if (value == JsonValue.NULL) {
            return empty();
        }
        return OptionalValue.create(MAPPERS, key, function.apply(this));
    }

    public <T> OptionalValue<T> as(Class<T> clazz) {
        if (value == JsonValue.NULL) {
            return empty();
        }
        return OptionalValue.create(MAPPERS, key, clazz);
    }

    public <T> OptionalValue<T> as(GenericType<T> type) {
        if (value == JsonValue.NULL) {
            return empty();
        }
        return OptionalValue.create(MAPPERS, key, type);
    }

    private static final class EmptyValue implements OptionalValue<Object> {

        @SuppressWarnings("unchecked")
        @Override
        public <N> OptionalValue<N> as(Class<N> type) {
            return (OptionalValue<N>) this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <N> OptionalValue<N> as(GenericType<N> type) {
            return (OptionalValue<N>) this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <N> OptionalValue<N> as(Function<? super Object, ? extends N> mapper) {
            return (OptionalValue<N>) this;
        }

        @Override
        public Optional<Object> asOptional() throws MapperException {
            return Optional.empty();
        }

        @Override
        public String name() {
            return "empty";
        }

        @Override
        public Object get() {
            throw new NoSuchElementException();
        }
    }

}
