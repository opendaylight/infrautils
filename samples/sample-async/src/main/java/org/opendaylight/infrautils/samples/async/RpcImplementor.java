/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.samples.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcImplementor implements IRpcImplementor, IOtherRpcImplementor {
    private static final Logger logger = LoggerFactory.getLogger(RpcImplementor.class);

    @Override
    public void runTheRpcAsync() {
        logger.info("AsyncMethod invoked with Threadname: <" + Thread.currentThread().getName()
                + "> Sleeping for 10 secs!");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void runTheRpcSync() {
        logger.info(
                "SyncMethod invoked with Threadname: <" + Thread.currentThread().getName() + "> Sleeping for 10 secs!");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void anotherAsyncRpcMethod() {
        logger.info("AnotherAsync invoked with Threadname: <" + Thread.currentThread().getName()
                + "> Sleeping for 10 secs!");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
