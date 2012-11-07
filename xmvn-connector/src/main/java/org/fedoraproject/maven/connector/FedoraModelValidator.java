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

import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.validation.DefaultModelValidator;
import org.apache.maven.model.validation.ModelValidator;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Custom Maven model object model (POM) validator that overrides default Maven model validator.
 * <p>
 * This component first modifies the model using all available customizers and then delegates the actual validation to
 * the default model validator provided by Maven itself.
 * 
 * @author Mikolaj Izdebski
 */
@Component( role = ModelValidator.class )
class FedoraModelValidator
    extends DefaultModelValidator
{
    @Requirement
    private List<ModelCustomizer> modelCustomizers;

    @Override
    public void validateEffectiveModel( Model model, ModelBuildingRequest request, ModelProblemCollector problems )
    {
        customizeModel( model );
        super.validateEffectiveModel( model, request, problems );
    }

    @Override
    public void validateRawModel( Model model, ModelBuildingRequest request, ModelProblemCollector problems )
    {
        customizeModel( model );
        super.validateRawModel( model, request, problems );
    }

    private void customizeModel( Model model )
    {
        for ( ModelCustomizer customizer : modelCustomizers )
        {
            customizer.customizeModel( model );
        }
    }
}
