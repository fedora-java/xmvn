/*-
 * Copyright (c) 2012-2013 Red Hat, Inc.
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

import java.util.List;
import java.util.Properties;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.artifact.Artifact;
import org.fedoraproject.maven.config.Stereotype;

/**
 * Repository of artifacts.
 * <p>
 * Repository is a container holding repositories. Unlike in case of Maven repositories, artifacts in XMvn repository
 * don't necessarily need to have unique paths -- one artifact can be stored in one of multiple locations. Methods or
 * {@code Repository} interface support multiple artifact paths.
 * 
 * @author Mikolaj Izdebski
 */
public interface Repository
{
    /**
     * Configure this repository with given set of properties and repository-specific XML configuration.
     * <p>
     * The meaning of properties and XML configuration is dependent on particular repository implementation.
     * 
     * @param stereotypes
     * @param properties
     * @param configuration
     */
    void configure( List<Stereotype> stereotypes, Properties properties, Xpp3Dom configuration );

    /**
     * Obtain the preferred path to given artifact in this repository.
     * <p>
     * Returned path is relative to the repository base.
     * 
     * @param artifact
     * @return preferred artifact path
     */
    RepositoryPath getPrimaryArtifactPath( Artifact artifact );

    /**
     * Obtain the preferred path to given artifact in this repository.
     * <p>
     * Returned path is relative to the repository base.
     * 
     * @param artifact
     * @param ignoreType whether repository artifact type should be ignored
     * @return preferred artifact path
     */
    RepositoryPath getPrimaryArtifactPath( Artifact artifact, boolean ignoreType );

    /**
     * Get list of possible paths to given artifact in this repository. The returned list is ordered by decreasing
     * preference - the first path on the returned list is the most preferred one.
     * <p>
     * All returned paths are relative to the repository base.
     * 
     * @param artifact
     * @return list of possible artifact paths
     */
    List<RepositoryPath> getArtifactPaths( Artifact artifact );

    /**
     * Get list of possible paths to given artifact in this repository. The returned list is ordered by decreasing
     * preference - the first path on the returned list is the most preferred one.
     * <p>
     * All returned paths are relative to the repository base.
     * 
     * @param artifact
     * @param ignoreType whether repository artifact type should be ignored
     * @return list of possible artifact paths
     */
    List<RepositoryPath> getArtifactPaths( Artifact artifact, boolean ignoreType );

    /**
     * Get list of possible paths to given artifacts in this repository. The returned list is ordered by decreasing
     * preference - the first path on the returned list is the most preferred one.
     * <p>
     * All returned paths are relative to the repository base.
     * 
     * @param artifacts list of artifacts to lookup
     * @return list of possible artifact paths
     */
    List<RepositoryPath> getArtifactPaths( List<Artifact> artifact );

    /**
     * Get list of possible paths to given artifacts in this repository. The returned list is ordered by decreasing
     * preference - the first path on the returned list is the most preferred one.
     * <p>
     * All returned paths are relative to the repository base.
     * 
     * @param artifacts list of artifacts to lookup
     * @param ignoreType whether repository artifact type should be ignored
     * @return list of possible artifact paths
     */
    List<RepositoryPath> getArtifactPaths( List<Artifact> artifact, boolean ignoreType );

    String getNamespace();

    void setNamespace( String namespace );
}
