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
package org.fedoraproject.maven.rpminstall.plugin;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.fedoraproject.maven.Configuration;

public class PackagingLayout
{
    private static class Rule
    {
        private final Pattern pattern;

        private final String replacement;

        public Rule( String pattern, String replacement )
        {
            this.pattern = Pattern.compile( pattern.trim() );
            this.replacement = replacement.trim();
        }

        public String apply( String text )
        {
            Matcher matcher = pattern.matcher( text );
            if ( !matcher.find() )
                return null;

            text = replacement;
            for ( int group = 1; group <= matcher.groupCount(); group++ )
                text = text.replaceAll( "@" + group, matcher.group( group ) );
            return text;
        }
    }

    private final List<Rule> ruleSet = new LinkedList<>();

    public PackagingLayout()
        throws MojoExecutionException
    {
        String config = Configuration.getInstallLayout();

        for ( String ruleText : config.split( "\n|;" ) )
        {
            ruleText = ruleText.trim();
            if ( ruleText.equals( "" ) )
                continue;

            int splitPoint = ruleText.indexOf( "=>" );
            if ( splitPoint < 0 )
                throw new MojoExecutionException( "Invalid layout rule: \"" + ruleText + "\"" );

            Rule rule = new Rule( ruleText.substring( 0, splitPoint ), ruleText.substring( splitPoint + 2 ) );
            ruleSet.add( rule );
        }
    }

    public String getPackageName( String artifactId )
    {
        for ( Rule rule : ruleSet )
        {
            String result = rule.apply( artifactId );
            if ( result != null )
                return result;
        }

        return "";
    }
}
