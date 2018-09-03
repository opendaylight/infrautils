/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.prometheus.impl;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import io.prometheus.client.CollectorRegistry;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Service;
import org.opendaylight.infrautils.metrics.Counter;
import org.opendaylight.infrautils.metrics.Labeled;
import org.opendaylight.infrautils.metrics.Meter;
import org.opendaylight.infrautils.metrics.MetricDescriptor;
import org.opendaylight.infrautils.metrics.MetricProvider;
import org.opendaylight.infrautils.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link MetricProvider} based on <a href="https://prometheus.io">Prometheus.IO</a>.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
@Service(classes = MetricProvider.class)
public class PrometheusMetricProviderImpl implements MetricProvider {

    private static final Logger LOG = LoggerFactory.getLogger(PrometheusMetricProviderImpl.class);

    private final CollectorRegistry prometheusRegistry;

    // TODO see if we could share this field and more of below with MetricProviderImpl
    private final Map<String, MeterNoChildAdapter> meterParents = new ConcurrentHashMap<>();
    private final Map<List<String>, MeterAdapter> meterChildren = new ConcurrentHashMap<>();

    /**
     * Constructor. We force passing an existing CollectorRegistry instead of
     * CollectorRegistry.defaultRegistry because defaultRegistry is static, which is
     * a problem for use e.g. in tests.
     */
    @Inject
    public PrometheusMetricProviderImpl(CollectorRegistry prometheusRegistry) {
        this.prometheusRegistry = prometheusRegistry;
    }

    @PreDestroy
    public void close() {
        prometheusRegistry.clear();
    }

    private org.opendaylight.infrautils.metrics.Meter newOrExistingMeter(
            MetricDescriptor descriptor, List<String> labelNames, List<String> labelValues) {
        if (labelNames.size() != labelValues.size()) {
            throw new IllegalArgumentException();
        }
        // TODO see below, don't make this a String but a real (Immutables) class spi.MetricID
        String fullId = makeID(descriptor, labelNames);
        MeterNoChildAdapter parent = meterParents.computeIfAbsent(fullId, newId -> {
            LOG.debug("New parent Meter metric: {}", fullId);
            return new MeterNoChildAdapter(prometheusRegistry, descriptor, labelNames);
        });
        if (labelValues.isEmpty()) {
            return parent;
        } else {
            return meterChildren.computeIfAbsent(labelValues, newLabelValues ->
                new MeterAdapter(parent.prometheusCounter, labelValues));
        }
    }

    // TODO Wait, this is stupid.. ;) we don't need a String, just a hash-/equal-able object as key for the Map!
    private static String makeID(MetricDescriptor descriptor, List<String> labelNames) {
        // This ID here in the Prometheus impl must *NOT* contain the label values
        StringBuilder sb = new StringBuilder(
                descriptor.project() + "/" + descriptor.module() + "/" + descriptor.id() + "{");
        for (int i = 0; i < labelNames.size(); i++) {
            sb.append(labelNames.get(0));
            sb.append(',');
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Meter newMeter(Object anchor, String id) {
        // The idea is that this method is removed as soon as all current usages have switched to the new signatures
        throw new UnsupportedOperationException("TODO Remove this (use the non-@Deprecated alternative method)");
    }

    @Override
    public Meter newMeter(MetricDescriptor descriptor) {
        return newOrExistingMeter(descriptor, emptyList(), emptyList());
    }

    @Override
    public Labeled<Meter> newMeter(MetricDescriptor descriptor, String labelName) {
        return labelValue -> newOrExistingMeter(descriptor, singletonList(labelName), singletonList(labelValue));
    }

    @Override
    public Labeled<Labeled<Meter>> newMeter(MetricDescriptor descriptor, String firstLabelName,
            String secondLabelName) {
        return firstLabelValue -> secondLabelValue -> newOrExistingMeter(descriptor,
                of(firstLabelName, secondLabelName), of(firstLabelValue, secondLabelValue));
    }

    @Override
    public Labeled<Labeled<Labeled<Meter>>> newMeter(MetricDescriptor descriptor, String firstLabelName,
            String secondLabelName, String thirdLabelName) {
        return firstLabelValue -> secondLabelValue -> thirdLabelValue -> newOrExistingMeter(descriptor,
                of(firstLabelName, secondLabelName, thirdLabelName),
                of(firstLabelValue, secondLabelValue, thirdLabelValue));
    }

    @Override
    public Labeled<Labeled<Labeled<Labeled<Meter>>>> newMeter(MetricDescriptor descriptor, String firstLabelName,
                                                              String secondLabelName, String thirdLabelName,
                                                              String fourthLabelName) {
        return firstLabelValue -> secondLabelValue -> thirdLabelValue -> fourthLabelValue ->
                newOrExistingMeter(descriptor,
                        of(firstLabelName, secondLabelName, thirdLabelName, fourthLabelName),
                        of(firstLabelValue, secondLabelValue, thirdLabelValue, fourthLabelValue));
    }

    @Override
    public Labeled<Labeled<Labeled<Labeled<Labeled<Meter>>>>> newMeter(MetricDescriptor descriptor,
                                                                       String firstLabelName, String secondLabelName,
                                                                       String thirdLabelName, String fourthLabelName,
                                                                       String fifthLabelName) {
        return firstLabelValue -> secondLabelValue -> thirdLabelValue -> fourthLabelValue -> fifthLabelValue ->
                newOrExistingMeter(descriptor,
                        of(firstLabelName, secondLabelName, thirdLabelName, fourthLabelName, fifthLabelName),
                        of(firstLabelValue, secondLabelValue, thirdLabelValue, fourthLabelValue, fifthLabelValue));
    }

    @Override
    public Counter newCounter(Object anchor, String id) {
        // The idea is that this method is removed as soon as all current usages have switched to the new signatures
        throw new UnsupportedOperationException("TODO Remove this (use the non-@Deprecated alternative method)");
    }

    @Override
    public Counter newCounter(MetricDescriptor descriptor) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Labeled<Counter> newCounter(MetricDescriptor descriptor, String labelName) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Labeled<Labeled<Counter>> newCounter(MetricDescriptor descriptor, String firstLabelName,
            String secondLabelName) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Labeled<Labeled<Labeled<Counter>>> newCounter(MetricDescriptor descriptor, String firstLabelName,
            String secondLabelName, String thirdLabelName) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Labeled<Labeled<Labeled<Labeled<Counter>>>> newCounter(MetricDescriptor descriptor, String firstLabelName,
            String secondLabelName, String thirdLabelName, String fourthLabelName) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Labeled<Labeled<Labeled<Labeled<Labeled<Counter>>>>> newCounter(MetricDescriptor descriptor,
            String firstLabelName, String secondLabelName, String thirdLabelName, String fourthLabelName,
            String fifthLabelName) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Timer newTimer(Object anchor, String id) {
        // The idea is that this method is removed as soon as all current usages have switched to the new signatures
        throw new UnsupportedOperationException("TODO Remove this (use the non-@Deprecated alternative method)");
    }

    @Override
    public Timer newTimer(MetricDescriptor descriptor) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Labeled<Timer> newTimer(MetricDescriptor descriptor, String labelName) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Labeled<Labeled<Timer>> newTimer(MetricDescriptor descriptor, String firstLabelName,
            String secondLabelName) {
        throw new UnsupportedOperationException("TODO");
    }

}
