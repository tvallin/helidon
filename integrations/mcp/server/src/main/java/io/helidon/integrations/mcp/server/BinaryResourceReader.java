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

import io.helidon.http.HeaderNames;
import io.helidon.webclient.api.HttpClientResponse;
import io.helidon.webclient.api.WebClient;

import io.modelcontextprotocol.spec.McpSchema;

public class BinaryResourceReader implements ResourceReader {

    private final String uri;

    public BinaryResourceReader(String uri) {
        this.uri = uri;
    }

    @Override
    public McpSchema.ResourceContents read() {
        if (uri.startsWith("http")) {
            return new HttpResourceReader(uri).read();
        }
        return null;
    }

    static class HttpResourceReader implements ResourceReader {

        private final String uri;

        HttpResourceReader(String uri) {
            this.uri = uri;
        }

        @Override
        public McpSchema.ResourceContents read() {
            WebClient client = WebClient.builder()
                    .baseUri(uri)
                    .build();
            try (HttpClientResponse request = client.get().request()) {
                String mimeType = request.headers().get(HeaderNames.CONTENT_TYPE).get();
                String data = request.entity().as(String.class);
                return new McpSchema.BlobResourceContents(uri, mimeType, data);
            }
        }
    }
}
