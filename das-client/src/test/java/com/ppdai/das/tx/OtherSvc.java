package com.ppdai.das.tx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class OtherSvc implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    TCCService tccService;
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Executors.newCachedThreadPool().submit(()->{
            tccService.testCommit();
          /*  try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tccService.testCommit();*/
        });

    }

}
