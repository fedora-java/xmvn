/*-
 * Copyright (c) 2015 Red Hat, Inc.
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
package org.fedoraproject.xmvn.connector.aether;

import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.apache.maven.model.Model;
import org.apache.maven.model.validation.ModelValidator;
import org.eclipse.sisu.launch.InjectedTest;
import org.junit.Test;

/**
 * @author Mikolaj Izdebski
 */
public class ModelValidatorTest
    extends InjectedTest
{
    @Inject
    private ModelValidator validator;

    @Test
    public void testMinimalModelValidation()
        throws Exception
    {
        Model model = new Model();
        model.setModelVersion( "4.0.0" );
        model.setGroupId( "foo" );
        model.setArtifactId( "bar" );
        model.setVersion( "1.2.3" );

        assertTrue( validator instanceof XMvnModelValidator );
        ( (XMvnModelValidator) validator ).customizeModel( model );
    }
}
