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
package org.fedoraproject.maven.config;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;

/**
 * Default implementation of configuration merger.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = ConfigurationMerger.class )
public class DefaultConfigurationMerger
    implements ConfigurationMerger
{
    private void mergeProperties( Properties dominant, Properties recessive )
    {
        Set<Object> dominantKeySet = dominant.keySet();
        for ( Object key : recessive.keySet() )
            if ( !dominantKeySet.contains( key ) )
                dominant.put( key, recessive.get( key ) );
    }

    private void mergeBuildSettings( BuildSettings dominant, BuildSettings recessive )
    {
        if ( dominant.isDebug() == null )
            dominant.setDebug( recessive.isDebug() );

        if ( dominant.isSkipTests() == null )
            dominant.setSkipTests( recessive.isSkipTests() );

        if ( dominant.getCompilerSource() == null )
            dominant.setCompilerSource( recessive.getCompilerSource() );
    }

    private void mergeArtifactmanagement( List<PackagingRule> dominant, List<PackagingRule> recessive )
    {
        dominant.addAll( recessive );
    }

    private void mergeResolverSettings( ResolverSettings dominant, ResolverSettings recessive )
    {
        if ( dominant.isDebug() == null )
            dominant.setDebug( recessive.isDebug() );

        dominant.getLocalRepositories().addAll( recessive.getLocalRepositories() );

        dominant.getJarRepositories().addAll( recessive.getJarRepositories() );

        dominant.getPomRepositories().addAll( recessive.getPomRepositories() );

        dominant.getMetadataRepositories().addAll( recessive.getMetadataRepositories() );

        dominant.getPrefixes().addAll( recessive.getPrefixes() );

        dominant.getBlacklist().addAll( recessive.getBlacklist() );
    }

    private void mergeInstallerSettings( InstallerSettings dominant, InstallerSettings recessive )
    {
        if ( dominant.getPackageName() == null )
            dominant.setPackageName( recessive.getPackageName() );

        if ( dominant.isSkipProvides() == null )
            dominant.setSkipProvides( recessive.isSkipProvides() );

        if ( dominant.isSkipRequires() == null )
            dominant.setSkipRequires( recessive.isSkipRequires() );

        if ( dominant.isEnableRawPoms() == null )
            dominant.setEnableRawPoms( recessive.isEnableRawPoms() );

        if ( dominant.isEnableEffectivePoms() == null )
            dominant.setEnableEffectivePoms( recessive.isEnableEffectivePoms() );

        if ( dominant.isPomRawModel() == null )
            dominant.setPomRawModel( recessive.isPomRawModel() );

        if ( dominant.isPomEffectiveModel() == null )
            dominant.setPomEffectiveModel( recessive.isPomEffectiveModel() );

        if ( dominant.isJarRawModel() == null )
            dominant.setJarRawModel( recessive.isJarRawModel() );

        if ( dominant.isJarEffectiveModel() == null )
            dominant.setJarEffectiveModel( recessive.isJarEffectiveModel() );

        if ( dominant.getJarDir() == null )
            dominant.setJarDir( recessive.getJarDir() );

        if ( dominant.getJniDir() == null )
            dominant.setJniDir( recessive.getJniDir() );

        if ( dominant.getPomDir() == null )
            dominant.setPomDir( recessive.getPomDir() );

        if ( dominant.getEffectivePomDir() == null )
            dominant.setEffectivePomDir( recessive.getEffectivePomDir() );

        if ( dominant.getMetadataDir() == null )
            dominant.setMetadataDir( recessive.getMetadataDir() );

        if ( dominant.getInstallRoot() == null )
            dominant.setInstallRoot( recessive.getInstallRoot() );
    }

    private void mergeConfiguration( Configuration dominant, Configuration recessive )
    {
        mergeProperties( dominant.getProperties(), recessive.getProperties() );

        if ( dominant.getBuildSettings() == null )
            dominant.setBuildSettings( new BuildSettings() );
        mergeBuildSettings( dominant.getBuildSettings(), recessive.getBuildSettings() );

        mergeArtifactmanagement( dominant.getArtifactManagement(), recessive.getArtifactManagement() );

        if ( dominant.getResolverSettings() == null )
            dominant.setResolverSettings( new ResolverSettings() );
        mergeResolverSettings( dominant.getResolverSettings(), recessive.getResolverSettings() );

        if ( dominant.getInstallerSettings() == null )
            dominant.setInstallerSettings( new InstallerSettings() );
        mergeInstallerSettings( dominant.getInstallerSettings(), recessive.getInstallerSettings() );
    }

    @Override
    public Configuration merge( Configuration dominant, Configuration recessive )
    {
        if ( dominant == null )
            dominant = new Configuration();

        mergeConfiguration( dominant, recessive );
        return dominant;
    }
}
