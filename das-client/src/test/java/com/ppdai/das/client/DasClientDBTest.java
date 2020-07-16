package com.ppdai.das.client;

import static com.ppdai.das.client.ParameterDefinition.integerVar;
import static com.ppdai.das.client.ParameterDefinition.varcharVar;
import static com.ppdai.das.client.SqlBuilder.deleteFrom;
import static com.ppdai.das.client.SqlBuilder.insertInto;
import static com.ppdai.das.client.SqlBuilder.select;
import static com.ppdai.das.client.SqlBuilder.selectAllFrom;
import static com.ppdai.das.client.SqlBuilder.selectTop;
import static com.ppdai.das.core.enums.DatabaseCategory.MySql;
import static com.ppdai.das.core.enums.DatabaseCategory.SqlServer;
import static junit.framework.Assert.assertNotSame;
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
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.ppdai.das.client.Person.PersonDefinition;
import com.ppdai.das.core.enums.DatabaseCategory;

@RunWith(Parameterized.class)
public class DasClientDBTest extends DataPreparer {
    private final static String DATABASE_LOGIC_NAME_MYSQL = "MySqlConditionDbShard";
    private final static String DATABASE_LOGIC_NAME_SQLSVR = "SqlSvrConditionDbShard";

    private ShardInfoProvider provider;
    private static PersonDefinition p = Person.PERSON;

    @Parameters
    public static Collection data() {
        return Arrays.asList(new Object[][]{
           /* {SqlServer, new DefaultProvider()},
            {SqlServer, new ShardIdProvider()},
            {SqlServer, new ShardValueProvider()},*/
            {MySql, new DefaultProvider()},
            {MySql, new ShardIdProvider()},
            {MySql, new ShardValueProvider()},
            });
        }

    public DasClientDBTest(DatabaseCategory dbCategory, ShardInfoProvider provider) throws SQLException {
        super(dbCategory);
        this.provider = provider;
    }
    
    public static DasClientDBTest of(DatabaseCategory dbCategory) throws SQLException {
        return new DasClientDBTest(dbCategory, new DefaultProvider());
    }

    @Override
    public String getDbName(DatabaseCategory dbCategory) {
        return dbCategory.equals(MySql) ? DATABASE_LOGIC_NAME_MYSQL : DATABASE_LOGIC_NAME_SQLSVR;
    }
    
    public static interface ShardInfoProvider {
        void process(Person p, Hints hints, int dbShard);
        void where(SqlBuilder sb, int dbShard);
        SqlBuilder insert(int dbShard);
        SqlBuilder update(int dbShard);
        void inShard(Hints hints, int dbShard);
    }
    
    private static class DefaultProvider implements ShardInfoProvider {
        public void process(Person p, Hints hints, int dbShard) {
            p.setCountryID(dbShard);
        }

        public void where(SqlBuilder sb, int dbShard) {
            sb.and(p.CountryID.eq(dbShard));
        }
        
        public SqlBuilder insert(int dbShard) {
            SqlBuilder builder = insertInto(p, p.Name, p.CountryID).values(p.Name.of("Jerry"), p.CountryID.of(dbShard));
            return builder;
        }
        
        public SqlBuilder update(int dbShard) {
            SqlBuilder builder = SqlBuilder.update(Person.PERSON).set(p.Name.eq("Tom"), p.CountryID.eq(dbShard));
            return builder;
        }
        
        public void inShard(Hints hints, int dbShard) {}
    }
    
    private static class ShardIdProvider implements ShardInfoProvider {
        public void process(Person p, Hints hints, int dbShard) {
            hints.inShard(dbShard);
        }

        public void where(SqlBuilder sb, int dbShard) {
            sb.hints().inShard(dbShard);
        }
        
        public SqlBuilder insert(int dbShard) {
            SqlBuilder builder = insertInto(p, p.Name).values(p.Name.of("Jerry"));
            builder.hints().inShard(dbShard);
            return builder;
        }
        
        public SqlBuilder update(int dbShard) {
            SqlBuilder builder = SqlBuilder.update(Person.PERSON).set(p.Name.eq("Tom"));
            builder.hints().inShard(dbShard);
            return builder;
        }
        
        public void inShard(Hints hints, int dbShard) {
            hints.inShard(dbShard);
        }
    }
    
    private static class ShardValueProvider implements ShardInfoProvider {
        public void process(Person p, Hints hints, int dbShard) {
            hints.shardValue(dbShard);
        }

        public void where(SqlBuilder sb, int dbShard) {
            sb.hints().shardValue(dbShard);
        }
        
        public SqlBuilder insert(int dbShard) {
            SqlBuilder builder = insertInto(p, p.Name).values(p.Name.of("Jerry"));
            builder.hints().shardValue(dbShard);
            return builder;
        }
        
        public SqlBuilder update(int dbShard) {
            SqlBuilder builder = SqlBuilder.update(Person.PERSON).set(p.Name.eq("Tom"));
            builder.hints().shardValue(dbShard);
            return builder;
        }

        public void inShard(Hints hints, int dbShard) {
            hints.shardValue(dbShard);
        }
    }
    
    public void process(Person p, Hints hints, int i) {
        provider.process(p, hints, i);
    }
    
    public void process(Person p, Hints hints, int i, int j) {
        process(p, hints, i);
        p.setCityID(j);
    }
    
    public SqlBuilder where(SqlBuilder sb, int i) {
        provider.where(sb, i);
        return sb;
    }
    
    public Hints hints(int i) {
        return new Hints().inShard(i);
    }
    
    public Hints hints() {
        return new Hints();
    }
    
    private long count(int i) throws SQLException {
        PersonDefinition p = Person.PERSON;
        SqlBuilder sb = new SqlBuilder().select("count(1)").from(p).intoObject();
        sb.hints().inShard(i);
        return ((Number)dao.queryObject(sb)).longValue();
    }
    
    @Before
    public void setup() throws Exception {
        for (int i = 0; i < DB_MODE; i++) {
            String[] statements = new String[TABLE_MODE];
            for (int j = 0; j < TABLE_MODE; j++) {
                statements[j] = String.format("INSERT INTO person(PeopleID, Name, CountryID, CityID, ProvinceID) VALUES(%d, 'test', %d, %d, 1)", j + 1, i, j);
            }

            if(!allowInsertWithId())
                statements = DbSetupUtil.handle("Person", statements);

            BatchUpdateBuilder builder = new BatchUpdateBuilder(statements);
            builder.hints().inShard(i);
            dao.batchUpdate(builder);
        }
    }
    
    private boolean allowInsertWithId() {
        return setuper.turnOnIdentityInsert("person") == null;
    }

    @After
    public void tearDown() throws SQLException {
        for (int i = 0; i < DB_MODE; i++) {
            String[] statements = new String[] {"DELETE FROM " + TABLE_NAME};
            BatchUpdateBuilder builder = new BatchUpdateBuilder(statements);
            builder.hints().inShard(i);
            dao.batchUpdate(builder);
        }
    }


    @Test
    public void testCrossShardsPageRoughly() throws Exception{
        for(int i = 0; i < DB_MODE;i++) {
            Person pk = new Person();
            pk.setName("test");
            Hints hints = new Hints();
            List<Person> plist = dao.queryBySample(pk, PageRange.atPage(2, 2, p.PeopleID.asc()), hints.crossShardsPageRoughly());
            assertNotNull(plist);
            assertEquals(2, plist.size());

            assertEquals(plist.get(0).getPeopleID(), plist.get(1).getPeopleID());
            assertNotSame(plist.get(0).getCountryID(), plist.get(1).getCountryID());
        }
    }

}
