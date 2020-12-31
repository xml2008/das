package com.ppdai.das.tx.aspect;

import com.ppdai.das.service.DasService;
import com.ppdai.das.service.TxNodeStartRequest;
import com.ppdai.das.service.TxNodeStartResponse;
import com.ppdai.das.service.TxXID;
import com.ppdai.das.tx.DasTxContext;
import com.ppdai.das.tx.XID;
import com.ppdai.das.tx.annotation.DasTransactional;
import org.apache.thrift.TException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Aspect
@Component
public class MethodAspect implements Ordered {

    static final int ORDER  = 0;

    private DasService.Client client;

    @Pointcut("@annotation(com.ppdai.das.tx.annotation.DasTransactional)")
    public void txTransactionMethodPointcut() {
    }

    @Before("txTransactionMethodPointcut()")
    public void transactionRunning(JoinPoint point) throws Throwable {
        System.out.println("txTransactionMethodPointcut");
        //DasTransactional annotation = getDasTransactional(point);
        //sendNodeStart();
        //return point.
    }

    private void sendNodeStart() throws TException {
        TxNodeStartRequest request = new TxNodeStartRequest();
        XID xid = DasTxContext.getXID();

        TxXID txXID = new TxXID();
        txXID.setIp(xid.getIp());
        txXID.setNumber(txXID.getNumber());
        request.setXid(txXID);
        request.setTxName("");//TODO:

        TxNodeStartResponse response = client.nodeStart(request);
        //TODO: keep node id
    }

    DasTransactional getDasTransactional(ProceedingJoinPoint point) throws NoSuchMethodException {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();
        Class<?> targetClass = point.getTarget().getClass();
        Method thisMethod = targetClass.getMethod(method.getName(), method.getParameterTypes());
        return thisMethod.getAnnotation(DasTransactional.class);
    }
    @Override
    public int getOrder() {
        return ORDER;
    }
}
