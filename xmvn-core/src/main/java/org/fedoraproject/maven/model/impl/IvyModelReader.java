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
package org.fedoraproject.maven.model.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DependencyArtifactDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ExcludeRule;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorParser;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Model;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.fedoraproject.maven.model.ModelFormatException;
import org.fedoraproject.maven.model.ModelReader;

/**
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = ModelReader.class, hint = "ivy", instantiationStrategy = "singleton" )
public class IvyModelReader
    implements ModelReader
{
    private static final List<String> MAVEN_SCOPES = Arrays.asList( "compile", "runtime", "provided", "test", "system" );

    private ModuleDescriptor readModule( Path modulePath )
        throws IOException, ParseException
    {
        try
        {
            XmlModuleDescriptorParser parser = XmlModuleDescriptorParser.getInstance();
            IvySettings settings = new IvySettings();
            return parser.parseDescriptor( settings, modulePath.toUri().toURL(), false );
        }
        catch ( MalformedURLException e )
        {
            throw new RuntimeException( e );
        }
    }

    private String nullify( String value, String defaultValue )
    {
        if ( StringUtils.isEmpty( value ) || value.equals( defaultValue ) )
            return null;

        return value;
    }

    private Model getModuleModel( ModuleDescriptor module )
    {
        Model model = new Model();
        model.setModelVersion( "4.0.0" );

        ModuleRevisionId moduleRevision = module.getModuleRevisionId();
        model.setGroupId( moduleRevision.getOrganisation() );
        model.setArtifactId( moduleRevision.getName() );
        model.setVersion( moduleRevision.getRevision() );

        model.setPackaging( "pom" );
        for ( Artifact artifact : module.getAllArtifacts() )
            if ( artifact.getName().equals( moduleRevision.getName() ) && artifact.getAttribute( "classifier" ) == null )
                model.setPackaging( artifact.getType() );

        for ( DependencyDescriptor dependencyModule : module.getDependencies() )
        {
            Set<String> scopes = new HashSet<>( MAVEN_SCOPES );
            scopes.retainAll( Arrays.asList( dependencyModule.getModuleConfigurations() ) );
            String scope = scopes.isEmpty() ? null : scopes.iterator().next();

            Map<String, String> map = new LinkedHashMap<>();
            for ( DependencyArtifactDescriptor dependencyArtifact : dependencyModule.getAllDependencyArtifacts() )
            {
                String classifier = dependencyArtifact.getExtraAttribute( "classifier" );
                String type = dependencyArtifact.getType();
                map.put( classifier, type );
            }
            if ( map.isEmpty() )
                map = Collections.singletonMap( null, null );

            List<Exclusion> exclusions = new ArrayList<>();
            for ( ExcludeRule rule : dependencyModule.getAllExcludeRules() )
            {
                Exclusion exclusion = new Exclusion();
                exclusions.add( exclusion );

                ModuleId exclusedModule = rule.getId().getModuleId();
                exclusion.setGroupId( exclusedModule.getOrganisation() );
                exclusion.setArtifactId( exclusedModule.getName() );
            }

            for ( Entry<String, String> entry : map.entrySet() )
            {
                Dependency dependency = new Dependency();
                model.addDependency( dependency );

                ModuleRevisionId dependencyRevision = dependencyModule.getDependencyRevisionId();
                dependency.setGroupId( dependencyRevision.getOrganisation() );
                dependency.setArtifactId( dependencyRevision.getName() );
                dependency.setVersion( nullify( dependencyRevision.getRevision(), "SYSTEM" ) );
                dependency.setType( nullify( entry.getValue(), "jar" ) );
                dependency.setClassifier( nullify( entry.getKey(), "" ) );
                dependency.setScope( nullify( scope, "compile" ) );
                dependency.setExclusions( new ArrayList<>( exclusions ) );
            }
        }

        return model;
    }

    @Override
    public Model readModel( Path modelPath )
        throws IOException, ModelFormatException
    {
        try
        {
            ModuleDescriptor module = readModule( modelPath );
            return getModuleModel( module );
        }
        catch ( ParseException e )
        {
            throw new ModelFormatException( "Unable to parse Ivy module", e );
        }
    }
}
