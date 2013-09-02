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
package org.fedoraproject.maven.config.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.StringUtils;
import org.fedoraproject.maven.config.Artifact;
import org.fedoraproject.maven.config.PackagingRule;
import org.fedoraproject.maven.utils.GlobUtils;

/**
 * Effective artifact packaging rule.
 * <p>
 * In general packaging rules are in n-to-m relation with artifacts. One artifact can have one or more packaging rules
 * and one packaging rule can match zero or more artifacts. This approach is well suited for configuring build process
 * by humans.
 * <p>
 * In contrast, effective packaging rules are in 1-to-1 relation with artifacts. Every artifact has exactly one
 * effective packaging rule. This form is best for machine processing.
 * <p>
 * Effective packaging rules are created from raw configuration rules by merging and/or splitting and expanding regular
 * expression patterns.
 * 
 * @author Mikolaj Izdebski
 */
// XXX this should be private, or documented as such
public class EffectivePackagingRule
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
        target.setStereotype( source.getStereotype() );
        target.setGroupId( source.getGroupId() );
        target.setArtifactId( source.getArtifactId() );
        target.setExtension( source.getExtension() );
        target.setClassifier( source.getClassifier() );
        target.setVersion( source.getVersion() );

        int group = 1;
        for ( Matcher matcher : matchers )
        {
            for ( int i = 1; i <= matcher.groupCount(); i++, group++ )
            {
                Pattern pattern = Pattern.compile( "@" + group );
                String replacement = matcher.group( i );
                target.setStereotype( pattern.matcher( target.getStereotype() ).replaceAll( replacement ) );
                target.setGroupId( pattern.matcher( target.getGroupId() ).replaceAll( replacement ) );
                target.setArtifactId( pattern.matcher( target.getArtifactId() ).replaceAll( replacement ) );
                target.setExtension( pattern.matcher( target.getExtension() ).replaceAll( replacement ) );
                target.setClassifier( pattern.matcher( target.getClassifier() ).replaceAll( replacement ) );
                target.setVersion( pattern.matcher( target.getVersion() ).replaceAll( replacement ) );
            }
        }

        return target;
    }

    private void applyRule( PackagingRule rule )
    {
        Artifact glob = rule.getArtifactGlob();
        Pattern stereotypePattern = GlobUtils.glob2pattern( glob.getStereotype() );
        Pattern groupIdPattern = GlobUtils.glob2pattern( glob.getGroupId() );
        Pattern artifactIdPattern = GlobUtils.glob2pattern( glob.getArtifactId() );
        Pattern extensionPattern = GlobUtils.glob2pattern( glob.getExtension() );
        Pattern classifierPattern = GlobUtils.glob2pattern( glob.getClassifier() );
        Pattern versionPattern = GlobUtils.glob2pattern( glob.getVersion() );

        Artifact artifact = getArtifactGlob();
        List<Matcher> matchers = new ArrayList<>( 3 );
        if ( stereotypePattern != null )
            matchers.add( stereotypePattern.matcher( artifact.getStereotype() ) );
        if ( groupIdPattern != null )
            matchers.add( groupIdPattern.matcher( artifact.getGroupId() ) );
        if ( artifactIdPattern != null )
            matchers.add( artifactIdPattern.matcher( artifact.getArtifactId() ) );
        if ( extensionPattern != null )
            matchers.add( extensionPattern.matcher( artifact.getExtension() ) );
        if ( classifierPattern != null )
            matchers.add( classifierPattern.matcher( artifact.getClassifier() ) );
        if ( versionPattern != null )
            matchers.add( versionPattern.matcher( artifact.getVersion() ) );

        for ( Matcher matcher : matchers )
            if ( !matcher.matches() )
                return;
        rule.setMatched( true );

        String targetPackage = rule.getTargetPackage();
        if ( StringUtils.isEmpty( getTargetPackage() ) && StringUtils.isNotEmpty( targetPackage ) )
            setTargetPackage( expandBackreferences( matchers, targetPackage ) );

        for ( org.fedoraproject.maven.config.Artifact alias : rule.getAliases() )
        {
            alias = expandBackreferences( matchers, alias );

            if ( StringUtils.isEmpty( alias.getStereotype() ) )
                alias.setStereotype( artifact.getStereotype() );
            if ( StringUtils.isEmpty( alias.getGroupId() ) )
                alias.setGroupId( artifact.getGroupId() );
            if ( StringUtils.isEmpty( alias.getArtifactId() ) )
                alias.setArtifactId( artifact.getArtifactId() );
            if ( StringUtils.isEmpty( alias.getExtension() ) )
                alias.setExtension( artifact.getExtension() );
            if ( StringUtils.isEmpty( alias.getClassifier() ) )
                alias.setClassifier( artifact.getClassifier() );
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

        for ( String version : rule.getVersions() )
        {
            version = expandBackreferences( matchers, version );
            if ( !getVersions().contains( version ) )
                addVersion( version );
        }
    }

    /**
     * Create effective packaging rule for given artifact.
     * 
     * @param artifactManagement list of raw packaging rules that are foundation of newly constructed effective rule
     * @param stereotype stereotype of artifact for which effective rule is to be created
     * @param groupId groupId of artifact for which effective rule is to be created
     * @param artifactId artifactId of artifact for which effective rule is to be created
     * @param extension extension of artifact for which effective rule is to be created
     * @param classifier classifier of artifact for which effective rule is to be created
     * @param version version of artifact for which effective rule is to be created
     */
    public EffectivePackagingRule( List<PackagingRule> artifactManagement, String stereotype, String groupId,
                                   String artifactId, String extension, String classifier, String version )
    {
        Artifact artifact = new Artifact();
        artifact.setStereotype( stereotype );
        artifact.setGroupId( groupId );
        artifact.setArtifactId( artifactId );
        artifact.setExtension( extension );
        artifact.setClassifier( classifier );
        artifact.setVersion( version );
        setArtifactGlob( artifact );
        setOptional( false );
        setMatched( true );

        for ( PackagingRule rule : artifactManagement )
        {
            applyRule( rule );
        }
    }
}
