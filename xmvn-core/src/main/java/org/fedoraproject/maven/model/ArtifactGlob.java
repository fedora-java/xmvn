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
package org.fedoraproject.maven.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fedoraproject.maven.utils.GlobUtils;
import org.fedoraproject.maven.utils.StringSplitter;

public class ArtifactGlob
{
    private final Pattern groupIdPattern;

    private final Pattern artifactIdPattern;

    private final Pattern versionPattern;

    public ArtifactGlob( String glob )
    {
        String[] tok = StringSplitter.split( glob, 3, ':' );
        groupIdPattern = GlobUtils.glob2pattern( tok[0] );
        artifactIdPattern = GlobUtils.glob2pattern( tok[1] );
        versionPattern = GlobUtils.glob2pattern( tok[2] );
    }

    public String match( String groupId, String artifactId, String version, String result )
    {
        List<Matcher> matchers = new ArrayList<>( 3 );
        if ( groupIdPattern != null )
            matchers.add( groupIdPattern.matcher( groupId ) );
        if ( artifactIdPattern != null )
            matchers.add( artifactIdPattern.matcher( artifactId ) );
        if ( versionPattern != null )
            matchers.add( versionPattern.matcher( version ) );

        int group = 1;
        for ( Matcher matcher : matchers )
        {
            if ( !matcher.matches() )
                return null;

            for ( int i = 1; i <= matcher.groupCount(); i++, group++ )
                result = result.replace( "@" + group, matcher.group( i ) );
        }

        return result.trim();
    }

    private String match( String source, Pattern pattern, String target )
    {
        String result = source != null ? source : target;

        if ( pattern != null )
        {
            Matcher matcher = pattern.matcher( target );
            if ( !matcher.matches() )
                return null;
            if ( source != null )
            {
                for ( int group = 1; group <= matcher.groupCount(); group++ )
                    result = result.replace( "@" + group, matcher.group( group ) );
            }
        }

        return result.trim();
    }

    public Artifact createArtifactFromTemplate( Artifact artifact, String template )
    {
        String[] tok = StringSplitter.split( template, 3, ':' );

        String groupId = match( tok[0], groupIdPattern, artifact.getGroupId() );
        String artifactId = match( tok[1], artifactIdPattern, artifact.getArtifactId() );
        String version = match( tok[2], versionPattern, artifact.getVersion() );

        if ( groupId == null || artifactId == null || version == null )
            return null;

        if ( groupId.equals( "" ) )
            groupId = null;
        if ( artifactId.equals( "" ) )
            artifactId = null;
        if ( version.equals( "" ) )
            version = null;

        return new Artifact( groupId, artifactId, version );
    }
}
