package org.opendaylight.infrautils.samples.async;

import org.opendaylight.infrautils.async.impl.AsyncMethod;
import org.opendaylight.infrautils.counters.impl.OccurenceCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcImplementor implements IRpcImplementor, IOtherRpcImplementor {
    private static final Logger logger = LoggerFactory.getLogger(RpcImplementor.class);

    @Override
    public void runTheRpcAsync() {
        System.out.println("Async Rpc was invoked!");
        logger.info("AsyncMethod Thread: " + Thread.currentThread().getName() + " Sleeping for 10 secs!");
        SampleImplementorCounters.rpc_async.inc();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void runTheRpcSync() {
        System.out.println("Sync Rpc was invoked!");
        logger.info("SyncMethod Thread: " + Thread.currentThread().getName() + " Sleeping for 10 secs!");
        SampleImplementorCounters.rpc_sync.inc();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    enum SampleImplementorCounters {
        rpc_async, //
        rpc_sync, //
        another_async, //
        ;

        private OccurenceCounter counter;

        private SampleImplementorCounters() {
            counter = new OccurenceCounter(getClass().getEnclosingClass().getSimpleName(), name(), name());
        }

        public void inc() {
            counter.inc();
        }
    }

    @Override
    public void anotherAsyncRpcMethod() {
        System.out.println("Another Async Rpc was invoked!");
        logger.info("AnotherAsyncRpc Thread: " + Thread.currentThread().getName() + " Sleeping for 10 secs!");
        SampleImplementorCounters.another_async.inc();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
