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

import java.util.List;

import io.helidon.common.mapper.OptionalValue;

import jakarta.json.Json;
import jakarta.json.JsonValue;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class McpParametersTest {
    private static final String KEY = "key";

    @Test
    void testSimpleString() {
        JsonValue object = Json.createObjectBuilder()
                .add("foo", "bar")
                .build();

        McpParameters parameters = new McpParameters(object, KEY);
        String foo = parameters.get("foo").asString().orElse(null);

        assertThat(foo, is("bar"));
    }

    @Test
    void testSimpleBoolean() {
        JsonValue object = Json.createObjectBuilder()
                .add("foo", true)
                .build();

        McpParameters parameters = new McpParameters(object, KEY);
        Boolean foo = parameters.get("foo").asBoolean().orElse(null);

        assertThat(foo, is(true));
    }

    @Test
    void testSimpleInteger() {
        JsonValue object = Json.createObjectBuilder()
                .add("foo", 1)
                .build();

        McpParameters parameters = new McpParameters(object, KEY);
        int foo = parameters.get("foo").asInt().orElse(null);

        assertThat(foo, is(1));
    }

    @Test
    void testSimpleList() {
        JsonValue object = Json.createObjectBuilder()
                .add("foo", Json.createArrayBuilder()
                        .add("foo1")
                        .add("foo2"))
                .build();

        McpParameters parameters = new McpParameters(object, KEY);
        List<String> foo = parameters.get("foo")
                .asList()
                .get()
                .stream()
                .map(McpParameters::asString)
                .map(OptionalValue::get)
                .toList();

        assertThat(foo, is(List.of("foo1", "foo2")));
    }
}
