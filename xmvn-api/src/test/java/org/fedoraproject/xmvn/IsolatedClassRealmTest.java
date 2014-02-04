/*-
 * Copyright (c) 2014 Red Hat, Inc.
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
package org.fedoraproject.xmvn;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Mikolaj Izdebski
 */
public class IsolatedClassRealmTest
{
    @Test
    public void testImports()
        throws Exception
    {
        try (IsolatedClassRealm realm = new IsolatedClassRealm( null ))
        {
            realm.importPackage( "java.lang" );
            realm.importPackage( "junit" );
            realm.importPackage( "org.eclipse.sisu.space" );

            assertTrue( realm.isImported( "java.lang.Object" ) );
            assertTrue( realm.isImported( "java/lang/Object" ) );
            assertTrue( realm.isImported( "java/lang/Object.class" ) );

            assertFalse( realm.isImported( "java.math.Random" ) );
            assertFalse( realm.isImported( "java/math/Random" ) );
            assertFalse( realm.isImported( "java/math/Random.class" ) );

            assertTrue( realm.isImported( "junit.Assert" ) );
            assertFalse( realm.isImported( "junit" ) );
            assertFalse( realm.isImported( "org.junit.Assert" ) );

            assertTrue( realm.isImported( "org.eclipse.sisu.space.ClassSpace" ) );
            assertFalse( realm.isImported( "org.eclipse.sisu.space.asm.ClassVisitor" ) );
        }
    }
}
