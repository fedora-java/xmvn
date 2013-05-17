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
package org.fedoraproject.maven.installer;

/**
 * @author Mikolaj Izdebski
 */
public class ProjectInstallationException
    extends Exception
{
    private static final long serialVersionUID = 1;

    public ProjectInstallationException()
    {
    }

    public ProjectInstallationException( String message )
    {
        super( message );
    }

    public ProjectInstallationException( Throwable cause )
    {
        super( cause );
    }

    public ProjectInstallationException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public ProjectInstallationException( String message, Throwable cause, boolean enableSuppression,
                                         boolean writableStackTrace )
    {
        super( message, cause, enableSuppression, writableStackTrace );
    }
}
