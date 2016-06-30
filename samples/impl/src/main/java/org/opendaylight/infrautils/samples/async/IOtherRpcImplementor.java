package org.opendaylight.infrautils.samples.async;

import org.opendaylight.infrautils.async.impl.AsyncMethod;

public interface IOtherRpcImplementor {
    
    @AsyncMethod
    public void anotherAsyncRpcMethod();
}
