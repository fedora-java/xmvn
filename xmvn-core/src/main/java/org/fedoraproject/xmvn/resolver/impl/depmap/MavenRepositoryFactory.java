/*-
 * Copyright (c) 2012-2014 Red Hat, Inc.
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
package org.fedoraproject.xmvn.resolver.impl.depmap;

import java.nio.file.Path;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;

import org.fedoraproject.xmvn.config.Stereotype;

/**
 * Factory creating JPP repositories.
 * <p>
 * <strong>WARNING</strong>: This class is part of internal implementation of XMvn and it is marked as public only for
 * technical reasons. This class is not part of XMvn API. Client code using XMvn should <strong>not</strong> reference
 * it directly.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = RepositoryFactory.class, hint = "maven" )
@Deprecated
public class MavenRepositoryFactory
    extends SimpleRepositoryFactory
{
    @Override
    protected Repository newInstance( String namespace, Path root, List<Stereotype> stereotypes )
    {
        return new MavenRepository( namespace, root, stereotypes );
    }
}
