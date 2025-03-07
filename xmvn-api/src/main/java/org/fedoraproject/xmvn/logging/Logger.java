/*-
 * Copyright (c) 2016-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.logging;

/**
 * Represents a logging interface for capturing application events at various levels.
 *
 * <p>Provides methods for logging messages at different severity levels, including debug, info,
 * warning, and error.
 *
 * @author Mikolaj Izdebski
 */
public interface Logger {

    /**
     * Checks whether debug logging is enabled.
     *
     * @return {@code true} if debug logging is enabled, {@code false} otherwise
     */
    boolean isDebugEnabled();

    /**
     * Logs a debug-level message.
     *
     * <p>The format string follows the SLF4J format, where '{}' is used as a placeholder for
     * arguments.
     *
     * @param format the SLF4J format string
     * @param args the arguments referenced by the format specifiers in the format string
     */
    void debug(String format, Object... args);

    /**
     * Logs an informational message.
     *
     * <p>The format string follows the SLF4J format, where '{}' is used as a placeholder for
     * arguments.
     *
     * @param format the SLF4J format string
     * @param args the arguments referenced by the format specifiers in the format string
     */
    void info(String format, Object... args);

    /**
     * Logs a warning message.
     *
     * <p>The format string follows the SLF4J format, where '{}' is used as a placeholder for
     * arguments.
     *
     * @param format the SLF4J format string
     * @param args the arguments referenced by the format specifiers in the format string
     */
    void warn(String format, Object... args);

    /**
     * Logs an error message.
     *
     * <p>The format string follows the SLF4J format, where '{}' is used as a placeholder for
     * arguments.
     *
     * @param format the SLF4J format string
     * @param args the arguments referenced by the format specifiers in the format string
     */
    void error(String format, Object... args);
}
