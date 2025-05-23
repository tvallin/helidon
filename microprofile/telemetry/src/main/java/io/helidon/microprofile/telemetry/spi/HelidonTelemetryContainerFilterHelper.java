/*
 * Copyright (c) 2024 Oracle and/or its affiliates.
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
package io.helidon.microprofile.telemetry.spi;

import jakarta.ws.rs.container.ContainerRequestContext;

/**
 * Service-loaded type applied while the Helidon-provided container filter executes.
 */
public interface HelidonTelemetryContainerFilterHelper {

    /**
     * Invoked to see if this helper votes to create and start a new span for the incoming
     * request reflected in the provided container request context.
     *
     * @param containerRequestContext the {@link jakarta.ws.rs.container.ContainerRequestContext} passed to the filter
     * @return true to vote to start a span; false to vote not to start a span
     */
    boolean shouldStartSpan(ContainerRequestContext containerRequestContext);
}
