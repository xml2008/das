package com.ppdai.das.tx.aspect;

import com.google.common.base.Splitter;
import com.ppdai.das.client.delegate.remote.ServerSelector;
import com.ppdai.das.service.CommandService;
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
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.concurrent.Executors;


@Aspect
@Component
public class TxMainAspect implements Ordered, InitializingBean {

    private ServerSelector serverSelector;

    private DasService.Client client;

    List<String> servers;

    @Autowired
    DasTransactionalBeanPostProcessor postProcessor;

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

    @Around("txTransactionPointcut()")
    public Object transactionRunning(ProceedingJoinPoint point) throws Throwable {
        if(DasTxContext.getAndIncrease() > 0) {
            return point.proceed();
        }

        BusEventManager.post(new BusEvent("start DasTransactional process"));
        DasTransactional annotation = getDasTransactional(point);

        final String node = point.getSignature().getName();
        registerApplication(annotation, node);

        try {
            sendTxBegin(node, annotation.type());

            Object result = retry(point, annotation.retry());

            sendTxCommit(node);

            return result;
        } catch (Throwable t) {
            BusEventManager.post(new BusEvent("ready to rollback: " + t.getMessage()));
            sendTxRollback(node);
            throw t;
        } finally {
            callConfirmCancel(node, point.getTarget());
            cleanUp();
        }
    }

    private void callConfirmCancel(String name, Object target) throws UnknownHostException {
        if (DasTxContext.isCommitted()) {
            callConfirm(name, target);
        } else {
            callCancel(name, target);
        }
    }

    private void callConfirm(String name, Object target) throws UnknownHostException {
        try {
            postProcessor.getConfirm(name).invoke(target);
            sendTxConfirm(name);
        } catch (Exception e) {
            e.printStackTrace();//TODO
            sendTxConfirmFail(name);
        }
    }

    private void sendTxConfirmFail(String name) throws UnknownHostException {
        TxGeneralRequest request = createTxGeneralRequest(name);
        try {
            TxGeneralResponse response = client.txConfirmFail(request);
            DasTxContext.setStatus("TxConfirmFail");
        } catch (TException tException){
            tException.printStackTrace();
            //TODO:
        }
    }

    private void sendTxConfirm(String name) throws UnknownHostException {
        TxGeneralRequest request =  createTxGeneralRequest(name);
        try {
            TxGeneralResponse response = client.txConfirm(request);
            DasTxContext.setStatus("TxConfirm");
        } catch (TException tException){
            tException.printStackTrace();
            //TODO:
        }
    }

    private void callCancel(String name, Object target) throws UnknownHostException {
        try{
            postProcessor.getCancel(name).invoke(target);
            sendTxCancel(name);
        }catch (Exception e) {
            e.printStackTrace();//TODO
            sendTxCancelFail(name);
        }

    }

    private TxGeneralRequest createTxGeneralRequest(String name) throws UnknownHostException {
        TxGeneralRequest request = new TxGeneralRequest() //TODO
                .setAppId("test_id")
                .setNode(name)
                .setIp(InetAddress.getLocalHost().getHostAddress())
                .setTxType(TxType.findByValue(0))
                .setXid(DasTxContext.getXID());
        return request;
    }

    private void sendTxCancelFail(String name) throws UnknownHostException {
        TxGeneralRequest request = createTxGeneralRequest(name);

        try {
            TxGeneralResponse response = client.txCancelFail(request);
            DasTxContext.setStatus("TxConfirm");
        } catch (TException tException){
            tException.printStackTrace();
            //TODO:
        }
    }

    private void sendTxCancel(String name) throws UnknownHostException {
        TxGeneralRequest request = createTxGeneralRequest(name);
        try {
            TxGeneralResponse response = client.txCancel(request);
            DasTxContext.setStatus("TxConfirm");
        } catch (TException tException){
            tException.printStackTrace();
            //TODO:
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

    private void sendTxCommit(String name) throws UnknownHostException {
        TxGeneralRequest request = createTxGeneralRequest(name);

        try {
            TxGeneralResponse response = client.txCommit(request);
            DasTxContext.setStatus("TxCommit");
        } catch (TException tException){
            tException.printStackTrace();
            //TODO:
        }
    }

    private void sendTxRollback(String name) throws UnknownHostException{
        TxGeneralRequest request =  createTxGeneralRequest(name);
        try {
            TxGeneralResponse response = client.txRollback(request);
            DasTxContext.setStatus("TxRollback");
        } catch (TException tException){
            tException.printStackTrace();
            //TODO:
        }
    }

    private void sendTxBegin(String tryName, TxTypeEnum typeEnum) throws  UnknownHostException {
        TxGeneralRequest request = createTxGeneralRequest(tryName);
        try {
            TxGeneralResponse response = client.txBegin(request);
            DasTxContext.setXID(response.getXid());
            DasTxContext.setStatus("TxBegin");
        } catch (TException tException){
            tException.printStackTrace();
            //TODO:
        }
    }

    private void registerApplication(DasTransactional annotation, String name) throws UnknownHostException {
        TxGeneralRequest request = createTxGeneralRequest(name);
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

        Executors.newCachedThreadPool().submit(()->{
            TBinaryProtocol.Factory protocolFactory = new TBinaryProtocol.Factory();

            CommandService.Processor<CommandService.Iface> processor = new CommandService.Processor<>(
                    new CommandService.Iface(){
                        @Override
                        public TxGeneralResponse confirmCommand(TxGeneralRequest request) throws TException {
                            String name = request.getNode();
                            TxGeneralResponse response = new TxGeneralResponse();
                            response.setXid(request.getXid());

                            try {
                                postProcessor.callConfirm(name);
                                response.setResult("success");
                            } catch (InvocationTargetException | IllegalAccessException e) {
                                response.setErrorMessage(e.getMessage());
                                response.setResult("fail");
                                e.printStackTrace(); //TODO
                            }

                            return response;
                        }

                        @Override
                        public TxGeneralResponse cancelCommand(TxGeneralRequest request) throws TException {
                            String name = request.getNode();
                            TxGeneralResponse response = new TxGeneralResponse();
                            response.setXid(request.getXid());

                            try {
                                postProcessor.callCancel(name);
                                response.setResult("success");
                            } catch (InvocationTargetException | IllegalAccessException e) {
                                response.setErrorMessage(e.getMessage());
                                response.setResult("fail");
                                e.printStackTrace(); //TODO
                            }

                            return response;
                        }
                    });

            TThreadedSelectorServer ttServer = null;
            try {
                ttServer = new TThreadedSelectorServer(
                        new TThreadedSelectorServer.Args(new TNonblockingServerSocket(9091))
                                .selectorThreads(2)
                                .processor(processor)
                                .workerThreads(2)
                                .protocolFactory(protocolFactory));
            } catch (TTransportException e) {
                e.printStackTrace();//TODO
            }
            System.out.println("CommandService up!");//TODO

            ttServer.serve();
        });

    }
}
