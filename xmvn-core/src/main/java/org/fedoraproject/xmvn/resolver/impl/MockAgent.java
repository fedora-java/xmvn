/*-
 * Copyright (c) 2015-2024 Red Hat, Inc.
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
package org.fedoraproject.xmvn.resolver.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.logging.Logger;

/** @author Mikolaj Izdebski */
class MockAgent {
    private final Logger logger;
    private final Path socketPath;

    public MockAgent(Logger logger) {
        this(logger, System.getenv("PM_REQUEST_SOCKET"));
    }

    MockAgent(Logger logger, String socketPath) {
        this.logger = logger;
        this.socketPath = socketPath != null ? Path.of(socketPath) : null;
    }

    private String formatDep(Artifact art, String pkgver, String ns) {
        boolean cusExt = !art.getExtension().equals(Artifact.DEFAULT_EXTENSION);
        boolean cusCla = !art.getClassifier().equals("");
        boolean cusVer = !art.getVersion().equals(Artifact.DEFAULT_VERSION);
        StringBuilder sb = new StringBuilder();
        if (ns != null && !ns.isBlank()) {
            sb.append(ns);
            sb.append("-");
        }
        sb.append("mvn(");
        sb.append(art.getGroupId());
        sb.append(":");
        sb.append(art.getArtifactId());
        if (cusCla || cusExt) {
            sb.append(":");
        }
        if (cusExt) {
            sb.append(art.getExtension());
        }
        if (cusCla) {
            sb.append(":");
            sb.append(art.getClassifier());
        }
        if (cusCla || cusExt || cusVer) {
            sb.append(":");
        }
        if (cusVer) {
            sb.append(art.getVersion());
        }
        sb.append(")");
        if (pkgver != null) {
            sb.append(" = ");
            sb.append(pkgver);
        }
        return sb.toString();
    }

    private void send(SocketChannel ch, ByteBuffer buf) throws IOException {
        while (buf.remaining() > 0) {
            ch.write(buf);
        }
    }

    private ByteBuffer recv(SocketChannel ch, int n) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(n);
        while (buf.remaining() > 0 && ch.read(buf) >= 0) {}
        buf.flip();
        return buf;
    }

    private boolean parseResponse(ByteBuffer resp) {
        if (resp.limit() >= 3 && resp.get() == 'o' && resp.get() == 'k' && resp.get() == '\n') {
            logger.info("Artifact was successfully installed");
            return true;
        }
        logger.info("Artifact was not installed");
        return false;
    }

    private boolean sendCommand(String command) {
        if (socketPath == null) {
            return false;
        }
        logger.debug("Trying to install artifact with mock PM command: {}", command);
        UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(socketPath);
        try (SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX)) {
            channel.connect(socketAddress);
            send(channel, ByteBuffer.wrap(command.getBytes(StandardCharsets.UTF_8)));
            return parseResponse(recv(channel, 3));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean tryInstallArtifact(Artifact artifact) {
        String dependency = formatDep(artifact, null, null);
        String command = "install %s\n".formatted(dependency);
        return sendCommand(command);
    }
}
