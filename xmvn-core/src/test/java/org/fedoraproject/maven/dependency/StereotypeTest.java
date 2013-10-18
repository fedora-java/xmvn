/*-
 * Copyright (c) 2013 Red Hat, Inc.
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
package org.fedoraproject.maven.dependency;

/**
 * @author Mikolaj Izdebski
 */
public class StereotypeTest
    extends AbstractDependencyTest
{
    public StereotypeTest()
        throws Exception
    {
        super( "stereotype.xml" );
    }

    @Override
    public void configureBuild()
        throws Exception
    {
        expect( "dep.group:dep.artifact:jar:tests:dep.version" );
    }

    @Override
    public void configureRuntime()
        throws Exception
    {
        expect( "dep.group:dep.artifact:jar:tests:dep.version" );
    }
}
