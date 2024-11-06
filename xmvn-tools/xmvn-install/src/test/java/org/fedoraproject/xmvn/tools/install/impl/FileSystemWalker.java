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

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * A file visitor which collects basic information about each file it visits.
 *
 * @author Mikolaj Izdebski
 */
class FileSystemWalker implements FileVisitor<Path> {
    private final Path root;

    private final List<String> lines;

    public FileSystemWalker(Path root, List<String> lines) {
        this.root = root;
        this.lines = lines;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attribs)
            throws IOException {
        if (!path.equals(root)) {
            lines.add("D /" + root.relativize(path));
        }

        return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attribs) {
        if (attribs.isRegularFile()) {
            lines.add("F /" + root.relativize(path));
        } else if (attribs.isSymbolicLink()) {
            lines.add("L /" + root.relativize(path));
        } else {
            lines.add("? /" + root.relativize(path));
        }

        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
        throw e;
    }
}
