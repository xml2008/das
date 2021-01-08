package com.ppdai.das.tx;

import com.ppdai.das.tx.annotation.DasTransactional;
import com.ppdai.das.tx.event.BusEvent;
import com.ppdai.das.tx.monitor.BusEventManager;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;

@Service
public class TCCService implements ApplicationListener<ApplicationReadyEvent> {
    int  i = 0;
   @DasTransactional(type = TxTypeEnum.TCC,
            retry = 2,
            confirmMethod = "cmt",
            cancelMethod = "cel")
    public void testCommit(){
       System.out.println("call testCommit "+ i);
       if(i==0){
           throw  new RuntimeException("XXXX");
       }
       BusEventManager.post(new BusEvent("call try"));
    }

    public TCCService(){
       System.out.println("");
    }
    public void cmt(){
        BusEventManager.post(new BusEvent("TX commited"));
    }

    public void cel(){
        System.out.println("call cel");
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
       // Executors.newCachedThreadPool().submit(()->{testCommit();});

    }
}
