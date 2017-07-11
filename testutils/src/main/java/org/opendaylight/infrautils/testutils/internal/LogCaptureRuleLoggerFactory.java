/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.internal;

import org.opendaylight.infrautils.testutils.LogCaptureRule;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.impl.SimpleLoggerFactory;

/**
 * ILoggerFactory for {@link LogCaptureRule}.
 *
 * @author Michael Vorburger.ch
 */
public class LogCaptureRuleLoggerFactory extends SimpleLoggerFactory implements ILoggerFactory {

    @Override
    public Logger getLogger(String name) {
        return new RememberingLogger(super.getLogger(name));
    }

}
