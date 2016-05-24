/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.types;

import java.math.BigInteger;
import java.util.Collection;

public class UnsignedLong extends Number implements Comparable<UnsignedLong> {

    public static final UnsignedLong ZERO = new UnsignedLong(BigInteger.ZERO);
    public static final UnsignedLong[] EMPTY = new UnsignedLong[0];
    private static final byte[] MAX_UNSIGNED_LONG_IN_BYTES = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
    private static final BigInteger MAX_UNSIGNED_LONG_BIGINT = new BigInteger(1, UnsignedLong.MAX_UNSIGNED_LONG_IN_BYTES);
    public static final UnsignedLong MAX_UNSIGNED_LONG = new UnsignedLong(MAX_UNSIGNED_LONG_BIGINT);
    private static final long serialVersionUID = 1388932495422976578L;
    private BigInteger number;

    private UnsignedLong(BigInteger number) {
        this.number = number;
    }

    public BigInteger bigIntegerValue() {
        return number;
    }

    public String toHex() {
        return number.toString(16);
    }

    @Override
    public double doubleValue() {
        throw new RuntimeException("only bigIntegerValue is permitted for unsigned-long value");
    }

    @Override
    public float floatValue() {
        throw new RuntimeException("only bigIntegerValue is permitted for unsigned-long value");
    }

    @Override
    public int intValue() {
        throw new RuntimeException("only bigIntegerValue is permitted for unsigned-long value");
    }

    @Override
    public long longValue() {
        return number.longValue();
    }

    @Override
    public byte byteValue() {
        throw new RuntimeException("only bigIntegerValue is permitted for unsigned-long value");
    }

    @Override
    public short shortValue() {
        throw new RuntimeException("only bigIntegerValue is permitted for unsigned-long value");
    }

    public static UnsignedLong valueOf(BigInteger value) {
        if (value.compareTo(UnsignedLong.MAX_UNSIGNED_LONG_BIGINT) == 1) {
            throw new IllegalArgumentException("Can not create unsigned long bigger then " + UnsignedLong.MAX_UNSIGNED_LONG_BIGINT + " (value="
                    + value + ")");
        }

        if (value == BigInteger.ZERO) {
            return UnsignedLong.ZERO;
        }

        return new UnsignedLong(value);
    }

    public static UnsignedLong valueOf(String value) {
        return UnsignedLong.valueOf(value, 10);
    }

    public static UnsignedLong valueOf(String value, int radix) {
        return UnsignedLong.valueOf(new BigInteger(value, radix));
    }

    public static UnsignedLong valueOf(long value) {
        return UnsignedLong.valueOf(BigInteger.valueOf(value));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UnsignedLong)) {
            return false;
        }
        UnsignedLong other = (UnsignedLong) obj;

        return number.equals(other.number);
    }

    @Override
    public int hashCode() {
        return number.hashCode();
    }

    @Override
    public String toString() {
        return number.toString();
    }

    public UnsignedLong add(UnsignedLong other) {
        if (other == UnsignedLong.ZERO) {// Optimization
            return this;
        }
        if (this == UnsignedLong.ZERO) {// Optimization
            return other;
        }
        return UnsignedLong.valueOf(number.add(other.bigIntegerValue()));
    }

    public UnsignedLong wrapAroundAdd(UnsignedLong other) {
        if (other == UnsignedLong.ZERO) {// Optimization
            return this;
        }
        if (this == UnsignedLong.ZERO) {// Optimization
            return other;
        }
        return UnsignedLong.valueOf(number.add(other.bigIntegerValue()).mod(MAX_UNSIGNED_LONG_BIGINT.add(BigInteger.ONE)));
    }

    public int compareTo(UnsignedLong o) {
        if (o == null) {
            return -1;
        }
        return number.compareTo(o.number);
    }

    public static UnsignedLong[] arr(Collection<UnsignedLong> collection) {
        if ((collection == null) || collection.isEmpty()) {
            return EMPTY;
        }
        return collection.toArray(new UnsignedLong[collection.size()]);
    }
}

