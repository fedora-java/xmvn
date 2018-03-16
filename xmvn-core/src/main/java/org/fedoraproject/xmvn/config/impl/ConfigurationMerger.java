/*-
 * Copyright (c) 2013-2018 Red Hat, Inc.
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
package org.fedoraproject.xmvn.config.impl;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.fedoraproject.xmvn.config.BuildSettings;
import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.InstallerSettings;
import org.fedoraproject.xmvn.config.PackagingRule;
import org.fedoraproject.xmvn.config.Repository;
import org.fedoraproject.xmvn.config.ResolverSettings;

/**
 * @author Mikolaj Izdebski
 */
class ConfigurationMerger
{
    private void mergeProperties( Properties dominant, Properties recessive )
    {
        Set<Object> dominantKeySet = dominant.keySet();
        for ( Object key : recessive.keySet() )
            if ( !dominantKeySet.contains( key ) )
                dominant.put( key, recessive.get( key ) );
    }

    private void mergeRepositories( List<Repository> dominant, List<Repository> recessive )
    {
        dominant.addAll( recessive );
    }

    private void mergeBuildSettings( BuildSettings dominant, BuildSettings recessive )
    {
        if ( dominant.isDebug() == null )
            dominant.setDebug( recessive.isDebug() );

        if ( dominant.isSkipTests() == null )
            dominant.setSkipTests( recessive.isSkipTests() );

        if ( dominant.getCompilerSource() == null || dominant.getCompilerSource().isEmpty() )
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

        dominant.getMetadataRepositories().addAll( recessive.getMetadataRepositories() );

        if ( dominant.isIgnoreDuplicateMetadata() == null )
            dominant.setIgnoreDuplicateMetadata( recessive.isIgnoreDuplicateMetadata() );

        dominant.getPrefixes().addAll( recessive.getPrefixes() );

        dominant.getBlacklist().addAll( recessive.getBlacklist() );
    }

    private void mergeInstallerSettings( InstallerSettings dominant, InstallerSettings recessive )
    {
        if ( dominant.isDebug() == null )
            dominant.setDebug( recessive.isDebug() );

        if ( dominant.getMetadataDir() == null || dominant.getMetadataDir().isEmpty() )
            dominant.setMetadataDir( recessive.getMetadataDir() );
    }

    private void mergeConfiguration( Configuration dominant, Configuration recessive )
    {
        mergeProperties( dominant.getProperties(), recessive.getProperties() );
        mergeRepositories( dominant.getRepositories(), recessive.getRepositories() );

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

    /**
     * Merge two configurations, with one with one having precedence in the case of conflict.
     * <p>
     * Caller should not depend on contents of dominant configuration after the merge was attempted as the
     * implementation is free to modify it. Recessive configuration is never changed.
     * 
     * @param dominant the dominant configuration into which the recessive configuration will be merged (may be
     *            {@code null})
     * @param recessive the recessive configuration from which the configuration will inherited (may not be {@code null}
     *            )
     * @return merged configuration (not {@code null}, may be the same as dominant)
     */
    public Configuration merge( Configuration dominant, Configuration recessive )
    {
        if ( dominant == null )
            dominant = new Configuration();

        mergeConfiguration( dominant, recessive );
        return dominant;
    }
}
