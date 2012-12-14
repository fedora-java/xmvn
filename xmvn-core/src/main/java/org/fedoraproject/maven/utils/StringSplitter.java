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
package org.fedoraproject.maven.utils;

public class StringSplitter
{
    public static String[] split( String s, int n, char delim )
    {
        String[] tokens = new String[n];
        s += new String( new char[n] ).replace( '\0', delim );

        for ( int i = 0; i < n; i++ )
        {
            int j = s.indexOf( delim );

            String tok = s.substring( 0, j ).trim();
            if ( tok.equals( "" ) )
                tok = null;

            s = s.substring( j + 1 );
            tokens[i] = tok;
        }

        return tokens;
    }
}
