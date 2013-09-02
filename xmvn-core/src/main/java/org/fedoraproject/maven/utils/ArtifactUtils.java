/*-
 * Copyright (c) 2012-2013 Red Hat, Inc.
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
package org.fedoraproject.maven.utils;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlSerializer;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

/**
 * @author Mikolaj Izdebski
 */
public class ArtifactUtils
{
    public static final String DEFAULT_EXTENSION = "jar";

    public static final String DEFAULT_VERSION = "SYSTEM";

    /**
     * Dummy artifact. Any dependencies on this artifact will be removed during model validation.
     */
    public static final Artifact DUMMY = new DefaultArtifact( "org.fedoraproject.xmvn:xmvn-void:SYSTEM" );

    /**
     * The same as {@code DUMMY}, but in JPP style. Any dependencies on this artifact will be removed during model
     * validation.
     */
    public static final Artifact DUMMY_JPP = new DefaultArtifact( "JPP/maven:empty-dep:SYSTEM" );

    private static final String KEY_SCOPE = "xmvn.artifact.scope";

    private static final String KEY_STEREOTYPE = "xmvn.artifact.stereotype";

    public static String getScope( Artifact artifact )
    {
        return artifact.getProperty( KEY_SCOPE, "" );
    }

    public static Artifact setScope( Artifact artifact, String scope )
    {
        return artifact.setProperties( Collections.singletonMap( KEY_SCOPE, scope ) );
    }

    public static String getStereotype( Artifact artifact )
    {
        return artifact.getProperty( KEY_STEREOTYPE, "" );
    }

    public static Artifact setStereotype( Artifact artifact, String stereotype )
    {
        return artifact.setProperties( Collections.singletonMap( KEY_STEREOTYPE, stereotype ) );
    }

    /**
     * Convert a collection of artifacts to a human-readable string. This function uses single-line representation.
     * 
     * @param collection collection of artifacts
     * @return string representation of given collection of artifacts
     */
    public static String collectionToString( Collection<Artifact> set )
    {
        return collectionToString( set, false );
    }

    /**
     * Convert a collection of artifacts to a human-readable string.
     * 
     * @param collection collection of artifacts
     * @param multiLine if multi-line representation should be used instead of single-line
     * @return string representation of given collection of artifacts
     */
    public static String collectionToString( Collection<Artifact> collection, boolean multiLine )
    {
        if ( collection.isEmpty() )
            return "[]";

        String separator = multiLine ? System.lineSeparator() : " ";
        String indent = multiLine ? "  " : "";

        StringBuilder sb = new StringBuilder();
        sb.append( "[" + separator );

        Iterator<Artifact> iter = collection.iterator();
        sb.append( indent + iter.next() );

        while ( iter.hasNext() )
        {
            sb.append( "," + separator );
            sb.append( indent + iter.next() );
        }

        sb.append( separator + "]" );
        return sb.toString();
    }

    private static void addOptionalChild( Xpp3Dom parent, String tag, String value, String defaultValue )
    {
        if ( defaultValue == null || !value.equals( defaultValue ) )
        {
            Xpp3Dom child = new Xpp3Dom( tag );
            child.setValue( value );
            parent.addChild( child );
        }
    }

    public static Xpp3Dom toXpp3Dom( Artifact artifact, String tag )
    {
        Xpp3Dom parent = new Xpp3Dom( tag );

        addOptionalChild( parent, "namespace", ArtifactUtils.getScope( artifact ), "" );
        addOptionalChild( parent, "groupId", artifact.getGroupId(), null );
        addOptionalChild( parent, "artifactId", artifact.getArtifactId(), null );
        addOptionalChild( parent, "extension", artifact.getExtension(), "jar" );
        addOptionalChild( parent, "classifier", artifact.getClassifier(), "" );
        addOptionalChild( parent, "version", artifact.getVersion(), "SYSTEM" );

        return parent;
    }

    public static void serialize( Artifact artifact, XmlSerializer serializer, String namespace, String tag )
        throws IOException
    {
        Xpp3Dom dom = toXpp3Dom( artifact, tag );
        dom.writeToSerializer( namespace, serializer );

    }
}
