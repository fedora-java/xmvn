package org.fedoraproject.xmvn.connector.aether;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.DefaultBuildPluginManager;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginConfigurationException;
import org.apache.maven.plugin.PluginContainerException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginExecutionException;
import org.apache.maven.plugin.PluginManagerException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;

@Component( role = BuildPluginManager.class )
public class XMvnBuildPluginManager
    extends DefaultBuildPluginManager
{
    @Requirement
    private MavenPluginManager mavenPluginManager;

    @Requirement
    private LegacySupport legacySupport;

    private final XMvnMojoExecutionListener listener;

    public XMvnBuildPluginManager()
    {
        listener = new XMvnMojoExecutionListener();
    }

    @Override
    public PluginDescriptor loadPlugin( Plugin plugin, List<RemoteRepository> repositories,
                                        RepositorySystemSession session )
        throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
        InvalidPluginDescriptorException
    {
        return mavenPluginManager.getPluginDescriptor( plugin, repositories, session );
    }

    @Override
    @SuppressWarnings( "resource" )
    public void executeMojo( MavenSession session, MojoExecution mojoExecution )
        throws MojoFailureException, MojoExecutionException, PluginConfigurationException, PluginManagerException
    {
        MavenProject project = session.getCurrentProject();

        MojoDescriptor mojoDescriptor = mojoExecution.getMojoDescriptor();

        Mojo mojo = null;

        ClassRealm pluginRealm;
        try
        {
            pluginRealm = getPluginRealm( session, mojoDescriptor.getPluginDescriptor() );
        }
        catch ( PluginResolutionException e )
        {
            throw new PluginExecutionException( mojoExecution, project, e );
        }

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( pluginRealm );

        MavenSession oldSession = legacySupport.getSession();

        try
        {
            mojo = mavenPluginManager.getConfiguredMojo( Mojo.class, session, mojoExecution );

            legacySupport.setSession( session );

            try
            {
                mojo.execute();

                listener.afterMojoExecutionSuccess( mojo, mojoExecution, project );
            }
            catch ( ClassCastException e )
            {
                throw e;
            }
            catch ( RuntimeException e )
            {
                throw new PluginExecutionException( mojoExecution, project, e );
            }
        }
        catch ( PluginContainerException e )
        {
            throw new PluginExecutionException( mojoExecution, project, e );
        }
        catch ( NoClassDefFoundError e )
        {
            ByteArrayOutputStream os = new ByteArrayOutputStream( 1024 );
            PrintStream ps = new PrintStream( os );
            ps.println( "A required class was missing while executing " + mojoDescriptor.getId() + ": "
                + e.getMessage() );
            pluginRealm.display( ps );

            Exception wrapper = new PluginContainerException( mojoDescriptor, pluginRealm, os.toString(), e );

            throw new PluginExecutionException( mojoExecution, project, wrapper );
        }
        catch ( LinkageError e )
        {
            ByteArrayOutputStream os = new ByteArrayOutputStream( 1024 );
            PrintStream ps = new PrintStream( os );
            ps.println( "An API incompatibility was encountered while executing " + mojoDescriptor.getId() + ": "
                + e.getClass().getName() + ": " + e.getMessage() );
            pluginRealm.display( ps );

            Exception wrapper = new PluginContainerException( mojoDescriptor, pluginRealm, os.toString(), e );
            throw new PluginExecutionException( mojoExecution, project, wrapper );
        }
        catch ( ClassCastException e )
        {
            ByteArrayOutputStream os = new ByteArrayOutputStream( 1024 );
            PrintStream ps = new PrintStream( os );
            ps.println( "A type incompatibility occured while executing " + mojoDescriptor.getId() + ": "
                + e.getMessage() );
            pluginRealm.display( ps );

            throw new PluginExecutionException( mojoExecution, project, os.toString(), e );
        }
        finally
        {
            mavenPluginManager.releaseMojo( mojo, mojoExecution );

            Thread.currentThread().setContextClassLoader( oldClassLoader );

            legacySupport.setSession( oldSession );
        }
    }

    @Override
    public ClassRealm getPluginRealm( MavenSession session, PluginDescriptor pluginDescriptor )
        throws PluginResolutionException, PluginManagerException
    {
        ClassRealm pluginRealm = pluginDescriptor.getClassRealm();
        if ( pluginRealm != null )
        {
            return pluginRealm;
        }

        mavenPluginManager.setupPluginRealm( pluginDescriptor, session, null, null, null );

        return pluginDescriptor.getClassRealm();
    }

    @Override
    public MojoDescriptor getMojoDescriptor( Plugin plugin, String goal, List<RemoteRepository> repositories,
                                             RepositorySystemSession session )
        throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
        MojoNotFoundException, InvalidPluginDescriptorException
    {
        return mavenPluginManager.getMojoDescriptor( plugin, goal, repositories, session );
    }
}
