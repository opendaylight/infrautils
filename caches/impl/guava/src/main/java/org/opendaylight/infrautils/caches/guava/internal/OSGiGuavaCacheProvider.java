/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.guava.internal;

import static com.google.common.base.Verify.verifyNotNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.infrautils.caches.Cache;
import org.opendaylight.infrautils.caches.CacheConfig;
import org.opendaylight.infrautils.caches.CachePolicy;
import org.opendaylight.infrautils.caches.CacheProvider;
import org.opendaylight.infrautils.caches.CheckedCache;
import org.opendaylight.infrautils.caches.CheckedCacheConfig;
import org.opendaylight.infrautils.caches.baseimpl.CacheManagersRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated(since = "2.0.7", forRemoval = true)
@Component(immediate = true)
@SuppressFBWarnings(value = "NP_STORE_INTO_NONNULL_FIELD",
    justification = "SpotBugs does not grok @Nullable with @NonNullByDefault")
public final class OSGiGuavaCacheProvider implements CacheProvider {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiGuavaCacheProvider.class);

    @Reference
    @Nullable CacheManagersRegistry registry = null;

    private @Nullable GuavaCacheProvider delegate = null;

    @Override
    public <K, V> Cache<K, V> newCache(CacheConfig<K, V> cacheConfig, CachePolicy initialPolicy) {
        return delegate().newCache(cacheConfig, initialPolicy);
    }

    @Override
    public <K, V, E extends Exception> CheckedCache<K, V, E> newCheckedCache(CheckedCacheConfig<K, V, E> cacheConfig,
            CachePolicy initialPolicy) {
        return delegate().newCheckedCache(cacheConfig, initialPolicy);
    }

    @Activate
    void activate() {
        LOG.info("Guava cache provider activated");
        delegate = new GuavaCacheProvider(verifyNotNull(registry));
    }

    @Deactivate
    void deactivate() {
        delegate = null;
        LOG.info("Guava cache provider deactivated");
    }

    private GuavaCacheProvider delegate() {
        return verifyNotNull(delegate);
    }
}
