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
package org.fedoraproject.maven.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.StringUtils;
import org.fedoraproject.maven.utils.GlobUtils;

/**
 * @author Mikolaj Izdebski
 */
class EffectivePackagingRule
    extends PackagingRule
{
    private static final long serialVersionUID = 1L;

    private static String expandBackreferences( List<Matcher> matchers, String result )
    {
        int group = 1;
        for ( Matcher matcher : matchers )
        {
            for ( int i = 1; i <= matcher.groupCount(); i++, group++ )
                result = result.replace( "@" + group, matcher.group( i ) );
        }

        return result.trim();
    }

    private static org.fedoraproject.maven.config.Artifact expandBackreferences( List<Matcher> matchers,
                                                                                 org.fedoraproject.maven.config.Artifact source )
    {
        org.fedoraproject.maven.config.Artifact target = new org.fedoraproject.maven.config.Artifact();
        target.setGroupId( source.getGroupId() );
        target.setArtifactId( source.getArtifactId() );
        target.setVersion( source.getVersion() );

        int group = 1;
        for ( Matcher matcher : matchers )
        {
            for ( int i = 1; i <= matcher.groupCount(); i++, group++ )
            {
                Pattern pattern = Pattern.compile( "@" + group );
                String replacement = matcher.group( i );
                target.setGroupId( pattern.matcher( target.getGroupId() ).replaceAll( replacement ) );
                target.setArtifactId( pattern.matcher( target.getArtifactId() ).replaceAll( replacement ) );
                target.setVersion( pattern.matcher( target.getVersion() ).replaceAll( replacement ) );
            }
        }

        return target;
    }

    private void applyRule( PackagingRule rule )
    {
        Artifact glob = rule.getArtifactGlob();
        Pattern groupIdPattern = GlobUtils.glob2pattern( glob.getGroupId() );
        Pattern artifactIdPattern = GlobUtils.glob2pattern( glob.getArtifactId() );
        Pattern versionPattern = GlobUtils.glob2pattern( glob.getVersion() );

        Artifact artifact = getArtifactGlob();
        List<Matcher> matchers = new ArrayList<>( 3 );
        if ( groupIdPattern != null )
            matchers.add( groupIdPattern.matcher( artifact.getGroupId() ) );
        if ( artifactIdPattern != null )
            matchers.add( artifactIdPattern.matcher( artifact.getArtifactId() ) );
        if ( versionPattern != null )
            matchers.add( versionPattern.matcher( artifact.getVersion() ) );

        for ( Matcher matcher : matchers )
            if ( !matcher.matches() )
                return;

        String targetPackage = rule.getTargetPackage();
        if ( StringUtils.isEmpty( getTargetPackage() ) && StringUtils.isNotEmpty( targetPackage ) )
            setTargetPackage( expandBackreferences( matchers, targetPackage ) );

        for ( org.fedoraproject.maven.config.Artifact alias : rule.getAliases() )
        {
            alias = expandBackreferences( matchers, alias );

            if ( StringUtils.isEmpty( alias.getGroupId() ) )
                alias.setGroupId( artifact.getGroupId() );
            if ( StringUtils.isEmpty( alias.getArtifactId() ) )
                alias.setArtifactId( artifact.getArtifactId() );
            if ( StringUtils.isEmpty( alias.getVersion() ) )
                alias.setVersion( artifact.getVersion() );

            if ( !getAliases().contains( alias ) )
                addAlias( alias );
        }

        for ( String file : rule.getFiles() )
        {
            file = expandBackreferences( matchers, file );
            if ( !getFiles().contains( file ) )
                addFile( file );
        }
    }

    public EffectivePackagingRule( List<PackagingRule> artifactManagement, String groupId, String artifactId,
                                   String version )
    {
        Artifact artifact = new Artifact();
        artifact.setGroupId( groupId );
        artifact.setArtifactId( artifactId );
        artifact.setVersion( version );
        setArtifactGlob( artifact );

        for ( PackagingRule rule : artifactManagement )
        {
            applyRule( rule );
        }
    }
}
