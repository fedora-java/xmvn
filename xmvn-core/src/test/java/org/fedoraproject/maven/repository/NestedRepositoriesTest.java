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
package org.fedoraproject.maven.repository;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.fedoraproject.maven.config.Configuration;
import org.fedoraproject.maven.config.Configurator;
import org.fedoraproject.maven.config.Repository;
import org.fedoraproject.maven.config.RepositoryConfigurator;
import org.fedoraproject.maven.config.Stereotype;

/**
 * @author Mikolaj Izdebski
 */
public class NestedRepositoriesTest
    extends PlexusTestCase
{
    private final Artifact POM = new DefaultArtifact( "JPP/sisu:sisu-plexus:pom:1.2.3" );

    private final Artifact POM2 = new DefaultArtifact( "JPP/plexus:utils:pom:3.0.14" );

    private final Artifact JAR = new DefaultArtifact( "JPP/sisu:sisu-plexus:1.2.3" );

    private final Artifact JAR2 = new DefaultArtifact( "JPP/plexus:utils:3.0.14" );

    private org.fedoraproject.maven.repository.Repository base;

    private org.fedoraproject.maven.repository.Repository addon;

    private void configureBaseEffectivePomRepository( Configuration configuration )
    {
        Repository repo = new Repository();
        repo.setId( "base-effective-pom" );
        repo.setType( "flat" );
        Stereotype stereotype = new Stereotype();
        stereotype.setExtension( "pom" );
        repo.addStereotype( stereotype );
        repo.addProperty( "root", "usr/share/maven-effective-poms" );
        configuration.addRepository( repo );
    }

    private void configureBasePomRepository( Configuration configuration )
    {
        Repository repo = new Repository();
        repo.setId( "base-pom" );
        repo.setType( "flat" );
        Stereotype stereotype = new Stereotype();
        stereotype.setExtension( "pom" );
        repo.addStereotype( stereotype );
        repo.addProperty( "root", "usr/share/maven-poms" );
        configuration.addRepository( repo );
    }

    private void configureBaseJniRepository( Configuration configuration )
    {
        Repository repo = new Repository();
        repo.setId( "base-jni" );
        repo.setType( "jpp" );
        Stereotype stereotype = new Stereotype();
        stereotype.setExtension( "jar" );
        repo.addStereotype( stereotype );
        repo.addProperty( "root", "usr/lib/java" );
        configuration.addRepository( repo );
    }

    private void configureBaseJarRepository( Configuration configuration )
    {
        Repository repo = new Repository();
        repo.setId( "base-jar" );
        repo.setType( "jpp" );
        Stereotype stereotype = new Stereotype();
        stereotype.setExtension( "jar" );
        repo.addStereotype( stereotype );
        repo.addProperty( "root", "usr/share/java" );
        configuration.addRepository( repo );
    }

    private void configureBaseRepository( Configuration configuration )
    {
        configureBaseEffectivePomRepository( configuration );
        configureBasePomRepository( configuration );
        configureBaseJniRepository( configuration );
        configureBaseJarRepository( configuration );

        Repository repo = new Repository();
        repo.setId( "base" );
        repo.setType( "compound" );
        Xpp3Dom child1 = new Xpp3Dom( "repository" );
        child1.setValue( "base-effective-pom" );
        Xpp3Dom child2 = new Xpp3Dom( "repository" );
        child2.setValue( "base-pom" );
        Xpp3Dom child3 = new Xpp3Dom( "repository" );
        child3.setValue( "base-jni" );
        Xpp3Dom child4 = new Xpp3Dom( "repository" );
        child4.setValue( "base-jar" );
        Xpp3Dom childreen = new Xpp3Dom( "repositories" );
        childreen.addChild( child1 );
        childreen.addChild( child2 );
        childreen.addChild( child3 );
        childreen.addChild( child4 );
        Xpp3Dom config = new Xpp3Dom( "configuration" );
        config.addChild( childreen );
        repo.setConfiguration( config );
        configuration.addRepository( repo );
    }

    private void configureAddonPrefixRepository( Configuration configuration )
    {
        Repository repo = new Repository();
        repo.setId( "addon-prefix" );
        repo.setType( "compound" );
        repo.addProperty( "prefix", "opt/rh/addon" );
        Xpp3Dom child = new Xpp3Dom( "repository" );
        child.setValue( "base" );
        Xpp3Dom childreen = new Xpp3Dom( "repositories" );
        childreen.addChild( child );
        Xpp3Dom config = new Xpp3Dom( "configuration" );
        config.addChild( childreen );
        repo.setConfiguration( config );
        configuration.addRepository( repo );
    }

    private void configureAddonRepository( Configuration configuration )
    {
        configureAddonPrefixRepository( configuration );

        Repository repo = new Repository();
        repo.setId( "addon" );
        repo.setType( "compound" );
        Xpp3Dom child1 = new Xpp3Dom( "repository" );
        child1.setValue( "addon-prefix" );
        Xpp3Dom child2 = new Xpp3Dom( "repository" );
        child2.setValue( "base" );
        Xpp3Dom childreen = new Xpp3Dom( "repositories" );
        childreen.addChild( child1 );
        childreen.addChild( child2 );
        Xpp3Dom config = new Xpp3Dom( "configuration" );
        config.addChild( childreen );
        repo.setConfiguration( config );
        configuration.addRepository( repo );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        Configurator configurator = lookup( Configurator.class );
        RepositoryConfigurator repositoryConfigurator = lookup( RepositoryConfigurator.class );

        Configuration configuration = configurator.getDefaultConfiguration();
        configureBaseRepository( configuration );
        configureAddonRepository( configuration );

        base = repositoryConfigurator.configureRepository( "base" );
        addon = repositoryConfigurator.configureRepository( "addon" );
    }

    /**
     * Test resolution of versioned POM artifacts from base repository.
     * 
     * @throws Exception
     */
    public void testBasePomVersioned()
        throws Exception
    {
        Iterator<RepositoryPath> it = base.getArtifactPaths( POM ).iterator();
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-effective-poms/JPP.sisu-sisu-plexus-1.2.3.pom" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-poms/JPP.sisu-sisu-plexus-1.2.3.pom" ), it.next().getPath() );
        assertFalse( it.hasNext() );
    }

    /**
     * Test resolution of versionless POM artifacts from base repository.
     * 
     * @throws Exception
     */
    public void testBasePomVersionless()
        throws Exception
    {
        Iterator<RepositoryPath> it = base.getArtifactPaths( POM.setVersion( "SYSTEM" ) ).iterator();
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-effective-poms/JPP.sisu-sisu-plexus.pom" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-poms/JPP.sisu-sisu-plexus.pom" ), it.next().getPath() );
        assertFalse( it.hasNext() );
    }

    /**
     * Test resolution of versioned JAR artifacts from base repository.
     * 
     * @throws Exception
     */
    public void testBaseJarVersioned()
        throws Exception
    {
        Iterator<RepositoryPath> it = base.getArtifactPaths( JAR ).iterator();
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/lib/java/sisu/sisu-plexus-1.2.3.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/java/sisu/sisu-plexus-1.2.3.jar" ), it.next().getPath() );
        assertFalse( it.hasNext() );
    }

    /**
     * Test resolution of versionless JAR artifacts from base repository.
     * 
     * @throws Exception
     */
    public void testBaseJarVersionless()
        throws Exception
    {
        Iterator<RepositoryPath> it = base.getArtifactPaths( JAR.setVersion( "SYSTEM" ) ).iterator();
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/lib/java/sisu/sisu-plexus.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/java/sisu/sisu-plexus.jar" ), it.next().getPath() );
        assertFalse( it.hasNext() );
    }

    /**
     * Test resolution of versioned POM artifacts from addon repository.
     * 
     * @throws Exception
     */
    public void testAddonPomVersioned()
        throws Exception
    {
        Iterator<RepositoryPath> it = addon.getArtifactPaths( POM ).iterator();
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/share/maven-effective-poms/JPP.sisu-sisu-plexus-1.2.3.pom" ),
                      it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/share/maven-poms/JPP.sisu-sisu-plexus-1.2.3.pom" ),
                      it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-effective-poms/JPP.sisu-sisu-plexus-1.2.3.pom" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-poms/JPP.sisu-sisu-plexus-1.2.3.pom" ), it.next().getPath() );
        assertFalse( it.hasNext() );
    }

    /**
     * Test resolution of versionless POM artifacts from addon repository.
     * 
     * @throws Exception
     */
    public void testAddonPomVersionless()
        throws Exception
    {
        Iterator<RepositoryPath> it = addon.getArtifactPaths( POM.setVersion( "SYSTEM" ) ).iterator();
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/share/maven-effective-poms/JPP.sisu-sisu-plexus.pom" ),
                      it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/share/maven-poms/JPP.sisu-sisu-plexus.pom" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-effective-poms/JPP.sisu-sisu-plexus.pom" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-poms/JPP.sisu-sisu-plexus.pom" ), it.next().getPath() );
        assertFalse( it.hasNext() );
    }

    /**
     * Test resolution of versioned JAR artifacts from addon repository.
     * 
     * @throws Exception
     */
    public void testAddonJarVersioned()
        throws Exception
    {
        Iterator<RepositoryPath> it = addon.getArtifactPaths( JAR ).iterator();
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/lib/java/sisu/sisu-plexus-1.2.3.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/share/java/sisu/sisu-plexus-1.2.3.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/lib/java/sisu/sisu-plexus-1.2.3.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/java/sisu/sisu-plexus-1.2.3.jar" ), it.next().getPath() );
        assertFalse( it.hasNext() );
    }

    /**
     * Test resolution of versionless JAR artifacts from addon repository.
     * 
     * @throws Exception
     */
    public void testAddonJarVersionless()
        throws Exception
    {
        Iterator<RepositoryPath> it = addon.getArtifactPaths( JAR.setVersion( "SYSTEM" ) ).iterator();
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/lib/java/sisu/sisu-plexus.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/share/java/sisu/sisu-plexus.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/lib/java/sisu/sisu-plexus.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/java/sisu/sisu-plexus.jar" ), it.next().getPath() );
        assertFalse( it.hasNext() );
    }

    /**
     * Test resolution of alternative versioned POM artifacts from base repository.
     * 
     * @throws Exception
     */
    public void testAlternativeBasePomVersioned()
        throws Exception
    {
        Iterator<RepositoryPath> it = base.getArtifactPaths( Arrays.asList( POM, POM2 ) ).iterator();
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-effective-poms/JPP.sisu-sisu-plexus-1.2.3.pom" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-effective-poms/JPP.plexus-utils-3.0.14.pom" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-poms/JPP.sisu-sisu-plexus-1.2.3.pom" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-poms/JPP.plexus-utils-3.0.14.pom" ), it.next().getPath() );
        assertFalse( it.hasNext() );
    }

    /**
     * Test resolution of alternative versionless POM artifacts from base repository.
     * 
     * @throws Exception
     */
    public void testAlternativeBasePomVersionless()
        throws Exception
    {
        Iterator<RepositoryPath> it =
            base.getArtifactPaths( Arrays.asList( POM.setVersion( "SYSTEM" ), POM2.setVersion( "SYSTEM" ) ) ).iterator();
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-effective-poms/JPP.sisu-sisu-plexus.pom" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-effective-poms/JPP.plexus-utils.pom" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-poms/JPP.sisu-sisu-plexus.pom" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-poms/JPP.plexus-utils.pom" ), it.next().getPath() );
        assertFalse( it.hasNext() );
    }

    /**
     * Test resolution of alternative versioned JAR artifacts from base repository.
     * 
     * @throws Exception
     */
    public void testAlternativeBaseJarVersioned()
        throws Exception
    {
        Iterator<RepositoryPath> it = base.getArtifactPaths( Arrays.asList( JAR, JAR2 ) ).iterator();
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/lib/java/sisu/sisu-plexus-1.2.3.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/lib/java/plexus/utils-3.0.14.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/java/sisu/sisu-plexus-1.2.3.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/java/plexus/utils-3.0.14.jar" ), it.next().getPath() );
        assertFalse( it.hasNext() );
    }

    /**
     * Test resolution of alternative versionless JAR artifacts from base repository.
     * 
     * @throws Exception
     */
    public void testAlternativeBaseJarVersionless()
        throws Exception
    {
        Iterator<RepositoryPath> it =
            base.getArtifactPaths( Arrays.asList( JAR.setVersion( "SYSTEM" ), JAR2.setVersion( "SYSTEM" ) ) ).iterator();
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/lib/java/sisu/sisu-plexus.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/lib/java/plexus/utils.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/java/sisu/sisu-plexus.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/java/plexus/utils.jar" ), it.next().getPath() );
        assertFalse( it.hasNext() );
    }

    /**
     * Test resolution of alternative versioned POM artifacts from addon repository.
     * 
     * @throws Exception
     */
    public void testAlternativeAddonPomVersioned()
        throws Exception
    {
        Iterator<RepositoryPath> it = addon.getArtifactPaths( Arrays.asList( POM, POM2 ) ).iterator();
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/share/maven-effective-poms/JPP.sisu-sisu-plexus-1.2.3.pom" ),
                      it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/share/maven-effective-poms/JPP.plexus-utils-3.0.14.pom" ),
                      it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/share/maven-poms/JPP.sisu-sisu-plexus-1.2.3.pom" ),
                      it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/share/maven-poms/JPP.plexus-utils-3.0.14.pom" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-effective-poms/JPP.sisu-sisu-plexus-1.2.3.pom" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-effective-poms/JPP.plexus-utils-3.0.14.pom" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-poms/JPP.sisu-sisu-plexus-1.2.3.pom" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-poms/JPP.plexus-utils-3.0.14.pom" ), it.next().getPath() );
        assertFalse( it.hasNext() );
    }

    /**
     * Test resolution of alternative versionless POM artifacts from addon repository.
     * 
     * @throws Exception
     */
    public void testAlternativeAddonPomVersionless()
        throws Exception
    {
        Iterator<RepositoryPath> it =
            addon.getArtifactPaths( Arrays.asList( POM.setVersion( "SYSTEM" ), POM2.setVersion( "SYSTEM" ) ) ).iterator();
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/share/maven-effective-poms/JPP.sisu-sisu-plexus.pom" ),
                      it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/share/maven-effective-poms/JPP.plexus-utils.pom" ),
                      it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/share/maven-poms/JPP.sisu-sisu-plexus.pom" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/share/maven-poms/JPP.plexus-utils.pom" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-effective-poms/JPP.sisu-sisu-plexus.pom" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-effective-poms/JPP.plexus-utils.pom" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-poms/JPP.sisu-sisu-plexus.pom" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/maven-poms/JPP.plexus-utils.pom" ), it.next().getPath() );
        assertFalse( it.hasNext() );
    }

    /**
     * Test resolution of alternative versioned JAR artifacts from addon repository.
     * 
     * @throws Exception
     */
    public void testAlternativeAddonJarVersioned()
        throws Exception
    {
        Iterator<RepositoryPath> it = addon.getArtifactPaths( Arrays.asList( JAR, JAR2 ) ).iterator();
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/lib/java/sisu/sisu-plexus-1.2.3.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/lib/java/plexus/utils-3.0.14.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/share/java/sisu/sisu-plexus-1.2.3.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/share/java/plexus/utils-3.0.14.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/lib/java/sisu/sisu-plexus-1.2.3.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/lib/java/plexus/utils-3.0.14.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/java/sisu/sisu-plexus-1.2.3.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/java/plexus/utils-3.0.14.jar" ), it.next().getPath() );
        assertFalse( it.hasNext() );
    }

    /**
     * Test resolution of alternative versionless JAR artifacts from addon repository.
     * 
     * @throws Exception
     */
    public void testAlternativeAddonJarVersionless()
        throws Exception
    {
        Iterator<RepositoryPath> it =
            addon.getArtifactPaths( Arrays.asList( JAR.setVersion( "SYSTEM" ), JAR2.setVersion( "SYSTEM" ) ) ).iterator();
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/lib/java/sisu/sisu-plexus.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/lib/java/plexus/utils.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/share/java/sisu/sisu-plexus.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "opt/rh/addon/usr/share/java/plexus/utils.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/lib/java/sisu/sisu-plexus.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/lib/java/plexus/utils.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/java/sisu/sisu-plexus.jar" ), it.next().getPath() );
        assertTrue( it.hasNext() );
        assertEquals( Paths.get( "usr/share/java/plexus/utils.jar" ), it.next().getPath() );
        assertFalse( it.hasNext() );
    }
}
