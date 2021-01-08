package com.ppdai.das.tx.aspect;

import com.ppdai.das.service.DasService;
import com.ppdai.das.service.TxNodeStartRequest;
import com.ppdai.das.service.TxNodeStartResponse;
import com.ppdai.das.service.TxXID;
import com.ppdai.das.tx.DasTxContext;
import com.ppdai.das.tx.event.BusEvent;
import com.ppdai.das.tx.monitor.BusEventManager;
import org.apache.thrift.TException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MethodAspect implements Ordered {

    static final int ORDER  = 0;

    private DasService.Client client;

    public MethodAspect() {
        BusEventManager.register(this);
    }

    @Pointcut("@annotation(com.ppdai.das.tx.annotation.DasTransactional)")
    public void txTransactionMethodPointcut() {
    }

    @Before("txTransactionMethodPointcut()")
    public void transactionRunning(JoinPoint point) throws Throwable {
        if(DasTxContext.getAndIncrease() > 0) {
            return;
        }
        BusEventManager.post(new BusEvent("start MethodAspect"));
    }

    private void sendNodeStart() throws TException {
        TxNodeStartRequest request = new TxNodeStartRequest();
        TxXID txXID = new TxXID();
        txXID.setNumber(txXID.getNumber());
        request.setXid(txXID);
        request.setTxName("");//TODO:
        TxNodeStartResponse response = client.nodeStart(request);
        //TODO: keep node id
    }

    public void setClient(DasService.Client client) {
        this.client = client;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
