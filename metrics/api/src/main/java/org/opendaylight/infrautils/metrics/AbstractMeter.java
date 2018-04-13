/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.concepts.AbstractRegistration;

/**
 * Utility base class for implementing {@link Meter}.
 *
 * @author Robert Varga
 */
@Beta
public abstract class AbstractMeter extends AbstractRegistration implements Meter {

}
