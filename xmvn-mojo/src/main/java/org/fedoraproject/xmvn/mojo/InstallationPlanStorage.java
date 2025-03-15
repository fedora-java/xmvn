/*-
 * Copyright (c) 2015-2025 Red Hat, Inc.
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
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * @author Mikolaj Izdebski
 */
class InstallationPlanStorage {
    private static final String DIGEST_ALGORITHM = "SHA-256";

    private final Path storageDir;

    public InstallationPlanStorage(Path storageDir) {
        this.storageDir = storageDir.toAbsolutePath();
    }

    private String hash(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM);
            byte[] digest = md.digest(bytes);
            return new BigInteger(1, digest)
                    .setBit(digest.length << 3)
                    .toString(16)
                    .substring(1)
                    .toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(
                    "Digest algorithm " + DIGEST_ALGORITHM + " is not available", e);
        }
    }

    public Path persistArtifact(Path artifactPath) throws MojoExecutionException {
        try {
            byte[] bytes = Files.readAllBytes(artifactPath);
            String hash = hash(bytes);

            Path persistDir = storageDir.resolve(artifactPath.getFileName()).resolve(hash);
            Files.createDirectories(persistDir);

            Path persistedPath = persistDir.resolve(artifactPath.getFileName());
            Files.write(persistedPath, bytes);
            return persistedPath;
        } catch (IOException e) {
            throw new MojoExecutionException(e);
        }
    }
}
