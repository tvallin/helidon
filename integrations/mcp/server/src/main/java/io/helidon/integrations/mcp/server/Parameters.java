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
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import io.helidon.common.mapper.OptionalValue;
import io.helidon.common.mapper.Value;

public class Parameters implements io.helidon.common.parameters.Parameters {

    io.helidon.common.parameters.Parameters delegate =
            io.helidon.common.parameters.Parameters.create("mcp", Map.of());

    static Parameters toParameters(Object value) {
        return new Parameters();
    }

    public <T> Optional<T> object(String key, Class<T> clazz) {
        return Optional.empty();
    }


    @Override
    public List<String> all(String name) throws NoSuchElementException {
        return delegate.all(name);
    }

    @Override
    public List<Value<String>> allValues(String name) throws NoSuchElementException {
        return delegate.allValues(name);
    }

    @Override
    public String get(String name) throws NoSuchElementException {
        return delegate.get(name);
    }

    @Override
    public OptionalValue<String> first(String name) {
        return delegate.first(name);
    }

    @Override
    public boolean contains(String name) {
        return delegate.contains(name);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public Set<String> names() {
        return delegate.names();
    }

    @Override
    public String component() {
        return delegate.component();
    }
}
