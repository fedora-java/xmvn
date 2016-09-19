/*-
 * Copyright (c) 2012-2016 Red Hat, Inc.
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
package org.fedoraproject.xmvn.repository.impl;

import java.nio.file.Path;

import org.w3c.dom.Element;

import org.fedoraproject.xmvn.repository.Repository;

/**
 * Factory creating JPP repositories.
 * 
 * @author Mikolaj Izdebski
 */
class JppRepositoryFactory
    extends SimpleRepositoryFactory
{
    @Override
    protected Repository newInstance( String namespace, Path root, Element filter )
    {
        return new JppRepository( namespace, root, filter );
    }
}
