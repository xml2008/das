package com.ppdai.das.client;

import static com.ppdai.das.client.SqlBuilder.*;
import static com.ppdai.das.core.enums.DatabaseCategory.MySql;
import static com.ppdai.das.core.enums.DatabaseCategory.SqlServer;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.ppdai.das.client.Person.PersonDefinition;
import com.ppdai.das.core.enums.DatabaseCategory;

@RunWith(Parameterized.class)
public class DasClientDiagnoseTest extends DataPreparer {
    private final static String DATABASE_LOGIC_NAME_MYSQL = "MySqlSimple";
    private final static String DATABASE_LOGIC_NAME_SQLSVR = "SqlSvrSimple";

    private static PersonDefinition p = Person.PERSON;

    @Parameters
    public static Collection data() {
        return Arrays.asList(new Object[][]{
                {SqlServer},
                {MySql},
        });
    }
    
    public DasClientDiagnoseTest(DatabaseCategory dbCategory) throws SQLException {
        super(dbCategory);
    }

    @Override
    public String getDbName(DatabaseCategory dbCategory) {
        return dbCategory.equals(MySql) ? DATABASE_LOGIC_NAME_MYSQL : DATABASE_LOGIC_NAME_SQLSVR;
    }
    @BeforeClass
    public static void before() throws Exception {
//        System.setProperty("das.client.debug", "true");
    }
    
    @AfterClass
    public static void after() throws Exception {
//        System.setProperty("das.client.debug", "false");
    }
    
    @Before
    public void setup() throws Exception {
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

    private void test(RunDiagnose d) {
        Hints hints = new Hints().diagnose();
        try {
            d.run(hints);
        } catch (Exception e) {
        } finally {
            String s = hints.getDiagnose().toString();
            Assert.assertFalse(s.trim().isEmpty());
        }
    }
    
    private interface RunDiagnose {
        void run(Hints hints) throws SQLException;
    }
    
    @Test
    public void testQueryById() throws Exception {
        test((hints)->{
            Person pk = new Person();
            pk.setPeopleID(1);
            dao.queryByPk(pk, hints);
        });
    }

    @Test
    public void testQueryBySample() throws Exception {
        test((hints)->{
            Person pk = new Person();
            pk.setName("tom");
            List<Person> plist = dao.queryBySample(pk, hints);
        });
    }

    @Test
    public void testQueryBySamplePage() throws Exception {
        test((hints)-> {
            for (int j = 0; j < TABLE_MODE; j++) {
                Person pk = new Person();
                pk.setName("test");
                dao.queryBySample(pk, PageRange.atPage(1, 10, p.PeopleID), hints);
            }
        });
    }

    @Test
    public void testCountBySample() throws Exception {
        test((hints -> {
            Person pk = new Person();
            pk.setName("test");
            dao.countBySample(pk, hints);
        }));

    }

    @Test
    public void testInsertOne() throws Exception {
        test((hints)->{
            Person p = new Person();
            p.setName("tom");
            dao.insert(p, hints);
        });
    }

    @Test
    public void testInsertList() throws Exception {
        test((hints)->{
            List<Person> pl = new ArrayList<>();
            for(int k = 0; k < TABLE_MODE;k++) {
                Person p = new Person();
                pl.add(p);
            }
           dao.insert(pl, hints);
        });
    }

    @Test
    public void testBatchInsert() throws Exception {
        List<Person> pl = new ArrayList<>();
        for(int k = 0; k < TABLE_MODE;k++) {
            Person p = new Person();
            pl.add(p);
        }

        test((hints)->{
            dao.batchInsert(pl, hints);
        });
    }

    @Test
    public void testDeleteOne() throws Exception {
        test((hints)->{
            Person pk = new Person();
            dao.deleteByPk(pk, hints);
        });
    }

    @Test
    public void testDeleteBySample() throws Exception {
        test((hints)->{
            Person sample = new Person();
            sample.setName("test");
            dao.deleteBySample(sample, hints);
         });
    }

    @Test
    public void testBatchDelete() throws Exception {
        List<Person> pl = new ArrayList<>();
        for (int k = 0; k < TABLE_MODE; k++) {
            Person pk = new Person();
            pk.setPeopleID(k + 1);
            pk.setCountryID(k);
            pk.setCityID(k);
            pl.add(pk);
        }

        test((hints)->{
            dao.batchDelete(pl, hints);
        });
    }

    @Test
    public void testUpdate() throws Exception {
        test((hints)->{
            Person pk = new Person();
            pk.setName("Tom");
            pk.setCountryID(100);
            pk.setCityID(200);
            dao.update(pk, hints);
        });
    }

//    @Test
//    public void testInsertBuilder() throws Exception {
//        PersonDefinition p = Person.PERSON;
//        test((hints)->{
//            int k = 0;
//            SqlBuilder builder = insertInto(p, p.Name, p.CountryID, p.CityID).values(p.Name.of("Jerry" + k), p.CountryID.of(k+100), p.CityID.of(k+200), "8888");
//            dao.update(builder.setHints(hints));
//        });
//    }
//
    @Test
    public void testUpdateBuilder() throws Exception {
        PersonDefinition p = Person.PERSON;
        test((hints)->{
            int k = 1;
            SqlBuilder builder = update(Person.PERSON).set(p.Name.eq("Tom"), p.CountryID.eq(100), p.CityID.eq(200)).where(p.PeopleID.eq(k+1), 1234);
            dao.update(builder.setHints(hints));
        });
    }
//
//    @Test
//    public void testDeleteBuilder() throws Exception {
//        PersonDefinition p = Person.PERSON;
//        test((hints)->{
//            int k = 1;
//            SqlBuilder builder = deleteFrom(p).where(p.PeopleID.eq(k+1), "1111");
//            dao.update(builder.setHints(hints));
//        });
//    }

    @Test
    public void testBatchUpdate() throws Exception {
        List<Person> pl = new ArrayList<>();
        for (int k = 0; k < TABLE_MODE; k++) {
            Person pk = new Person();
            pl.add(pk);
        }

        test((hints)->{
            dao.batchUpdate(pl, hints);
        });
    }

//    @Test
//    public void testBatchUpdateBuillder() throws Exception {
//        String[] statements = new String[]{
//                "INdSERT INTO " + TABLE_NAME + "( Name, CityID, ProvinceID, CountryID)"
//                        + " VALUES( 'test', 10, 1, 1)",
//                "INSdERT INTO " + TABLE_NAME + "( Name, CityID, ProvinceID, CountryID)"
//                        + " VALUES( 'test', 10, 1, 1)",
//                "INSEdRT INTO " + TABLE_NAME + "( Name, CityID, ProvinceID, CountryID)"
//                        + " VALUES( 'test', 10, 1, 1)",};
//
//        BatchUpdateBuilder builder = new BatchUpdateBuilder(statements);
//        test((hints)->{
//            dao.batchUpdate(builder);
//        });
//    }
//    
    @Test
    public void testBatchUpdateBuillderValues() throws Exception {
        Person.PersonDefinition p = Person.PERSON;
        
        SqlBuilder sqlbuilder = new SqlBuilder().appendBatchTemplate(
                "INSERT INTOe " + TABLE_NAME + "( Name, CityID, ProvinceID, CountryID) VALUES( ?, ?, ?, ?)",
                p.Name.var(), p.CityID.var(), p.ProvinceID.var(), p.CountryID.var());

        BatchUpdateBuilder builder = new BatchUpdateBuilder(sqlbuilder);
        
        builder.addBatch("test1", 10, 100, 200);
        builder.addBatch("test2", 20, 200, 100);
        builder.addBatch("test3", 30, 300, 0);

        test((hints)->{
            builder.setHints(hints);
            dao.batchUpdate(builder);
        });
    }
    
    @Test
    public void testQueryObject() throws Exception {
        PersonDefinition p = Person.PERSON;
        test((hints)->{
            SqlBuilder builder = selectAllFrom(p).where(p.PeopleID.eq(1), 1212).into(Person.class);
            builder.setHints(hints);
            Person pk = dao.queryObject(builder);
        });

        test((hints)->{
            SqlBuilder builder = selectAllFrom(p).where(p.PeopleID.eq(1), 123).into(Person.class).withLock();
            builder.setHints(hints);
            Person pk = dao.queryObject(builder);
        });
    }

    @Test
    public void testQueryAll() throws Exception {
        test((hints)->{
            SqlBuilder builder = selectAll().from(p).where(p.PeopleID.eq(1), 123).into(Person.class);
            builder.setHints(hints);
            Person p = dao.queryObject(builder);
        });
    }

    @Test
    public void testQueryObjectNullable() throws Exception {
        test((hints)->{
            SqlBuilder builder = selectAll().from(p).where(p.PeopleID.eq(1), 123).into(Person.class);
            builder.setHints(hints);
            dao.queryObjectNullable(builder);
        });
    }

    @Test
    public void testQueryIntoObjectID() throws Exception {
        test((hints)->{
            PersonDefinition p = Person.PERSON;
            SqlBuilder builder = selectDistinct(p.PeopleID).from(p).orderBy(111, p.PeopleID.asc()).intoObject();
            builder.setHints(hints);
            dao.query(builder);
        });
    }

    @Test
    public void testBatchQueryBuilder() throws Exception {
        PersonDefinition p = Person.PERSON;
        BatchQueryBuilder batchBuilder = new BatchQueryBuilder();

        for (int k = 0; k < TABLE_MODE; k++) {
            batchBuilder.addBatch(selectAllFrom(p).where().allOf("111", p.PeopleID.eq(k+1)).orderBy(p.PeopleID.asc()).into(Person.class));
        }
        
        test((hints)->{
            batchBuilder.setHints(hints);
            dao.batchQuery(batchBuilder);
        });
    }

    @Test
    public void testTransaction() throws Exception {
        PersonDefinition p = Person.PERSON;
        
        test((hints)->{
            dao.execute(() -> {
                SqlBuilder builder = selectAllFrom(p).where(111, p.PeopleID.gt(0)).orderBy(p.PeopleID.asc()).into(Person.class);
                builder.setHints(hints);
                List<Person> plist = dao.query(builder);
            });
        });
    }
    
    @Test
    public void testTransactionNest() throws Exception {
        PersonDefinition p = Person.PERSON;
        test((hints)->{
            dao.execute(() -> {
                SqlBuilder builder = selectAllFrom(p).where(p.PeopleID.gt(0)).orderBy(p.PeopleID.asc()).into(Person.class);
                builder.setHints(hints);
                List<Person> plist = dao.query(builder);
    
                assertEquals(4, plist.size());
                
                testTransactionNestBatchDelete(plist);
                
                builder = selectAllFrom(p).where(p.PeopleID.gt(0)).orderBy(p.PeopleID.asc()).into(Person.class);
                builder.setHints(hints);
                assertEquals(0, dao.query(builder).size());
                
                testTransactionNestInsert(plist);
                
                builder = selectAllFrom(p).where(p.PeopleID.gt(0)).orderBy(p.PeopleID.asc()).into(Person.class);
                builder.setHints(hints);
                assertEquals(4, dao.query(builder).size());
            });
        });
    }
    
    @Test
    public void testCallableTransaction() throws Exception {
        PersonDefinition p = Person.PERSON;
        test((hints)->{
            List<Person> plistx = dao.execute(() -> {
                SqlBuilder builder = selectAllFrom(p).where(111, p.PeopleID.gt(0)).orderBy(p.PeopleID.asc()).into(Person.class);
                builder.setHints(hints);
                List<Person> plist = dao.query(builder);
                return plist;
            });            
        });        
    }

    @Test
    public void testCallableTransactionNest() throws Exception {
        PersonDefinition p = Person.PERSON;
        test((hints)->{
            List<Person> plistx = dao.execute(() -> {
                SqlBuilder builder = selectAllFrom(p).where(p.PeopleID.gt(0)).orderBy(p.PeopleID.asc()).into(Person.class);
                builder.setHints(hints);
                List<Person> plist = dao.query(builder);
    
                assertEquals(4, plist.size());
                
                testTransactionNestBatchDelete(plist);
                
                builder = selectAllFrom(p).where(p.PeopleID.gt(0)).orderBy(p.PeopleID.asc()).into(Person.class);
                builder.setHints(hints);
                assertEquals(0, dao.query(builder).size());
                
                testTransactionNestInsert(plist);
                
                builder = selectAllFrom(p).where(p.PeopleID.gt(0)).orderBy(p.PeopleID.asc()).into(Person.class);
                builder.setHints(hints);
                assertEquals(4, dao.query(builder).size());
                return plist;
            });
        });
    }
    
    private void testTransactionNestBatchDelete(List<Person> plist) throws SQLException {
        PersonDefinition p = Person.PERSON;
        dao.execute(() -> {
            assertArrayEquals(new int[] {1, 1, 1, 1}, dao.batchDelete(plist));
            
            SqlBuilder builder = selectAllFrom(p).where(111, p.PeopleID.gt(0)).orderBy(p.PeopleID.asc()).into(Person.class);
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