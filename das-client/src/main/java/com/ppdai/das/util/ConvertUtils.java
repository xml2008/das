package com.ppdai.das.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.ppdai.das.client.Hints;
import com.ppdai.das.client.SqlBuilder;
import com.ppdai.das.client.sqlbuilder.ColumnOrder;
import com.ppdai.das.client.sqlbuilder.SqlBuilderSerializer;
import com.ppdai.das.core.HintEnum;
import com.ppdai.das.service.DasHintEnum;
import com.ppdai.das.service.DasHints;
import com.ppdai.das.service.DasRequest;
import com.ppdai.das.service.Entity;
import com.ppdai.das.service.EntityMeta;
import org.apache.commons.lang.reflect.FieldUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Utility for convert:
 *
 *    List<Person> -> List<Entity>  -> List<Map> -> List<Entity> -> List<Person>
 *
 * @author Shengyuan
 */
public class ConvertUtils {

    public static DasHints toDasHints(Hints hints) {
        Map<DasHintEnum, String> map = ImmutableMap.<DasHintEnum, String>builder()
                .put(DasHintEnum.dbShard, Objects.toString(hints.getShard(), ""))
                .put(DasHintEnum.tableShard, Objects.toString(hints.getTableShard(), ""))
                .put(DasHintEnum.dbShardValue, Objects.toString(hints.getShardValue(), ""))
                .put(DasHintEnum.tableShardValue, Objects.toString(hints.getTableShardValue(), ""))
                .put(DasHintEnum.setIdentityBack, Boolean.toString(hints.isSetIdBack()))
                .put(DasHintEnum.enableIdentityInsert, Boolean.toString(hints.isInsertWithId()))
                .put(DasHintEnum.diagnoseMode, Boolean.toString(hints.isDiagnose()))
                .put(DasHintEnum.updateNullField, Boolean.toString(hints.isUpdateNullField()))
                .put(DasHintEnum.excludedColumns, set2String(hints.getExcluded()))
                .put(DasHintEnum.sortColumns, serializeSortColumns(hints.getSorter()))
                .put(DasHintEnum.crossShardsPageRoughly, Boolean.toString(hints.isCrossShardsPageRoughly()))
                .build();
        return new DasHints().setHints(map);
    }

    static String serializeSortColumns(List<ColumnOrder> sorter) {
        if(sorter == null || sorter.isEmpty()) {
            return "";
        } else {
            return SqlBuilderSerializer.serializeSegment(
                    new SqlBuilder().append(sorter.toArray(new ColumnOrder[sorter.size()])));
        }
    }

    static String set2String(Set<String> set) {
        if(set == null || set.isEmpty()) {
            return "";
        } else {
            return Joiner.on(",").join(set);
        }
    }

    public static Hints translate(DasHints dasHints) {
        if (dasHints == null) {
            return null;
        }

        Map<DasHintEnum, String> map = dasHints.getHints();
        Hints result = new Hints();
        String dbShard = map.get(DasHintEnum.dbShard);
        if(!isNullOrEmpty(dbShard)){
            result.inShard(dbShard);
        }
        String tableShard = map.get(DasHintEnum.tableShard);
        if(!isNullOrEmpty(tableShard)){
            result.inTableShard(tableShard);
        }
        String dbShardValue = map.get(DasHintEnum.dbShardValue);
        if(!isNullOrEmpty(dbShardValue)){
            result.shardValue(dbShardValue);
        }
        String tableShardValue = map.get(DasHintEnum.tableShardValue);
        if(!isNullOrEmpty(tableShardValue)){
            result.tableShardValue(tableShardValue);
        }
        if(Boolean.valueOf(map.get(DasHintEnum.setIdentityBack))) {
            result.setIdBack();
        }
        if(Boolean.valueOf(map.get(DasHintEnum.enableIdentityInsert))) {
            result.insertWithId();
        }
        if(Boolean.valueOf(map.get(DasHintEnum.diagnoseMode))) {
            result.diagnose();
        }
        if(Boolean.valueOf(map.get(DasHintEnum.updateNullField))) {
            result.updateNullField();
        }
        if(Boolean.valueOf(map.get(DasHintEnum.crossShardsPageRoughly))) {
            result.crossShardsPageRoughly();
        }
        String excludedColumns = map.get(DasHintEnum.excludedColumns);
        if(!isNullOrEmpty(excludedColumns)){
            result.set(HintEnum.excludedColumns, set2String(excludedColumns));
        }
        String sortColumns = map.get(DasHintEnum.sortColumns);
        if(!isNullOrEmpty(sortColumns)){
            result.set(HintEnum.sortColumns, deserializeSortColumns(sortColumns));
        }
        return result;
    }

    static List<ColumnOrder> deserializeSortColumns(String sortColumns) {
        return SqlBuilderSerializer.deserializeSegment(sortColumns).getSegments()
                .stream().map(s -> (ColumnOrder)s)
                .collect(Collectors.toList());
    }

    static Set<String> set2String(String excludedColumns) {
        return Sets.newHashSet(Splitter.on(",").split(excludedColumns));
    }

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
        tree.forEach(e-> entity.put(map.get(e.getKey()), e.getValue()));
        return new Entity().setValue(new Gson().toJson(entity)).setEntityMeta(meta);
    }

    public static List<Entity> pojo2Entities(List rows, EntityMeta meta) {
        checkNotNull(rows);

        return ((List<Object>)rows).stream().map(obj -> pojo2Entity(obj, meta)).collect(Collectors.toList());
    }

    public static <T> T entity2POJO(Entity r, EntityMeta meta, Class clz) {
        try {
            if(clz == long.class) {
                Long l = new Gson().fromJson(r.getValue(), Long.class);
                return (T) l;
            } else if(clz == int.class) {
                Integer i = new Gson().fromJson(r.getValue(), Integer.class);
                return (T) i;
            }else if (clz == Map.class) {
                ObjectMapper mapper = new ObjectMapper();
                return (T) mapper.readValue(r.getValue(), Map.class);
            }  else if (clz == String.class) {
                return (T) new Gson().fromJson(r.getValue(), String.class);
            }else if(clz == Object.class) {
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
            }

            T obj = (T) clz.newInstance();
            Map map = new Gson().fromJson(r.getValue(), Map.class);
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
                    }
                    FieldUtils.writeField(obj, fieldName, value, true);
                }else {
                    Object value = map.get(col);
                    if(java.util.Date.class.isAssignableFrom(f.getType()) && value != null){
                        Constructor constructor = f.getType().getDeclaredConstructor(long.class);
                        constructor.setAccessible(true);
                        value = (Date) constructor.newInstance(((Number)value).longValue());
                    }
                    FieldUtils.writeField(obj, meta.getFieldMap().get(col), value, true);
                }
            }
            return obj;
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
