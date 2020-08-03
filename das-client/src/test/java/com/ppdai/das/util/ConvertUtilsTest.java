package com.ppdai.das.util;

import com.google.gson.Gson;
import com.ppdai.das.client.delegate.remote.DasRemoteDelegate;
import com.ppdai.das.service.DasRequest;
import com.ppdai.das.service.Entity;
import com.ppdai.das.service.EntityList;
import com.ppdai.das.service.EntityMeta;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


public class ConvertUtilsTest {

    @Test
    public void testPojo2EntityMySQLNull() {
        TesttableMySQL pojo = new TesttableMySQL();
        EntityMeta meta = DasRemoteDelegate.extract(pojo.getClass());
        List<Entity> entities = ConvertUtils.pojo2Entities(Arrays.asList(pojo), meta);
        List<Map<String, Object>> maps = ConvertUtils.entity2POJOs(entities, meta, Map.class);
        List<TesttableMySQL> entity = ConvertUtils.entity2POJOs(entities, meta, TesttableMySQL.class);

        Assert.assertNull(maps.get(0).get("MyID"));
        Assert.assertNull(entity.get(0).getMyID());
    }

    @Test
    public void testPojo2EntityMySQL() {
        TesttableMySQL pojo = createMySQLPOJO();
        EntityMeta meta = DasRemoteDelegate.extract(pojo.getClass());
        List<Entity> entities = ConvertUtils.pojo2Entities(Arrays.asList(pojo), meta);
        List<Map<String, Object>> maps = ConvertUtils.entity2POJOs(entities, meta, Map.class);
        List<TesttableMySQL> entity = ConvertUtils.entity2POJOs(entities, meta, TesttableMySQL.class);
        assertEquals(1, maps.size());
        Map<String, Object> map = maps.get(0);

        assertEquals(pojo.getMyID(), map.get("MyID"));
        assertEquals(pojo.getMyBit(), map.get("MyBit"));
        assertEquals(pojo.getMyTinyint(), Integer.valueOf(map.get("MyTinyint").toString()));
        assertEquals(pojo.getMySmallint(), Integer.valueOf(map.get("MySmallint").toString()));
        assertEquals(pojo.getMyMediumint(), map.get("MyMediumint"));
        assertEquals(pojo.getMyBigint(), map.get("MyBigint"));
        assertEquals(pojo.getMyDecimal().doubleValue(), ((BigDecimal)map.get("MyDecimal")).doubleValue(), 1d);
        assertEquals(pojo.getMyFloat(), map.get("MyFloat"));
        assertEquals(pojo.getMyDouble(), map.get("MyDouble"));
        assertEquals(pojo.getMyBool(), map.get("MyBool"));
        assertEquals(pojo.getMyDate(), map.get("MyDate"));
        assertEquals(pojo.getMyDatetime(), map.get("MyDatetime"));
        assertEquals(pojo.getMyTimestamp(), map.get("MyTimestamp"));
        assertEquals(pojo.getMyTime(), map.get("MyTime"));
        assertEquals(pojo.getMyYear(), map.get("MyYear"));
        assertEquals(pojo.getMyVarchar(), map.get("MyVarchar"));
        assertEquals(pojo.getMyChar(), map.get("MyChar"));
        assertEquals(pojo.getMyText(), map.get("MyText"));
        assertArrayEquals(pojo.getMyBinary(), (byte[]) map.get("MyBinary"));
        assertArrayEquals(pojo.getMyVarbinary(), (byte[])map.get("MyVarbinary"));
        assertArrayEquals(pojo.getMyBlob(), (byte[])map.get("MyBlob"));

        TesttableMySQL testtableMySQL = entity.get(0);
        assertEquals(pojo.getMyID(), testtableMySQL.getMyID());
        assertEquals(pojo.getMyBit(), testtableMySQL.getMyBit());
        assertEquals(pojo.getMyTinyint(), testtableMySQL.getMyTinyint());
        assertEquals(pojo.getMySmallint(), testtableMySQL.getMySmallint());
        assertEquals(pojo.getMyMediumint(), testtableMySQL.getMyMediumint());
        assertEquals(pojo.getMyBigint(), testtableMySQL.getMyBigint());
        assertEquals(pojo.getMyDecimal().doubleValue(), testtableMySQL.getMyDecimal().doubleValue(), 1);
        assertEquals(pojo.getMyFloat(), testtableMySQL.getMyFloat());
        assertEquals(pojo.getMyDouble(), testtableMySQL.getMyDouble());
        assertEquals(pojo.getMyBool(), testtableMySQL.getMyBool());
        assertEquals(pojo.getMyDate(), testtableMySQL.getMyDate());
        assertEquals(pojo.getMyDatetime(), testtableMySQL.getMyDatetime());
        assertEquals(pojo.getMyTimestamp(), testtableMySQL.getMyTimestamp());
        assertEquals(pojo.getMyTime(), testtableMySQL.getMyTime());
        assertEquals(pojo.getMyYear(), testtableMySQL.getMyYear());
        assertEquals(pojo.getMyVarchar(), testtableMySQL.getMyVarchar());
        assertEquals(pojo.getMyChar(), testtableMySQL.getMyChar());
        assertEquals(pojo.getMyText(), testtableMySQL.getMyText());
        assertArrayEquals(pojo.getMyBinary(), testtableMySQL.getMyBinary());
        assertArrayEquals(pojo.getMyVarbinary(), testtableMySQL.getMyVarbinary());
        assertArrayEquals(pojo.getMyBlob(), testtableMySQL.getMyBlob());
    }

    @Test
    public void testPojo2EntitySQLServer() {
        TesttableSQLServer pojo = createSQLServerPOJO();
        EntityMeta meta = DasRemoteDelegate.extract(pojo.getClass());
        List<Entity> entities = ConvertUtils.pojo2Entities(Arrays.asList(pojo), meta);
        List<Map<String, Object>> maps = ConvertUtils.entity2POJOs(entities, meta, Map.class);

        assertEquals(1, maps.size());
        Map<String, Object> map = maps.get(0);

        assertEquals(pojo.getMyID(), map.get("MyID"));
        assertEquals(pojo.getMyBigint(), map.get("MyBigint"));
        assertEquals(pojo.getMyNumeric().doubleValue(), ((BigDecimal)map.get("MyNumeric")).doubleValue(), 1);
        assertEquals(pojo.getMyBit(), map.get("MyBit"));
        assertEquals(pojo.getMySmallint(), Short.valueOf(map.get("MySmallint").toString()));
        assertEquals(pojo.getMyDecimal().doubleValue(), ((BigDecimal)map.get("MyDecimal")).doubleValue(), 1);
        assertEquals(pojo.getMySmallmoney().doubleValue(), ((BigDecimal)map.get("MySmallmoney")).doubleValue(), 1);
        assertEquals(pojo.getMyTinyint(), Short.valueOf(map.get("MyTinyint").toString()));
        assertEquals(pojo.getMyMoney().doubleValue(), ((BigDecimal)map.get("MyMoney")).doubleValue(), 1);
        assertEquals(pojo.getMyFloat(), map.get("MyFloat"));
        assertEquals(pojo.getMyReal(), map.get("MyReal"));
        assertEquals(pojo.getMyDate(), map.get("MyDate"));
        assertEquals(pojo.getMyDatetime2(), map.get("MyDatetime2"));
        assertEquals(pojo.getMySmalldatetime(), map.get("MySmalldatetime"));
        assertEquals(pojo.getMyDatetime(), map.get("MyDatetime"));
        assertEquals(pojo.getMyTime(), map.get("MyTime"));
        assertEquals(pojo.getMyChar(), map.get("MyChar"));
        assertEquals(pojo.getMyText(), map.get("MyText"));
        assertArrayEquals(pojo.getMyBinary(), (byte[]) map.get("MyBinary"));
        assertArrayEquals(pojo.getMyVarbinary(), (byte[])map.get("MyVarbinary"));
        assertArrayEquals(pojo.getMyImage(), (byte[])map.get("MyImage"));

        TesttableSQLServer testtableSQLServer = (TesttableSQLServer) ConvertUtils.entity2POJOs(entities, meta, TesttableSQLServer.class).get(0);
        assertEquals(pojo.getMyID(), testtableSQLServer.getMyID());
        assertEquals(pojo.getMyBigint(), testtableSQLServer.getMyBigint());
        assertEquals(pojo.getMyNumeric().doubleValue(), testtableSQLServer.getMyNumeric().doubleValue(), 1);
        assertEquals(pojo.getMyBit(), testtableSQLServer.getMyBit());
        assertEquals(pojo.getMySmallint(), testtableSQLServer.getMySmallint());
        assertEquals(pojo.getMyDecimal().doubleValue(), testtableSQLServer.getMyDecimal().doubleValue(), 1);
        assertEquals(pojo.getMySmallmoney().doubleValue(), testtableSQLServer.getMySmallmoney().doubleValue(), 1);
        assertEquals(pojo.getMyTinyint(), testtableSQLServer.getMyTinyint());
        assertEquals(pojo.getMyMoney().doubleValue(), testtableSQLServer.getMyMoney().doubleValue(), 1);
        assertEquals(pojo.getMyFloat(), testtableSQLServer.getMyFloat());
        assertEquals(pojo.getMyReal(), testtableSQLServer.getMyReal());
        assertEquals(pojo.getMyDate(), testtableSQLServer.getMyDate());
        assertEquals(pojo.getMyDatetime2(), testtableSQLServer.getMyDatetime2());
        assertEquals(pojo.getMySmalldatetime(), testtableSQLServer.getMySmalldatetime());
        assertEquals(pojo.getMyDatetime(), testtableSQLServer.getMyDatetime());
        assertEquals(pojo.getMyTime(), testtableSQLServer.getMyTime());
        assertEquals(pojo.getMyChar(), testtableSQLServer.getMyChar());
        assertEquals(pojo.getMyText(), testtableSQLServer.getMyText());
        assertArrayEquals(pojo.getMyBinary(),  testtableSQLServer.getMyBinary());
        assertArrayEquals(pojo.getMyVarbinary(), testtableSQLServer.getMyVarbinary());
        assertArrayEquals(pojo.getMyImage(), testtableSQLServer.getMyImage());
    }

    @Test
    public void testFillMeta() {
        EntityList entityList = new EntityList();
        TesttableMySQL pojo = createMySQLPOJO();
        EntityMeta meta = DasRemoteDelegate.extract(pojo.getClass());
        entityList.setEntityMeta(meta);
        Entity entity = new Entity();
        entity.setValue(new Gson().toJson(pojo));
        entityList.addToRows(new Entity());

        DasRequest dasRequest = new DasRequest().setEntityList(entityList);
        Entity entity1 = ConvertUtils.fillMeta(dasRequest);
        List<Entity> entity2 = ConvertUtils.fillMetas(dasRequest);
        Assert.assertNotNull(dasRequest.getEntityList().getRows().get(0).getEntityMeta());
    }

    private TesttableMySQL createMySQLPOJO() {
        TesttableMySQL pojo = new TesttableMySQL();
        pojo.setMyBigint(18L);
        pojo.setMyBinary("MyBinary".getBytes());
        pojo.setMyBit(true);
        pojo.setMyBlob("MyBlob".getBytes());
        pojo.setMyChar("Mychar");
        pojo.setMyDate(new Date(1));
        pojo.setMyDatetime(new Timestamp(2));
        pojo.setMyDecimal(new BigDecimal(345.567));
        pojo.setMyDouble(4.1d);
        pojo.setMyFloat(5.1f);
        pojo.setMyID(6);
        pojo.setMyMediumint(7);
        pojo.setMySmallint(8);
        pojo.setMyText(null);//null test
        pojo.setMyTime(new Time(4));
        pojo.setMyTinyint(1);
        pojo.setMyVarbinary("varchBinary".getBytes());
        pojo.setMyVarchar("varch");
        pojo.setMyYear(new Date(5));
        pojo.setMyBool(true);
        pojo.setMyTimestamp(new Timestamp(5));

        return pojo;
    }

    private TesttableSQLServer createSQLServerPOJO() {
        TesttableSQLServer pojo = new TesttableSQLServer();

        pojo.setMyID(6);
        pojo.setMyBigint(18L);
        pojo.setMyBinary("MyBinary".getBytes());
        pojo.setMyNumeric(new BigDecimal(98.123));
        pojo.setMyBit(true);
        pojo.setMySmallmoney(new BigDecimal(9876.1234));
        pojo.setMyChar("Mychar");
        pojo.setMyDate(new Date(1));
        pojo.setMyDatetime(new Timestamp(2));
        pojo.setMyDatetime2(new Timestamp(200000));
        pojo.setMySmalldatetime(new Timestamp(300000));
        pojo.setMyDecimal(new BigDecimal(345.567));
        pojo.setMyFloat(5.1d);
        pojo.setMyReal(3.22f);
        pojo.setMySmallint((short)2);
        pojo.setMyText(null);//null test
        pojo.setMyTime(new Time(4));
        pojo.setMyTinyint((short)3);
        pojo.setMyMoney(new BigDecimal(100000.002));
        pojo.setMyVarbinary("varchBinary".getBytes());
        pojo.setMyVarchar("varch");
        pojo.setMyImage("image".getBytes());

        return pojo;
    }


}
