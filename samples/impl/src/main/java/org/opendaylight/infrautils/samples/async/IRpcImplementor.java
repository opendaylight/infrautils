package org.opendaylight.infrautils.samples.async;

import org.opendaylight.infrautils.async.impl.AsyncMethod;

public interface IRpcImplementor {
	
    @AsyncMethod
    public void runTheRpcAsync();
    
    public void runTheRpcSync();

}
