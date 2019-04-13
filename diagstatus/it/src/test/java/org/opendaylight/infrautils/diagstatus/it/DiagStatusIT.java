/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.it;

import static org.ops4j.pax.exam.CoreOptions.maven;

import javax.inject.Inject;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceState;
import org.opendaylight.infrautils.itestutils.AbstractIntegrationTest;
import org.ops4j.pax.exam.options.UrlReference;

/**
 * DiagStatus Integration Test.
 * @author Faseela K
 */
public class DiagStatusIT extends AbstractIntegrationTest {

    @Inject
    DiagStatusService diagStatusService;

    @Override
    protected UrlReference featureRepositoryURL() {
        return maven()
                .groupId("org.opendaylight.infrautils")
                .artifactId("infrautils-features")
                .classifier("features")
                .type("xml")
                .versionAsInProject();
    }

    @Override
    protected String featureName() {
        return "odl-infrautils-diagstatus";
    }

    @Test
    public void testDiagStatusFeatureLoad() {
        Assert.assertTrue(true);
    }

    @Test
    public void testDiagStatusPushModel() {
        String testService1 = "testService";
        diagStatusService.register(testService1);
        // Verify if "testService" got registered with STARTING state.
        ServiceDescriptor serviceDescriptor = diagStatusService.getServiceDescriptor(testService1);
        Assert.assertEquals(serviceDescriptor.getServiceState(), ServiceState.STARTING);

        // Verify if "testService" status is updated as OPERATIONAL.
        diagStatusService.report(new ServiceDescriptor(testService1, ServiceState.OPERATIONAL,
                "service is UP"));
        serviceDescriptor = diagStatusService.getServiceDescriptor(testService1);
        Assert.assertEquals(serviceDescriptor.getServiceState(), ServiceState.OPERATIONAL);
    }

}
