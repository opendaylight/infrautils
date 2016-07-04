/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.samples.asyncmdsal.provider;

import java.lang.reflect.InvocationTargetException;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.infrautils.async.impl.AsyncUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.infrautils.rpcsample.rev160703.RpcsampleService;

public class SampleMdsalAsyncRpcProviderMain {

    private RpcImplementor implementor;

    public void init()
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            InterruptedException {
        System.out.println("\nStarted Async MD-SAL Rpc Provider!");
    }

    public void setRpcRegistry(RpcProviderRegistry rpcRegistry)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        implementor = new RpcImplementor();
        RpcsampleService proxy = (RpcsampleService) AsyncUtil.makeAsync(implementor, RpcsampleService.class);
        rpcRegistry.addRpcImplementation(RpcsampleService.class, proxy);
    }
}
