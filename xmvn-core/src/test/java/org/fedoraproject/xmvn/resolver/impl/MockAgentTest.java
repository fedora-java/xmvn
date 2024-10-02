/*-
 * Copyright (c) 2024 Red Hat, Inc.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.easymock.EasyMock;
import org.fedoraproject.xmvn.artifact.Artifact;
import org.fedoraproject.xmvn.artifact.DefaultArtifact;
import org.fedoraproject.xmvn.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** @author Mikolaj Izdebski */
public class MockAgentTest {
    @TempDir
    private Path tempDir;

    private volatile Throwable tt;

    private Logger logger;

    private Thread listenerThread;

    private Path socketPath;
    private SocketAddress socketAddress;

    @BeforeEach
    public void setUp() {
        logger = EasyMock.createNiceMock(Logger.class);
        socketPath = tempDir.resolve("sock");
        socketAddress = UnixDomainSocketAddress.of(socketPath);
    }

    private String recvRequest(SocketChannel channel) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ByteBuffer buf = ByteBuffer.allocate(1);
            while (channel.read(buf) == 1) {
                buf.flip();
                byte ch = buf.get();
                bos.write(ch);
                if (ch == '\n') break;
                buf.rewind();
            }
            return bos.toString(StandardCharsets.UTF_8);
        }
    }

    private void sendResponse(SocketChannel channel, String response) throws IOException {
        ByteBuffer b = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));
        while (b.remaining() > 0) {
            channel.write(b);
        }
    }

    private void startSocketListener(String expectedRequest, String response) throws Throwable {
        CountDownLatch latch = new CountDownLatch(1);
        listenerThread = new Thread(() -> {
            try (ServerSocketChannel serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX)) {
                serverChannel.bind(socketAddress);
                latch.countDown();
                try (SocketChannel channel = serverChannel.accept()) {
                    String request = recvRequest(channel);
                    assertEquals(expectedRequest, request);
                    sendResponse(channel, response);
                }
            } catch (Throwable t) {
                tt = t;
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
        // Wait until the thread binds the socket
        latch.await(1000, TimeUnit.MILLISECONDS);
        if (tt != null) {
            throw tt;
        }
        assertTrue(Files.exists(socketPath));
        assertFalse(Files.isRegularFile(socketPath));
        assertFalse(Files.isDirectory(socketPath));
    }

    private void joinSocketListener() throws Throwable {
        listenerThread.join();
        if (tt != null) {
            throw tt;
        }
    }

    private void performTest(String coords, String expectedRequest, String response, boolean outcome) throws Throwable {
        Artifact artifact = new DefaultArtifact(coords);
        startSocketListener(expectedRequest, response);

        EasyMock.replay(logger);

        MockAgent agent = new MockAgent(logger, socketPath.toString());
        boolean ret = agent.tryInstallArtifact(artifact);
        assertEquals(outcome, ret);

        EasyMock.verify(logger);

        joinSocketListener();
    }

    @Test
    public void testNoSocket() throws Exception {
        MockAgent agent = new MockAgent(logger, null);
        boolean ret = agent.tryInstallArtifact(new DefaultArtifact("foo:bar"));
        assertEquals(false, ret);
    }

    @Test
    public void testMissingSocket() throws Exception {
        try {
            MockAgent agent = new MockAgent(logger, tempDir.resolve("dummy").toString());
            agent.tryInstallArtifact(new DefaultArtifact("foo:bar"));
            fail("Expected SocketException");
        } catch (UncheckedIOException e) {
            assertInstanceOf(SocketException.class, e.getCause());
        }
    }

    @Test
    public void testSimple() throws Throwable {
        performTest("foo:bar", "install mvn(foo:bar)\n", "ok\n", true);
    }

    @Test
    public void testCompatVersion() throws Throwable {
        performTest("foo:bar:1.2.3", "install mvn(foo:bar:1.2.3)\n", "ok\n", true);
    }

    @Test
    public void testClassifier() throws Throwable {
        performTest("foo:bar::no_aop:", "install mvn(foo:bar::no_aop:)\n", "ok\n", true);
    }

    @Test
    public void testNOK() throws Throwable {
        performTest("foo:bar:pom:", "install mvn(foo:bar:pom:)\n", "nok\n", false);
    }

    @Test
    public void testProtocolError() throws Throwable {
        performTest("foo:bar", "install mvn(foo:bar)\n", "BOOM", false);
    }

    @Test
    public void testNoResponse() throws Throwable {
        performTest("foo:bar", "install mvn(foo:bar)\n", "", false);
    }
}
