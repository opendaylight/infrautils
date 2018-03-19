/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.sample;

import javax.inject.Inject;
import org.opendaylight.infrautils.metrics.Labeled;
import org.opendaylight.infrautils.metrics.Meter;
import org.opendaylight.infrautils.metrics.MetricDescriptor;
import org.opendaylight.infrautils.metrics.MetricProvider;
import org.ops4j.pax.cdi.api.OsgiService;

/**
 * Example illustrating advanced type safe usage of metrics API.
 *
 * @author Michael Vorburger.ch, based on an idea by Stephen Kitt
 */
public class MetricsAdvancedExample {

    public static class MacLabeledMeter {
        private final Labeled<Meter> labeledMeter;

        public MacLabeledMeter(Labeled<Meter> labeledMeter) {
            this.labeledMeter = labeledMeter;
        }

        public Meter mac(String mac) {
            return labeledMeter.label(mac);
        }
    }

    public static class PortLabeledMeter {
        private final Labeled<Labeled<Meter>> labeledLabeledMeter;

        public PortLabeledMeter(Labeled<Labeled<Meter>> labeledLabeledMeter) {
            this.labeledLabeledMeter = labeledLabeledMeter;
        }

        public MacLabeledMeter port(int port) {
            return new MacLabeledMeter(labeledLabeledMeter.label(Integer.toString(port)));
        }
    }

    private final PortLabeledMeter meter;

    @Inject
    public MetricsAdvancedExample(@OsgiService MetricProvider metricProvider) {
        meter = new PortLabeledMeter(metricProvider.newMeter(MetricDescriptor.builder().anchor(this)
                .project("infrautils").module("metrics").id("example_meter").build(),
                "port", "mac"));
    }

    public void run() {
        meter.port(456).mac("1A:0B:F2:25:1C:68").mark();
    }
}
