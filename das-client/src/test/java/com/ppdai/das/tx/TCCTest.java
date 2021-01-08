package com.ppdai.das.tx;

import com.google.common.eventbus.Subscribe;
import com.ppdai.das.service.TxCommitCommandRequest;
import com.ppdai.das.service.TxXID;
import com.ppdai.das.tx.aspect.TxMainAspect;
import com.ppdai.das.tx.event.BusEvent;
import com.ppdai.das.tx.monitor.BusEventManager;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.locks.ReentrantLock;

public class TCCTest {
    static AbstractApplicationContext applicationContext = null;

    static TCCService tccService;

    public static void main(String[] args) throws InterruptedException, InvocationTargetException, IllegalAccessException {
        applicationContext = new ClassPathXmlApplicationContext(new String[] {"tcc-test.xml"});

        tccService = (TCCService) applicationContext.getBean("tCCService");

        BusEventManager.register(new TCCTest());

        tccService.testCommit();

        MockClient mockClient = (MockClient) applicationContext.getBean("mockClient");

        TxMainAspect txMainAspect = (TxMainAspect) applicationContext.getBean("txMainAspect");

        TxCommitCommandRequest txCommitCommandRequest = new TxCommitCommandRequest();
        txCommitCommandRequest.setTxName("testCommit");
        TxXID id = new TxXID();
     //   id.setNumber(DasTxContext.getXID().getNumber());
     //   id.setIp(DasTxContext.getXID().getIp());
        txCommitCommandRequest.setXid(id);

        txMainAspect.onCommitCommand(txCommitCommandRequest);
        System.out.println("over");
        //分布式事务回滚demo
        //;
        Object o = new Object();
        synchronized (o){
            o.wait();
        }


    }

    @Subscribe
    public void sub(BusEvent e) {
        System.out.println(e);
    }

}
