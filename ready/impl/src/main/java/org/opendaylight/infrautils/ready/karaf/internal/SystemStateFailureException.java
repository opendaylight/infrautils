/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.karaf.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;

/**
 * Exception used (only) by {@link KarafSystemReady} if there are any OSGi bundles that failed to start. This is based
 * on not only {@link Bundle#getState()}'s {@link Bundle#ACTIVE}, but also DI wiring systems such as blueprint
 * containers.
 *
 * <p>The {@link #getMessage() message} will likely contain a longer multi-line String with extensive technical details
 * including all failed bundles' states, detailed technical information related to OSGi bundle and blueprint resolution,
 * and possibly exceptions incl. stack traces thrown by {@link BundleActivator} start() methods and dependency injection
 * object wiring {@code PostConstruct} "init" type methods.
 *
 * @author Michael Vorburger.ch
 */
class SystemStateFailureException extends Exception {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final BundleDiagInfos bundleDiagInfos;

    SystemStateFailureException(String message, BundleDiagInfos bundleDiagInfos, Throwable cause) {
        super(getExtendedMessage(message, bundleDiagInfos), cause);
        this.bundleDiagInfos = bundleDiagInfos;
    }

    SystemStateFailureException(String message, BundleDiagInfos bundleDiagInfos) {
        super(getExtendedMessage(message, bundleDiagInfos));
        this.bundleDiagInfos = bundleDiagInfos;
    }

    private static String getExtendedMessage(String message, BundleDiagInfos bundleDiagInfos) {
        return message + "\n" + bundleDiagInfos.getFullDiagnosticText();
    }

    public BundleDiagInfos getBundleDiagInfos() {
        return bundleDiagInfos;
    }
}
