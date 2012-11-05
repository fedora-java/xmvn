/*-
 * Copyright (c) 2012 Red Hat, Inc.
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
package org.fedoraproject.maven.connector;

import java.util.List;

import org.apache.maven.model.InputLocation;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblem.Severity;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.validation.DefaultModelValidator;
import org.apache.maven.model.validation.ModelValidator;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Maven project object model (POM) validator that is ignoring certain types of model errors.
 * <p>
 * A problem is ignored if its message matches some regular expression specified in configuration file. We could skip
 * validation and pretend that all models are valid, but ignoring potential problems might be too dangerous...
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = ModelValidator.class )
class FedoraModelValidator
    extends DefaultModelValidator
{
    @Requirement
    private List<ModelCustomizer> modelCustomizers;

    private static final String[] ignoredModelProblems =
        new String[] { "'dependencies.dependency.version' for [^ ]+ is missing." };

    @Override
    public void validateEffectiveModel( Model model, ModelBuildingRequest request, ModelProblemCollector problems )
    {
        super.validateEffectiveModel( model, request, filterProblems( problems ) );
        customizeModel( model );
    }

    @Override
    public void validateRawModel( Model model, ModelBuildingRequest request, ModelProblemCollector problems )
    {
        super.validateRawModel( model, request, filterProblems( problems ) );
        customizeModel( model );
    }

    private void customizeModel( Model model )
    {
        for ( ModelCustomizer customizer : modelCustomizers )
        {
            customizer.customizeModel( model );
        }
    }

    private ModelProblemCollector filterProblems( final ModelProblemCollector problems )
    {
        return new ModelProblemCollector()
        {
            @Override
            public void add( Severity severity, String message, InputLocation location, Exception exception )
            {
                for ( String regex : ignoredModelProblems )
                {
                    if ( message.matches( regex ) )
                    {
                        severity = Severity.WARNING;
                        break;
                    }
                }
                problems.add( severity, message, location, exception );
            }
        };
    }
}
