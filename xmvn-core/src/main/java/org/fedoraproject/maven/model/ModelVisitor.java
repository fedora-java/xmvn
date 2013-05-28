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
package org.fedoraproject.maven.model;

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

/**
 * @author Mikolaj Izdebski
 */
public interface ModelVisitor
{
    Build replaceBuild( Build build );

    Extension replaceBuildExtension( Extension extension );

    String replaceBuildFilter( String filter );

    Plugin replaceBuildPlugin( Plugin plugin );

    Dependency replaceBuildPluginDependency( Dependency dependency );

    Exclusion replaceBuildPluginDependencyExclusion( Exclusion exclusion );

    PluginExecution replaceBuildPluginExecution( PluginExecution execution );

    String replaceBuildPluginExecutionGoal( String goal );

    PluginManagement replaceBuildPluginManagement( PluginManagement pluginManagement );

    Plugin replaceBuildPluginManagementPlugin( Plugin plugin );

    Dependency replaceBuildPluginManagementPluginDependency( Dependency dependency );

    Exclusion replaceBuildPluginManagementPluginDependencyExclusion( Exclusion exclusion );

    PluginExecution replaceBuildPluginManagementPluginExecution( PluginExecution execution );

    String replaceBuildPluginManagementPluginExecutionGoal( String goal );

    Resource replaceBuildResource( Resource resource );

    String replaceBuildResourceExclude( String exclude );

    String replaceBuildResourceInclude( String include );

    Resource replaceBuildTestResource( Resource testResource );

    String replaceBuildTestResourceExclude( String exclude );

    String replaceBuildTestResourceInclude( String include );

    CiManagement replaceCiManagement( CiManagement ciManagement );

    Notifier replaceCiManagementNotifier( Notifier notifier );

    String replaceCiManagementNotifierConfiguration( String configurationElementKey, String configurationElementValue );

    Contributor replaceContributor( Contributor contributor );

    String replaceContributorProperty( String propertyKey, String propertyValue );

    String replaceContributorRole( String role );

    Dependency replaceDependency( Dependency dependency );

    Exclusion replaceDependencyExclusion( Exclusion exclusion );

    DependencyManagement replaceDependencyManagement( DependencyManagement dependencyManagement );

    Dependency replaceDependencyManagementDependency( Dependency dependency );

    Exclusion replaceDependencyManagementDependencyExclusion( Exclusion exclusion );

    Developer replaceDeveloper( Developer developer );

    String replaceDeveloperProperty( String propertyKey, String propertyValue );

    String replaceDeveloperRole( String role );

    DistributionManagement replaceDistributionManagement( DistributionManagement distributionManagement );

    Relocation replaceDistributionManagementRelocation( Relocation relocation );

    DeploymentRepository replaceDistributionManagementRepository( DeploymentRepository repository );

    RepositoryPolicy replaceDistributionManagementRepositoryRelease( RepositoryPolicy releases );

    RepositoryPolicy replaceDistributionManagementRepositorySnapshot( RepositoryPolicy snapshots );

    Site replaceDistributionManagementSite( Site site );

    DeploymentRepository replaceDistributionManagementSnapshotRepository( DeploymentRepository snapshotRepository );

    RepositoryPolicy replaceDistributionManagementSnapshotRepositoryRelease( RepositoryPolicy releases );

    RepositoryPolicy replaceDistributionManagementSnapshotRepositorySnapshot( RepositoryPolicy snapshots );

    IssueManagement replaceIssueManagement( IssueManagement issueManagement );

    License replaceLicense( License license );

    MailingList replaceMailingList( MailingList mailingList );

    String replaceMailingListOtherArchive( String otherArchive );

    String replaceModule( String module );

    Organization replaceOrganization( Organization organization );

    Parent replaceParent( Parent parent );

    Repository replacePluginRepository( Repository pluginRepository );

    RepositoryPolicy replacePluginRepositoryRelease( RepositoryPolicy releases );

    RepositoryPolicy replacePluginRepositorySnapshot( RepositoryPolicy snapshots );

    Prerequisites replacePrerequisite( Prerequisites prerequisites );

    Profile replaceProfile( Profile profile );

    Activation replaceProfileActivation( Activation activation );

    ActivationFile replaceProfileActivationFile( ActivationFile file );

    ActivationOS replaceProfileActivationO( ActivationOS os );

    ActivationProperty replaceProfileActivationProperty( ActivationProperty property );

    BuildBase replaceProfileBuild( BuildBase build );

    String replaceProfileBuildFilter( String filter );

    Plugin replaceProfileBuildPlugin( Plugin plugin );

    Dependency replaceProfileBuildPluginDependency( Dependency dependency );

    Exclusion replaceProfileBuildPluginDependencyExclusion( Exclusion exclusion );

    PluginExecution replaceProfileBuildPluginExecution( PluginExecution execution );

    String replaceProfileBuildPluginExecutionGoal( String goal );

    PluginManagement replaceProfileBuildPluginManagement( PluginManagement pluginManagement );

    Plugin replaceProfileBuildPluginManagementPlugin( Plugin plugin );

    Dependency replaceProfileBuildPluginManagementPluginDependency( Dependency dependency );

    Exclusion replaceProfileBuildPluginManagementPluginDependencyExclusion( Exclusion exclusion );

    PluginExecution replaceProfileBuildPluginManagementPluginExecution( PluginExecution execution );

    String replaceProfileBuildPluginManagementPluginExecutionGoal( String goal );

    Resource replaceProfileBuildResource( Resource resource );

    String replaceProfileBuildResourceExclude( String exclude );

    String replaceProfileBuildResourceInclude( String include );

    Resource replaceProfileBuildTestResource( Resource testResource );

    String replaceProfileBuildTestResourceExclude( String exclude );

    String replaceProfileBuildTestResourceInclude( String include );

    Dependency replaceProfileDependency( Dependency dependency );

    Exclusion replaceProfileDependencyExclusion( Exclusion exclusion );

    DependencyManagement replaceProfileDependencyManagement( DependencyManagement dependencyManagement );

    Dependency replaceProfileDependencyManagementDependency( Dependency dependency );

    Exclusion replaceProfileDependencyManagementDependencyExclusion( Exclusion exclusion );

    DistributionManagement replaceProfileDistributionManagement( DistributionManagement distributionManagement );

    Relocation replaceProfileDistributionManagementRelocation( Relocation relocation );

    DeploymentRepository replaceProfileDistributionManagementRepository( DeploymentRepository repository );

    RepositoryPolicy replaceProfileDistributionManagementRepositoryRelease( RepositoryPolicy releases );

    RepositoryPolicy replaceProfileDistributionManagementRepositorySnapshot( RepositoryPolicy snapshots );

    Site replaceProfileDistributionManagementSite( Site site );

    DeploymentRepository replaceProfileDistributionManagementSnapshotRepository( DeploymentRepository snapshotRepository );

    RepositoryPolicy replaceProfileDistributionManagementSnapshotRepositoryRelease( RepositoryPolicy releases );

    RepositoryPolicy replaceProfileDistributionManagementSnapshotRepositorySnapshot( RepositoryPolicy snapshots );

    String replaceProfileModule( String module );

    Repository replaceProfilePluginRepository( Repository pluginRepository );

    RepositoryPolicy replaceProfilePluginRepositoryRelease( RepositoryPolicy releases );

    RepositoryPolicy replaceProfilePluginRepositorySnapshot( RepositoryPolicy snapshots );

    String replaceProfileProperty( String propertyKey, String propertyValue );

    Reporting replaceProfileReporting( Reporting reporting );

    ReportPlugin replaceProfileReportingPlugin( ReportPlugin plugin );

    ReportSet replaceProfileReportingPluginReportSet( ReportSet reportSet );

    String replaceProfileReportingPluginReportSetReport( String report );

    Repository replaceProfileRepository( Repository repository );

    RepositoryPolicy replaceProfileRepositoryRelease( RepositoryPolicy releases );

    RepositoryPolicy replaceProfileRepositorySnapshot( RepositoryPolicy snapshots );

    String replaceProperty( String propertyKey, String propertyValue );

    Reporting replaceReporting( Reporting reporting );

    ReportPlugin replaceReportingPlugin( ReportPlugin plugin );

    ReportSet replaceReportingPluginReportSet( ReportSet reportSet );

    String replaceReportingPluginReportSetReport( String report );

    Repository replaceRepository( Repository repository );

    RepositoryPolicy replaceRepositoryRelease( RepositoryPolicy releases );

    RepositoryPolicy replaceRepositorySnapshot( RepositoryPolicy snapshots );

    Scm replaceScm( Scm scm );

    void visitBuild( Build build );

    void visitBuildExtension( Extension extension );

    void visitBuildFilter( String filter );

    void visitBuildPlugin( Plugin plugin );

    void visitBuildPluginDependency( Dependency dependency );

    void visitBuildPluginDependencyExclusion( Exclusion exclusion );

    void visitBuildPluginExecution( PluginExecution execution );

    void visitBuildPluginExecutionGoal( String goal );

    void visitBuildPluginManagement( PluginManagement pluginManagement );

    void visitBuildPluginManagementPlugin( Plugin plugin );

    void visitBuildPluginManagementPluginDependency( Dependency dependency );

    void visitBuildPluginManagementPluginDependencyExclusion( Exclusion exclusion );

    void visitBuildPluginManagementPluginExecution( PluginExecution execution );

    void visitBuildPluginManagementPluginExecutionGoal( String goal );

    void visitBuildResource( Resource resource );

    void visitBuildResourceExclude( String exclude );

    void visitBuildResourceInclude( String include );

    void visitBuildTestResource( Resource testResource );

    void visitBuildTestResourceExclude( String exclude );

    void visitBuildTestResourceInclude( String include );

    void visitCiManagement( CiManagement ciManagement );

    void visitCiManagementNotifier( Notifier notifier );

    void visitCiManagementNotifierConfiguration( String configurationElementKey, String configurationElementValue );

    void visitContributor( Contributor contributor );

    void visitContributorProperty( String propertyKey, String propertyValue );

    void visitContributorRole( String role );

    void visitDependency( Dependency dependency );

    void visitDependencyExclusion( Exclusion exclusion );

    void visitDependencyManagement( DependencyManagement dependencyManagement );

    void visitDependencyManagementDependency( Dependency dependency );

    void visitDependencyManagementDependencyExclusion( Exclusion exclusion );

    void visitDeveloper( Developer developer );

    void visitDeveloperProperty( String propertyKey, String propertyValue );

    void visitDeveloperRole( String role );

    void visitDistributionManagement( DistributionManagement distributionManagement );

    void visitDistributionManagementRelocation( Relocation relocation );

    void visitDistributionManagementRepository( DeploymentRepository repository );

    void visitDistributionManagementRepositoryRelease( RepositoryPolicy releases );

    void visitDistributionManagementRepositorySnapshot( RepositoryPolicy snapshots );

    void visitDistributionManagementSite( Site site );

    void visitDistributionManagementSnapshotRepository( DeploymentRepository snapshotRepository );

    void visitDistributionManagementSnapshotRepositoryRelease( RepositoryPolicy releases );

    void visitDistributionManagementSnapshotRepositorySnapshot( RepositoryPolicy snapshots );

    void visitIssueManagement( IssueManagement issueManagement );

    void visitLicense( License license );

    void visitMailingList( MailingList mailingList );

    void visitMailingListOtherArchive( String otherArchive );

    void visitModule( String module );

    void visitOrganization( Organization organization );

    void visitParent( Parent parent );

    void visitPluginRepository( Repository pluginRepository );

    void visitPluginRepositoryRelease( RepositoryPolicy releases );

    void visitPluginRepositorySnapshot( RepositoryPolicy snapshots );

    void visitPrerequisite( Prerequisites prerequisites );

    void visitProfile( Profile profile );

    void visitProfileActivation( Activation activation );

    void visitProfileActivationFile( ActivationFile file );

    void visitProfileActivationO( ActivationOS os );

    void visitProfileActivationProperty( ActivationProperty property );

    void visitProfileBuild( BuildBase build );

    void visitProfileBuildFilter( String filter );

    void visitProfileBuildPlugin( Plugin plugin );

    void visitProfileBuildPluginDependency( Dependency dependency );

    void visitProfileBuildPluginDependencyExclusion( Exclusion exclusion );

    void visitProfileBuildPluginExecution( PluginExecution execution );

    void visitProfileBuildPluginExecutionGoal( String goal );

    void visitProfileBuildPluginManagement( PluginManagement pluginManagement );

    void visitProfileBuildPluginManagementPlugin( Plugin plugin );

    void visitProfileBuildPluginManagementPluginDependency( Dependency dependency );

    void visitProfileBuildPluginManagementPluginDependencyExclusion( Exclusion exclusion );

    void visitProfileBuildPluginManagementPluginExecution( PluginExecution execution );

    void visitProfileBuildPluginManagementPluginExecutionGoal( String goal );

    void visitProfileBuildResource( Resource resource );

    void visitProfileBuildResourceExclude( String exclude );

    void visitProfileBuildResourceInclude( String include );

    void visitProfileBuildTestResource( Resource testResource );

    void visitProfileBuildTestResourceExclude( String exclude );

    void visitProfileBuildTestResourceInclude( String include );

    void visitProfileDependency( Dependency dependency );

    void visitProfileDependencyExclusion( Exclusion exclusion );

    void visitProfileDependencyManagement( DependencyManagement dependencyManagement );

    void visitProfileDependencyManagementDependency( Dependency dependency );

    void visitProfileDependencyManagementDependencyExclusion( Exclusion exclusion );

    void visitProfileDistributionManagement( DistributionManagement distributionManagement );

    void visitProfileDistributionManagementRelocation( Relocation relocation );

    void visitProfileDistributionManagementRepository( DeploymentRepository repository );

    void visitProfileDistributionManagementRepositoryRelease( RepositoryPolicy releases );

    void visitProfileDistributionManagementRepositorySnapshot( RepositoryPolicy snapshots );

    void visitProfileDistributionManagementSite( Site site );

    void visitProfileDistributionManagementSnapshotRepository( DeploymentRepository snapshotRepository );

    void visitProfileDistributionManagementSnapshotRepositoryRelease( RepositoryPolicy releases );

    void visitProfileDistributionManagementSnapshotRepositorySnapshot( RepositoryPolicy snapshots );

    void visitProfileModule( String module );

    void visitProfilePluginRepository( Repository pluginRepository );

    void visitProfilePluginRepositoryRelease( RepositoryPolicy releases );

    void visitProfilePluginRepositorySnapshot( RepositoryPolicy snapshots );

    void visitProfileProperty( String propertyKey, String propertyValue );

    void visitProfileReporting( Reporting reporting );

    void visitProfileReportingPlugin( ReportPlugin plugin );

    void visitProfileReportingPluginReportSet( ReportSet reportSet );

    void visitProfileReportingPluginReportSetReport( String report );

    void visitProfileRepository( Repository repository );

    void visitProfileRepositoryRelease( RepositoryPolicy releases );

    void visitProfileRepositorySnapshot( RepositoryPolicy snapshots );

    void visitProject( Model model );

    void visitProperty( String propertyKey, String propertyValue );

    void visitReporting( Reporting reporting );

    void visitReportingPlugin( ReportPlugin plugin );

    void visitReportingPluginReportSet( ReportSet reportSet );

    void visitReportingPluginReportSetReport( String report );

    void visitRepository( Repository repository );

    void visitRepositoryRelease( RepositoryPolicy releases );

    void visitRepositorySnapshot( RepositoryPolicy snapshots );

    void visitScm( Scm scm );
}
