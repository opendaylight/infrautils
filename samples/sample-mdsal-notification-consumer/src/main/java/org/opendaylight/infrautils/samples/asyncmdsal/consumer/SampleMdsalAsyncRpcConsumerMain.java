/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.samples.asyncmdsal.consumer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.infrautils.rpcsample.rev160703.AsyncRpcCallOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.infrautils.rpcsample.rev160703.RpcsampleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.infrautils.rpcsample.rev160703.SyncRpcCallOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class SampleMdsalAsyncRpcConsumerMain {

    private RpcProviderRegistry registry;

    public void init() throws InterruptedException, ExecutionException {
        System.out.println("\nStarted Async MD-SAL Rpc Consumer!");
        System.out.println("Will call RPCs in 10 seconds!");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RpcsampleService rpcService = registry.getRpcService(RpcsampleService.class);
        System.out.println("Calling SyncRPC:");
        Future<RpcResult<SyncRpcCallOutput>> syncResult = rpcService.syncRpcCall();
        System.out.println("Sync Call Result is: " + syncResult.get().getResult().getSyncValue());
        System.out.println("Calling AsyncRPC:");
        Future<RpcResult<AsyncRpcCallOutput>> asyncResult = rpcService.asyncRpcCall();
        System.out.println("Async Call Result is: " + asyncResult.get().getResult().getAsyncValue());
    }

    public void setRpcRegistry(RpcProviderRegistry rpcRegistry) {
        this.registry = rpcRegistry;
    }
}
