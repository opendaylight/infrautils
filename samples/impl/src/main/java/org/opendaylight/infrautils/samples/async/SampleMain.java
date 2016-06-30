package org.opendaylight.infrautils.samples.async;

import java.lang.reflect.InvocationTargetException;
import org.opendaylight.infrautils.async.impl.AsyncInvocationProxy;
import org.opendaylight.infrautils.async.impl.AsyncUtil;

public class SampleMain {
    
    public SampleMain() {
        
    }
    
    public void init() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        System.out.println("Creating proxy"); // TODO add warn proxy created with workmode 0
        Object proxy = AsyncUtil.makeAsync(new RpcImplementor(), IRpcImplementor.class, IOtherRpcImplementor.class);
        System.out.println("PROXY CREATED!");
        
        RpcInvoker invoker = new RpcInvoker(proxy);
        invoker.startWorking();
    }
}
