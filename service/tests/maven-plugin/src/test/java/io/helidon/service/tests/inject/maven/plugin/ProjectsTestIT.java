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

package io.helidon.service.tests.inject.maven.plugin;

import java.nio.file.Path;
import java.nio.file.Paths;

import io.helidon.build.common.test.utils.ConfigurationParameterSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;

import static io.helidon.build.common.test.utils.FileMatchers.fileExists;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration test that verifies the projects under {@code src/it/projects}.
 */
public class ProjectsTestIT {
    @ParameterizedTest
    @ConfigurationParameterSource("basedir")
    @DisplayName("Test default binding and main class names")
    void test1(String basedir) {
        // test the first project under src/it/projects/test1 (referenced from postbuild.groovy)
        // make sure all the required types are created
        Path projectPath = Paths.get(basedir);
        Path compiledClasses = projectPath.resolve("target/classes/my/module");
        Path generatedSources = projectPath.resolve("target/generated-sources/annotations/my/module");

        assertThat("Generated by the Maven plugin", generatedSources.resolve("ApplicationBinding.java"), fileExists());
        assertThat("Should nto be generated by the Maven plugin",
                   generatedSources.resolve("ApplicationBinding__ServiceDescriptor.java"),
                   not(fileExists()));
        assertThat("Generated by the service inject codegen during compilation",
                   generatedSources.resolve("ServiceType__ServiceDescriptor.java"),
                   fileExists());
        assertThat("Generated by the Maven plugin", generatedSources.resolve("ApplicationMain.java"), fileExists());
        assertThat("Not a generated type, exists in project sources only",
                   generatedSources.resolve("ServiceType.java"),
                   not(fileExists()));

        assertThat("Generated by the Maven plugin", compiledClasses.resolve("ApplicationBinding.class"), fileExists());
        assertThat("Should nto be generated by the Maven plugin",
                   generatedSources.resolve("ApplicationBinding__ServiceDescriptor.java"),
                   not(fileExists()));
        assertThat("Generated by the service inject codegen during compilation",
                   compiledClasses.resolve("ServiceType__ServiceDescriptor.class"),
                   fileExists());
        assertThat("Generated by the Maven plugin", compiledClasses.resolve("ApplicationMain.class"), fileExists());
        assertThat("Compiled service", compiledClasses.resolve("ServiceType.class"), fileExists());

    }

    @ParameterizedTest
    @ConfigurationParameterSource("basedir")
    @DisplayName("Test custom binding and main class names and custom package")
    void test2(String basedir) {
        // test the first project under src/it/projects/test1 (referenced from postbuild.groovy)
        // make sure all the required types are created
        Path projectPath = Paths.get(basedir);
        Path compiledClasses = projectPath.resolve("target/classes/my/module");
        Path compiledCustomClasses = projectPath.resolve("target/classes/my/updated");
        Path generatedSources = projectPath.resolve("target/generated-sources/annotations/my/module");
        Path generatedCustomSources = projectPath.resolve("target/generated-sources/annotations/my/updated");

        assertThat("Generated by the Maven plugin",
                   generatedCustomSources.resolve("UpdatedBinding.java"),
                   fileExists());
        assertThat("Generated by the Maven plugin",
                   generatedCustomSources.resolve("UpdatedBinding__ServiceDescriptor.java"),
                   not(fileExists()));
        assertThat("Generated by the Maven plugin",
                   generatedCustomSources.resolve("UpdatedMain.java"),
                   fileExists());
        assertThat("Generated by the service inject codegen during compilation",
                   generatedSources.resolve("ServiceType__ServiceDescriptor.java"),
                   fileExists());
        assertThat("Not a generated type, exists in project sources only",
                   generatedSources.resolve("ServiceType.java"),
                   not(fileExists()));

        assertThat("Generated by the Maven plugin",
                   compiledCustomClasses.resolve("UpdatedBinding.class"),
                   fileExists());
        assertThat("Generated by the Maven plugin",
                   compiledCustomClasses.resolve("UpdatedBinding__ServiceDescriptor.class"),
                   not(fileExists()));
        assertThat("Generated by the Maven plugin",
                   compiledCustomClasses.resolve("UpdatedMain.class"),
                   fileExists());
        assertThat("Generated by the service inject codegen during compilation",
                   compiledClasses.resolve("ServiceType__ServiceDescriptor.class"),
                   fileExists());
        assertThat("Compiled service", compiledClasses.resolve("ServiceType.class"), fileExists());

    }

    @ParameterizedTest
    @ConfigurationParameterSource("basedir")
    @DisplayName("Test binding and main class generation disabled")
    void test3(String basedir) {
        // test the first project under src/it/projects/test1 (referenced from postbuild.groovy)
        // make sure all the required types are created
        Path projectPath = Paths.get(basedir);
        Path compiledClasses = projectPath.resolve("target/classes/my/module");
        Path generatedSources = projectPath.resolve("target/generated-sources/annotations/my/module");

        assertThat("Should not be generated by the Maven plugin",
                   generatedSources.resolve("ApplicationBinding.java"),
                   not(fileExists()));
        assertThat("Should nto be generated by the Maven plugin",
                   generatedSources.resolve("ApplicationBinding__ServiceDescriptor.java"),
                   not(fileExists()));
        assertThat("Generated by the service inject codegen during compilation",
                   generatedSources.resolve("ServiceType__ServiceDescriptor.java"),
                   fileExists());
        assertThat("Should not be generated by the Maven plugin",
                   generatedSources.resolve("ApplicationMain.java"),
                   not(fileExists()));
        assertThat("Not a generated type, exists in project sources only",
                   generatedSources.resolve("ServiceType.java"),
                   not(fileExists()));

        assertThat("Should not be generated by the Maven plugin",
                   compiledClasses.resolve("ApplicationBinding.class"),
                   not(fileExists()));
        assertThat("Should nto be generated by the Maven plugin",
                   compiledClasses.resolve("ApplicationBinding__ServiceDescriptor.class"),
                   not(fileExists()));
        assertThat("Generated by the service inject codegen during compilation",
                   compiledClasses.resolve("ServiceType__ServiceDescriptor.class"),
                   fileExists());
        assertThat("Should not be generated by the Maven plugin",
                   compiledClasses.resolve("ApplicationMain.class"),
                   not(fileExists()));
        assertThat("Compiled service", compiledClasses.resolve("ServiceType.class"), fileExists());

    }
}