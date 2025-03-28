/*-
 * Copyright (c) 2014-2025 Red Hat, Inc.
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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.fedoraproject.xmvn.tools.install.Package;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlunit.assertj3.XmlAssert;

/**
 * @author Mikolaj Izdebski
 */
public abstract class AbstractInstallerTest {
    protected Path workdir;
    protected Path installRoot;
    protected Path descriptorRoot;
    protected final List<String> descriptors = new ArrayList<>();

    @BeforeEach
    public void setUpWorkdir(TestInfo testInfo) throws IOException {
        Path workPath = Path.of("target").resolve("test-work");
        workdir = workPath.resolve(testInfo.getTestMethod().get().getName()).toAbsolutePath();
        delete(workdir);
        Files.createDirectories(workdir);
        installRoot = workdir.resolve("install-root");
        Files.createDirectory(installRoot);
        descriptorRoot = workdir.resolve("descriptor-root");
        Files.createDirectory(descriptorRoot);
    }

    private void delete(Path path) throws IOException {
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
                for (Path child : ds) {
                    delete(child);
                }
            }
        }

        Files.deleteIfExists(path);
    }

    protected void assertDirectoryStructure(String... expected) throws Exception {
        assertDirectoryStructure(installRoot, expected);
    }

    protected void assertDirectoryStructure(Path root, String... expected) throws Exception {
        List<String> actualList = new ArrayList<>();
        Files.walkFileTree(root, new FileSystemWalker(root, actualList));
        assertThat(actualList).containsExactlyInAnyOrderElementsOf(Arrays.asList(expected));
    }

    protected void assertDescriptorEquals(String... expected) {
        assertThat(descriptors).containsExactlyInAnyOrder(expected);
    }

    protected Path getResource(String name) {
        return Path.of("src/test/resources/", name).toAbsolutePath();
    }

    protected void assertDescriptorEquals(Path mfiles, String... expected) throws IOException {
        List<String> lines = Files.readAllLines(mfiles, Charset.defaultCharset());

        assertThat(lines).containsExactlyInAnyOrderElementsOf(Arrays.asList(expected));
    }

    protected void assertDescriptorEquals(Package pkg, String... expected) throws IOException {
        Path mfiles = descriptorRoot.resolve(".mfiles");
        pkg.writeDescriptor(mfiles);
        assertDescriptorEquals(mfiles, expected);
    }

    protected void assertMetadataEqual(Path expected, Path actual) throws Exception {
        assertThat(actual).isRegularFile();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document expectedXml = builder.parse(expected.toString());
        Document actualXml = builder.parse(actual.toString());

        NodeList nodes = expectedXml.getElementsByTagName("path");

        for (int i = 0; i < nodes.getLength(); i++) {
            Node pathNode = nodes.item(i);
            String path = pathNode.getTextContent();
            if (path.startsWith("???")) {
                pathNode.setTextContent(getResource(path.substring(3)).toAbsolutePath().toString());
            }
        }

        XmlAssert.assertThat(expectedXml)
                .and(actualXml)
                .ignoreComments()
                .ignoreWhitespace()
                .areSimilar();
    }
}
