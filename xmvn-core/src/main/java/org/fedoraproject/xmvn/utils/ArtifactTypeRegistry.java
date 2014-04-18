/*-
 * Copyright (c) 2012-2014 Red Hat, Inc.
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
package org.fedoraproject.xmvn.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Mikolaj Izdebski
 */
class ArtifactTypeRegistry
{
    private static final Map<String, String> EXTENSIONS = new LinkedHashMap<>();

    private static final Map<String, String> CLASSIFIERS = new LinkedHashMap<>();

    private static void addStereotype( String type, String extension, String classifier )
    {
        EXTENSIONS.put( type, extension );
        CLASSIFIERS.put( type, classifier );
    }

    // The list was taken from MavenRepositorySystemUtils in maven-aether-provider.
    static
    {
        addStereotype( "maven-plugin", "jar", "" );
        addStereotype( "ejb", "jar", "" );
        addStereotype( "ejb-client", "jar", "client" );
        addStereotype( "test-jar", "jar", "tests" );
        addStereotype( "javadoc", "jar", "javadoc" );
        addStereotype( "java-source", "jar", "sources" );
    }

    public static boolean isRegisteredType( String type )
    {
        return EXTENSIONS.get( type ) != null;
    }

    public static String getExtension( String type )
    {
        return EXTENSIONS.get( type );
    }

    public static String getClassifier( String type )
    {
        return CLASSIFIERS.get( type );
    }
}
