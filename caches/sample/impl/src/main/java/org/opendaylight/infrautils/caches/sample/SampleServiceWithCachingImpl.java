/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.sample;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.infrautils.caches.Cache;
import org.opendaylight.infrautils.caches.CacheConfigBuilder;
import org.opendaylight.infrautils.caches.CachePolicyBuilder;
import org.opendaylight.infrautils.caches.CacheProvider;
import org.ops4j.pax.cdi.api.Component;
import org.ops4j.pax.cdi.api.Contract;
import org.ops4j.pax.cdi.api.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example service implementation.
 * Illustrates how to use the caches infrautils.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
@Service
@Component
@Contract(SampleService.class)
public class SampleServiceWithCachingImpl implements SampleService {

    private static final Logger LOG = LoggerFactory.getLogger(SampleServiceWithCachingImpl.class);

    private final Cache<String, String> hellosCache;

    @Inject
    public SampleServiceWithCachingImpl(@Service CacheProvider cacheProvider) {
        LOG.warn("SampleServiceWithCachingImpl() cacheProvider = {}", cacheProvider);
        hellosCache = cacheProvider.newCache(
                new CacheConfigBuilder<String, String>()
                    .anchor(this)
                    .cacheFunction(SampleServiceWithCachingImpl::sayHelloThatIsExpensive)
                    .id("hellos") // optional
                    .description("world's very first ODL infrautils cache!") // optional
                    .build(),
                // remove this once default method has been reworked
                new CachePolicyBuilder().build());
    }

    @PreDestroy
    public void close() throws Exception {
        hellosCache.close();
    }

    @Override
    public String sayHello(String toWho) {
        return hellosCache.get(toWho);
    }

    private static String sayHelloThatIsExpensive(String toWho) {
        try {
            // Saying HELO is a really expensive operation... ;-)
            Thread.sleep(379);
        } catch (InterruptedException e) {
            LOG.error("Interrupted", e);
        }
        return "hello, " + toWho;
    }

}
