/*-
 * Copyright (c) 2016 Red Hat, Inc.
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
package org.fedoraproject.xmvn.metadata;

import java.util.List;

/**
 * Specifies parameters of metadata resolution.
 * 
 * @author Mikolaj Izdebski
 */
public class MetadataRequest
{
    private final List<String> metadataRepositories;

    private boolean ignoreDuplicates = true;

    public MetadataRequest( List<String> metadataRepositories )
    {
        this.metadataRepositories = metadataRepositories;
    }

    public List<String> getMetadataRepositories()
    {
        return metadataRepositories;
    }

    public boolean isIgnoreDuplicates()
    {
        return ignoreDuplicates;
    }

    public void setIgnoreDuplicates( boolean ignoreDuplicates )
    {
        this.ignoreDuplicates = ignoreDuplicates;
    }
}
