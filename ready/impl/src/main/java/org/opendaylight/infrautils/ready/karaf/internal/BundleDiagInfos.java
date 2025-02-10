/*
 * Copyright (c) 2016, 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.karaf.internal;

import com.google.errorprone.annotations.Var;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.opendaylight.odlparent.bundles.diag.ContainerState;
import org.opendaylight.odlparent.bundles.diag.Diag;

/**
 * System readiness diagnostic summary information.
 *
 * @author Michael Vorburger.ch
 */
// intentionally just package-local
final class BundleDiagInfos implements Serializable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private static final Map<String, ContainerState> WHITELISTED_BUNDLES = Map.of(
        "slf4j.log4j12", ContainerState.INSTALLED,
        // ODLPARENT-144
        "org.apache.karaf.scr.management", ContainerState.WAITING);

    private final List<String> nokBundleStateInfoTexts;
    private final Map<ContainerState, Integer> bundleStatesCounters;

    /**
     * Create an instance. The collections provided as arguments will be kept as-is; it’s up to the caller
     * to ensure they’re handled defensively, as appropriate.
     *
     * @param nokBundleStateInfoTexts information about bundles not in OK state.
     * @param bundleStatesCounters bundle state counters.
     */
    private BundleDiagInfos(List<String> nokBundleStateInfoTexts, Map<ContainerState, Integer> bundleStatesCounters) {
        this.nokBundleStateInfoTexts = nokBundleStateInfoTexts;
        this.bundleStatesCounters = bundleStatesCounters;
    }

    static BundleDiagInfos ofDiag(Diag diag) {
        var nokBundleStateInfoTexts = new ArrayList<String>();

        for (var bundle : diag.bundles()) {
            var bundleSymbolicName = bundle.symbolicName();
            var bundleSymbolicNameWithVersion = new BundleSymbolicNameWithVersion(bundleSymbolicName, bundle.version());

            var serviceState = bundle.serviceState();
            var diagText = serviceState.diag();
            var karafBundleState = serviceState.containerState();

            var bundleStateDiagText = "OSGi state = " + bundle.frameworkState().symbolicName()
                + ", Karaf bundleState = " + karafBundleState.name()
                + (diagText.isEmpty() ? "" : ", due to: " + diagText);

            if (bundleSymbolicName != null && karafBundleState.equals(WHITELISTED_BUNDLES.get(bundleSymbolicName))) {
                continue;
            }

            // BundleState comparison as in Karaf's "diag" command,
            // see https://github.com/apache/karaf/blob/master/bundle/core/src/main/java/org/apache/karaf/bundle/command/Diag.java
            // but we intentionally, got a little further than Karaf's "diag" command,
            // and instead of only checking some states, we check what's really Active,
            // but accept that some remain just Resolved:
            if (karafBundleState != ContainerState.ACTIVE && karafBundleState != ContainerState.RESOLVED) {
                nokBundleStateInfoTexts.add("NOK " + bundleSymbolicNameWithVersion + ": " + bundleStateDiagText);
            }
        }

        return new BundleDiagInfos(List.copyOf(nokBundleStateInfoTexts),
            Collections.unmodifiableMap(diag.containerStateFrequencies()));
    }

    SystemState getSystemState() {
        if (bundleStatesCounters.get(ContainerState.FAILURE) > 0) {
            return SystemState.Failure;
        } else if (bundleStatesCounters.get(ContainerState.STOPPING) > 0) {
            return SystemState.Stopping;
        } else if (bundleStatesCounters.get(ContainerState.INSTALLED) == 0
                // No, just Resolved is OK, so do not: && bundleStatesCounters.get(BundleState.Resolved) == 0
                && bundleStatesCounters.get(ContainerState.UNKNOWN) == 0
                && bundleStatesCounters.get(ContainerState.GRACE_PERIOD) == 0
                && bundleStatesCounters.get(ContainerState.WAITING) == 0
                && bundleStatesCounters.get(ContainerState.STARTING) == 0
                // BundleState.Active *should* be ~= total # of bundles (minus Resolved, and whitelisted installed)
                && bundleStatesCounters.get(ContainerState.STOPPING) == 0
                && bundleStatesCounters.get(ContainerState.FAILURE) == 0) {
            return SystemState.Active;
        } else {
            return SystemState.Booting;
        }
    }

    String getFullDiagnosticText() {
        var sb = new StringBuilder(getSummaryText());
        @Var int failureNumber = 1;
        for (var nokBundleStateInfoText : getNokBundleStateInfoTexts()) {
            sb.append('\n').append(failureNumber++).append(". ").append(nokBundleStateInfoText);
        }
        return sb.toString();
    }

    String getSummaryText() {
        return "diag: " + getSystemState() + " " + bundleStatesCounters.toString();
    }

    List<String> getNokBundleStateInfoTexts() {
        return nokBundleStateInfoTexts;
    }

    @Override
    public String toString() {
        return getFullDiagnosticText();
    }
}
