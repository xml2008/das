package com.ppdai.das.tx;

import java.lang.reflect.Method;

public class NodeMeta {
    Object target;

    Method tryMethod;
    Method confirmMethod;

    Method cancelMethod;

    public Object getTarget() {
        return target;
    }

    public NodeMeta setTarget(Object target) {
        this.target = target;
        return this;
    }

    public Method getConfirmMethod() {
        return confirmMethod;
    }

    public NodeMeta setConfirmMethod(Method confirmMethod) {
        this.confirmMethod = confirmMethod;
        return this;
    }

    public Method getCancelMethod() {
        return cancelMethod;
    }

    public NodeMeta setCancelMethod(Method cancelMethod) {
        this.cancelMethod = cancelMethod;
        return this;
    }

    public Method getTryMethod() {
        return tryMethod;
    }

    public NodeMeta setTryMethod(Method tryMethod) {
        this.tryMethod = tryMethod;
        return this;
    }
}
