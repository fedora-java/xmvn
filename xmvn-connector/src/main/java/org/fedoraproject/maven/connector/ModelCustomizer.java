package org.fedoraproject.maven.connector;

import org.apache.maven.model.Model;

public interface ModelCustomizer
{
    void customizeModel( Model model );
}
