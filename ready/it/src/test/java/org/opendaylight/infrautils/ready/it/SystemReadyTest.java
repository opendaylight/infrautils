/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.it;

import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.maven;

import javax.inject.Inject;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.infrautils.itestutils.AbstractIntegrationTest;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.infrautils.ready.SystemState;
import org.ops4j.pax.exam.options.UrlReference;

/**
 * Integration Test for {@link SystemReadyMonitor}.
 *
 * @author Michael Vorburger.ch
 */
public class SystemReadyTest extends AbstractIntegrationTest {

    // TODO Customize logging to enable seeing INFO logs from SystemReadyImpl..
    //      Just in test, or by feature fragment to always enable it?

    @Inject SystemReadyMonitor systemReadyMonitor;

    // private volatile boolean isReady = false;

    @Test
    @Ignore // TODO This can't work reliably as-is; it needs to await isReady.. which needs Awaitility in Pax Exam
    public void testSystemState() {
        // systemReadyMonitor.registerListener(() -> isReady = true);
        assertEquals(SystemState.ACTIVE, systemReadyMonitor.getSystemState());
    }

    @Override
    protected UrlReference featureRepositoryURL() {
        return maven()
                .groupId("org.opendaylight.infrautils")
                .artifactId("infrautils-features4")
                .classifier("features")
                .type("xml")
                .versionAsInProject();
    }

    @Override
    protected String featureName() {
        return "odl-infrautils-ready";
    }

}
