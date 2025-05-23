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
package io.helidon.tests.integration.jpa.h2;

import java.nio.file.Path;
import java.util.Map;

import io.helidon.tests.integration.harness.ProcessRunner;
import io.helidon.tests.integration.harness.ProcessRunner.ExecMode;
import io.helidon.tests.integration.harness.WaitStrategy;
import io.helidon.tests.integration.harness.TestProcess;
import io.helidon.tests.integration.harness.TestProcesses;
import io.helidon.tests.integration.jpa.common.RemoteTest;

/**
 * Base class for the remote tests.
 */
@TestProcesses
abstract class H2RemoteTest extends RemoteTest {
    @TestProcess
    static final ProcessRunner PROCESS_RUNNER = ProcessRunner.of(ExecMode.CLASS_PATH)
            .finalName("helidon-tests-integration-jpa-h2")
            .properties(Map.of("java.util.logging.config.file", Path.of("target/classes/logging.properties").toAbsolutePath()))
            .waitingFor(WaitStrategy.waitForPort());

    /**
     * Create a new instance.
     *
     * @param path base path
     */
    @SuppressWarnings("resource")
    H2RemoteTest(String path) {
        super(path, PROCESS_RUNNER.process().port());
    }
}
