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
import org.slf4j.MDC;

/**
 * Origin MDC utility.
 *
 * <p>An "Origin ID" is a String identifying the first (original)
 * point of contact of an external event entering the system. That first event
 * typically leads to numerous subsequent internal events in a system, and the
 * point of such an ID is (only) to be able to correlate a series of such
 * internal events among themselves and back to the original first external
 * event.  All log messages automatically include this ID.
 *
 * <p>In the case of OpenDaylight, such "origins" include HTTP requests (which are
 * often from the nortbound APIs exposed by YANG RESTCONF, but not limited to),
 * or something like an incoming Thrift request from a router, or an OVSDB notification, etc.
 *
 * <p>An Origin ID is "unique" within a certain given time span, which is typically "long enough"
 * to correlate events. However it may "overflow" and repeat at some point in time.  This is
 * dependent on the number of events (and thus the load of the system).  This implementation
 * issues an INFO level log indicating when such an overflow occurred.
 *
 * @author Michael Vorburger.ch
 */
public final class ExecutionOrigin extends MDCEntry {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionOrigin.class);

    /**
     * The String to be used as key to {@link MDCs#put(String, String)} an Origin ID
     * into the MDC.
     */
    public static final String MDC_KEY = "originID";

    private static final AtomicLong NEXT_ID = new AtomicLong();

    // base 32 is chosen because the implementation is faster than a higher one
    // (bigger ones internally use BigInteger, in Java 8 at least), and because
    // using e.g. 36 (for 26 letter plus 10 digits..) the max. long would give
    // "3W5E11264SGSE" instead of "FVVVVVVVVVVVU" (in base 32) - but both are 13 chars
    // long, so a higher base would just make it slower without getting us a
    // shorter String ID (and the shorter the better, in logs)
    private static final int RADIX = 32;
    private static final int ID_STRING_MAX_LENGTH = 13;

    /**
     * Returns the next origin ID.
     *
     * <p>This method does <b>NOT</b> put that next value into the MDC;
     * doing that, as well as (crucially) cleaning it up again at some point,
     * is the responsibility of the caller of this method, typically using
     * one of {@link MDCs}' methods.
     */
    public static ExecutionOrigin next() {
        final long nextId = NEXT_ID.getAndIncrement();
        if (nextId == 0) {
            LOG.info("Origin ID is restarting at 0 (either the system just started, or it has now overflown)");
        }
        return new ExecutionOrigin(nextId);
    }

    /**
     * Returns the current origin ID.
     *
     * <p>Normal application code will never have to invoke this.  Do <b>NOT</b> explicitly use this in your
     * Logger; the origin ID will already be automatically included in <b>all ODL logs</b>, always.
     *
     * <p>Typical usage of this method would only be in order to e.g. propagate the current origin ID onwards
     * to another system during an RPC call; if that other system has it's own tracing facility similar to
     * this, it will make it possible to correlate events.
     *
     * <p>This method is really just a convenience short-cut around <code>MDC.get(MDC_KEY)</code>,
     * and can be used for more readable code (but does not have to be).  It also does throw a clear
     * error message instead of returning null.
     */
    public static String currentID() throws IllegalStateException {
        String originID = MDC.get(MDC_KEY);
        if (originID == null) {
            throw new IllegalStateException("No Origin ID available in MDC :(");
        }
        return originID;
    }

    // package-private!!  Only ever intended to be used in the unit test of this class
    static void resetOriginID_used_only_for_testing(long newOriginID) {
        NEXT_ID.set(newOriginID);
    }

    private final long id;
    private transient String idAsString;

    private ExecutionOrigin(long id) {
        this.id = id;
    }

    @Override
    public String mdcKeyString() {
        return MDC_KEY;
    }

    /**
     * The exact internal implementation format of this ID may change over time, and
     * should not be relied upon. It currently is a char '0' padded base-32 encoded
     * atomically incremented 64 bit long.
     */
    @Override
    public String toString() {
        if (idAsString == null) {
            final String nextIdString = Long.toUnsignedString(id, RADIX).toUpperCase();
            final String paddedNextIdString = Strings.padStart(nextIdString, ID_STRING_MAX_LENGTH, '0');
            this.idAsString = paddedNextIdString;
        }
        return idAsString;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ id >>> 32);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ExecutionOrigin)) {
            return false;
        }
        ExecutionOrigin other = (ExecutionOrigin) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

}
