package org.fedoraproject.maven.connector;

public class Parameters
{
    public static final boolean SKIP_TESTS = System.getProperty( "maven.test.skip" ) != null;
}
