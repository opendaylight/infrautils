/*
 * Copyright (c) 2016, 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.karaf.internal;

import com.google.errorprone.annotations.Var;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.opendaylight.odlparent.bundles.diag.ContainerState;
import org.opendaylight.odlparent.bundles.diag.Diag;

/**
 * {@link BundleDiagInfos} implementation.
 *
 * @author Michael Vorburger.ch
 */
// intentionally just package-local
final class BundleDiagInfosImpl implements BundleDiagInfos {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private static final Map<String, ContainerState> WHITELISTED_BUNDLES = Map.of(
        "slf4j.log4j12", ContainerState.INSTALLED,
        // ODLPARENT-144
        "org.apache.karaf.scr.management", ContainerState.WAITING);

    private final List<String> okBundleStateInfoTexts;
    private final List<String> nokBundleStateInfoTexts;
    private final List<String> whitelistedBundleStateInfoTexts;
    private final Map<ContainerState, Integer> bundleStatesCounters;

    /**
     * Create an instance. The collections provided as arguments will be kept as-is; it’s up to the caller
     * to ensure they’re handled defensively, as appropriate.
     *
     * @param okBundleStateInfoTexts information about bundles in OK state.
     * @param nokBundleStateInfoTexts information about bundles not in OK state.
     * @param whitelistedBundleStateInfoTexts information about whitelisted bundles.
     * @param bundleStatesCounters bundle state counters.
     */
    private BundleDiagInfosImpl(List<String> okBundleStateInfoTexts, List<String> nokBundleStateInfoTexts,
            List<String> whitelistedBundleStateInfoTexts, Map<ContainerState, Integer> bundleStatesCounters) {
        this.okBundleStateInfoTexts = okBundleStateInfoTexts;
        this.nokBundleStateInfoTexts = nokBundleStateInfoTexts;
        this.whitelistedBundleStateInfoTexts = whitelistedBundleStateInfoTexts;
        this.bundleStatesCounters = bundleStatesCounters;
    }

    static BundleDiagInfosImpl ofDiag(Diag diag) {
        var okBundleStateInfoTexts = new ArrayList<String>();
        var nokBundleStateInfoTexts = new ArrayList<String>();
        var whitelistedBundleStateInfoTexts = new ArrayList<String>();

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
                whitelistedBundleStateInfoTexts.add(
                    "WHITELISTED " + bundleSymbolicNameWithVersion + ": " + bundleStateDiagText);
                continue;
            }

            // BundleState comparison as in Karaf's "diag" command,
            // see https://github.com/apache/karaf/blob/master/bundle/core/src/main/java/org/apache/karaf/bundle/command/Diag.java
            // but we intentionally, got a little further than Karaf's "diag" command,
            // and instead of only checking some states, we check what's really Active,
            // but accept that some remain just Resolved:
            if (karafBundleState != ContainerState.ACTIVE && karafBundleState != ContainerState.RESOLVED) {
                nokBundleStateInfoTexts.add("NOK " + bundleSymbolicNameWithVersion + ": " + bundleStateDiagText);
            } else {
                okBundleStateInfoTexts.add("OK " + bundleSymbolicNameWithVersion + ": " + bundleStateDiagText);
            }
        }

        return new BundleDiagInfosImpl(List.copyOf(okBundleStateInfoTexts), List.copyOf(nokBundleStateInfoTexts),
            List.copyOf(whitelistedBundleStateInfoTexts),
            Collections.unmodifiableMap(diag.containerStateFrequencies()));
    }

    @Override
    public SystemState getSystemState() {
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

    @Override
    public String getFullDiagnosticText() {
        var sb = new StringBuilder(getSummaryText());
        @Var int failureNumber = 1;
        for (var nokBundleStateInfoText : getNokBundleStateInfoTexts()) {
            sb.append('\n').append(failureNumber++).append(". ").append(nokBundleStateInfoText);
        }
        return sb.toString();
    }

    @Override
    public String getSummaryText() {
        return "diag: " + getSystemState() + " " + bundleStatesCounters.toString();
    }

    @Override
    public List<String> getNokBundleStateInfoTexts() {
        return nokBundleStateInfoTexts;
    }

    @Override
    public List<String> getOkBundleStateInfoTexts() {
        return okBundleStateInfoTexts;
    }

    @Override
    public List<String> getWhitelistedBundleStateInfoTexts() {
        return whitelistedBundleStateInfoTexts;
    }

    @Override
    public String toString() {
        return getFullDiagnosticText();
    }
}
