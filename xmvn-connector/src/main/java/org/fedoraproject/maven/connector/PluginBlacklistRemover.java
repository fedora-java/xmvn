/*-
 * Copyright (c) 2012 Red Hat, Inc.
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
package org.fedoraproject.maven.connector;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

/**
 * Model customizer that removes all blacklisted plugins.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = ModelCustomizer.class, hint = "PluginBlacklistRemover" )
public class PluginBlacklistRemover
    implements ModelCustomizer
{
    @Requirement
    private Logger logger;

    @Override
    public void customizeModel( Model model )
    {
        Build build = model.getBuild();
        if ( build == null )
            return;

        List<Plugin> plugins = build.getPlugins();

        for ( Iterator<Plugin> iter = plugins.iterator(); iter.hasNext(); )
        {
            Plugin plugin = iter.next();
            String groupId = plugin.getGroupId();
            String artifactId = plugin.getArtifactId();
            Set<String> group = blacklist.get( groupId );
            if ( group != null && group.contains( artifactId ) )
            {
                logger.debug( "Removed plugin " + groupId + ":" + artifactId + " because it was blacklisted." );
                iter.remove();
            }
        }
    }

    private static final Map<String, Set<String>> blacklist = new TreeMap<>();

    private static void blacklist( String groupId, String artifactId )
    {
        Set<String> group = blacklist.get( groupId );

        if ( group == null )
        {
            group = new TreeSet<>();
            blacklist.put( groupId, group );
        }

        group.add( artifactId );
    }

    static
    {
        blacklist( "org.fedoraproject.xmvn", "xmvn-void" );
        blacklist( "org.apache.maven.plugins", "maven-clean-plugin" );
        blacklist( "org.codehaus.mojo", "clirr-maven-plugin" );
        blacklist( "org.codehaus.mojo", "animal-sniffer-maven-plugin" );
    }
}
