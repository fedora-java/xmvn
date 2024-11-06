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
package org.fedoraproject.xmvn.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;
import org.fedoraproject.xmvn.metadata.io.stax.MetadataStaxReader;
import org.fedoraproject.xmvn.metadata.io.stax.MetadataStaxWriter;
import org.junit.jupiter.api.Test;

/**
 * @author Mikolaj Izdebski
 */
public class MetadataTest {
    private MetadataStaxReader reader = new MetadataStaxReader();

    private MetadataStaxWriter writer = new MetadataStaxWriter();

    @Test
    void testMetadataWithUuid() throws Exception {
        PackageMetadata md = reader.read("src/test/resources/metadata1.xml");
        assertEquals(2, md.getArtifacts().size());
        assertEquals("7c399c04-8621-4b4a-9c3b-5514399b228f", md.getUuid());
        assertEquals("0ddf2120-12f0-42fe-8810-9a888889aa36", md.getArtifacts().get(0).getUuid());

        StringWriter sw = new StringWriter();
        writer.write(sw, md);
        assertTrue(sw.toString().contains("<uuid>7c399c04-8621-4b4a-9c3b-5514399b228f</uuid>"));
    }

    @Test
    void testMetadataWithoutUuid() throws Exception {
        PackageMetadata md = reader.read("src/test/resources/metadata2.xml");
        assertEquals(2, md.getArtifacts().size());
        assertEquals("", md.getUuid());
        assertEquals("", md.getArtifacts().get(0).getUuid());

        StringWriter sw = new StringWriter();
        writer.write(sw, md);
        assertFalse(sw.toString().contains("uuid"));
    }
}
