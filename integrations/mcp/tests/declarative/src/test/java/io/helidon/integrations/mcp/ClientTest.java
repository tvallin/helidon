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

package io.helidon.integrations.mcp;

import java.net.URI;

import io.helidon.service.registry.ServiceRegistry;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.testing.junit5.ServerTest;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ServerTest
class ClientTest {

    private final URI uri;

    ClientTest(URI serverUri) {
        this.uri = serverUri;
    }

    @Test
    void testClient() {
        McpTransport transport = new HttpMcpTransport.Builder()
                .sseUrl(uri + "/sse")
                .logRequests(true)
                .logResponses(true)
                .build();

        try (McpClient client = new DefaultMcpClient.Builder()
                .transport(transport)
                .build()) {

            var list = client.listTools();
            assertThat(list.size(), is(1));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
