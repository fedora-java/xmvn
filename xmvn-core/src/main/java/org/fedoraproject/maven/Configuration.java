/*-
 * Copyright (c) 2012 Red Hat, Inc.
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
package org.fedoraproject.maven;

public class Configuration
{
    public static final String LOCAL_DEPMAP = "depmap.xml";

    /**
     * This file is provided by maven2-common-poms package. Support for this file may be removed in future.
     */
    public static final String VERSIONLESS_DEPMAP = "/etc/maven/maven2-versionless-depmap.xml";

    /**
     * Directories in which Maven will look for depmap fragment files. Fragments used to be stored in /etc, but this was
     * deprecated in favor of /usr. Support for /etc fragments may be removed in future.
     */
    public static final String[] FRAGMENT_DIRS = new String[] { "/etc/maven/fragments", "/usr/share/maven-fragments" };

    /**
     * Directories where Maven looks for non-POM artifacts, in descending precedence order.
     */
    public static final String[] REPOS = new String[] { "/usr/share/maven/repository/",
        "/usr/share/maven/repository-java-jni/", "/usr/share/maven/repository-jni/" };

    /**
     * Directories where Maven looks for POM artifacts, in descending precedence order.
     */
    public static final String[] POM_REPOS = new String[] { "/usr/share/maven2/poms/", "/usr/share/maven/poms/",
        "/usr/share/maven-poms/", "/usr/share/maven2/default_poms/" };

    /**
     * Logger verbosity.
     */
    public static int LOGGER_VERBOSITY = 2;

    public static final String PREFIX = System.getProperty( "maven.local.prefix" );
}
