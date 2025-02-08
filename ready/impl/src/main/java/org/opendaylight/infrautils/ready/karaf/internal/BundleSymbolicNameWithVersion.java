/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.karaf.internal;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Bundle's symbolic name + its version.
 *
 * @author Michael Vorburger.ch
 */
@NonNullByDefault
final class BundleSymbolicNameWithVersion implements Serializable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final String symbolicName;
    private final String version;

    BundleSymbolicNameWithVersion(String symbolicName, String version) {
        this.symbolicName = requireNonNull(symbolicName, "symbolicName");
        this.version = requireNonNull(version, "version");
    }

    String getSymbolicName() {
        return symbolicName;
    }

    String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbolicName, version);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return this == obj || obj instanceof BundleSymbolicNameWithVersion other
            && symbolicName.equals(other.symbolicName) && version.equals(other.version);
    }

    @Override
    public String toString() {
        return symbolicName + ":" + version;
    }
}
