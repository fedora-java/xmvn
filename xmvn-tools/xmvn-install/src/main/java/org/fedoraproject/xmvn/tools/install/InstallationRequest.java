/*-
 * Copyright (c) 2013-2018 Red Hat, Inc.
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
package org.fedoraproject.xmvn.tools.install;

import java.nio.file.Path;

/**
 * @author Mikolaj Izdebski
 */
public class InstallationRequest
{
    private boolean checkForUnmatchedRules;

    private Path installationPlanPath;

    private String basePackageName;

    private Path installRoot;

    private Path descriptorRoot;

    private String repositoryId;

    public boolean isCheckForUnmatchedRules()
    {
        return checkForUnmatchedRules;
    }

    public void setCheckForUnmatchedRules( boolean checkForUnmatchedRules )
    {
        this.checkForUnmatchedRules = checkForUnmatchedRules;
    }

    public Path getInstallationPlan()
    {
        return installationPlanPath;
    }

    public void setInstallationPlan( Path installationPlanPath )
    {
        this.installationPlanPath = installationPlanPath;
    }

    public String getBasePackageName()
    {
        return basePackageName;
    }

    public void setBasePackageName( String basePackageName )
    {
        this.basePackageName = basePackageName;
    }

    public Path getInstallRoot()
    {
        return installRoot;
    }

    public void setInstallRoot( Path installRoot )
    {
        this.installRoot = installRoot;
    }

    public Path getDescriptorRoot()
    {
        return descriptorRoot;
    }

    public void setDescriptorRoot( Path descriptorRoot )
    {
        this.descriptorRoot = descriptorRoot;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId( String repositoryId )
    {
        this.repositoryId = repositoryId;
    }

}
