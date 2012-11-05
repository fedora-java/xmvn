package org.fedoraproject.maven.connector;

import java.util.Iterator;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

@Component( role = ModelCustomizer.class )
public class TestDependencyRemover
    implements ModelCustomizer
{
    @Requirement
    private Logger logger;

    @Override
    public void customizeModel( Model model )
    {
        if ( Parameters.SKIP_TESTS == false )
            return;

        List<Dependency> dependencies = model.getDependencies();
        for ( Iterator<Dependency> iter = dependencies.iterator(); iter.hasNext(); )
        {
            Dependency dependency = iter.next();
            String scope = dependency.getScope();
            if ( scope != null && scope.equals( "test" ) )
            {
                iter.remove();
                String groupId = dependency.getGroupId();
                String artifactId = dependency.getArtifactId();
                logger.debug( "Dropped dependency on " + groupId + ":" + artifactId + " because tests are skipped." );
            }
        }
    }
}
