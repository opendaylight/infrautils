package org.opendaylight.infrautils.samples.async;

public class RpcInvoker {
    
    IRpcImplementor implementor = null;
    IOtherRpcImplementor otherImplementor = null;
    
    public RpcInvoker(Object obj) {
        this.implementor = (IRpcImplementor) obj;
        this.otherImplementor = (IOtherRpcImplementor) obj;
    }
    
    public void startWorking() {
        System.out.println("Before invoking the Async RPC");
        implementor.runTheRpcAsync();
        System.out.println("After invoking the Async RPC");
        System.out.println("Before invoking the Sync RPC");
        implementor.runTheRpcSync();
        System.out.println("After invoking the Sync RPC");
        System.out.println("Before invoking the Other ASync RPC");
        otherImplementor.anotherAsyncRpcMethod();
        System.out.println("After invoking the Other ASync RPC");
    }
}
