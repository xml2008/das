package com.ppdai.das.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.ppdai.das.client.Hints;
import com.ppdai.das.core.client.DalParser;
import com.ppdai.das.core.task.DaoTask;
import com.ppdai.das.core.task.KeyHolderAwaredTask;
import com.ppdai.das.service.ColumnMeta;
import com.ppdai.das.service.Entity;
import com.ppdai.das.service.EntityMeta;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyHolderTest {

    @BeforeClass
    public static void beforeClass(){
        DasConfigureContext dasConfigureContext = new DasConfigureContext(new DefaultLogger());
        DasConfigureFactory.initialize(dasConfigureContext);
    }

    @Test
    public void test() throws SQLException {
        KeyHolder keyHolder = new KeyHolder();
        keyHolder.initialize(1);
        keyHolder.addKey(ImmutableMap.of("1", Integer.parseInt("2")));
        List<Map<String, Object>> keyList = keyHolder.getKeyList();
        List<Number> ids = keyHolder.getIdList();
        Assert.assertEquals(1, keyList.size());
        Assert.assertEquals(1, ids.size());
        Assert.assertEquals(2, keyHolder.getKey());
        Assert.assertEquals(2, keyHolder.getKey(0));
        Assert.assertEquals(1, keyHolder.getKeys().size());
        Assert.assertEquals(1, keyHolder.getUniqueKey().size());
        Assert.assertFalse(keyHolder.isMerged());
        Assert.assertFalse(keyHolder.isRequireMerge());

        keyHolder.requireMerge();
        keyHolder.addKeys(Lists.newArrayList(ImmutableMap.of("3", Integer.parseInt("4"))));
        Assert.assertEquals(1, keyHolder.getUniqueKey().size());
    }

    @Test
    public void testSetPrimaryKey() throws Exception {
        MyEntity entity = new MyEntity();
        KeyHolder.setPrimaryKey(MyEntity.class.getDeclaredField("bif"), entity, null);
        Assert.assertNull(entity.bif);
        KeyHolder.setPrimaryKey(MyEntity.class.getDeclaredField("lf"), entity, 1);
        Assert.assertEquals(1, entity.lf);
        KeyHolder.setPrimaryKey(MyEntity.class.getDeclaredField("intf"), entity, 1);
        Assert.assertEquals(1, entity.intf);
        KeyHolder.setPrimaryKey(MyEntity.class.getDeclaredField("bytef"), entity, 1);
        Assert.assertEquals(1, entity.bytef);
        KeyHolder.setPrimaryKey(MyEntity.class.getDeclaredField("shf"), entity, 1);
        Assert.assertEquals(1, entity.shf);
        KeyHolder.setPrimaryKey(MyEntity.class.getDeclaredField("bif"), entity, 1);
        Assert.assertEquals(BigInteger.valueOf(1), entity.bif);
    }

    @Test
    public void testSetPrimaryKeyMap() throws Exception {
        Map map = new HashMap();
        KeyHolder.setPrimaryKey(null, "nullf", map, null);
        Assert.assertNull(map.get("nullf"));

        KeyHolder.setPrimaryKey(JDBCType.BIGINT.getName(), "bif", map, 1);
        Assert.assertEquals(1L, map.get("bif"));

        KeyHolder.setPrimaryKey(JDBCType.INTEGER.getName(), "intf", map, 1);
        Assert.assertEquals(1, map.get("intf"));

        KeyHolder.setPrimaryKey(JDBCType.BINARY.getName(), "binf", map, 1);
        Assert.assertEquals(Byte.valueOf("1"), map.get("binf"));

        KeyHolder.setPrimaryKey(JDBCType.SMALLINT.getName(), "sf", map, 1);
        Assert.assertEquals(Short.valueOf("1"), map.get("sf"));
    }

    static class MyTask implements DaoTask, KeyHolderAwaredTask {

        @Override
        public void initialize(DalParser parser) {

        }

        @Override
        public DalParser getParser() {
            return null;
        }

        @Override
        public Map<String, ?> getPojoFields(Object daoPojo) {
            return null;
        }

        @Override
        public List<Map<String, ?>> getPojosFields(List daoPojos) {
            return null;
        }
    }

    @Test
    public void testSetGeneratedKeyBack () throws SQLException {
        MyTask myTask = new MyTask();
        Entity entity = new Entity();
        entity.setValue(new Gson().toJson(ImmutableMap.of("keyC", 1)));
        EntityMeta meta = new EntityMeta();
        meta.setPrimaryKeyNames(Lists.newArrayList("keyC"));
        meta.setColumnNames(Lists.newArrayList("keyC"));
        ColumnMeta columnMeta = new ColumnMeta();
        columnMeta.setType(JDBCType.INTEGER.getName());
        meta.setMetaMap(ImmutableMap.of("keyC", columnMeta));
        entity.setEntityMeta(meta);
        Hints hints = Hints.hints().setIdBack().setSize(Lists.newArrayList(entity));
        hints.getKeyHolder().addKey(ImmutableMap.of("keyC", 2));
        KeyHolder.setGeneratedKeyBack(myTask, hints, Lists.newArrayList(entity));
        Assert.assertTrue(Double.parseDouble(new Gson().fromJson(entity.getValue(), Map.class).get("keyC").toString()) > 1);
    }

    @Test(timeout = 1000)
    public void testMerge() throws InterruptedException {
        KeyHolder keyHolder = new KeyHolder();
        KeyHolder tmp = new KeyHolder();
        keyHolder.waitForMerge(1);
        keyHolder.addPatial(new Integer[]{}, tmp);
        keyHolder.waitForMerge();
    }

    @Test(expected = DasException.class)
    public void testkeyListException() throws DasException {
        KeyHolder keyHolder = new KeyHolder();
        keyHolder.requireMerge();
        keyHolder.getKeyList();
    }

    static class MyEntity {
        long lf;
        int intf;
        byte bytef;
        short shf;
        BigInteger bif;
    }
}
