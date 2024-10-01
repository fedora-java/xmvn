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
package org.fedoraproject.xmvn.tools.install;

import org.fedoraproject.xmvn.config.PackagingRule;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;

/** @author Mikolaj Izdebski */
public interface ArtifactInstaller {
    String DEFAULT_REPOSITORY_ID = "install";

    default void install(
            JavaPackage targetPackage,
            ArtifactMetadata am,
            PackagingRule rule,
            String basePackageName,
            String repositoryId)
            throws ArtifactInstallationException {
        if (repositoryId.equals(DEFAULT_REPOSITORY_ID)) {
            install(targetPackage, am, rule, basePackageName);
        } else {
            throw new UnsupportedOperationException("This artifact installer does not support non-default repository.");
        }
    }

    @Deprecated
    default void install(JavaPackage targetPackage, ArtifactMetadata am, PackagingRule rule, String basePackageName)
            throws ArtifactInstallationException {
        install(targetPackage, am, rule, basePackageName, DEFAULT_REPOSITORY_ID);
    }

    void postInstallation() throws ArtifactInstallationException;
}
