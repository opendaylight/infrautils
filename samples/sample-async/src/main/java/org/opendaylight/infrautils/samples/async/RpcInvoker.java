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

public class RpcInvoker {

    private IRpcImplementor implementor = null;
    private IOtherRpcImplementor otherImplementor = null;
    protected static final Logger logger = LoggerFactory.getLogger(RpcInvoker.class);

    public RpcInvoker(Object obj) {
        this.implementor = (IRpcImplementor) obj;
        this.otherImplementor = (IOtherRpcImplementor) obj;
    }

    public void startWorking() {
        logger.info("Before invoking the Async RPC");
        implementor.runTheRpcAsync();
        logger.info("After invoking the Async RPC");
        logger.info("Before invoking the Sync RPC");
        implementor.runTheRpcSync();
        logger.info("After invoking the Sync RPC");
        logger.info("Before invoking the Other ASync RPC");
        otherImplementor.anotherAsyncRpcMethod();
        logger.info("After invoking the Other ASync RPC");
    }
}
