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

import java.util.LinkedList;
import java.util.List;

class ToolContentList implements ToolContent {

    List<ToolContent> contents = new LinkedList<>();

    ToolContentList(ToolContent content) {
        contents.add(content);
    }

    public ToolContentList(ToolContent content, ToolContent content1) {
        contents.add(content);
        contents.add(content1);
    }

    public ToolContentList(ToolContent content, ToolContent content1, ToolContent content2) {
        contents.add(content);
        contents.add(content1);
        contents.add(content2);
    }

    @Override
    public String type() {
        return "list";
    }
}
