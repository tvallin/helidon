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

/**
 * Server capabilities.
 */
enum Capability {
    /**
     * Notify clients when list of {@link Tool} changes.
     */
    TOOL_LIST_CHANGED,
    /**
     * Notify clients when list of {@link Resource} changes
     */
    RESOURCE_LIST_CHANGED,
    /**
     * Allow clients to subscribe to a {@link Resource} and be notified when its content changes.
     */
    RESOURCE_SUBSCRIBE,
    /**
     * Notify clients when list of {@link Prompt} changes.
     */
    PROMPT_LIST_CHANGED,
    /**
     * Enable logging capability.
     */
    LOGGING,
    /**
     * Enable completion capability.
     */
    COMPLETION,
    /**
     * Enable pagination capability.
     */
    PAGINATION
}
