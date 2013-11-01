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
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.maven.model.Activation;
import org.apache.maven.model.ActivationFile;
import org.apache.maven.model.ActivationOS;
import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.Build;
import org.apache.maven.model.BuildBase;
import org.apache.maven.model.CiManagement;
import org.apache.maven.model.Contributor;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.Developer;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Extension;
import org.apache.maven.model.IssueManagement;
import org.apache.maven.model.License;
import org.apache.maven.model.MailingList;
import org.apache.maven.model.Model;
import org.apache.maven.model.Notifier;
import org.apache.maven.model.Organization;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Prerequisites;
import org.apache.maven.model.Profile;
import org.apache.maven.model.Relocation;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.ReportSet;
import org.apache.maven.model.Reporting;
import org.apache.maven.model.Repository;
import org.apache.maven.model.RepositoryPolicy;
import org.apache.maven.model.Resource;
import org.apache.maven.model.Scm;
import org.apache.maven.model.Site;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.fedoraproject.maven.model.ModelFormatException;
import org.fedoraproject.maven.model.ModelProcessor;
import org.fedoraproject.maven.model.ModelReader;
import org.fedoraproject.maven.model.ModelVisitor;

/**
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = ModelProcessor.class )
public class DefaultModelProcessor
    implements ModelProcessor
{
    @Requirement
    private ModelReader modelReader;

    @Override
    public void processModel( Path modelPath, ModelVisitor visitor )
        throws IOException, ModelFormatException
    {
        Model model = modelReader.readModel( modelPath );
        visitor.visitProject( model );
        visit( visitor, model );
    }

    private void visit( ModelVisitor visitor, Model model )
    {
        Parent parent = model.getParent();
        if ( parent != null )
        {
            visitor.visitParent( parent );
            parent = visitor.replaceParent( parent );
            model.setParent( parent );
        }

        Organization organization = model.getOrganization();
        if ( organization != null )
        {
            visitor.visitOrganization( organization );
            organization = visitor.replaceOrganization( organization );
            model.setOrganization( organization );
        }

        List<License> licenses = model.getLicenses();
        if ( licenses != null )
        {
            ListIterator<License> licenseIterator = licenses.listIterator();
            while ( licenseIterator.hasNext() )
            {
                License license = licenseIterator.next();
                visitor.visitLicense( license );
                license = visitor.replaceLicense( license );
                if ( license == null )
                    licenseIterator.remove();
                else
                    licenseIterator.set( license );
            }
        }

        List<Developer> developers = model.getDevelopers();
        if ( developers != null )
        {
            ListIterator<Developer> developerIterator = developers.listIterator();
            while ( developerIterator.hasNext() )
            {
                Developer developer = developerIterator.next();
                visitor.visitDeveloper( developer );
                visitDeveloper( visitor, developer );
                developer = visitor.replaceDeveloper( developer );
                if ( developer == null )
                    developerIterator.remove();
                else
                    developerIterator.set( developer );
            }
        }

        List<Contributor> contributors = model.getContributors();
        if ( contributors != null )
        {
            ListIterator<Contributor> contributorIterator = contributors.listIterator();
            while ( contributorIterator.hasNext() )
            {
                Contributor contributor = contributorIterator.next();
                visitor.visitContributor( contributor );
                visitContributor( visitor, contributor );
                contributor = visitor.replaceContributor( contributor );
                if ( contributor == null )
                    contributorIterator.remove();
                else
                    contributorIterator.set( contributor );
            }
        }

        List<MailingList> mailingLists = model.getMailingLists();
        if ( mailingLists != null )
        {
            ListIterator<MailingList> mailingListIterator = mailingLists.listIterator();
            while ( mailingListIterator.hasNext() )
            {
                MailingList mailingList = mailingListIterator.next();
                visitor.visitMailingList( mailingList );
                visitMailingList( visitor, mailingList );
                mailingList = visitor.replaceMailingList( mailingList );
                if ( mailingList == null )
                    mailingListIterator.remove();
                else
                    mailingListIterator.set( mailingList );
            }
        }

        Prerequisites prerequisites = model.getPrerequisites();
        if ( prerequisites != null )
        {
            visitor.visitPrerequisite( prerequisites );
            prerequisites = visitor.replacePrerequisite( prerequisites );
            model.setPrerequisites( prerequisites );
        }

        List<String> modules = model.getModules();
        if ( modules != null )
        {
            ListIterator<String> moduleIterator = modules.listIterator();
            while ( moduleIterator.hasNext() )
            {
                String module = moduleIterator.next();
                visitor.visitModule( module );
                module = visitor.replaceModule( module );
                if ( module == null )
                    moduleIterator.remove();
                else
                    moduleIterator.set( module );
            }
        }

        Scm scm = model.getScm();
        if ( scm != null )
        {
            visitor.visitScm( scm );
            scm = visitor.replaceScm( scm );
            model.setScm( scm );
        }

        IssueManagement issueManagement = model.getIssueManagement();
        if ( issueManagement != null )
        {
            visitor.visitIssueManagement( issueManagement );
            issueManagement = visitor.replaceIssueManagement( issueManagement );
            model.setIssueManagement( issueManagement );
        }

        CiManagement ciManagement = model.getCiManagement();
        if ( ciManagement != null )
        {
            visitor.visitCiManagement( ciManagement );
            visitCiManagement( visitor, ciManagement );
            ciManagement = visitor.replaceCiManagement( ciManagement );
            model.setCiManagement( ciManagement );
        }

        DistributionManagement distributionManagement = model.getDistributionManagement();
        if ( distributionManagement != null )
        {
            visitor.visitDistributionManagement( distributionManagement );
            visitDistributionManagement( visitor, distributionManagement );
            distributionManagement = visitor.replaceDistributionManagement( distributionManagement );
            model.setDistributionManagement( distributionManagement );
        }

        Properties properties = model.getProperties();
        if ( properties != null )
        {
            Iterator<Entry<Object, Object>> propertyIterator = properties.entrySet().iterator();
            while ( propertyIterator.hasNext() )
            {
                Entry<Object, Object> property = propertyIterator.next();
                String propertyKey = (String) property.getKey();
                String propertyValue = (String) property.getKey();
                visitor.visitProperty( propertyKey, propertyValue );
                propertyValue = visitor.replaceProperty( propertyKey, propertyValue );
                if ( propertyValue == null )
                    propertyIterator.remove();
                else
                    property.setValue( propertyValue );
            }
        }

        DependencyManagement dependencyManagement = model.getDependencyManagement();
        if ( dependencyManagement != null )
        {
            visitor.visitDependencyManagement( dependencyManagement );
            visitDependencyManagement( visitor, dependencyManagement );
            dependencyManagement = visitor.replaceDependencyManagement( dependencyManagement );
            model.setDependencyManagement( dependencyManagement );
        }

        List<Dependency> dependencies = model.getDependencies();
        if ( dependencies != null )
        {
            ListIterator<Dependency> dependencyIterator = dependencies.listIterator();
            while ( dependencyIterator.hasNext() )
            {
                Dependency dependency = dependencyIterator.next();
                visitor.visitDependency( dependency );
                visitDependency( visitor, dependency );
                dependency = visitor.replaceDependency( dependency );
                if ( dependency == null )
                    dependencyIterator.remove();
                else
                    dependencyIterator.set( dependency );
            }
        }

        List<Repository> repositories = model.getRepositories();
        if ( repositories != null )
        {
            ListIterator<Repository> repositoryIterator = repositories.listIterator();
            while ( repositoryIterator.hasNext() )
            {
                Repository repository = repositoryIterator.next();
                visitor.visitRepository( repository );
                visitRepository( visitor, repository );
                repository = visitor.replaceRepository( repository );
                if ( repository == null )
                    repositoryIterator.remove();
                else
                    repositoryIterator.set( repository );
            }
        }

        List<Repository> pluginRepositories = model.getPluginRepositories();
        if ( pluginRepositories != null )
        {
            ListIterator<Repository> pluginRepositoryIterator = pluginRepositories.listIterator();
            while ( pluginRepositoryIterator.hasNext() )
            {
                Repository pluginRepository = pluginRepositoryIterator.next();
                visitor.visitPluginRepository( pluginRepository );
                visitPluginRepository( visitor, pluginRepository );
                pluginRepository = visitor.replacePluginRepository( pluginRepository );
                if ( pluginRepository == null )
                    pluginRepositoryIterator.remove();
                else
                    pluginRepositoryIterator.set( pluginRepository );
            }
        }

        Build build = model.getBuild();
        if ( build != null )
        {
            visitor.visitBuild( build );
            visitBuild( visitor, build );
            build = visitor.replaceBuild( build );
            model.setBuild( build );
        }

        Reporting reporting = model.getReporting();
        if ( reporting != null )
        {
            visitor.visitReporting( reporting );
            visitReporting( visitor, reporting );
            reporting = visitor.replaceReporting( reporting );
            model.setReporting( reporting );
        }

        List<Profile> profiles = model.getProfiles();
        if ( profiles != null )
        {
            ListIterator<Profile> profileIterator = profiles.listIterator();
            while ( profileIterator.hasNext() )
            {
                Profile profile = profileIterator.next();
                visitor.visitProfile( profile );
                visitProfile( visitor, profile );
                profile = visitor.replaceProfile( profile );
                if ( profile == null )
                    profileIterator.remove();
                else
                    profileIterator.set( profile );
            }
        }
    }

    private void visitBuild( ModelVisitor visitor, Build build )
    {
        List<Extension> extensions = build.getExtensions();
        if ( extensions != null )
        {
            ListIterator<Extension> extensionIterator = extensions.listIterator();
            while ( extensionIterator.hasNext() )
            {
                Extension extension = extensionIterator.next();
                visitor.visitBuildExtension( extension );
                extension = visitor.replaceBuildExtension( extension );
                if ( extension == null )
                    extensionIterator.remove();
                else
                    extensionIterator.set( extension );
            }
        }

        List<Resource> resources = build.getResources();
        if ( resources != null )
        {
            ListIterator<Resource> resourceIterator = resources.listIterator();
            while ( resourceIterator.hasNext() )
            {
                Resource resource = resourceIterator.next();
                visitor.visitBuildResource( resource );
                visitBuildResource( visitor, resource );
                resource = visitor.replaceBuildResource( resource );
                if ( resource == null )
                    resourceIterator.remove();
                else
                    resourceIterator.set( resource );
            }
        }

        List<Resource> testResources = build.getTestResources();
        if ( testResources != null )
        {
            ListIterator<Resource> testResourceIterator = testResources.listIterator();
            while ( testResourceIterator.hasNext() )
            {
                Resource testResource = testResourceIterator.next();
                visitor.visitBuildTestResource( testResource );
                visitBuildTestResource( visitor, testResource );
                testResource = visitor.replaceBuildTestResource( testResource );
                if ( testResource == null )
                    testResourceIterator.remove();
                else
                    testResourceIterator.set( testResource );
            }
        }

        List<String> filters = build.getFilters();
        if ( filters != null )
        {
            ListIterator<String> filterIterator = filters.listIterator();
            while ( filterIterator.hasNext() )
            {
                String filter = filterIterator.next();
                visitor.visitBuildFilter( filter );
                filter = visitor.replaceBuildFilter( filter );
                if ( filter == null )
                    filterIterator.remove();
                else
                    filterIterator.set( filter );
            }
        }

        PluginManagement pluginManagement = build.getPluginManagement();
        if ( pluginManagement != null )
        {
            visitor.visitBuildPluginManagement( pluginManagement );
            visitBuildPluginManagement( visitor, pluginManagement );
            pluginManagement = visitor.replaceBuildPluginManagement( pluginManagement );
            build.setPluginManagement( pluginManagement );
        }

        List<Plugin> plugins = build.getPlugins();
        if ( plugins != null )
        {
            ListIterator<Plugin> pluginIterator = plugins.listIterator();
            while ( pluginIterator.hasNext() )
            {
                Plugin plugin = pluginIterator.next();
                visitor.visitBuildPlugin( plugin );
                visitBuildPlugin( visitor, plugin );
                plugin = visitor.replaceBuildPlugin( plugin );
                if ( plugin == null )
                    pluginIterator.remove();
                else
                    pluginIterator.set( plugin );
            }
        }
    }

    private void visitBuildPlugin( ModelVisitor visitor, Plugin plugin )
    {
        List<PluginExecution> executions = plugin.getExecutions();
        if ( executions != null )
        {
            ListIterator<PluginExecution> executionIterator = executions.listIterator();
            while ( executionIterator.hasNext() )
            {
                PluginExecution execution = executionIterator.next();
                visitor.visitBuildPluginExecution( execution );
                visitBuildPluginExecution( visitor, execution );
                execution = visitor.replaceBuildPluginExecution( execution );
                if ( execution == null )
                    executionIterator.remove();
                else
                    executionIterator.set( execution );
            }
        }

        List<Dependency> dependencies = plugin.getDependencies();
        if ( dependencies != null )
        {
            ListIterator<Dependency> dependencyIterator = dependencies.listIterator();
            while ( dependencyIterator.hasNext() )
            {
                Dependency dependency = dependencyIterator.next();
                visitor.visitBuildPluginDependency( dependency );
                visitBuildPluginDependency( visitor, dependency );
                dependency = visitor.replaceBuildPluginDependency( dependency );
                if ( dependency == null )
                    dependencyIterator.remove();
                else
                    dependencyIterator.set( dependency );
            }
        }
    }

    private void visitBuildPluginDependency( ModelVisitor visitor, Dependency dependency )
    {
        List<Exclusion> exclusions = dependency.getExclusions();
        if ( exclusions != null )
        {
            ListIterator<Exclusion> exclusionIterator = exclusions.listIterator();
            while ( exclusionIterator.hasNext() )
            {
                Exclusion exclusion = exclusionIterator.next();
                visitor.visitBuildPluginDependencyExclusion( exclusion );
                exclusion = visitor.replaceBuildPluginDependencyExclusion( exclusion );
                if ( exclusion == null )
                    exclusionIterator.remove();
                else
                    exclusionIterator.set( exclusion );
            }
        }
    }

    private void visitBuildPluginExecution( ModelVisitor visitor, PluginExecution pluginExecution )
    {
        List<String> goals = pluginExecution.getGoals();
        if ( goals != null )
        {
            ListIterator<String> goalIterator = goals.listIterator();
            while ( goalIterator.hasNext() )
            {
                String goal = goalIterator.next();
                visitor.visitBuildPluginExecutionGoal( goal );
                goal = visitor.replaceBuildPluginExecutionGoal( goal );
                if ( goal == null )
                    goalIterator.remove();
                else
                    goalIterator.set( goal );
            }
        }
    }

    private void visitBuildPluginManagement( ModelVisitor visitor, PluginManagement pluginManagement )
    {
        List<Plugin> plugins = pluginManagement.getPlugins();
        if ( plugins != null )
        {
            ListIterator<Plugin> pluginIterator = plugins.listIterator();
            while ( pluginIterator.hasNext() )
            {
                Plugin plugin = pluginIterator.next();
                visitor.visitBuildPluginManagementPlugin( plugin );
                visitBuildPluginManagementPlugin( visitor, plugin );
                plugin = visitor.replaceBuildPluginManagementPlugin( plugin );
                if ( plugin == null )
                    pluginIterator.remove();
                else
                    pluginIterator.set( plugin );
            }
        }
    }

    private void visitBuildPluginManagementPlugin( ModelVisitor visitor, Plugin plugin )
    {
        List<PluginExecution> executions = plugin.getExecutions();
        if ( executions != null )
        {
            ListIterator<PluginExecution> executionIterator = executions.listIterator();
            while ( executionIterator.hasNext() )
            {
                PluginExecution execution = executionIterator.next();
                visitor.visitBuildPluginManagementPluginExecution( execution );
                visitBuildPluginManagementPluginExecution( visitor, execution );
                execution = visitor.replaceBuildPluginManagementPluginExecution( execution );
                if ( execution == null )
                    executionIterator.remove();
                else
                    executionIterator.set( execution );
            }
        }

        List<Dependency> dependencies = plugin.getDependencies();
        if ( dependencies != null )
        {
            ListIterator<Dependency> dependencyIterator = dependencies.listIterator();
            while ( dependencyIterator.hasNext() )
            {
                Dependency dependency = dependencyIterator.next();
                visitor.visitBuildPluginManagementPluginDependency( dependency );
                visitBuildPluginManagementPluginDependency( visitor, dependency );
                dependency = visitor.replaceBuildPluginManagementPluginDependency( dependency );
                if ( dependency == null )
                    dependencyIterator.remove();
                else
                    dependencyIterator.set( dependency );
            }
        }
    }

    private void visitBuildPluginManagementPluginDependency( ModelVisitor visitor, Dependency dependency )
    {
        List<Exclusion> exclusions = dependency.getExclusions();
        if ( exclusions != null )
        {
            ListIterator<Exclusion> exclusionIterator = exclusions.listIterator();
            while ( exclusionIterator.hasNext() )
            {
                Exclusion exclusion = exclusionIterator.next();
                visitor.visitBuildPluginManagementPluginDependencyExclusion( exclusion );
                exclusion = visitor.replaceBuildPluginManagementPluginDependencyExclusion( exclusion );
                if ( exclusion == null )
                    exclusionIterator.remove();
                else
                    exclusionIterator.set( exclusion );
            }
        }
    }

    private void visitBuildPluginManagementPluginExecution( ModelVisitor visitor, PluginExecution pluginExecution )
    {
        List<String> goals = pluginExecution.getGoals();
        if ( goals != null )
        {
            ListIterator<String> goalIterator = goals.listIterator();
            while ( goalIterator.hasNext() )
            {
                String goal = goalIterator.next();
                visitor.visitBuildPluginManagementPluginExecutionGoal( goal );
                goal = visitor.replaceBuildPluginManagementPluginExecutionGoal( goal );
                if ( goal == null )
                    goalIterator.remove();
                else
                    goalIterator.set( goal );
            }
        }
    }

    private void visitBuildResource( ModelVisitor visitor, Resource resource )
    {
        List<String> includes = resource.getIncludes();
        if ( includes != null )
        {
            ListIterator<String> includeIterator = includes.listIterator();
            while ( includeIterator.hasNext() )
            {
                String include = includeIterator.next();
                visitor.visitBuildResourceInclude( include );
                include = visitor.replaceBuildResourceInclude( include );
                if ( include == null )
                    includeIterator.remove();
                else
                    includeIterator.set( include );
            }
        }

        List<String> excludes = resource.getExcludes();
        if ( excludes != null )
        {
            ListIterator<String> excludeIterator = excludes.listIterator();
            while ( excludeIterator.hasNext() )
            {
                String exclude = excludeIterator.next();
                visitor.visitBuildResourceExclude( exclude );
                exclude = visitor.replaceBuildResourceExclude( exclude );
                if ( exclude == null )
                    excludeIterator.remove();
                else
                    excludeIterator.set( exclude );
            }
        }
    }

    private void visitBuildTestResource( ModelVisitor visitor, Resource resource )
    {
        List<String> includes = resource.getIncludes();
        if ( includes != null )
        {
            ListIterator<String> includeIterator = includes.listIterator();
            while ( includeIterator.hasNext() )
            {
                String include = includeIterator.next();
                visitor.visitBuildTestResourceInclude( include );
                include = visitor.replaceBuildTestResourceInclude( include );
                if ( include == null )
                    includeIterator.remove();
                else
                    includeIterator.set( include );
            }
        }

        List<String> excludes = resource.getExcludes();
        if ( excludes != null )
        {
            ListIterator<String> excludeIterator = excludes.listIterator();
            while ( excludeIterator.hasNext() )
            {
                String exclude = excludeIterator.next();
                visitor.visitBuildTestResourceExclude( exclude );
                exclude = visitor.replaceBuildTestResourceExclude( exclude );
                if ( exclude == null )
                    excludeIterator.remove();
                else
                    excludeIterator.set( exclude );
            }
        }
    }

    private void visitCiManagement( ModelVisitor visitor, CiManagement ciManagement )
    {
        List<Notifier> notifiers = ciManagement.getNotifiers();
        if ( notifiers != null )
        {
            ListIterator<Notifier> notifierIterator = notifiers.listIterator();
            while ( notifierIterator.hasNext() )
            {
                Notifier notifier = notifierIterator.next();
                visitor.visitCiManagementNotifier( notifier );
                visitCiManagementNotifier( visitor, notifier );
                notifier = visitor.replaceCiManagementNotifier( notifier );
                if ( notifier == null )
                    notifierIterator.remove();
                else
                    notifierIterator.set( notifier );
            }
        }
    }

    private void visitCiManagementNotifier( ModelVisitor visitor, Notifier notifier )
    {
        Properties configuration = notifier.getConfiguration();
        if ( configuration != null )
        {
            Iterator<Entry<Object, Object>> configurationElementIterator = configuration.entrySet().iterator();
            while ( configurationElementIterator.hasNext() )
            {
                Entry<Object, Object> configurationElement = configurationElementIterator.next();
                String configurationElementKey = (String) configurationElement.getKey();
                String configurationElementValue = (String) configurationElement.getKey();
                visitor.visitCiManagementNotifierConfiguration( configurationElementKey, configurationElementValue );
                configurationElementValue =
                    visitor.replaceCiManagementNotifierConfiguration( configurationElementKey,
                                                                      configurationElementValue );
                if ( configurationElementValue == null )
                    configurationElementIterator.remove();
                else
                    configurationElement.setValue( configurationElementValue );
            }
        }
    }

    private void visitContributor( ModelVisitor visitor, Contributor contributor )
    {
        List<String> roles = contributor.getRoles();
        if ( roles != null )
        {
            ListIterator<String> roleIterator = roles.listIterator();
            while ( roleIterator.hasNext() )
            {
                String role = roleIterator.next();
                visitor.visitContributorRole( role );
                role = visitor.replaceContributorRole( role );
                if ( role == null )
                    roleIterator.remove();
                else
                    roleIterator.set( role );
            }
        }

        Properties properties = contributor.getProperties();
        if ( properties != null )
        {
            Iterator<Entry<Object, Object>> propertyIterator = properties.entrySet().iterator();
            while ( propertyIterator.hasNext() )
            {
                Entry<Object, Object> property = propertyIterator.next();
                String propertyKey = (String) property.getKey();
                String propertyValue = (String) property.getKey();
                visitor.visitContributorProperty( propertyKey, propertyValue );
                propertyValue = visitor.replaceContributorProperty( propertyKey, propertyValue );
                if ( propertyValue == null )
                    propertyIterator.remove();
                else
                    property.setValue( propertyValue );
            }
        }
    }

    private void visitDependency( ModelVisitor visitor, Dependency dependency )
    {
        List<Exclusion> exclusions = dependency.getExclusions();
        if ( exclusions != null )
        {
            ListIterator<Exclusion> exclusionIterator = exclusions.listIterator();
            while ( exclusionIterator.hasNext() )
            {
                Exclusion exclusion = exclusionIterator.next();
                visitor.visitDependencyExclusion( exclusion );
                exclusion = visitor.replaceDependencyExclusion( exclusion );
                if ( exclusion == null )
                    exclusionIterator.remove();
                else
                    exclusionIterator.set( exclusion );
            }
        }
    }

    private void visitDependencyManagement( ModelVisitor visitor, DependencyManagement dependencyManagement )
    {
        List<Dependency> dependencies = dependencyManagement.getDependencies();
        if ( dependencies != null )
        {
            ListIterator<Dependency> dependencyIterator = dependencies.listIterator();
            while ( dependencyIterator.hasNext() )
            {
                Dependency dependency = dependencyIterator.next();
                visitor.visitDependencyManagementDependency( dependency );
                visitDependencyManagementDependency( visitor, dependency );
                dependency = visitor.replaceDependencyManagementDependency( dependency );
                if ( dependency == null )
                    dependencyIterator.remove();
                else
                    dependencyIterator.set( dependency );
            }
        }
    }

    private void visitDependencyManagementDependency( ModelVisitor visitor, Dependency dependency )
    {
        List<Exclusion> exclusions = dependency.getExclusions();
        if ( exclusions != null )
        {
            ListIterator<Exclusion> exclusionIterator = exclusions.listIterator();
            while ( exclusionIterator.hasNext() )
            {
                Exclusion exclusion = exclusionIterator.next();
                visitor.visitDependencyManagementDependencyExclusion( exclusion );
                exclusion = visitor.replaceDependencyManagementDependencyExclusion( exclusion );
                if ( exclusion == null )
                    exclusionIterator.remove();
                else
                    exclusionIterator.set( exclusion );
            }
        }
    }

    private void visitDeveloper( ModelVisitor visitor, Developer developer )
    {
        List<String> roles = developer.getRoles();
        if ( roles != null )
        {
            ListIterator<String> roleIterator = roles.listIterator();
            while ( roleIterator.hasNext() )
            {
                String role = roleIterator.next();
                visitor.visitDeveloperRole( role );
                role = visitor.replaceDeveloperRole( role );
                if ( role == null )
                    roleIterator.remove();
                else
                    roleIterator.set( role );
            }
        }

        Properties properties = developer.getProperties();
        if ( properties != null )
        {
            Iterator<Entry<Object, Object>> propertyIterator = properties.entrySet().iterator();
            while ( propertyIterator.hasNext() )
            {
                Entry<Object, Object> property = propertyIterator.next();
                String propertyKey = (String) property.getKey();
                String propertyValue = (String) property.getKey();
                visitor.visitDeveloperProperty( propertyKey, propertyValue );
                propertyValue = visitor.replaceDeveloperProperty( propertyKey, propertyValue );
                if ( propertyValue == null )
                    propertyIterator.remove();
                else
                    property.setValue( propertyValue );
            }
        }
    }

    private void visitDistributionManagement( ModelVisitor visitor, DistributionManagement distributionManagement )
    {
        DeploymentRepository repository = distributionManagement.getRepository();
        if ( repository != null )
        {
            visitor.visitDistributionManagementRepository( repository );
            visitDistributionManagementRepository( visitor, repository );
            repository = visitor.replaceDistributionManagementRepository( repository );
            distributionManagement.setRepository( repository );
        }

        DeploymentRepository snapshotRepository = distributionManagement.getSnapshotRepository();
        if ( snapshotRepository != null )
        {
            visitor.visitDistributionManagementSnapshotRepository( snapshotRepository );
            visitDistributionManagementSnapshotRepository( visitor, snapshotRepository );
            snapshotRepository = visitor.replaceDistributionManagementSnapshotRepository( snapshotRepository );
            distributionManagement.setSnapshotRepository( snapshotRepository );
        }

        Site site = distributionManagement.getSite();
        if ( site != null )
        {
            visitor.visitDistributionManagementSite( site );
            site = visitor.replaceDistributionManagementSite( site );
            distributionManagement.setSite( site );
        }

        Relocation relocation = distributionManagement.getRelocation();
        if ( relocation != null )
        {
            visitor.visitDistributionManagementRelocation( relocation );
            relocation = visitor.replaceDistributionManagementRelocation( relocation );
            distributionManagement.setRelocation( relocation );
        }
    }

    private void visitDistributionManagementRepository( ModelVisitor visitor, DeploymentRepository deploymentRepository )
    {
        RepositoryPolicy releases = deploymentRepository.getReleases();
        if ( releases != null )
        {
            visitor.visitDistributionManagementRepositoryRelease( releases );
            releases = visitor.replaceDistributionManagementRepositoryRelease( releases );
            deploymentRepository.setReleases( releases );
        }

        RepositoryPolicy snapshots = deploymentRepository.getSnapshots();
        if ( snapshots != null )
        {
            visitor.visitDistributionManagementRepositorySnapshot( snapshots );
            snapshots = visitor.replaceDistributionManagementRepositorySnapshot( snapshots );
            deploymentRepository.setSnapshots( snapshots );
        }
    }

    private void visitDistributionManagementSnapshotRepository( ModelVisitor visitor,
                                                                DeploymentRepository deploymentRepository )
    {
        RepositoryPolicy releases = deploymentRepository.getReleases();
        if ( releases != null )
        {
            visitor.visitDistributionManagementSnapshotRepositoryRelease( releases );
            releases = visitor.replaceDistributionManagementSnapshotRepositoryRelease( releases );
            deploymentRepository.setReleases( releases );
        }

        RepositoryPolicy snapshots = deploymentRepository.getSnapshots();
        if ( snapshots != null )
        {
            visitor.visitDistributionManagementSnapshotRepositorySnapshot( snapshots );
            snapshots = visitor.replaceDistributionManagementSnapshotRepositorySnapshot( snapshots );
            deploymentRepository.setSnapshots( snapshots );
        }
    }

    private void visitMailingList( ModelVisitor visitor, MailingList mailingList )
    {
        List<String> otherArchives = mailingList.getOtherArchives();
        if ( otherArchives != null )
        {
            ListIterator<String> otherArchiveIterator = otherArchives.listIterator();
            while ( otherArchiveIterator.hasNext() )
            {
                String otherArchive = otherArchiveIterator.next();
                visitor.visitMailingListOtherArchive( otherArchive );
                otherArchive = visitor.replaceMailingListOtherArchive( otherArchive );
                if ( otherArchive == null )
                    otherArchiveIterator.remove();
                else
                    otherArchiveIterator.set( otherArchive );
            }
        }
    }

    private void visitPluginRepository( ModelVisitor visitor, Repository repository )
    {
        RepositoryPolicy releases = repository.getReleases();
        if ( releases != null )
        {
            visitor.visitPluginRepositoryRelease( releases );
            releases = visitor.replacePluginRepositoryRelease( releases );
            repository.setReleases( releases );
        }

        RepositoryPolicy snapshots = repository.getSnapshots();
        if ( snapshots != null )
        {
            visitor.visitPluginRepositorySnapshot( snapshots );
            snapshots = visitor.replacePluginRepositorySnapshot( snapshots );
            repository.setSnapshots( snapshots );
        }
    }

    private void visitProfile( ModelVisitor visitor, Profile profile )
    {
        Activation activation = profile.getActivation();
        if ( activation != null )
        {
            visitor.visitProfileActivation( activation );
            visitProfileActivation( visitor, activation );
            activation = visitor.replaceProfileActivation( activation );
            profile.setActivation( activation );
        }

        BuildBase build = profile.getBuild();
        if ( build != null )
        {
            visitor.visitProfileBuild( build );
            visitProfileBuild( visitor, build );
            build = visitor.replaceProfileBuild( build );
            profile.setBuild( build );
        }

        List<String> modules = profile.getModules();
        if ( modules != null )
        {
            ListIterator<String> moduleIterator = modules.listIterator();
            while ( moduleIterator.hasNext() )
            {
                String module = moduleIterator.next();
                visitor.visitProfileModule( module );
                module = visitor.replaceProfileModule( module );
                if ( module == null )
                    moduleIterator.remove();
                else
                    moduleIterator.set( module );
            }
        }

        DistributionManagement distributionManagement = profile.getDistributionManagement();
        if ( distributionManagement != null )
        {
            visitor.visitProfileDistributionManagement( distributionManagement );
            visitProfileDistributionManagement( visitor, distributionManagement );
            distributionManagement = visitor.replaceProfileDistributionManagement( distributionManagement );
            profile.setDistributionManagement( distributionManagement );
        }

        Properties properties = profile.getProperties();
        if ( properties != null )
        {
            Iterator<Entry<Object, Object>> propertyIterator = properties.entrySet().iterator();
            while ( propertyIterator.hasNext() )
            {
                Entry<Object, Object> property = propertyIterator.next();
                String propertyKey = (String) property.getKey();
                String propertyValue = (String) property.getKey();
                visitor.visitProfileProperty( propertyKey, propertyValue );
                propertyValue = visitor.replaceProfileProperty( propertyKey, propertyValue );
                if ( propertyValue == null )
                    propertyIterator.remove();
                else
                    property.setValue( propertyValue );
            }
        }

        DependencyManagement dependencyManagement = profile.getDependencyManagement();
        if ( dependencyManagement != null )
        {
            visitor.visitProfileDependencyManagement( dependencyManagement );
            visitProfileDependencyManagement( visitor, dependencyManagement );
            dependencyManagement = visitor.replaceProfileDependencyManagement( dependencyManagement );
            profile.setDependencyManagement( dependencyManagement );
        }

        List<Dependency> dependencies = profile.getDependencies();
        if ( dependencies != null )
        {
            ListIterator<Dependency> dependencyIterator = dependencies.listIterator();
            while ( dependencyIterator.hasNext() )
            {
                Dependency dependency = dependencyIterator.next();
                visitor.visitProfileDependency( dependency );
                visitProfileDependency( visitor, dependency );
                dependency = visitor.replaceProfileDependency( dependency );
                if ( dependency == null )
                    dependencyIterator.remove();
                else
                    dependencyIterator.set( dependency );
            }
        }

        List<Repository> repositories = profile.getRepositories();
        if ( repositories != null )
        {
            ListIterator<Repository> repositoryIterator = repositories.listIterator();
            while ( repositoryIterator.hasNext() )
            {
                Repository repository = repositoryIterator.next();
                visitor.visitProfileRepository( repository );
                visitProfileRepository( visitor, repository );
                repository = visitor.replaceProfileRepository( repository );
                if ( repository == null )
                    repositoryIterator.remove();
                else
                    repositoryIterator.set( repository );
            }
        }

        List<Repository> pluginRepositories = profile.getPluginRepositories();
        if ( pluginRepositories != null )
        {
            ListIterator<Repository> pluginRepositoryIterator = pluginRepositories.listIterator();
            while ( pluginRepositoryIterator.hasNext() )
            {
                Repository pluginRepository = pluginRepositoryIterator.next();
                visitor.visitProfilePluginRepository( pluginRepository );
                visitProfilePluginRepository( visitor, pluginRepository );
                pluginRepository = visitor.replaceProfilePluginRepository( pluginRepository );
                if ( pluginRepository == null )
                    pluginRepositoryIterator.remove();
                else
                    pluginRepositoryIterator.set( pluginRepository );
            }
        }

        Reporting reporting = profile.getReporting();
        if ( reporting != null )
        {
            visitor.visitProfileReporting( reporting );
            visitProfileReporting( visitor, reporting );
            reporting = visitor.replaceProfileReporting( reporting );
            profile.setReporting( reporting );
        }
    }

    private void visitProfileActivation( ModelVisitor visitor, Activation activation )
    {
        ActivationOS os = activation.getOs();
        if ( os != null )
        {
            visitor.visitProfileActivationO( os );
            os = visitor.replaceProfileActivationO( os );
            activation.setOs( os );
        }

        ActivationProperty property = activation.getProperty();
        if ( property != null )
        {
            visitor.visitProfileActivationProperty( property );
            property = visitor.replaceProfileActivationProperty( property );
            activation.setProperty( property );
        }

        ActivationFile file = activation.getFile();
        if ( file != null )
        {
            visitor.visitProfileActivationFile( file );
            file = visitor.replaceProfileActivationFile( file );
            activation.setFile( file );
        }
    }

    private void visitProfileBuild( ModelVisitor visitor, BuildBase buildBase )
    {
        List<Resource> resources = buildBase.getResources();
        if ( resources != null )
        {
            ListIterator<Resource> resourceIterator = resources.listIterator();
            while ( resourceIterator.hasNext() )
            {
                Resource resource = resourceIterator.next();
                visitor.visitProfileBuildResource( resource );
                visitProfileBuildResource( visitor, resource );
                resource = visitor.replaceProfileBuildResource( resource );
                if ( resource == null )
                    resourceIterator.remove();
                else
                    resourceIterator.set( resource );
            }
        }

        List<Resource> testResources = buildBase.getTestResources();
        if ( testResources != null )
        {
            ListIterator<Resource> testResourceIterator = testResources.listIterator();
            while ( testResourceIterator.hasNext() )
            {
                Resource testResource = testResourceIterator.next();
                visitor.visitProfileBuildTestResource( testResource );
                visitProfileBuildTestResource( visitor, testResource );
                testResource = visitor.replaceProfileBuildTestResource( testResource );
                if ( testResource == null )
                    testResourceIterator.remove();
                else
                    testResourceIterator.set( testResource );
            }
        }

        List<String> filters = buildBase.getFilters();
        if ( filters != null )
        {
            ListIterator<String> filterIterator = filters.listIterator();
            while ( filterIterator.hasNext() )
            {
                String filter = filterIterator.next();
                visitor.visitProfileBuildFilter( filter );
                filter = visitor.replaceProfileBuildFilter( filter );
                if ( filter == null )
                    filterIterator.remove();
                else
                    filterIterator.set( filter );
            }
        }

        PluginManagement pluginManagement = buildBase.getPluginManagement();
        if ( pluginManagement != null )
        {
            visitor.visitProfileBuildPluginManagement( pluginManagement );
            visitProfileBuildPluginManagement( visitor, pluginManagement );
            pluginManagement = visitor.replaceProfileBuildPluginManagement( pluginManagement );
            buildBase.setPluginManagement( pluginManagement );
        }

        List<Plugin> plugins = buildBase.getPlugins();
        if ( plugins != null )
        {
            ListIterator<Plugin> pluginIterator = plugins.listIterator();
            while ( pluginIterator.hasNext() )
            {
                Plugin plugin = pluginIterator.next();
                visitor.visitProfileBuildPlugin( plugin );
                visitProfileBuildPlugin( visitor, plugin );
                plugin = visitor.replaceProfileBuildPlugin( plugin );
                if ( plugin == null )
                    pluginIterator.remove();
                else
                    pluginIterator.set( plugin );
            }
        }
    }

    private void visitProfileBuildPlugin( ModelVisitor visitor, Plugin plugin )
    {
        List<PluginExecution> executions = plugin.getExecutions();
        if ( executions != null )
        {
            ListIterator<PluginExecution> executionIterator = executions.listIterator();
            while ( executionIterator.hasNext() )
            {
                PluginExecution execution = executionIterator.next();
                visitor.visitProfileBuildPluginExecution( execution );
                visitProfileBuildPluginExecution( visitor, execution );
                execution = visitor.replaceProfileBuildPluginExecution( execution );
                if ( execution == null )
                    executionIterator.remove();
                else
                    executionIterator.set( execution );
            }
        }

        List<Dependency> dependencies = plugin.getDependencies();
        if ( dependencies != null )
        {
            ListIterator<Dependency> dependencyIterator = dependencies.listIterator();
            while ( dependencyIterator.hasNext() )
            {
                Dependency dependency = dependencyIterator.next();
                visitor.visitProfileBuildPluginDependency( dependency );
                visitProfileBuildPluginDependency( visitor, dependency );
                dependency = visitor.replaceProfileBuildPluginDependency( dependency );
                if ( dependency == null )
                    dependencyIterator.remove();
                else
                    dependencyIterator.set( dependency );
            }
        }
    }

    private void visitProfileBuildPluginDependency( ModelVisitor visitor, Dependency dependency )
    {
        List<Exclusion> exclusions = dependency.getExclusions();
        if ( exclusions != null )
        {
            ListIterator<Exclusion> exclusionIterator = exclusions.listIterator();
            while ( exclusionIterator.hasNext() )
            {
                Exclusion exclusion = exclusionIterator.next();
                visitor.visitProfileBuildPluginDependencyExclusion( exclusion );
                exclusion = visitor.replaceProfileBuildPluginDependencyExclusion( exclusion );
                if ( exclusion == null )
                    exclusionIterator.remove();
                else
                    exclusionIterator.set( exclusion );
            }
        }
    }

    private void visitProfileBuildPluginExecution( ModelVisitor visitor, PluginExecution pluginExecution )
    {
        List<String> goals = pluginExecution.getGoals();
        if ( goals != null )
        {
            ListIterator<String> goalIterator = goals.listIterator();
            while ( goalIterator.hasNext() )
            {
                String goal = goalIterator.next();
                visitor.visitProfileBuildPluginExecutionGoal( goal );
                goal = visitor.replaceProfileBuildPluginExecutionGoal( goal );
                if ( goal == null )
                    goalIterator.remove();
                else
                    goalIterator.set( goal );
            }
        }
    }

    private void visitProfileBuildPluginManagement( ModelVisitor visitor, PluginManagement pluginManagement )
    {
        List<Plugin> plugins = pluginManagement.getPlugins();
        if ( plugins != null )
        {
            ListIterator<Plugin> pluginIterator = plugins.listIterator();
            while ( pluginIterator.hasNext() )
            {
                Plugin plugin = pluginIterator.next();
                visitor.visitProfileBuildPluginManagementPlugin( plugin );
                visitProfileBuildPluginManagementPlugin( visitor, plugin );
                plugin = visitor.replaceProfileBuildPluginManagementPlugin( plugin );
                if ( plugin == null )
                    pluginIterator.remove();
                else
                    pluginIterator.set( plugin );
            }
        }
    }

    private void visitProfileBuildPluginManagementPlugin( ModelVisitor visitor, Plugin plugin )
    {
        List<PluginExecution> executions = plugin.getExecutions();
        if ( executions != null )
        {
            ListIterator<PluginExecution> executionIterator = executions.listIterator();
            while ( executionIterator.hasNext() )
            {
                PluginExecution execution = executionIterator.next();
                visitor.visitProfileBuildPluginManagementPluginExecution( execution );
                visitProfileBuildPluginManagementPluginExecution( visitor, execution );
                execution = visitor.replaceProfileBuildPluginManagementPluginExecution( execution );
                if ( execution == null )
                    executionIterator.remove();
                else
                    executionIterator.set( execution );
            }
        }

        List<Dependency> dependencies = plugin.getDependencies();
        if ( dependencies != null )
        {
            ListIterator<Dependency> dependencyIterator = dependencies.listIterator();
            while ( dependencyIterator.hasNext() )
            {
                Dependency dependency = dependencyIterator.next();
                visitor.visitProfileBuildPluginManagementPluginDependency( dependency );
                visitProfileBuildPluginManagementPluginDependency( visitor, dependency );
                dependency = visitor.replaceProfileBuildPluginManagementPluginDependency( dependency );
                if ( dependency == null )
                    dependencyIterator.remove();
                else
                    dependencyIterator.set( dependency );
            }
        }
    }

    private void visitProfileBuildPluginManagementPluginDependency( ModelVisitor visitor, Dependency dependency )
    {
        List<Exclusion> exclusions = dependency.getExclusions();
        if ( exclusions != null )
        {
            ListIterator<Exclusion> exclusionIterator = exclusions.listIterator();
            while ( exclusionIterator.hasNext() )
            {
                Exclusion exclusion = exclusionIterator.next();
                visitor.visitProfileBuildPluginManagementPluginDependencyExclusion( exclusion );
                exclusion = visitor.replaceProfileBuildPluginManagementPluginDependencyExclusion( exclusion );
                if ( exclusion == null )
                    exclusionIterator.remove();
                else
                    exclusionIterator.set( exclusion );
            }
        }
    }

    private void visitProfileBuildPluginManagementPluginExecution( ModelVisitor visitor, PluginExecution pluginExecution )
    {
        List<String> goals = pluginExecution.getGoals();
        if ( goals != null )
        {
            ListIterator<String> goalIterator = goals.listIterator();
            while ( goalIterator.hasNext() )
            {
                String goal = goalIterator.next();
                visitor.visitProfileBuildPluginManagementPluginExecutionGoal( goal );
                goal = visitor.replaceProfileBuildPluginManagementPluginExecutionGoal( goal );
                if ( goal == null )
                    goalIterator.remove();
                else
                    goalIterator.set( goal );
            }
        }
    }

    private void visitProfileBuildResource( ModelVisitor visitor, Resource resource )
    {
        List<String> includes = resource.getIncludes();
        if ( includes != null )
        {
            ListIterator<String> includeIterator = includes.listIterator();
            while ( includeIterator.hasNext() )
            {
                String include = includeIterator.next();
                visitor.visitProfileBuildResourceInclude( include );
                include = visitor.replaceProfileBuildResourceInclude( include );
                if ( include == null )
                    includeIterator.remove();
                else
                    includeIterator.set( include );
            }
        }

        List<String> excludes = resource.getExcludes();
        if ( excludes != null )
        {
            ListIterator<String> excludeIterator = excludes.listIterator();
            while ( excludeIterator.hasNext() )
            {
                String exclude = excludeIterator.next();
                visitor.visitProfileBuildResourceExclude( exclude );
                exclude = visitor.replaceProfileBuildResourceExclude( exclude );
                if ( exclude == null )
                    excludeIterator.remove();
                else
                    excludeIterator.set( exclude );
            }
        }
    }

    private void visitProfileBuildTestResource( ModelVisitor visitor, Resource resource )
    {
        List<String> includes = resource.getIncludes();
        if ( includes != null )
        {
            ListIterator<String> includeIterator = includes.listIterator();
            while ( includeIterator.hasNext() )
            {
                String include = includeIterator.next();
                visitor.visitProfileBuildTestResourceInclude( include );
                include = visitor.replaceProfileBuildTestResourceInclude( include );
                if ( include == null )
                    includeIterator.remove();
                else
                    includeIterator.set( include );
            }
        }

        List<String> excludes = resource.getExcludes();
        if ( excludes != null )
        {
            ListIterator<String> excludeIterator = excludes.listIterator();
            while ( excludeIterator.hasNext() )
            {
                String exclude = excludeIterator.next();
                visitor.visitProfileBuildTestResourceExclude( exclude );
                exclude = visitor.replaceProfileBuildTestResourceExclude( exclude );
                if ( exclude == null )
                    excludeIterator.remove();
                else
                    excludeIterator.set( exclude );
            }
        }
    }

    private void visitProfileDependency( ModelVisitor visitor, Dependency dependency )
    {
        List<Exclusion> exclusions = dependency.getExclusions();
        if ( exclusions != null )
        {
            ListIterator<Exclusion> exclusionIterator = exclusions.listIterator();
            while ( exclusionIterator.hasNext() )
            {
                Exclusion exclusion = exclusionIterator.next();
                visitor.visitProfileDependencyExclusion( exclusion );
                exclusion = visitor.replaceProfileDependencyExclusion( exclusion );
                if ( exclusion == null )
                    exclusionIterator.remove();
                else
                    exclusionIterator.set( exclusion );
            }
        }
    }

    private void visitProfileDependencyManagement( ModelVisitor visitor, DependencyManagement dependencyManagement )
    {
        List<Dependency> dependencies = dependencyManagement.getDependencies();
        if ( dependencies != null )
        {
            ListIterator<Dependency> dependencyIterator = dependencies.listIterator();
            while ( dependencyIterator.hasNext() )
            {
                Dependency dependency = dependencyIterator.next();
                visitor.visitProfileDependencyManagementDependency( dependency );
                visitProfileDependencyManagementDependency( visitor, dependency );
                dependency = visitor.replaceProfileDependencyManagementDependency( dependency );
                if ( dependency == null )
                    dependencyIterator.remove();
                else
                    dependencyIterator.set( dependency );
            }
        }
    }

    private void visitProfileDependencyManagementDependency( ModelVisitor visitor, Dependency dependency )
    {
        List<Exclusion> exclusions = dependency.getExclusions();
        if ( exclusions != null )
        {
            ListIterator<Exclusion> exclusionIterator = exclusions.listIterator();
            while ( exclusionIterator.hasNext() )
            {
                Exclusion exclusion = exclusionIterator.next();
                visitor.visitProfileDependencyManagementDependencyExclusion( exclusion );
                exclusion = visitor.replaceProfileDependencyManagementDependencyExclusion( exclusion );
                if ( exclusion == null )
                    exclusionIterator.remove();
                else
                    exclusionIterator.set( exclusion );
            }
        }
    }

    private void visitProfileDistributionManagement( ModelVisitor visitor, DistributionManagement distributionManagement )
    {
        DeploymentRepository repository = distributionManagement.getRepository();
        if ( repository != null )
        {
            visitor.visitProfileDistributionManagementRepository( repository );
            visitProfileDistributionManagementRepository( visitor, repository );
            repository = visitor.replaceProfileDistributionManagementRepository( repository );
            distributionManagement.setRepository( repository );
        }

        DeploymentRepository snapshotRepository = distributionManagement.getSnapshotRepository();
        if ( snapshotRepository != null )
        {
            visitor.visitProfileDistributionManagementSnapshotRepository( snapshotRepository );
            visitProfileDistributionManagementSnapshotRepository( visitor, snapshotRepository );
            snapshotRepository = visitor.replaceProfileDistributionManagementSnapshotRepository( snapshotRepository );
            distributionManagement.setSnapshotRepository( snapshotRepository );
        }

        Site site = distributionManagement.getSite();
        if ( site != null )
        {
            visitor.visitProfileDistributionManagementSite( site );
            site = visitor.replaceProfileDistributionManagementSite( site );
            distributionManagement.setSite( site );
        }

        Relocation relocation = distributionManagement.getRelocation();
        if ( relocation != null )
        {
            visitor.visitProfileDistributionManagementRelocation( relocation );
            relocation = visitor.replaceProfileDistributionManagementRelocation( relocation );
            distributionManagement.setRelocation( relocation );
        }
    }

    private void visitProfileDistributionManagementRepository( ModelVisitor visitor,
                                                               DeploymentRepository deploymentRepository )
    {
        RepositoryPolicy releases = deploymentRepository.getReleases();
        if ( releases != null )
        {
            visitor.visitProfileDistributionManagementRepositoryRelease( releases );
            releases = visitor.replaceProfileDistributionManagementRepositoryRelease( releases );
            deploymentRepository.setReleases( releases );
        }

        RepositoryPolicy snapshots = deploymentRepository.getSnapshots();
        if ( snapshots != null )
        {
            visitor.visitProfileDistributionManagementRepositorySnapshot( snapshots );
            snapshots = visitor.replaceProfileDistributionManagementRepositorySnapshot( snapshots );
            deploymentRepository.setSnapshots( snapshots );
        }
    }

    private void visitProfileDistributionManagementSnapshotRepository( ModelVisitor visitor,
                                                                       DeploymentRepository deploymentRepository )
    {
        RepositoryPolicy releases = deploymentRepository.getReleases();
        if ( releases != null )
        {
            visitor.visitProfileDistributionManagementSnapshotRepositoryRelease( releases );
            releases = visitor.replaceProfileDistributionManagementSnapshotRepositoryRelease( releases );
            deploymentRepository.setReleases( releases );
        }

        RepositoryPolicy snapshots = deploymentRepository.getSnapshots();
        if ( snapshots != null )
        {
            visitor.visitProfileDistributionManagementSnapshotRepositorySnapshot( snapshots );
            snapshots = visitor.replaceProfileDistributionManagementSnapshotRepositorySnapshot( snapshots );
            deploymentRepository.setSnapshots( snapshots );
        }
    }

    private void visitProfilePluginRepository( ModelVisitor visitor, Repository repository )
    {
        RepositoryPolicy releases = repository.getReleases();
        if ( releases != null )
        {
            visitor.visitProfilePluginRepositoryRelease( releases );
            releases = visitor.replaceProfilePluginRepositoryRelease( releases );
            repository.setReleases( releases );
        }

        RepositoryPolicy snapshots = repository.getSnapshots();
        if ( snapshots != null )
        {
            visitor.visitProfilePluginRepositorySnapshot( snapshots );
            snapshots = visitor.replaceProfilePluginRepositorySnapshot( snapshots );
            repository.setSnapshots( snapshots );
        }
    }

    private void visitProfileReporting( ModelVisitor visitor, Reporting reporting )
    {
        List<ReportPlugin> plugins = reporting.getPlugins();
        if ( plugins != null )
        {
            ListIterator<ReportPlugin> pluginIterator = plugins.listIterator();
            while ( pluginIterator.hasNext() )
            {
                ReportPlugin plugin = pluginIterator.next();
                visitor.visitProfileReportingPlugin( plugin );
                visitProfileReportingPlugin( visitor, plugin );
                plugin = visitor.replaceProfileReportingPlugin( plugin );
                if ( plugin == null )
                    pluginIterator.remove();
                else
                    pluginIterator.set( plugin );
            }
        }
    }

    private void visitProfileReportingPlugin( ModelVisitor visitor, ReportPlugin reportPlugin )
    {
        List<ReportSet> reportSets = reportPlugin.getReportSets();
        if ( reportSets != null )
        {
            ListIterator<ReportSet> reportSetIterator = reportSets.listIterator();
            while ( reportSetIterator.hasNext() )
            {
                ReportSet reportSet = reportSetIterator.next();
                visitor.visitProfileReportingPluginReportSet( reportSet );
                visitProfileReportingPluginReportSet( visitor, reportSet );
                reportSet = visitor.replaceProfileReportingPluginReportSet( reportSet );
                if ( reportSet == null )
                    reportSetIterator.remove();
                else
                    reportSetIterator.set( reportSet );
            }
        }
    }

    private void visitProfileReportingPluginReportSet( ModelVisitor visitor, ReportSet reportSet )
    {
        List<String> reports = reportSet.getReports();
        if ( reports != null )
        {
            ListIterator<String> reportIterator = reports.listIterator();
            while ( reportIterator.hasNext() )
            {
                String report = reportIterator.next();
                visitor.visitProfileReportingPluginReportSetReport( report );
                report = visitor.replaceProfileReportingPluginReportSetReport( report );
                if ( report == null )
                    reportIterator.remove();
                else
                    reportIterator.set( report );
            }
        }
    }

    private void visitProfileRepository( ModelVisitor visitor, Repository repository )
    {
        RepositoryPolicy releases = repository.getReleases();
        if ( releases != null )
        {
            visitor.visitProfileRepositoryRelease( releases );
            releases = visitor.replaceProfileRepositoryRelease( releases );
            repository.setReleases( releases );
        }

        RepositoryPolicy snapshots = repository.getSnapshots();
        if ( snapshots != null )
        {
            visitor.visitProfileRepositorySnapshot( snapshots );
            snapshots = visitor.replaceProfileRepositorySnapshot( snapshots );
            repository.setSnapshots( snapshots );
        }
    }

    private void visitReporting( ModelVisitor visitor, Reporting reporting )
    {
        List<ReportPlugin> plugins = reporting.getPlugins();
        if ( plugins != null )
        {
            ListIterator<ReportPlugin> pluginIterator = plugins.listIterator();
            while ( pluginIterator.hasNext() )
            {
                ReportPlugin plugin = pluginIterator.next();
                visitor.visitReportingPlugin( plugin );
                visitReportingPlugin( visitor, plugin );
                plugin = visitor.replaceReportingPlugin( plugin );
                if ( plugin == null )
                    pluginIterator.remove();
                else
                    pluginIterator.set( plugin );
            }
        }
    }

    private void visitReportingPlugin( ModelVisitor visitor, ReportPlugin reportPlugin )
    {
        List<ReportSet> reportSets = reportPlugin.getReportSets();
        if ( reportSets != null )
        {
            ListIterator<ReportSet> reportSetIterator = reportSets.listIterator();
            while ( reportSetIterator.hasNext() )
            {
                ReportSet reportSet = reportSetIterator.next();
                visitor.visitReportingPluginReportSet( reportSet );
                visitReportingPluginReportSet( visitor, reportSet );
                reportSet = visitor.replaceReportingPluginReportSet( reportSet );
                if ( reportSet == null )
                    reportSetIterator.remove();
                else
                    reportSetIterator.set( reportSet );
            }
        }
    }

    private void visitReportingPluginReportSet( ModelVisitor visitor, ReportSet reportSet )
    {
        List<String> reports = reportSet.getReports();
        if ( reports != null )
        {
            ListIterator<String> reportIterator = reports.listIterator();
            while ( reportIterator.hasNext() )
            {
                String report = reportIterator.next();
                visitor.visitReportingPluginReportSetReport( report );
                report = visitor.replaceReportingPluginReportSetReport( report );
                if ( report == null )
                    reportIterator.remove();
                else
                    reportIterator.set( report );
            }
        }
    }

    private void visitRepository( ModelVisitor visitor, Repository repository )
    {
        RepositoryPolicy releases = repository.getReleases();
        if ( releases != null )
        {
            visitor.visitRepositoryRelease( releases );
            releases = visitor.replaceRepositoryRelease( releases );
            repository.setReleases( releases );
        }

        RepositoryPolicy snapshots = repository.getSnapshots();
        if ( snapshots != null )
        {
            visitor.visitRepositorySnapshot( snapshots );
            snapshots = visitor.replaceRepositorySnapshot( snapshots );
            repository.setSnapshots( snapshots );
        }
    }
}
