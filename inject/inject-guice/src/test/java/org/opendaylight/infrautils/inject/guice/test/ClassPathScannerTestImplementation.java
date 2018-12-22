/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.test;

import javax.inject.Singleton;

@Singleton
public class ClassPathScannerTestImplementation
        implements ClassPathScannerTestTopInterface, ClassPathScannerTestAnotherInterface {
}
