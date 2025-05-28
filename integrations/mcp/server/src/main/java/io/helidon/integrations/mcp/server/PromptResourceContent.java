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

class PromptResourceContent implements PromptContent, ResourceReference {
    private final Role role;
    private final String uri;
    private PromptContent content;

    PromptResourceContent(String uri, Role role) {
        this.uri = uri;
        this.role = role;
    }

    @Override
    public String uri() {
        return uri;
    }

    @Override
    public Role role() {
        return role;
    }

    @Override
    public Content content() {
        //TODO - Look up resources from server...
        throw new UnsupportedOperationException("This is only a resource reference");
    }

    @Override
    public PromptContent chain(PromptContent content) {
        this.content = content;
        return this.content;
    }
}
