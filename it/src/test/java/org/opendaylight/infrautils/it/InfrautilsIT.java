/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.it;

import static org.ops4j.pax.exam.CoreOptions.maven;

import org.junit.Test;
import org.opendaylight.infrautils.itestutils.AbstractIntegrationTest;
import org.opendaylight.infrautils.utils.mdc.ExecutionOrigin;
import org.opendaylight.infrautils.utils.mdc.MDCs;
import org.ops4j.pax.exam.options.UrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration test for various small features from different infrautils modules.
 *
 * @author Michael Vorburger.ch
 */
@ExamReactorStrategy(PerClass.class)
// PerClass is less isolated than the default PerMethod, but runs this faster for more than 1 @Test
public class InfrautilsIT extends AbstractIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(InfrautilsIT.class);

/*  Uncomment this to see the OID in the log if you've locally installed odlparent master,
 *  until https://git.opendaylight.org/gerrit/#/c/60828/ is merged in odlparent,
 *  and infrautils bumped up to a new version of odlparent which includes that:

    @Override
    protected MavenUrlReference getKarafURL(boolean isKaraf4) {
        return maven().groupId("org.opendaylight.odlparent")
                .artifactId("opendaylight-karaf-empty").version("3.0.0-SNAPSHOT").type("tar.gz");
    }
 */

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
        return "odl-infrautils-all";
    }

    @Test
    public void testLogMDC() {
        // Not a good real test, as it's not trivial to easily assert the logging in Karaf,
        // like we can do for standalone using the LogCaptureRule; so for now, this is really
        // just for manual verification, but still MUCH easier and a much more productive
        // in-IDE experience than local build and manual CLI mvn re-assembly and test.

        MDCs.putRunRemove(ExecutionOrigin.next(), () -> {
            LOG.warn("Ho, ho - do you also see this funky Origin ID at the <== start of this log message: {}",
                    ExecutionOrigin.currentID());
        });
    }

}
