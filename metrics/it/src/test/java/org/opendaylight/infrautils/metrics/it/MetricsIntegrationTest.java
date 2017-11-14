/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.it;

import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.maven;

import com.codahale.metrics.Counter;
import javax.inject.Inject;
import org.junit.Test;
import org.opendaylight.infrautils.itestutils.AbstractIntegrationTest;
import org.opendaylight.infrautils.metrics.MetricProvider;
import org.ops4j.pax.exam.options.UrlReference;

/**
 * Integration Test for {@link MetricProvider}.
 *
 * @author Michael Vorburger.ch
 */
public class MetricsIntegrationTest extends AbstractIntegrationTest {

    @Inject MetricProvider metricProvider;

    @Test
    public void testMetrics() {
        Counter counter1 = metricProvider.newCounter(this, "odl.infrautils.metrics.IntegrationTest.counter1");
        assertEquals(0, counter1.getCount());
        counter1.inc();
        assertEquals(1, counter1.getCount());
    }

    @Override
    protected String featureName() {
        return "odl-infrautils-metrics";
    }

    @Override
    protected UrlReference featureRepositoryURL() {
        return maven()
                .groupId("org.opendaylight.infrautils")
                .artifactId("infrautils-features")
                .classifier("features")
                .type("xml")
                .versionAsInProject();
    }

}
