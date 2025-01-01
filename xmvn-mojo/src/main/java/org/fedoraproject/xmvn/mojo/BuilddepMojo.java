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

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.maven.lifecycle.mapping.Lifecycle;
import org.apache.maven.lifecycle.mapping.LifecycleMapping;
import org.apache.maven.lifecycle.mapping.LifecycleMojo;
import org.apache.maven.lifecycle.mapping.LifecyclePhase;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Mikolaj Izdebski
 */
@Mojo(name = "builddep", aggregator = true, requiresDependencyResolution = ResolutionScope.NONE)
public class BuilddepMojo extends AbstractMojo {
    private static class NamespacedArtifact {
        String namespace;

        Artifact artifact;

        public NamespacedArtifact(String namespace, Artifact artifact) {
            this.namespace = namespace != null ? namespace : "";
            this.artifact = artifact;
        }

        @Override
        public int hashCode() {
            return artifact.hashCode() ^ namespace.hashCode();
        }

        @Override
        public boolean equals(Object rhs) {
            NamespacedArtifact other = (NamespacedArtifact) rhs;
            return namespace.equals(other.namespace) && artifact.equals(other.artifact);
        }
    }

    @Inject private Logger logger;

    @Parameter(defaultValue = "xmvn.builddep.skip")
    private boolean skip;

    @Parameter(defaultValue = "${reactorProjects}", readonly = true, required = true)
    private List<MavenProject> reactorProjects;

    @Inject private Map<String, LifecycleMapping> lifecycleMappings;

    // Injected through reflection by XMvn lifecycle participant
    private List<String[]> resolutions;

    private Set<Artifact> commonDeps = new LinkedHashSet<>();

    public BuilddepMojo() {
        try (InputStream xmlStream =
                ArtifactTypeRegistry.class.getResourceAsStream("/common-deps.xml")) {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(xmlStream);
            NodeList dependencies = doc.getElementsByTagName("dependency");
            for (int i = 0; i < dependencies.getLength(); i++) {
                Element dependency = (Element) dependencies.item(i);
                String groupId = dependency.getAttribute("groupId");
                String artifactId = dependency.getAttribute("artifactId");
                commonDeps.add(new DefaultArtifact(groupId, artifactId));
            }
        } catch (ParserConfigurationException | IOException | SAXException ex) {
            throw new RuntimeException("Couldnt load resource 'common-deps.xml'", ex);
        }
    }

    private boolean isExternalLocation(InputLocation location) {
        return !reactorProjects.stream()
                .map(
                        project ->
                                project.getGroupId()
                                        + ':'
                                        + project.getArtifactId()
                                        + ':'
                                        + project.getVersion())
                .anyMatch(location.getSource().getModelId()::equals);
    }

    private Set<Artifact> getModelDependencies(Model model) {
        BuildDependencyVisitor visitor = new BuildDependencyVisitor(this::isExternalLocation);
        visitor.visitModel(model);
        return visitor.getArtifacts();
    }

    private Lifecycle getDefaultLifecycle(MavenProject project) throws MojoExecutionException {
        LifecycleMapping lifecycleMapping = lifecycleMappings.get(project.getPackaging());
        if (lifecycleMapping == null) {
            throw new MojoExecutionException(
                    "Unable to get lifecycle for project " + project.getId());
        }
        return lifecycleMapping.getLifecycles().get("default");
    }

    private void addLifecycleDependencies(Set<Artifact> artifacts, MavenProject project)
            throws MojoExecutionException {
        Lifecycle defaultLifecycle = getDefaultLifecycle(project);
        if (defaultLifecycle == null) {
            return;
        }

        for (LifecyclePhase phase : defaultLifecycle.getLifecyclePhases().values()) {
            if (phase.getMojos() == null) {
                continue;
            }

            for (LifecycleMojo mojo : phase.getMojos()) {
                // Goal can be in one of three formats (per MojoDescriptorCreator):
                // - (1) groupId:artifactId:version:goal
                // - (2) groupId:artifactId:goal
                // - (3) prefix:goal

                // We don't care about version (currently, plugins can't have compat versions), so
                // we can just parse
                // plugin groupId and artifactId from string in formats (1) and (2), ignoring goals
                // in format (3).
                // Format with prefix is rarely (if ever) used and therefore not supported by XMvn.
                // If needed, support
                // for that format can be implemented with help of PluginPrefixResolver.

                String[] goalCoords = mojo.getGoal().split(":");
                if (goalCoords.length >= 3) {
                    artifacts.add(new DefaultArtifact(goalCoords[0], goalCoords[1]));
                }
            }
        }
    }

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            logger.info("Skipping buiddep: xmvn.builddep.skip property was set");
            return;
        }

        if (resolutions == null) {
            logger.warn("Skipping buiddep: XMvn lifecycle participant is absent");
            return;
        }

        Set<Artifact> artifacts = new LinkedHashSet<>();
        Set<Artifact> lifecycleArtifacts = new LinkedHashSet<>();
        for (MavenProject project : reactorProjects) {
            artifacts.addAll(getModelDependencies(project.getModel()));
            addLifecycleDependencies(lifecycleArtifacts, project);
        }

        artifacts.removeIf(dep -> commonDeps.contains(dep.setVersion(Artifact.DEFAULT_VERSION)));
        lifecycleArtifacts.removeIf(commonDeps::contains);

        Set<NamespacedArtifact> deps = new LinkedHashSet<>();
        for (String[] resolution : resolutions) {
            if (resolution == null) {
                continue;
            }

            Artifact artifact = new DefaultArtifact(resolution[0]);
            Artifact versionlessArtifact = artifact.setVersion(Artifact.DEFAULT_VERSION);
            String compatVersion = resolution[1];
            String namespace = resolution[2];

            if (artifacts.contains(artifact) || lifecycleArtifacts.contains(versionlessArtifact)) {
                deps.add(new NamespacedArtifact(namespace, artifact.setVersion(compatVersion)));
            }
        }

        serializeArtifacts(deps);
    }

    private void addOptionalChild(
            XMLStreamWriter cursor, String tag, String value, String defaultValue)
            throws XMLStreamException {
        if (defaultValue == null || !value.equals(defaultValue)) {
            cursor.writeCharacters("    ");
            cursor.writeStartElement(tag);
            cursor.writeCharacters(value);
            cursor.writeEndElement();
            cursor.writeCharacters("\n");
        }
    }

    private void serializeArtifact(XMLStreamWriter cursor, NamespacedArtifact namespacedArtifact)
            throws XMLStreamException {
        Artifact artifact = namespacedArtifact.artifact;
        cursor.writeCharacters("  ");
        cursor.writeStartElement("dependency");
        cursor.writeCharacters("\n");

        addOptionalChild(cursor, "namespace", namespacedArtifact.namespace, "");
        addOptionalChild(cursor, "groupId", artifact.getGroupId(), null);
        addOptionalChild(cursor, "artifactId", artifact.getArtifactId(), null);
        addOptionalChild(cursor, "extension", artifact.getExtension(), "jar");
        addOptionalChild(cursor, "classifier", artifact.getClassifier(), "");
        addOptionalChild(cursor, "version", artifact.getVersion(), "SYSTEM");

        cursor.writeCharacters("  ");
        cursor.writeEndElement();
        cursor.writeCharacters("\n");
    }

    private void serializeArtifacts(Set<NamespacedArtifact> artifacts)
            throws MojoExecutionException {
        try (Writer writer =
                Files.newBufferedWriter(Path.of(".xmvn-builddep"), StandardCharsets.UTF_8)) {
            XMLStreamWriter cursor = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
            cursor.writeStartDocument();
            cursor.writeCharacters("\n");
            cursor.writeComment(" Build dependencies generated by XMvn ");
            cursor.writeCharacters("\n");
            cursor.writeStartElement("dependencies");
            cursor.writeCharacters("\n");

            for (NamespacedArtifact dependencyArtifact : artifacts) {
                serializeArtifact(cursor, dependencyArtifact);
            }

            cursor.writeEndElement();
            cursor.writeCharacters("\n");
            cursor.writeEndDocument();
        } catch (IOException | XMLStreamException e) {
            throw new MojoExecutionException("Unable to write builddep file", e);
        }
    }
}
