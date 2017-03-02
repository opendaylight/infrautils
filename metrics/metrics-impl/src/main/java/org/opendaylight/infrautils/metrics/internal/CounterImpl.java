package org.opendaylight.infrautils.metrics.internal;

import org.opendaylight.infrautils.metrics.api.ICounter;

import com.codahale.metrics.Counter;

public class CounterImpl implements ICounter {
    private final Counter counter;

    CounterImpl(final Counter counter) {
        this.counter = counter;
    }

    @Override
    public void increment() {
        counter.inc();
    }

    @Override
    public void decrement() {
        counter.dec();
    }

    @Override
    public long get() {
        return counter.getCount();
    }
}
