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

import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.config.PackagingRule;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.tools.install.ArtifactInstaller;
import org.fedoraproject.xmvn.tools.install.JavaPackage;

/**
 * @author Mikolaj Izdebski
 */
class ArtifactState {
    private final Artifact artifact;

    private final ArtifactMetadata metadata;

    private JavaPackage targetPackage;

    private PackagingRule packagingRule;

    private ArtifactInstaller installer;

    public ArtifactState(Artifact artifact, ArtifactMetadata metadata) {
        this.artifact = artifact;
        this.metadata = metadata;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public ArtifactMetadata getMetadata() {
        return metadata;
    }

    public JavaPackage getTargetPackage() {
        return targetPackage;
    }

    public void setTargetPackage(JavaPackage targetPackage) {
        this.targetPackage = targetPackage;
    }

    public PackagingRule getPackagingRule() {
        return packagingRule;
    }

    public void setPackagingRule(PackagingRule packagingRule) {
        this.packagingRule = packagingRule;
    }

    public ArtifactInstaller getInstaller() {
        return installer;
    }

    public void setInstaller(ArtifactInstaller installer) {
        this.installer = installer;
    }

    @Override
    public int hashCode() {
        return artifact.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null
                && getClass() == obj.getClass()
                && artifact.equals(((ArtifactState) obj).artifact);
    }
}
