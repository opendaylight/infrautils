package org.opendaylight.infrautils.async.impl;

import java.lang.reflect.Method;

public class MethodCallMessage {
    Method method;
    Object[] args;

    public MethodCallMessage(Method method, Object... args) {
        this.method = method;
        this.args = args;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }


}
