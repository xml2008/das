package com.ppdai.das.client.transaction.autowire;

import com.ppdai.das.client.transaction.DasAnnotationValidator;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DalTransactionalValidatorAutoWireTest {
    @Test
    public void testValidateFail() throws InstantiationException, IllegalAccessException {
        ApplicationContext ctx;
        try {
            ctx = new ClassPathXmlApplicationContext("transactionTestFailByAutowire.xml");
            Assert.fail();
        } catch (BeansException e) {
            Assert.assertTrue(e.getMessage().contains(DasAnnotationValidator.VALIDATION_MSG));
        }
    }   
}
