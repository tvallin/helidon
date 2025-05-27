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

import java.util.Set;

/**
 * Mcp Server information.
 */
public interface McpServerInfo {
    /**
     * Server name.
     *
     * @return name
     */
    String name();

    /**
     * Server version.
     *
     * @return version
     */
    String version();

    /**
     * Server {@link Capability}.
     *
     * @return capabilities
     */
    Set<Capability> capabilities();

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private String name;
        private String version;
        private Set<Capability> capabilities;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder capability(Capability... capabilities) {
            this.capabilities = Set.of(capabilities);
            return this;
        }

        public McpServerInfo build() {
            return new McpServerInfo() {
                @Override
                public String name() {
                    return name;
                }

                @Override
                public String version() {
                    return version;
                }

                @Override
                public Set<Capability> capabilities() {
                    return capabilities;
                }
            };
        }
    }
}
