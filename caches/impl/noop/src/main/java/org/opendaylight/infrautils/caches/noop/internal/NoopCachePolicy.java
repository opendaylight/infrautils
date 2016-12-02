/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.noop.internal;

import org.opendaylight.infrautils.caches.CachePolicy;

/**
 * No Operation ("NOOP") implementation of CachePolicy.
 *
 * @author Michael Vorburger.ch
 */
public class NoopCachePolicy implements CachePolicy {

    static final CachePolicy INSTANCE = new NoopCachePolicy();

    @Override
    public long maxEntries() {
        return 0;
    }

}
