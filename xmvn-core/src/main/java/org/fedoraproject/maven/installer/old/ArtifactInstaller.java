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
package org.fedoraproject.maven.installer.old;

import java.io.IOException;

import org.eclipse.aether.artifact.Artifact;
import org.fedoraproject.maven.config.PackagingRule;

/**
 * Component that can install some kinds of Maven artifacts.
 * <p>
 * There are several Plexus components implementing this interface. Each of the components is responsible for installing
 * some kind of artifacts.
 * <p>
 * Users of this class should can either iterate over all project installer components available in Plexus container and
 * call {@code getSupportedPackagingType()} to see which components are suitable for installing which projects, or use
 * role hint to lookup the right component.
 * 
 * @author Mikolaj Izdebski
 */
public interface ArtifactInstaller
{
    /**
     * Install Maven project into binary package.
     * <p>
     * This method can be called only if project packaging type is on the list of supported packaging types, as returned
     * by {@code getSupportedPackagingTypes()} method.
     * 
     * @param artifact Maven artifact to be installed
     * @param targetPackage binary package into which project should be installed
     * @param rule effective packaging rule to use
     * @return true iff installation was possible
     * @throws ArtifactInstallationException if project cannot be installed for some reason
     * @throws IOException if I/O error occurs when installing package files
     */
    boolean install( Artifact artifact, Package targetPackage, PackagingRule rule )
        throws ArtifactInstallationException;
}
