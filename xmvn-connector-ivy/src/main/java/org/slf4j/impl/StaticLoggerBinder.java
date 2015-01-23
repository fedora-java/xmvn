/*-
 * Copyright (c) 2014-2015 Red Hat, Inc.
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
package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * @author Mikolaj Izdebski
 */
public class StaticLoggerBinder
    implements LoggerFactoryBinder
{
    public static String REQUESTED_API_VERSION = "1.6.99";

    private static final StaticLoggerBinder INSTANCE = new StaticLoggerBinder();

    private static final String FACTORY_CLASS = IvyLoggerFactory.class.getName();

    private static final ILoggerFactory FACTORY = new IvyLoggerFactory();

    private StaticLoggerBinder()
    {
    }

    public static final StaticLoggerBinder getSingleton()
    {
        return INSTANCE;
    }

    @Override
    public ILoggerFactory getLoggerFactory()
    {
        return FACTORY;
    }

    @Override
    public String getLoggerFactoryClassStr()
    {
        return FACTORY_CLASS;
    }
}
