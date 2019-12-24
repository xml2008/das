package com.ppdai.das.client;

import static com.ppdai.das.client.Hints.hints;
import static com.ppdai.das.client.ParameterDefinition.integerVar;
import static com.ppdai.das.client.ParameterDefinition.varcharVar;
import static com.ppdai.das.client.SqlBuilder.*;
import static com.ppdai.das.core.enums.DatabaseCategory.MySql;
import static com.ppdai.das.core.enums.DatabaseCategory.SqlServer;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import com.ppdai.das.client.sqlbuilder.InExpression;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.ppdai.das.client.Person.PersonDefinition;
import com.ppdai.das.core.enums.DatabaseCategory;

@RunWith(Parameterized.class)
public class DasClientTest extends DataPreparer {
    private final static String DATABASE_LOGIC_NAME_MYSQL = "MySqlSimple";
    private final static String DATABASE_LOGIC_NAME_SQLSVR = "SqlSvrSimple";

    private static PersonDefinition p = Person.PERSON;

    @Parameters
    public static Collection data() {
        return Arrays.asList(new Object[][]{
               // {SqlServer},
                {MySql},
        });
    }
    
    public DasClientTest(DatabaseCategory dbCategory) throws SQLException {
        super(dbCategory);
    }

    @Override
    public String getDbName(DatabaseCategory dbCategory) {
        return dbCategory.equals(MySql) ? DATABASE_LOGIC_NAME_MYSQL : DATABASE_LOGIC_NAME_SQLSVR;
    }
    
    @Before
    public void setup() throws Exception {
       // TimeUnit.SECONDS.sleep(15);
        String[] statements = new String[TABLE_MODE];
        for (int k = 0; k < TABLE_MODE; k++) {
            statements[k] = String.format("INSERT INTO person(PeopleID, Name, CountryID, CityID, ProvinceID) VALUES(%d, 'test', %d, %d, 1)", k + 1, k, k);
        }
        
        if(!allowInsertWithId())
            statements = DbSetupUtil.handle("Person", statements);
        
        BatchUpdateBuilder builder = new BatchUpdateBuilder(statements);
        dao.batchUpdate(builder);
    }

    private boolean allowInsertWithId() {
        return setuper.turnOnIdentityInsert("person") == null;
    }

    @After
    public void tearDown() throws SQLException {
        String[] statements = new String[]{"DELETE FROM " + TABLE_NAME};

        BatchUpdateBuilder builder = new BatchUpdateBuilder(statements);
        dao.batchUpdate(builder);
    }



    @Test
    public void testQueryCount() throws Exception {
        for(int i=0; i<999;i++) {
            try {
                SqlBuilder builder = selectCount().from(p).intoObject();
                Hints h = builder.hints().diagnose();
                Number n = dao.queryObject(builder);
                System.out.println(n);
                System.out.println(h.getDiagnose());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                TimeUnit.SECONDS.sleep(1);
            }



        }


    }

    
    private void testTransactionNestBatchDelete(List<Person> plist) throws SQLException {
        PersonDefinition p = Person.PERSON;
        dao.execute(() -> {
            assertArrayEquals(new int[] {1, 1, 1, 1}, dao.batchDelete(plist));
            
            SqlBuilder builder = selectAllFrom(p).where(p.PeopleID.gt(0)).orderBy(p.PeopleID.asc()).into(Person.class);
            assertEquals(0, dao.query(builder).size());
        });            
    }
    
    private void testTransactionNestInsert(List<Person> plist) throws SQLException {
        PersonDefinition p = Person.PERSON;
        dao.execute(() -> {
            assertEquals(4, dao.insert(plist));
            SqlBuilder builder = selectAllFrom(p).where(p.PeopleID.gt(0)).orderBy(p.PeopleID.asc()).into(Person.class);
            assertEquals(4, dao.query(builder).size());
        });            
    }    
}