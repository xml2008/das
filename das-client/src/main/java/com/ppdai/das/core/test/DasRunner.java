package com.ppdai.das.core.test;

import com.ppdai.das.core.client.DalTransactionManager;
import org.junit.runners.model.InitializationError;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Use this Runner to support declarative rollback in JUnit.
 * The DasRunner is-a SpringJUnit4ClassRunner.
 *
 * sample:
 *     @Test
 *     @DasTransactional(logicDbName="MySqlSimple", rollback = true)
 *     public void test() throws SQLException {
 *         dao.insert(list);
 *     }
 *
 * @see {@link com.ppdai.das.client.annotation.DasTransactional}.
 */
public class DasRunner extends SpringJUnit4ClassRunner {
    public DasRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    protected Object createTest() throws Exception {
        Class clz = this.getTestClass().getJavaClass();
        Object testInstance = DalTransactionManager.create(clz);
        this.getTestContextManager().prepareTestInstance(testInstance);
        return testInstance;
    }
}
