/*-
 * Copyright (c) 2023-2024 Red Hat, Inc.
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
import java.util.List;
import java.util.Set;

/**
 * @author Mikolaj Izdebski
 */
class JavadocModule {
    private String moduleName;

    private boolean isAutomatic;

    private final Path artifactPath;

    private final List<Path> sourcePaths;

    private final List<Path> binaryPaths;

    public JavadocModule(
            String moduleName,
            boolean isAutomatic,
            Path artifactPath,
            List<Path> sourcePaths,
            List<Path> dependencies) {
        this.moduleName = moduleName;
        this.isAutomatic = isAutomatic;
        this.artifactPath = artifactPath;
        this.sourcePaths = sourcePaths;
        binaryPaths = new ArrayList<>(Set.of(artifactPath));
        binaryPaths.addAll(dependencies);
    }

    public String getModuleName() {
        return moduleName;
    }

    public boolean isModular() {
        return moduleName != null;
    }

    public boolean isNotModular() {
        return moduleName == null;
    }

    public boolean isAutomatic() {
        return isAutomatic;
    }

    public List<Path> getSourcePaths() {
        return sourcePaths;
    }

    public List<Path> getClassPaths() {
        return binaryPaths;
    }

    public JavadocModule demodularize() {
        return new JavadocModule(null, false, artifactPath, sourcePaths, binaryPaths);
    }

    @Override
    public String toString() {
        String name = moduleName != null ? moduleName : "UNNAMED";
        String automatic = isAutomatic ? "automatic" : "non-automatic";
        return automatic + " module " + name + " at " + artifactPath;
    }
}
