/*-
 * Copyright (c) 2013-2016 Red Hat, Inc.
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

import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.base.Strings;

import org.fedoraproject.xmvn.config.BuildSettings;
import org.fedoraproject.xmvn.config.Configuration;
import org.fedoraproject.xmvn.config.ConfigurationMerger;
import org.fedoraproject.xmvn.config.InstallerSettings;
import org.fedoraproject.xmvn.config.PackagingRule;
import org.fedoraproject.xmvn.config.Repository;
import org.fedoraproject.xmvn.config.ResolverSettings;

/**
 * Default implementation of configuration merger.
 * <p>
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
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

        if ( Strings.isNullOrEmpty( dominant.getCompilerSource() ) )
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

        dominant.getPrefixes().addAll( recessive.getPrefixes() );

        dominant.getBlacklist().addAll( recessive.getBlacklist() );
    }

    private void mergeInstallerSettings( InstallerSettings dominant, InstallerSettings recessive )
    {
        if ( dominant.isDebug() == null )
            dominant.setDebug( recessive.isDebug() );

        if ( Strings.isNullOrEmpty( dominant.getMetadataDir() ) )
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

    @Override
    public Configuration merge( Configuration dominant, Configuration recessive )
    {
        if ( dominant == null )
            dominant = new Configuration();

        mergeConfiguration( dominant, recessive );
        return dominant;
    }
}
