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
 * @author Mikolaj Izdebski
 */
public interface Logger {
    boolean isDebugEnabled();

    void debug(String format, Object... args);

    void info(String format, Object... args);

    void warn(String format, Object... args);

    void error(String format, Object... args);
}
