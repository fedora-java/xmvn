/*-
 * Copyright (c) 2012-2013 Red Hat, Inc.
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
package org.fedoraproject.maven.utils;

import java.util.regex.Pattern;

import org.codehaus.plexus.util.StringUtils;

/**
 * Utility routines for converting glob patterns to regular expressions.
 * 
 * @author Mikolaj Izdebski
 */
public class GlobUtils
{
    /**
     * Character with special meaning in regular expression namespace. These characters should be escaped when
     * converting glob expression to regular expression.
     */
    private static final String specialChars = "(){}.,?*+|<=>!";

    /**
     * Convert wildcard pattern to regular expression.
     * 
     * @param glob wildcard pattern to convert
     * @return regular expression
     */
    public static String glob2re( String glob )
    {
        StringBuilder re = new StringBuilder();
        boolean escape = false;
        int level = 0;

        re.append( '^' );

        for ( char ch : glob.toCharArray() )
        {
            if ( escape )
            {
                if ( specialChars.indexOf( ch ) >= 0 )
                    re.append( '\\' );
                re.append( ch );
                escape = false;
            }
            else if ( ch == '\\' )
            {
                escape = true;
            }
            else if ( ch == '{' )
            {
                level++;
                re.append( '(' );
            }
            else if ( ch == '}' && level > 0 )
            {
                re.append( ')' );
                level--;
            }
            else if ( ch == ',' && level > 0 )
            {
                re.append( '|' );
            }
            else if ( ch == '?' )
            {
                re.append( '.' );
            }
            else if ( ch == '*' )
            {
                re.append( '.' );
                re.append( '*' );
            }
            else
            {
                if ( specialChars.indexOf( ch ) >= 0 )
                    re.append( '\\' );
                re.append( ch );
            }
        }

        if ( escape )
            throw new Error( "Excape sequence ends prematurely" );
        if ( level != 0 )
            throw new Error( "Alternative not closed" );

        re.append( '$' );
        return re.toString();
    }

    /**
     * Create {@code Pattern} from wildcard patter.
     * 
     * @param glob wildcard pattern to convert
     * @return pattern corresponding to given wildcard pattern
     */
    public static Pattern glob2pattern( String glob )
    {
        if ( StringUtils.isEmpty( glob ) )
            return null;
        return Pattern.compile( glob2re( glob ) );
    }
}
