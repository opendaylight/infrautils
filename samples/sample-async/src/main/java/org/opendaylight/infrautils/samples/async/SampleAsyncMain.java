/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.samples.async;

import java.lang.reflect.InvocationTargetException;
import org.opendaylight.infrautils.async.impl.AsyncUtil;

public class SampleAsyncMain {

    public void init()
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        System.out.println("\nStarted Async Client Sample!");
        System.out.println("Code currently runs with default async pool. "
                + "You will be able to see in the karaf.log the different thread names that invoke the methods.");
        System.out.println("You can change the configuration at: org.opendaylight.async.cfg "
                        + "and restart the karaf to see the effects.");
        Object proxy = AsyncUtil.makeAsync(new RpcImplementor(), IRpcImplementor.class, IOtherRpcImplementor.class);

        RpcInvoker invoker = new RpcInvoker(proxy);
        System.out.println("\nInvoking 3 methods.");
        invoker.startWorking();
    }
}
