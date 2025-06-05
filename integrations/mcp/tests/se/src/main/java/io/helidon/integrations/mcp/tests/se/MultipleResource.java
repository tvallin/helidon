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

package io.helidon.integrations.mcp.tests.se;

import io.helidon.common.media.type.MediaType;
import io.helidon.common.media.type.MediaTypes;
import io.helidon.integrations.mcp.server.McpHttpFeatureConfig;
import io.helidon.integrations.mcp.server.Resource;
import io.helidon.integrations.mcp.server.ResourceContent;
import io.helidon.integrations.mcp.server.ResourceContents;
import io.helidon.webserver.WebServer;

class MultipleResource {

    static WebServer server;

    static WebServer start() {
        server = WebServer.builder()
                .routing(routing -> routing.addFeature(
                        McpHttpFeatureConfig.builder()
                                .addResource(resource -> resource
                                        .name("resource1")
                                        .description("Resouce 1")
                                        .uri("resource://resource1")
                                        .mediaType(MediaTypes.APPLICATION_JSON)
                                        .ressource(() -> ResourceContents.textContent("resource1 content as text")))

                                .addResource(resource -> resource
                                        .name("resource3")
                                        .description("Resouce 3")
                                        .uri("resource://resource3")
                                        .mediaType(MediaTypes.APPLICATION_JSON)
                                        .ressource(() -> ResourceContents.list(
                                                ResourceContents.textContent("resource3 text"),
                                                ResourceContents.binaryContent("resource3 blob", MediaTypes.APPLICATION_OCTET_STREAM))))

                                .addResource(new MyResource())))
                .build()
                .start();
        return server;
    }

    static void stop() {
        server.stop();
    }

    private static final class MyResource implements Resource {

        private static final MediaType TYPE = MediaTypes.APPLICATION_OCTET_STREAM;

        @Override
        public String uri() {
            return "resource://resource2";
        }

        @Override
        public String name() {
            return "resource2";
        }

        @Override
        public String description() {
            return "Resource 2";
        }

        @Override
        public MediaType mediaType() {
            return TYPE;
        }

        @Override
        public ResourceContent read() {
            return ResourceContents.binaryContent("base64", TYPE);
        }
    }

}
