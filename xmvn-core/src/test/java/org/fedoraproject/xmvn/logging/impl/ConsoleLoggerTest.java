/*-
 * Copyright (c) 2025-2026 Red Hat, Inc.
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
package org.fedoraproject.xmvn.logging.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Mikolaj Izdebski
 */
class ConsoleLoggerTest {
    private ConsoleLogger logger;
    private final ByteArrayOutputStream log = new ByteArrayOutputStream();
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        System.setErr(new PrintStream(log));
        logger = new ConsoleLogger();
    }

    String getLog() {
        return log.toString();
    }

    @Test
    void isDebugEnabled() {
        boolean debugExpected = System.getProperty("xmvn.debug") != null;
        assertThat(logger.isDebugEnabled()).isEqualTo(debugExpected);
    }

    @Test
    void debugLogging() {
        System.setProperty("xmvn.debug", "true");
        logger = new ConsoleLogger();
        logger.debug("Debug message {}", 123);
        assertThat(getLog()).contains("DEBUG: Debug message 123");
        System.clearProperty("xmvn.debug");
    }

    @Test
    void infoLogging() {
        logger.info("Info message {}", 42);
        assertThat(getLog()).contains("Info message 42");
    }

    @Test
    void warnLogging() {
        logger.warn("Warning message {}");
        assertThat(getLog()).contains("WARNING: Warning message");
    }

    @Test
    void errorLogging() {
        logger.error("Error message {}", "Critical");
        assertThat(getLog()).contains("ERROR: Error message Critical");
    }

    @BeforeEach
    void tearDown() {
        System.setErr(originalErr);
    }
}
