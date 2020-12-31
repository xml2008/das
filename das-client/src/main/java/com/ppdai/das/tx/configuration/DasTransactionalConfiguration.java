package com.ppdai.das.tx.configuration;

import com.ppdai.das.tx.aspect.MethodAspect;
import com.ppdai.das.tx.aspect.TxMainAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class DasTransactionalConfiguration {

    @Bean
    public TxMainAspect dasTransactionalAspect() {
        return new TxMainAspect();
    }

    @Bean
    public MethodAspect dasTransactionalMethodAspect() {
        return new MethodAspect();
    }
}
