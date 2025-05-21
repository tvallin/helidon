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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.channels.FileLock;
import java.nio.channels.NonWritableChannelException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import io.helidon.common.media.type.MediaTypes;

import io.modelcontextprotocol.spec.McpSchema;

public class FileResourceReader implements ResourceReader {

    private final Path path;
    private final String uri;

    public FileResourceReader(String uri) {
        this.path = Path.of(URI.create(uri));
        this.uri = uri;
    }

    @Override
    public McpSchema.ResourceContents read() {
        try (FileInputStream fis = new FileInputStream(path.toFile())) {
            FileLock lock = null;
            try {
                lock = fis.getChannel().tryLock(0L, Long.MAX_VALUE, false);
            } catch (NonWritableChannelException ignored) {
                // non writable channel means that we do not need to lock it
            }
            try {
                try (BufferedReader bufferedReader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    String content = bufferedReader.lines().collect(Collectors.joining("\n"));
                    return new McpSchema.TextResourceContents(uri, MediaTypes.TEXT_PLAIN_VALUE, content);
                } catch (IOException e) {
                    throw new McpException(String.format("Cannot read from path '%s'", path), e);
                }
            } finally {
                if (lock != null) {
                    lock.release();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
