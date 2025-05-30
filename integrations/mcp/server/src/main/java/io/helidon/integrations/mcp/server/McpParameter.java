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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

/**
 * Mcp parameters to {@link Tool} and {@link Prompt}.
 */
public class McpParameter implements JsonObject {

    private JsonObject delegate;

    private McpParameter() {
    }

    McpParameter(JsonObject root) {
        this.delegate = root;
    }

    @Override
    public JsonArray getJsonArray(String name) {
        return delegate.getJsonArray(name);
    }

    @Override
    public JsonObject getJsonObject(String name) {
        return delegate.getJsonObject(name);
    }

    @Override
    public JsonNumber getJsonNumber(String name) {
        return delegate.getJsonNumber(name);
    }

    @Override
    public JsonString getJsonString(String name) {
        return delegate.getJsonString(name);
    }

    @Override
    public String getString(String name) {
        return delegate.getString(name);
    }

    @Override
    public String getString(String name, String defaultValue) {
        return delegate.getString(name, defaultValue);
    }

    @Override
    public int getInt(String name) {
        return delegate.getInt(name);
    }

    @Override
    public int getInt(String name, int defaultValue) {
        return delegate.getInt(name, defaultValue);
    }

    @Override
    public boolean getBoolean(String name) {
        return delegate.getBoolean(name);
    }

    @Override
    public boolean getBoolean(String name, boolean defaultValue) {
        return delegate.getBoolean(name, defaultValue);
    }

    @Override
    public boolean isNull(String name) {
        return delegate.isNull(name);
    }

    @Override
    public ValueType getValueType() {
        return delegate.getValueType();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public JsonValue get(Object key) {
        return delegate.get(key);
    }

    @Override
    public JsonValue put(String key, JsonValue value) {
        return delegate.put(key, value);
    }

    @Override
    public JsonValue remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends JsonValue> m) {
        delegate.putAll(m);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<String> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<JsonValue> values() {
        return delegate.values();
    }

    @Override
    public Set<Entry<String, JsonValue>> entrySet() {
        return delegate.entrySet();
    }
}
