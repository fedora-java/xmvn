/*-
 * Copyright (c) 2014-2024 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.install.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.PackageMetadata;

/**
 * @author Michael Simacek
 */
public final class InstallationPlanLoader {
    private InstallationPlanLoader() {
        // Don't generate default public constructor
    }

    public static Path prepareInstallationPlanFile(String filename) throws Exception {
        Path metadataPath = Path.of("src/test/resources/", filename);
        PackageMetadata metadata = PackageMetadata.readFromXML(metadataPath);
        for (ArtifactMetadata artifact : metadata.getArtifacts()) {
            String path = artifact.getPath();
            if (path != null) {
                path = Path.of(path).toAbsolutePath().toString();
                artifact.setPath(path);
            }
        }
        Path newMetadata = Files.createTempFile(filename, "");
        metadata.writeToXML(newMetadata);
        return newMetadata;
    }

    public static InstallationPlan createInstallationPlan(String filename) throws Exception {
        return new InstallationPlan(prepareInstallationPlanFile(filename));
    }
}
