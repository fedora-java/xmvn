/*-
 * Copyright (c) 2012-2013 Red Hat, Inc.
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
package org.fedoraproject.maven.config;

import java.math.BigDecimal;

//FIXME: get rid of this class
public class ConfigurationXXX
{
    private static Configuration c;

    public static Configuration getConfiguration()
    {
        return c;
    }

    private static String compilerSource = null;

    public static BigDecimal getCompilerSource()
    {
        String source = isCompilerSourceSpecified() ? compilerSource : "1.5";
        return new BigDecimal( source );
    }

    public static boolean isCompilerSourceSpecified()
    {
        return compilerSource != null;
    }
}
