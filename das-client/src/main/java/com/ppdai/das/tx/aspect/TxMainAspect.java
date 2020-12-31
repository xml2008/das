package com.ppdai.das.tx.aspect;

import com.ppdai.das.client.delegate.remote.ServerSelector;
import com.ppdai.das.service.DasService;
import com.ppdai.das.service.TxBeginRequest;
import com.ppdai.das.service.TxBeginResponse;
import com.ppdai.das.service.TxRegisterApplicationRequest;
import com.ppdai.das.service.TxRegisterApplicationResponse;
import com.ppdai.das.service.TxType;
import com.ppdai.das.tx.DasTxContext;
import com.ppdai.das.tx.NodeMeta;
import com.ppdai.das.tx.TxTypeEnum;
import com.ppdai.das.tx.XID;
import com.ppdai.das.tx.annotation.DasTransactional;
import com.ppdai.das.tx.event.CommitEvent;
import org.apache.thrift.TException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Aspect
public class TxMainAspect implements Ordered {

    private ServerSelector serverSelector;

    private DasService.Client client;

    @Override
    public int getOrder() {
        return MethodAspect.ORDER - 1;
    }

    public TxMainAspect() {

    }

    @Pointcut("@annotation(com.ppdai.das.tx.annotation.DasTransactional)")
    public void txTransactionPointcut() {
    }



    Map<String, NodeMeta> metas = new HashMap<>();

    @Around("txTransactionPointcut()")
    public Object transactionRunning(ProceedingJoinPoint point) throws Throwable {
        System.out.println("txTransactionPointcut");
        DasTransactional annotation = getDasTransactional(point);

        saveNodeMeta(point, annotation);

        registerApplication(annotation);

        try {
            sendTxBegin();

            Object result = point.proceed();

            sendTxCommit();
            return result;
        } catch (Throwable t) {
            sendTxRollback();
            throw t;
        } finally {
            cleanUp();
        }
    }

    private void saveNodeMeta(ProceedingJoinPoint point, DasTransactional annotation) throws NoSuchMethodException {
        NodeMeta nodeMeta = new NodeMeta();
        nodeMeta.setTarget(point.getTarget());
        //nodeMeta.setTryMethod()
        //metas.put(point.getSignature().getName(), );
        nodeMeta.setConfirmMethod(point.getTarget().getClass().getMethod(annotation.confirmMethod()));
        nodeMeta.setCancelMethod(point.getTarget().getClass().getMethod(annotation.cancelMethod()));
        nodeMeta.setTryMethod(point.getTarget().getClass().getMethod(point.getSignature().getName()));
        metas.put(point.getSignature().getName(), nodeMeta);
    }

    private void cleanUp() {
    }

    DasTransactional getDasTransactional(ProceedingJoinPoint point) throws NoSuchMethodException {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();
        Class<?> targetClass = point.getTarget().getClass();
        Method thisMethod = targetClass.getMethod(method.getName(), method.getParameterTypes());
        return thisMethod.getAnnotation(DasTransactional.class);
    }

    public void onCommitCommand(CommitEvent commitEvent) throws InvocationTargetException, IllegalAccessException {
        NodeMeta nodeMeta = metas.get(commitEvent.getName());
        nodeMeta.getConfirmMethod().invoke(nodeMeta.getTarget());
    }

    private void sendTxCommit() {

    }

    private void sendTxRollback() {

    }

    private void sendTxBegin() throws TException {
       /* TxBeginRequest request = new TxBeginRequest();
        request.setTxName("");//TODO:
        TxBeginResponse response = client.txBegin(request);
        XID xid = new XID();
        xid.setIp(response.getXid().getIp());
        xid.setNumber(response.getXid().getNumber());
        DasTxContext.setXID(xid);*/
    }

    private void registerApplication(DasTransactional annotation) {
        TxRegisterApplicationRequest request = new TxRegisterApplicationRequest();
        request.setAppId("");//TODO
        request.setNodes(null);//
        TxType txType = TxType.findByValue(annotation.type() == TxTypeEnum.TCC ? 0 : 1);
        request.setTxType(txType);//
      /*  try {
            TxRegisterApplicationResponse response = client.registerApplication(request);
        } catch (TException tException){
            //TODO:
        }*/

    }
}
