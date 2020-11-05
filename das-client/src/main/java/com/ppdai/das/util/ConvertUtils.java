package com.ppdai.das.util;

import com.google.common.collect.HashBiMap;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.ppdai.das.service.ColumnMeta;
import com.ppdai.das.service.DasRequest;
import com.ppdai.das.service.Entity;
import com.ppdai.das.service.EntityMeta;
import org.apache.commons.lang.reflect.FieldUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.JDBCType;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility for convert:
 *
 *    List<Person> -> List<Entity>  -> List<Map> -> List<Entity> -> List<Person>
 *
 * @author Shengyuan
 */
public class ConvertUtils {

    public static Entity pojo2Entity(Object row, EntityMeta meta)  {
        checkNotNull(row);

        if(row instanceof Entity) {
            return (Entity) row;
        }

        if(meta == null || row instanceof Map) {
            String json = new GsonBuilder().registerTypeHierarchyAdapter(Date.class, (JsonSerializer<Date>) (date, typeOfSrc, context) ->
                    new JsonPrimitive(date.getTime())
            ).create().toJson(row);
            return new Entity().setValue(json);
        }

        Set<Map.Entry<String, JsonElement>> tree = ((JsonObject)
                new GsonBuilder()
                        .registerTypeHierarchyAdapter(Date.class, (JsonSerializer<Date>) (date, typeOfSrc, context) ->
                                new JsonPrimitive(date.getTime())
                        ).create().toJsonTree(row)).entrySet();

        Map<String, Object> entity = new HashMap<>();
        Map<String, String> map = HashBiMap.create(meta.getFieldMap()).inverse();
        tree.forEach(e-> {
            String k = map.get(e.getKey());
            if(k != null) {
                entity.put(k, e.getValue());
            }
        });
        return new Entity().setValue(new Gson().toJson(entity)).setEntityMeta(meta);
    }

    public static List<Entity> pojo2Entities(List rows, EntityMeta meta) {
        checkNotNull(rows);

        return ((List<Object>)rows).stream().map(obj -> pojo2Entity(obj, meta)).collect(Collectors.toList());
    }

    private static void queryEntity(Object obj, Map<String, Object> map, EntityMeta meta, Class clz) throws Exception{
        for (String col : meta.getColumnNames()) {
            Field f = clz.getDeclaredField(meta.getFieldMap().get(col));
            f.setAccessible(true);

            if(Number.class.isAssignableFrom(f.getType())) {
                Number value = (Number) map.get(col);
                if (value == null) {
                    continue;
                }
                String fieldName = meta.getFieldMap().get(col);
                Class fieldClz = FieldUtils.getField(clz, fieldName, true).getType();

                if (fieldClz == long.class || fieldClz == Long.class) {
                    value = value.longValue();
                } else if (fieldClz == int.class || fieldClz == Integer.class) {
                    value = value.intValue();
                } else if (fieldClz == float.class || fieldClz == Float.class) {
                    value =  value.floatValue();
                } else if (fieldClz == double.class || fieldClz == Double.class) {
                    value = value.doubleValue();
                }else if (fieldClz == BigDecimal.class) {
                    value = BigDecimal.valueOf(value.doubleValue());
                }
                FieldUtils.writeField(obj, fieldName, value, true);
            }else {
                if(f.getType().getSimpleName().equals("byte[]")) {
                    //TODO:
                    continue;
                }
                Object value = map.get(col);
                if(java.util.Date.class.isAssignableFrom(f.getType()) && value != null){
                    Constructor constructor = f.getType().getDeclaredConstructor(long.class);
                    constructor.setAccessible(true);
                    value = constructor.newInstance(((Number)value).longValue());
                }
                FieldUtils.writeField(obj, meta.getFieldMap().get(col), value, true);
            }
        }
    }

    public static <T> T entity2POJO(Entity r, EntityMeta meta, Class clz) {
        try {
            if(clz == long.class) {
                Long l = new Gson().fromJson(r.getValue(), Long.class);
                return (T) l;
            } else if(clz == int.class) {
                Integer i = new Gson().fromJson(r.getValue(), Integer.class);
                return (T) i;
            } else if (clz == String.class) {
                return (T) new Gson().fromJson(r.getValue(), String.class);
            } else if(clz == Object.class) {
                String str = new Gson().fromJson(r.getValue(), String.class);
                Long l = Longs.tryParse(str);
                if(l != null) {
                    return (T) l;
                }
                Double d = Doubles.tryParse(str);
                if(d != null) {
                    return (T) d;
                }
                return (T) str;
            } else {
                Map<String, Object> origin = new Gson().fromJson(r.getValue(), Map.class);
                Object instance = clz == Map.class ? new HashMap<>() : clz.newInstance();

                if(meta.getColumnTypes() == null) {
                    return (T) origin;
                }
                //For query object without enough EntityMeta
                if(meta.getColumnTypes().isEmpty()) {
                    queryEntity(instance, origin, meta, clz);
                    return (T) instance;
                }
                for(Map.Entry<String, Object> e: origin.entrySet()) {
                    final String key = e.getKey();
                    Object value = e.getValue();
                    Object value2 = e.getValue();

                    Optional optional = meta.getColumnNames().stream().filter(n -> n.equals(key)).findFirst();
                    if(optional.isPresent() && value != null) {
                        ColumnMeta columnMeta = meta.getMetaMap().get(key);
                        if (columnMeta != null) {
                            final String columnMetaType= columnMeta.getType();
                            if(columnMetaType !=null ) {
                                if (columnMetaType.equals(JDBCType.INTEGER.getName()) ||
                                        columnMetaType.equals(JDBCType.TINYINT.getName()) ||
                                        columnMetaType.equals(JDBCType.SMALLINT.getName())) {
                                    value = ((Number)value).intValue();
                                    value2 = ((Number)value).shortValue();

                                } else if (columnMetaType.equals(JDBCType.DOUBLE.getName())) {
                                    value = ((Number)value).doubleValue();

                                } else if (columnMetaType.equals(JDBCType.REAL.getName())) {
                                    value = ((Number)value).floatValue();

                                } else if (columnMetaType.equals(JDBCType.BIGINT.getName())) {
                                    value = ((Number)value).longValue();

                                } else if(columnMetaType.equals(JDBCType.LONGVARBINARY.getName()) ||
                                        columnMetaType.equals(JDBCType.BINARY.getName()) ||
                                        columnMetaType.equals(JDBCType.VARBINARY.getName())) {

                                    byte[] bytes = new byte[((List)value).size()];
                                    for(int i = 0; i < bytes.length; i++) {
                                        bytes[i] =  ((Number)((List)value).get(i)).byteValue();
                                    }
                                    value = bytes;

                                } else if (columnMetaType.equals(JDBCType.DECIMAL.getName()) ||
                                        columnMetaType.equals(JDBCType.NUMERIC.getName())) {
                                    value = BigDecimal.valueOf(((Number)value).doubleValue());

                                } else if (columnMetaType.equals(JDBCType.TIME.getName())) {
                                    value = new Time(((Number)value).longValue());

                                } else if (columnMetaType.equals(JDBCType.TIMESTAMP.getName())) {
                                    value = new Timestamp(((Number)value).longValue());

                                } else if (columnMetaType.equals(JDBCType.DATE.getName())) {
                                    value = new java.sql.Date(((Number)value).longValue());
                                }
                            }

                            if (instance instanceof Map) {
                                ((Map) instance).put(key, value);
                            } else {
                                String f = meta.getFieldMap().get(key);

                                Field field = FieldUtils.getDeclaredField(instance.getClass(), f, true);

                                if(field.getType().isAssignableFrom(value.getClass())) {
                                    FieldUtils.writeField(instance, f, value, true);

                                } else if(field.getType().isAssignableFrom(value2.getClass())) {
                                    FieldUtils.writeField(instance, f, value2, true);

                                } else {
                                    //Number types
                                    final Class fieldType = field.getType();
                                    if(fieldType == Long.class || fieldType == long.class) {
                                        value = ((Number)value).longValue();
                                    } else if(fieldType == Integer.class || fieldType == int.class) {
                                        value = ((Number)value).intValue();
                                    } else if(fieldType == Double.class || fieldType == double.class) {
                                        value = ((Number)value).doubleValue();
                                    } else if(fieldType == Float.class || fieldType == float.class) {
                                        value = ((Number)value).floatValue();
                                    } else if(fieldType == Short.class || fieldType == short.class) {
                                        value = ((Number)value).shortValue();
                                    } else if(fieldType == Byte.class || fieldType == byte.class) {
                                        value = ((Number)value).byteValue();
                                    } else if(fieldType == BigInteger.class) {
                                        value = BigInteger.valueOf(((Number)value).longValue());
                                    }

                                    FieldUtils.writeField(instance, f, value, true);
                                }
                            }
                        }
                    }
                }
                return (T) instance;
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> entity2POJOs(List<Entity> rows, EntityMeta meta, Class clz) {
        checkNotNull(rows);
        checkNotNull(clz);

        return rows.stream().map(r -> (T) entity2POJO(r, meta, clz)).collect(Collectors.toList());
    }

    public static Entity fillMeta(DasRequest request) {
        EntityMeta meta = request.getEntityList().getEntityMeta();
        Entity entity = request.getEntityList().getRows().get(0);
        return entity.setEntityMeta(meta);
    }

    public static List<Entity> fillMetas(DasRequest request) {
        EntityMeta meta = request.getEntityList().getEntityMeta();
        return request.getEntityList().getRows().stream().map(
                e -> e.setEntityMeta(meta)
        ).collect(Collectors.toList());
    }

}
