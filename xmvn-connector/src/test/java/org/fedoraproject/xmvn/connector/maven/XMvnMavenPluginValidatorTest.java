/*-
 * Copyright (c) 2016-2025 Red Hat, Inc.
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
package org.fedoraproject.xmvn.connector.maven;

import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Roman Vais
 */
public class XMvnMavenPluginValidatorTest {
    private XMvnMavenPluginValidator validator;

    private Artifact art;
    private PluginDescriptor desc;
    private List<String> err;

    @BeforeEach
    public void setUp() throws Exception {
        validator = new XMvnMavenPluginValidator();
        art = EasyMock.createMock(Artifact.class);
        desc = EasyMock.createMock(PluginDescriptor.class);
        err = EasyMock.createMock(List.class);
    }

    @Test
    public void testMavenPluginValidator() throws Exception {
        EasyMock.expect(desc.getVersion()).andReturn(null);
        desc.setVersion(EasyMock.anyObject(String.class));
        EasyMock.expectLastCall();
        EasyMock.replay(art, desc, err);

        validator.validate(art, desc, err);
        EasyMock.verify(art, desc, err);

        EasyMock.reset(art, desc, err);
        EasyMock.expect(desc.getVersion()).andReturn("SYSTEM");
        EasyMock.replay(art, desc, err);
        validator.validate(art, desc, err);
        EasyMock.verify(art, desc, err);
    }
}
