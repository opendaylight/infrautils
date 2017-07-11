/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.slf4j.impl;

import org.opendaylight.infrautils.testutils.internal.LogCaptureRuleLoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * slf4j glue.
 *
 * @author Michael Vorburger.ch
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {

    // Implementation copy/pasted from the class with the same name in slf4j-simple
    // but org.slf4j.impl.SimpleLoggerFactory replaced by LogCaptureRuleLoggerFactory

    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    public static final StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    // to avoid constant folding by the compiler, this field must *not* be final
    public static String REQUESTED_API_VERSION = "1.6.99"; // !final

    private static final String LOGGER_FACTORY_CLASS_NAME = LogCaptureRuleLoggerFactory.class.getName();

    private final ILoggerFactory loggerFactory;

    private StaticLoggerBinder() {
        loggerFactory = new LogCaptureRuleLoggerFactory();
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return LOGGER_FACTORY_CLASS_NAME;
    }

}
