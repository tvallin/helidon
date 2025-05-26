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

    public <T> Optional<T> object(String name, Class<T> clazz) {
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
