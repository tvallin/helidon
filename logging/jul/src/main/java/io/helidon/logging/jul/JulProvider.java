/*
 * Copyright (c) 2019, 2024 Oracle and/or its affiliates.
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
package io.helidon.logging.jul;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import io.helidon.common.NativeImageHelper;
import io.helidon.common.Weight;
import io.helidon.logging.common.spi.LoggingProvider;

/**
 * JUL Logging provider.
 * You do not need to explicitly configure
 * Java Util logging as long as a file {@code logging.properties} is on the classpath or
 * in the current directory, or you configure logging explicitly using System properties.
 * Both {@value #SYS_PROP_LOGGING_CLASS} and {@value #SYS_PROP_LOGGING_FILE} are
 * honored.
 * If you wish to configure the logging system differently, just do not include the file and/or
 * system properties, or set system property {@value #SYS_PROP_DISABLE_CONFIG} to {@code true}.
 */
@Weight(1)
public class JulProvider implements LoggingProvider {
    private static final String TEST_LOGGING_FILE = "logging-test.properties";
    private static final String LOGGING_FILE = "logging.properties";
    private static final String SYS_PROP_DISABLE_CONFIG = "io.helidon.logging.config.disabled";
    private static final String SYS_PROP_LOGGING_CLASS = "java.util.logging.config.class";
    private static final String SYS_PROP_LOGGING_FILE = "java.util.logging.config.file";
    private static final String WHEN_INIT = "initialization";
    private static final String WHEN_RUNTIME = "runtime";

    /**
     * Default constructor required by {@link java.util.ServiceLoader}.
     */
    public JulProvider() {
    }

    @Override
    public void initialization() {
        configureLogging(false);
    }

    @Override
    public void runTime() {
        configureLogging(true);
    }

    // when is either `initialization` or `runtime`
    // when building native image, the `initialization` is called
    // when running it, the `runtime` is called
    // when outside of native-image, only `initialization` is called
    private static void configureLogging(boolean runtime) {
        try {
            doConfigureLogging(runtime);
        } catch (IOException e) {
            System.err.println("Failed to configure logging");
            e.printStackTrace();
        }
    }

    private static void doConfigureLogging(boolean runtime) throws IOException {
        String disableConfigProperty = System.getProperty(SYS_PROP_DISABLE_CONFIG);
        if (Boolean.parseBoolean(disableConfigProperty)) {
            // we are explicitly request to disable this feature
            return;
        }
        String configClass = System.getProperty(SYS_PROP_LOGGING_CLASS);
        String configPath = System.getProperty(SYS_PROP_LOGGING_FILE);
        String source;

        if (configClass != null) {
            source = "class: " + configClass;
        } else if (configPath != null) {
            Path path = Paths.get(configPath);
            source = path.toAbsolutePath().toString();
        } else {
            // we want to configure logging ourselves
            source = findAndConfigureLogging();
        }

        if (runtime || NativeImageHelper.isBuildTime()) {
            String when = runtime ? WHEN_RUNTIME : WHEN_INIT;
            Logger.getLogger(JulProvider.class.getName()).info("Logging at " + when + " configured using " + source);
        } else {
            // build time without native image (when not in native, this is executed only on class initialization,
            // so for the user it is runtime)
            Logger.getLogger(JulProvider.class.getName()).info("Logging at " + WHEN_RUNTIME + " configured using " + source);
        }
    }

    private static String findAndConfigureLogging() throws IOException {
        String source = "defaults";

        // Let's try to find a logging.properties
        // first as a file in the current working directory
        InputStream logConfigStream;

        Path path = Paths.get("").resolve(LOGGING_FILE);

        if (Files.exists(path)) {
            logConfigStream = new BufferedInputStream(Files.newInputStream(path));
            source = "file: " + path.toAbsolutePath();
        } else {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            // check if there is a logging-test.properties first (we are running within a unit test)
            InputStream resourceStream = classPath(TEST_LOGGING_FILE);
            String cpSource = "classpath: /" + TEST_LOGGING_FILE;

            if (resourceStream == null) {
                resourceStream = contextClassPath(TEST_LOGGING_FILE, cl);
                cpSource = "context classpath: /" + TEST_LOGGING_FILE;
            }

            if (resourceStream == null) {
                resourceStream = classPath(LOGGING_FILE);
                cpSource = "classpath: /" + LOGGING_FILE;
            }

            if (resourceStream == null) {
                resourceStream = contextClassPath(LOGGING_FILE, cl);
                cpSource = "context classpath: /" + LOGGING_FILE;
            }

            if (resourceStream == null) {
                // defaults
                return source;
            }

            logConfigStream = new BufferedInputStream(resourceStream);
            source = cpSource;
        }

        try {
            LogManager.getLogManager().readConfiguration(logConfigStream);
        } finally {
            logConfigStream.close();
        }

        return source;
    }

    private static InputStream contextClassPath(String loggingFile, ClassLoader cl) {
        return cl.getResourceAsStream(loggingFile);
    }

    private static InputStream classPath(String loggingFile) {
        return JulProvider.class.getResourceAsStream("/" + loggingFile);
    }
}
