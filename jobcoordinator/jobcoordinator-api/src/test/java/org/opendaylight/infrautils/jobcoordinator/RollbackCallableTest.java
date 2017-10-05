/*
 * Copyright © 2017 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.jobcoordinator;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class RollbackCallableTest {
    @Test(expected = NullPointerException.class)
    public void setFailedFuturesChecksNulls() {
        RollbackCallable rc = new RollbackCallable() {
            @Override
            public List<ListenableFuture<Void>> call() throws Exception {
                return Collections.emptyList();
            }
        };
        rc.setFailedFutures(Collections.singletonList(null));
    }

    @Test
    public void futuresArePreserved() {
        List<ListenableFuture<Void>> futures = new ArrayList<>();
        futures.add(Futures.immediateCancelledFuture());
        futures.add(Futures.immediateFailedFuture(new IllegalStateException()));
        futures.add(Futures.immediateFuture(null));
        RollbackCallable rc = new RollbackCallable() {
            @Override
            public List<ListenableFuture<Void>> call() throws Exception {
                return Collections.emptyList();
            }
        };
        rc.setFailedFutures(futures);
        Assert.assertTrue(rc.getFailedFutures().containsAll(futures));
    }
}
