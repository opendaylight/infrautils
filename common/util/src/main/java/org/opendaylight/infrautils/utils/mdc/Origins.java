/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.mdc;

import com.google.common.base.Strings;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Origin MDC utility.
 *
 * <p>An "Origin ID" (AKA an OID) is a String identifying the first (original)
 * point of contact of an external event entering the system. That first event
 * typically leads to numerous subsequent internal events in a system, and the
 * point of such an ID is (only) to be able to correlate a series of such
 * internal events among themselves and back to the original first external
 * event.  All log messages automatically include this OID.
 *
 * <p>In the case of OpenDaylight, such "origins" include HTTP requests (which are
 * often from the nortbound APIs exposed by YANG RESTCONF, but not limited to),
 * or something like an incoming Thrift request from a router, or an OVSDB notification, etc.
 *
 * <p>An Origin ID is "unique" within a certain given time span, which is typically "long enough"
 * to correlate events. However it may "overflow" and repeat at some point in time.  This is
 * dependent on the number of events (and thus the load of the system).  This implementation
 * issues an INFO level log indicating when such an overflow occurred.  This overflow log may
 * be issued with duplicates for one given overflow due to internal technical reasons; this can be safely ignored.
 *
 * <p>The exact internal implementation format of an OID may change over time, and should
 * not be relied upon. It currently is a padded base-32 encoded atomically incremented 64 bit long.
 *
 * @author Michael Vorburger.ch
 */
public final class Origins {

    private static final Logger LOG = LoggerFactory.getLogger(Origins.class);

    /**
     * The String to be used as key to {@link MDCs#put(String, String)} an Origin ID
     * into the MDC.
     */
    public static final String OID_MDC_KEY = "originID";

    private static final AtomicLong ID = new AtomicLong();

    // base 32 is chosen because the implementation is faster than a higher one
    // (bigger ones internally use BigInteger, in Java 8 at least), and because
    // using e.g. 36 (for 26 letter plus 10 digits..) the max. long would give
    // 3W5E11264SGSE instead of FVVVVVVVVVVVU (in base 32) - both are 13 chars
    // long, so a higher base would just make it slower without getting us a,
    // shorter String ID (and the shorter the better, in logs)
    private static final int RADIX = 32;
    private static final int OID_STRING_MAX_LENGTH = 13;

    private Origins() {
        LOG.info("Origin ID (OID) initialized to {}", nextOriginID());
    }

    /**
     * Returns the next origin ID.
     */
    public static String nextOriginID() {
        final long nextId = ID.getAndIncrement();
        if (nextId == 0) {
            LOG.info("Origin ID (OID) has overflown and is restarting at 0");
        }
        final String nextIdString = Long.toUnsignedString(nextId, RADIX).toUpperCase();
        final String paddedNextIdString = Strings.padStart(nextIdString, OID_STRING_MAX_LENGTH, '0');
        return paddedNextIdString;
    }

    // package-private!!
    static void resetOriginID_used_only_for_testing(long newOriginID) {
        ID.set(newOriginID);
    }
}
