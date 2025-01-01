/*-
 * Copyright (c) 2013-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.deployer.impl;

import io.kojan.xml.XMLException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Named;
import javax.inject.Singleton;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.deployer.DependencyDescriptor;
import org.fedoraproject.xmvn.deployer.Deployer;
import org.fedoraproject.xmvn.deployer.DeploymentRequest;
import org.fedoraproject.xmvn.deployer.DeploymentResult;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.Dependency;
import org.fedoraproject.xmvn.metadata.DependencyExclusion;
import org.fedoraproject.xmvn.metadata.PackageMetadata;

/**
 * Default implementation of XMvn {@code Deployer} interface.
 *
 * <p><strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is
 * marked as public only for technical reasons. This class is not part of XMvn API. Client code
 * using XMvn should <strong>not</strong> reference it directly.
 *
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class DefaultDeployer implements Deployer {
    @Override
    public DeploymentResult deploy(DeploymentRequest request) {
        DefaultDeploymentResult result = new DefaultDeploymentResult();

        try {
            PackageMetadata plan = readInstallationPlan(request.getPlanPath());

            ArtifactMetadata am = new ArtifactMetadata();
            plan.addArtifact(am);

            Artifact artifact = request.getArtifact();
            am.setGroupId(artifact.getGroupId());
            am.setArtifactId(artifact.getArtifactId());
            am.setExtension(artifact.getExtension());
            am.setClassifier(artifact.getClassifier());
            am.setVersion(artifact.getVersion());
            am.setPath(artifact.getPath().toString());
            am.getProperties().putAll(request.getProperties());

            for (DependencyDescriptor depDescriptor : request.getDependencies()) {
                Dependency dependency = new Dependency();
                am.addDependency(dependency);

                Artifact dependencyArtifact = depDescriptor.getDependencyArtifact();
                dependency.setGroupId(dependencyArtifact.getGroupId());
                dependency.setArtifactId(dependencyArtifact.getArtifactId());
                dependency.setExtension(dependencyArtifact.getExtension());
                dependency.setClassifier(dependencyArtifact.getClassifier());
                dependency.setRequestedVersion(dependencyArtifact.getVersion());
                if (depDescriptor.isOptional()) {
                    dependency.setOptional(true);
                }

                for (Artifact exclusionArtifact : depDescriptor.getExclusions()) {
                    DependencyExclusion exclusion = new DependencyExclusion();
                    dependency.addExclusion(exclusion);

                    exclusion.setGroupId(exclusionArtifact.getGroupId());
                    exclusion.setArtifactId(exclusionArtifact.getArtifactId());
                }
            }

            writeInstallationPlan(plan, request.getPlanPath());
        } catch (Exception e) {
            result.setException(e);
        }

        return result;
    }

    private PackageMetadata readInstallationPlan(Path planPath) throws IOException {
        if (!Files.exists(planPath)) {
            return new PackageMetadata();
        }

        try {
            return PackageMetadata.readFromXML(planPath);
        } catch (XMLException e) {
            throw new IOException("Failed to parse reactor installation plan", e);
        }
    }

    private void writeInstallationPlan(PackageMetadata plan, Path planPath) throws IOException {
        try {
            plan.writeToXML(planPath);
        } catch (XMLException e) {
            throw new IOException("Unable to write reactor installation plan", e);
        }
    }
}
