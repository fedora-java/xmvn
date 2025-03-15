/*-
 * Copyright (c) 2012-2025 Red Hat, Inc.
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

import io.kojan.xml.XMLException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import javax.inject.Inject;
import org.apache.maven.api.DependencyCoordinates;
import org.apache.maven.api.DependencyScope;
import org.apache.maven.api.Exclusion;
import org.apache.maven.api.ProducedArtifact;
import org.apache.maven.api.Project;
import org.apache.maven.api.Session;
import org.apache.maven.api.model.Model;
import org.apache.maven.api.services.ArtifactManager;
import org.apache.maven.api.services.DependencyCoordinatesFactory;
import org.apache.maven.api.services.ProjectManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.fedoraproject.xmvn.logging.Logger;
import org.fedoraproject.xmvn.metadata.ArtifactMetadata;
import org.fedoraproject.xmvn.metadata.Dependency;
import org.fedoraproject.xmvn.metadata.DependencyExclusion;
import org.fedoraproject.xmvn.metadata.PackageMetadata;

/**
 * @author Mikolaj Izdebski
 */
@Mojo(name = "install", aggregator = true, requiresDependencyResolution = ResolutionScope.NONE)
public class InstallMojo extends AbstractMojo {

    private final Logger logger;
    private final Session session;
    private final ProjectManager projectManager;
    private final ArtifactManager artifactManager;
    private final DependencyCoordinatesFactory dependencyCoordsFactory;

    @Inject
    public InstallMojo(
            Logger logger,
            Session session,
            ProjectManager projectManager,
            ArtifactManager artifactManager,
            DependencyCoordinatesFactory dependencyCoordinatesFactory) {
        this.logger = logger;
        this.session = session;
        this.projectManager = projectManager;
        this.artifactManager = artifactManager;
        this.dependencyCoordsFactory = dependencyCoordinatesFactory;
    }

    private PackageMetadata readInstallationPlan(Path planPath) throws MojoExecutionException {
        if (!Files.exists(planPath)) {
            return new PackageMetadata();
        }

        try {
            return PackageMetadata.readFromXML(planPath);
        } catch (XMLException | IOException e) {
            throw new MojoExecutionException(
                    "Failed to read reactor installation plan " + planPath, e);
        }
    }

    private void writeInstallationPlan(PackageMetadata plan, Path planPath)
            throws MojoExecutionException {
        try {
            plan.writeToXML(planPath);
        } catch (XMLException | IOException e) {
            throw new MojoExecutionException(
                    "Unable to write reactor installation plan " + planPath, e);
        }
    }

    /**
     * Dump project dependencies with "system" scope and fail if there are any such dependencies are
     * found.
     */
    private void handleSystemDependencies() throws MojoFailureException {
        boolean systemDepsFound = false;

        for (Project project : session.getProjects()) {
            for (org.apache.maven.api.model.Dependency dependency :
                    project.getModel().getDependencies()) {
                if ("system".equals(dependency.getScope())) {
                    logger.error(
                            "Reactor project {}:{} has system-scoped dependency: {}:{}",
                            project.getGroupId(),
                            project.getArtifactId(),
                            dependency.getGroupId(),
                            dependency.getArtifactId());
                    systemDepsFound = true;
                }
            }
        }

        if (systemDepsFound) {
            throw new MojoFailureException(
                    "Some reactor artifacts have dependencies with scope \"system\"."
                            + " Such dependencies are not supported by XMvn installer."
                            + " You should either remove any dependencies with scope \"system\""
                            + " before the build or not run XMvn instaler.");
        }
    }

    private void addArtifactToPlan(
            PackageMetadata plan,
            ProducedArtifact mavenArtifact,
            Path artifactPath,
            String type,
            Model model)
            throws MojoExecutionException {

        ArtifactMetadata artifact = new ArtifactMetadata();
        artifact.setGroupId(mavenArtifact.getGroupId());
        artifact.setArtifactId(mavenArtifact.getArtifactId());
        artifact.setExtension(mavenArtifact.getExtension());
        artifact.setClassifier(mavenArtifact.getClassifier());
        artifact.setVersion(mavenArtifact.getVersion().asString());
        artifact.setPath(artifactPath.toString());
        artifact.getProperties().put("type", type);

        for (var mavenDependency : model.getDependencies()) {
            DependencyCoordinates coords = dependencyCoordsFactory.create(session, mavenDependency);
            DependencyScope scope = coords.getScope();
            if (coords.getScope().is(DependencyScope.UNDEFINED.id())) {
                scope = DependencyScope.COMPILE;
            }
            if (scope.isTransitive()) {
                Dependency dependency = new Dependency();
                dependency.setGroupId(coords.getGroupId());
                dependency.setArtifactId(coords.getArtifactId());
                dependency.setExtension(coords.getExtension());
                dependency.setClassifier(coords.getClassifier());
                dependency.setRequestedVersion(coords.getVersionConstraint().asString());
                dependency.setOptional(coords.getOptional());

                for (Exclusion mavenExclusion : coords.getExclusions()) {
                    DependencyExclusion exclusion = new DependencyExclusion();
                    exclusion.setGroupId(mavenExclusion.getGroupId());
                    exclusion.setArtifactId(mavenExclusion.getArtifactId());
                    dependency.addExclusion(exclusion);
                }

                artifact.addDependency(dependency);
            }
        }

        plan.addArtifact(artifact);
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        handleSystemDependencies();

        Path planPath = Path.of(".xmvn-reactor");
        PackageMetadata plan = readInstallationPlan(planPath);

        Path storageDir = Path.of(".xmvn").resolve("install-repo");
        InstallationPlanStorage storage = new InstallationPlanStorage(storageDir);

        for (Project project : session.getProjects()) {

            String type = project.getPackaging().type().id();

            for (ProducedArtifact artifact : projectManager.getAllArtifacts(project)) {
                logger.debug("Trying to install artifact {}", artifact);
                Optional<Path> optionalPath = artifactManager.getPath(artifact);
                if (optionalPath.isEmpty()) {
                    logger.warn(
                            "Skipped installation of artifact {}: artifact was not produced yet",
                            artifact);
                    continue;
                }
                Path path = optionalPath.get();
                logger.debug("Artifact file was resolved to {}", path);
                if (!Files.isRegularFile(path)) {
                    logger.warn(
                            "Skipped installation of artifact {}: artifact file {} is not a regular file",
                            artifact,
                            path);
                    continue;
                }

                Path persistentPath = storage.persistArtifact(path);

                addArtifactToPlan(plan, artifact, persistentPath, type, project.getModel());
            }
        }

        writeInstallationPlan(plan, planPath);
    }
}
