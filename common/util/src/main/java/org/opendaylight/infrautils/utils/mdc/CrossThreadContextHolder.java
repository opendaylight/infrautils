/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.mdc;

import java.util.Map;
import org.slf4j.MDC;

/**
 * Utility to transport thread local context accross thread boundaries.
 *
 * <p>Currently just a thin wrapper around slf4j MDC, but in the future
 * may be delegating to more optimized (e.g. non String based) MDC-like
 * APIs, or non-logging related thread local context.
 *
 * @author Michael Vorburger.ch
 */
public abstract class CrossThreadContextHolder {

    private static final class CrossThreadContextHolderImpl extends CrossThreadContextHolder {

        final Map<String, String> contextMap;

        CrossThreadContextHolderImpl(Map<String, String> contextMap) {
            this.contextMap = contextMap;
        }
    }

    public static CrossThreadContextHolder get() {
        return new CrossThreadContextHolderImpl(MDC.getCopyOfContextMap());
    }

    public static void set(CrossThreadContextHolder holder) {
        MDC.setContextMap(((CrossThreadContextHolderImpl) holder).contextMap);
    }

    CrossThreadContextHolder() { }

}
