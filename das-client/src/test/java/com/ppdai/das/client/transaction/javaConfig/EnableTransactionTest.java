package com.ppdai.das.client.transaction.javaConfig;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class EnableTransactionTest {
    @Test
    public void testPostProcessBeforeInitialization() throws Exception {
        ApplicationContext ctx = new AnnotationConfigApplicationContext("com.ppdai.das.client.transaction.javaConfig");
        TransactionAnnoClass bean = ctx.getBean(TransactionAnnoClass.class);

        assertNull(bean.perform());
        assertNotNull(bean.getTest());
        assertNull(bean.performOld());
        assertTrue(bean.performPrivateInnner());

    }
}
