package com.ppdai.das.tx.aspect;

import com.google.common.base.Splitter;
import com.ppdai.das.client.delegate.remote.ServerSelector;
import com.ppdai.das.service.DasService;
import com.ppdai.das.service.TxCommitCommandRequest;
import com.ppdai.das.service.TxGeneralRequest;
import com.ppdai.das.service.TxGeneralResponse;
import com.ppdai.das.service.TxType;
import com.ppdai.das.tx.DasTxContext;
import com.ppdai.das.tx.NodeMeta;
import com.ppdai.das.tx.TxTypeEnum;
import com.ppdai.das.tx.annotation.DasTransactional;
import com.ppdai.das.tx.event.BusEvent;
import com.ppdai.das.tx.monitor.BusEventManager;
import com.ppdai.das.tx.process.DasTransactionalBeanPostProcessor;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Aspect
@Component
public class TxMainAspect implements Ordered, InitializingBean {

    private ServerSelector serverSelector;

    private DasService.Client client;

    List<String> servers;

    @Value("${txServers}")
    private String txServers;

    @Override
    public int getOrder() {
        return MethodAspect.ORDER - 1;
    }

    public TxMainAspect() {
        BusEventManager.register(this);
    }

    @Pointcut("@annotation(com.ppdai.das.tx.annotation.DasTransactional)")
    public void txTransactionPointcut() {
    }

    Map<String, NodeMeta> metas = new HashMap<>();

    @Around("txTransactionPointcut()")
    public Object transactionRunning(ProceedingJoinPoint point) throws Throwable {
        if(DasTxContext.getAndIncrease() > 0) {
            return point.proceed();
        }

        BusEventManager.post(new BusEvent("start DasTransactional process"));
        DasTransactional annotation = getDasTransactional(point);

        registerApplication(annotation, point.getSignature().getName());

        try {
            sendTxBegin(point.getSignature().getName(), annotation.type());

            Object result = retry(point, annotation.retry());

            sendTxCommit(point.getSignature().getName());

            return result;
        } catch (Throwable t) {
            BusEventManager.post(new BusEvent("ready to rollback: " + t.getMessage()));
            sendTxRollback(point.getSignature().getName());
            throw t;
        } finally {
            cleanUp();
        }
    }

    private Object retry(ProceedingJoinPoint point, final int retryTimes) throws Throwable {
        Object result = null;
        int times = 0;
        while (true) {
            try {
                result = point.proceed();
                BusEventManager.post(new BusEvent("ready to commit: " + result + ", with " + times + " tries"));
                return result;
            }catch (Exception e) {
                if(times++ == retryTimes){
                    throw e;
                }
            }
        }
    }

    private void cleanUp() {
        DasTxContext.cleanUp();
    }

    DasTransactional getDasTransactional(ProceedingJoinPoint point) throws NoSuchMethodException {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();
        Class<?> targetClass = point.getTarget().getClass();
        Method thisMethod = targetClass.getMethod(method.getName(), method.getParameterTypes());
        return thisMethod.getAnnotation(DasTransactional.class);
    }

    public void onCommitCommand(TxCommitCommandRequest request) throws InvocationTargetException, IllegalAccessException {
        NodeMeta nodeMeta = DasTransactionalBeanPostProcessor.getMetas().get(request.getTxName());
        nodeMeta.getConfirmMethod().invoke(nodeMeta.getTarget());
    }

    private void sendTxCommit(String name) throws TException, UnknownHostException {
        TxGeneralRequest request = new TxGeneralRequest();
        request.setAppId("test_id");//TODO
        request.setNode(name);//TODO
        request.setIp(InetAddress.getLocalHost().getHostAddress());
        TxType txType = TxType.findByValue(0);
        request.setTxType(txType);//
        request.setXid(DasTxContext.getXID());
        try {
            TxGeneralResponse response = client.txCommit(request);
        } catch (TException tException){
            tException.printStackTrace();
            //TODO:
        }
    }

    private void sendTxRollback(String name) throws TException, UnknownHostException{
        TxGeneralRequest request = new TxGeneralRequest();
        request.setAppId("test_id");//TODO
        request.setNode(name);//TODO
        request.setIp(InetAddress.getLocalHost().getHostAddress());
        TxType txType = TxType.findByValue(0);
        request.setTxType(txType);//
        request.setXid(DasTxContext.getXID());
        try {
            TxGeneralResponse response = client.txRollback(request);
        } catch (TException tException){
            tException.printStackTrace();
            //TODO:
        }
    }

    private void sendTxBegin(String tryName, TxTypeEnum typeEnum) throws TException, UnknownHostException {
        TxGeneralRequest request = new TxGeneralRequest();
        request.setAppId("test_id");//TODO
        request.setNode(tryName);//TODO
        request.setIp(InetAddress.getLocalHost().getHostAddress());
        TxType txType = TxType.findByValue(typeEnum == TxTypeEnum.TCC ? 0 : 1);
        request.setTxType(txType);//
        request.setXid(DasTxContext.getXID());
        try {
            TxGeneralResponse response = client.txBegin(request);
            DasTxContext.setXID(response.getXid());
        } catch (TException tException){
            tException.printStackTrace();
            //TODO:
        }
    }

    private void registerApplication(DasTransactional annotation, String name) throws UnknownHostException {
        TxGeneralRequest request = new TxGeneralRequest();
        request.setAppId("test_id");//TODO
        request.setNode(name);//TODO
        request.setIp(InetAddress.getLocalHost().getHostAddress());
        TxType txType = TxType.findByValue(annotation.type() == TxTypeEnum.TCC ? 0 : 1);
        request.setTxType(txType);//
        request.setXid(DasTxContext.getXID());
       try {
            TxGeneralResponse response = client.registerApplication(request);
        } catch (TException tException){
           tException.printStackTrace();
            //TODO:
        }
    }

    public void setClient(DasService.Client client) {
        this.client = client;
    }



    @Override
    public void afterPropertiesSet() throws Exception {
        servers = Splitter.on(",").omitEmptyStrings().splitToList(txServers);
        List<String> hostPort = Splitter.on(":").splitToList(servers.get(0));//TODO:
        TSocket transport = new TSocket(hostPort.get(0), Integer.parseInt(hostPort.get(1)));
        TFramedTransport ft = new TFramedTransport(transport);
        TBinaryProtocol protocol = new TBinaryProtocol(ft);
        transport.open();//TODO:
        this.client = new DasService.Client(protocol);
    }
}
