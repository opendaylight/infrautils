/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.mdc;

import com.google.common.annotations.Beta;
import java.io.Serializable;
import org.slf4j.MDC;

/**
 * Marker for objects which get put into the {@link MDC}.
 *
 * @author Robert Varga
 */
@Beta
@SuppressWarnings("serial")
public abstract class MDCEntry implements Serializable {

    public abstract String mdcKeyString();

    public abstract String mdcValueString();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public final String toString() {
        return mdcValueString();
    }

}
