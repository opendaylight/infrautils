/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics;

import org.immutables.value.Value;
import org.immutables.value.Value.Default;
import org.opendaylight.infrautils.utils.types.IDs;

/**
 * Descriptor of Metric.
 *
 * @see MetricProvider
 *
 * @author Michael Vorburger.ch
 */
@Value.Immutable
@Value.Style(stagedBuilder = true)
public abstract class MetricDescriptor {

    public static ImmutableMetricDescriptor.AnchorBuildStage builder() {
        return ImmutableMetricDescriptor.builder();
    }

    /**
     * Instance of the class "containing" this Metric.
     */
    public abstract Object anchor();

    /**
     * Name of OpenDaylight project the Metric is for, unique at opendaylight.org.
     * E.g. "netvirt" or "genius" or "openflowplugin" or "infrautils" etc.
     * The project/module/id together must be unique within ODL.
     * Valid values match <code>[a-z0-9]+</code> (lower case and no dots nor underscores).
     */
    public abstract String project();

    /**
     * Name of OpenDaylight module the Metric is for, unique within given {@link #project()}.
     * E.g. "vpnmanager" or "lockmanager" or "jobcoordinator" etc.
     * The project/module/id together must be unique within ODL.
     * Valid values match <code>[a-z0-9]+</code> (lower case and no dots nor underscores).
     */
    public abstract String module();

    /**
     * ID of the Metric, unique within given {@link #project()} + {@link #module()}.
     * E.g. "jobsPending" or "dropped_packets" or "traffic" etc. The
     * project/module/id together must be unique within ODL. Valid values match
     * <code>[a-zA-Z0-9_]+</code> (lower and upper case and underscore allowed, but
     * not starting with).  The dot character is not allowed here because at least
     * one of the implementations (Prometheus.io) does not accept dots in its IDs.
     */
    public abstract String id();

    /**
     * Human readable description of the Metric.  E.g. "Counts the number of bla bla bla".
     * No validation of valid values; anything goes.  Defaults to be the same as id();
     * but highly recommended to be set so that external Dashboard type UIs can show it
     * as documentation or help for this Metric.
     */
    public @Default String description() {
        return id();
    }

    // TODO unit(Unit unit) @see https://prometheus.io/docs/practices/naming/#base-units
    // automatically add to name https://prometheus.io/docs/practices/naming/#metric-names

    @Value.Check
    protected void check() {
        IDs.checkOnlyLowercaseAZ09(project());
        IDs.checkOnlyLowercaseAZ09(module());
        IDs.checkOnlyAZ09Underscore(id());
    }
}
