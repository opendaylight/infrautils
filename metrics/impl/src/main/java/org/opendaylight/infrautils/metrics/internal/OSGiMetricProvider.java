/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.internal;

import org.opendaylight.infrautils.metrics.MetricProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

@Component(immediate = true, service = MetricProvider.class, configurationPid = "org.opendaylight.infrautils.metrics")
@Designate(ocd = Configuration.class)
public final class OSGiMetricProvider extends AbstractMetricProvider {
    @Activate
    void activate(Configuration configuration) {
        start();
        updateConfiguration(configuration);
    }

    @Modified
    void modified(Configuration configuration) {
        updateConfiguration(configuration);
    }

    @Deactivate
    void deactivate() {
        stop();
    }
}
