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
