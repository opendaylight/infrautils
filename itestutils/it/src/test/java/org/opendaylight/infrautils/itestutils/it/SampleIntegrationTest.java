/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.itestutils.it;

import org.junit.Test;
import org.opendaylight.infrautils.itestutils.AbstractIntegrationTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example Integration Test.
 * @author Michael Vorburger.ch
 */
// @ExamReactorStrategy(PerClass.class) IFF the default PerMethod (which provides better isolation!) really is too slow?
public class SampleIntegrationTest extends AbstractIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(SampleIntegrationTest.class);

    @Test
    public void testEmptyJustToMakeSureKarafStartedOK() {
        LOG.info("info log is not visible, as not enabled");
        LOG.warn("warn log is visible");
    }

//    @Test
//    public void testGoogleTruthWorksInsideOSGi() {
//        assertThat(Boolean.TRUE).isTrue();
//    }

}
