/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject;

import javax.inject.Scope;
import javax.inject.Singleton;

/**
 * A Singleton AbstractLifecycle.
 *
 * <p>In ODL, most wired objects ("beans") are {@link Singleton}, and it therefore
 * makes sense to have this class and let both exposed Services and wired
 * objects internal to bundles extend this.
 *
 * <p>Future use of DI in ODL may introduce additional non-{@link Singleton} {@link Scope}s.
 *
 * @author Michael Vorburger
 */
@Singleton
public abstract class SingletonWithLifecycle extends AbstractLifecycle {
}
