/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.samples.asyncmdsal.provider;

import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.Future;
import org.opendaylight.infrautils.async.impl.AsyncMethod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.infrautils.rpcsample.rev160703.AsyncRpcCallOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.infrautils.rpcsample.rev160703.AsyncRpcCallOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.infrautils.rpcsample.rev160703.RpcsampleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.infrautils.rpcsample.rev160703.SyncRpcCallOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.infrautils.rpcsample.rev160703.SyncRpcCallOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class RpcImplementor implements RpcsampleService {

    @Override
    @AsyncMethod
    public Future<RpcResult<AsyncRpcCallOutput>> asyncRpcCall() {
        System.out.println("Async RPC was called on thread: " + Thread.currentThread().getName());
        SettableFuture<RpcResult<AsyncRpcCallOutput>> retVal = SettableFuture.create();
        AsyncRpcCallOutput output = new AsyncRpcCallOutputBuilder().setAsyncValue("ASyncRESULT!").build();
        RpcResult<AsyncRpcCallOutput> rpcResult = RpcResultBuilder.success(output).build();
        retVal.set(rpcResult);
        return retVal;
    }

    @Override
    public Future<RpcResult<SyncRpcCallOutput>> syncRpcCall() {
        System.out.println("Sync RPC was called on thread: " + Thread.currentThread().getName());
        SettableFuture<RpcResult<SyncRpcCallOutput>> retVal = SettableFuture.create();
        SyncRpcCallOutput output = new SyncRpcCallOutputBuilder().setSyncValue("SyncRESULT!").build();
        RpcResult<SyncRpcCallOutput> rpcResult = RpcResultBuilder.success(output).build();
        retVal.set(rpcResult);
        return retVal;
    }

}
