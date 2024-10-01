/*-
 * Copyright (c) 2016-2024 Red Hat, Inc.
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
package org.apache.maven.plugin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** @author Roman Vais */
public class MavenPluginValidatorTest {
    private MavenPluginValidator validator;

    private PluginDescriptor desc;

    @BeforeEach
    public void setUp() throws Exception {
        validator = new MavenPluginValidator(null);
        desc = EasyMock.createMock(PluginDescriptor.class);
    }

    @Test
    public void testMavenPluginValidator() throws Exception {
        EasyMock.expect(desc.getVersion()).andReturn(null);
        desc.setVersion(EasyMock.anyObject(String.class));
        EasyMock.expectLastCall();
        EasyMock.replay(desc);

        validator.validate(desc);
        EasyMock.verify(desc);

        EasyMock.reset(desc);
        EasyMock.expect(desc.getVersion()).andReturn("SYSTEM");
        EasyMock.replay(desc);
        validator.validate(desc);

        assertFalse(validator.hasErrors());
        assertTrue(validator.getErrors().isEmpty());
    }
}
