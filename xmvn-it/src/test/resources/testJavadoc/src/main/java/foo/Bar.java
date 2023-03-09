/*-
 * Copyright (c) 2016-2021 Red Hat, Inc.
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
package foo;

import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.apache.maven.project.DefaultMavenProjectBuilder;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.xmlpull.mxp1.MXParser;

/**
 * @author Mikolaj Izdebski
 */
public class Bar
{
    /**
     * pubDesc
     * 
     * @throws Exception
     */
    public void pubMethod()
        throws Exception
    {
        MXParser mxp = new MXParser();
        mxp.setInput( new CpioArchiveInputStream( System.in ), null );
    }

    /** protDesc 
     * @return 
     * @throws ProjectBuildingException */
    @Deprecated
    protected MavenProject protMethod() throws ProjectBuildingException
    {
        MavenProjectBuilder projectBuilder=new DefaultMavenProjectBuilder();
        return projectBuilder.build( null, null );
    }

    /** defDesc */
    void defMethod()
    {
        privMethod();
    }

    /** privDesc */
    private void privMethod()
    {
    }
}
