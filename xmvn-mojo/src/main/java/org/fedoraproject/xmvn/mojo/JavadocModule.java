/*-
 * Copyright (c) 2023 Red Hat, Inc.
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
package org.fedoraproject.xmvn.mojo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Mikolaj Izdebski
 */
class JavadocModule
{
    private final String moduleName;

    private final List<Path> sourcePaths;

    private final List<Path> binaryPaths;

    public JavadocModule( String moduleName, Path artifactPath, List<Path> sourcePaths, List<Path> dependencies )
    {
        this.moduleName = moduleName;
        this.sourcePaths = sourcePaths;
        binaryPaths = new ArrayList<>( Collections.singleton( artifactPath ) );
        binaryPaths.addAll( dependencies );
    }

    public String getModuleName()
    {
        return moduleName;
    }

    public boolean isModular()
    {
        return moduleName != null;
    }

    public boolean isNotModular()
    {
        return moduleName == null;
    }

    public List<Path> getSourcePaths()
    {
        return sourcePaths;
    }

    public List<Path> getClassPaths()
    {
        return binaryPaths;
    }
}
