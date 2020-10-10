package com.ppdai.das.client.sqlbuilder;

import com.ppdai.das.client.*;
import com.ppdai.das.client.delegate.local.DasBuilderContext;
import com.ppdai.das.client.Hints;

import com.ppdai.das.client.delegate.remote.BuilderUtils;
import com.ppdai.das.service.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.JDBCType;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.ppdai.das.client.SegmentConstants.AND;
import static com.ppdai.das.client.SegmentConstants.INSERT;
import static com.ppdai.das.client.SegmentConstants.INTO;
import static com.ppdai.das.client.SegmentConstants.OR;
import static com.ppdai.das.client.SegmentConstants.expression;
import static com.ppdai.das.client.SegmentConstants.var;
import static com.ppdai.das.client.SqlBuilder.select;
import static com.ppdai.das.client.sqlbuilder.Person.PERSON;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import com.ppdai.das.core.enums.ParameterDirection;
public class BuilderUtilsTest {

    @Test
    public void testFromJoin(){
        Person.PersonDefinition p = Person.PERSON;
        SqlBuilder builder1 = new SqlBuilder().from(p.as("TN"));
        assertBuilder(builder1);

        SqlBuilder builder2 = new SqlBuilder().from(p.as("T1"), p.as("T2"));
        assertBuilder(builder2);

        SqlBuilder builder3 = new SqlBuilder().from(p).join(p.as("T1"));
        assertBuilder(builder3);

        SqlBuilder builder4 = new SqlBuilder().from(p).crossJoin(p.as("TC"));
        assertBuilder(builder4);

        SqlBuilder builder5 = new SqlBuilder().from(p).fullJoin(p.as("TF"));
        assertBuilder(builder5);

        SqlBuilder builder6 = new SqlBuilder().from(p).innerJoin(p.as("TI"));
        assertBuilder(builder6);

        SqlBuilder builder7 = new SqlBuilder().from(p).leftJoin(p.as("TL"));
        assertBuilder(builder7);
    }

    @Test
    public void testDeleteInsertUpdate(){
        Person.PersonDefinition p = Person.PERSON;
        SqlBuilder builder1 = SqlBuilder.deleteFrom(p);
        assertBuilder(builder1);

        SqlBuilder builder2 = SqlBuilder.insertInto(p);
        assertBuilder(builder2);

        SqlBuilder builder3 = SqlBuilder.update(p).set(p.Name.eq("Tom"), p.CountryID.eq(100));
        assertBuilder(builder3);

        SqlBuilder builder4 = SqlBuilder.insertInto(p, p.Name, p.ProvinceID)
                .values(p.Name.of("PN"), p.ProvinceID.of(6));
        assertBuilder(builder4);
    }

    @Test
    public void testTpLimit(){
        SqlBuilder builder1 = new SqlBuilder().top(3);
        assertBuilder(builder1);

        SqlBuilder builder2 = new SqlBuilder().limit(4);
        assertBuilder(builder2);

        SqlBuilder builder3 = new SqlBuilder().limit(1, 5);
        assertBuilder(builder3);

        SqlBuilder builder4 = new SqlBuilder().offset(2,5);
        assertBuilder(builder4);
    }

    @Test
    public void testWhere(){
        Person.PersonDefinition p = Person.PERSON;
        SqlBuilder builder1 = new SqlBuilder().where(p.Name.eq("X"), p.CityID.eq(2), p.DataChange_LastTime.eq(new Date()));
        assertBuilder(builder1);

        SqlBuilder builder2 = new SqlBuilder().where().allOf(p.Name.eq("X"), p.CityID.eq(2));
        assertBuilder(builder2);

        SqlBuilder builder3 = SqlBuilder.selectAllFrom(p)
                .where(p.ProvinceID.eq(1)).and().leftBracket().append(p.CityID.eq(1)).or(p.CityID.eq(2)).rightBracket().append(" FOR UPDATE");
        assertBuilder(builder3);

        SqlBuilder builder4 = new SqlBuilder().where().not(p.CityID.eq(9));
        assertBuilder(builder4);

        SqlBuilder builder5 = new SqlBuilder().anyOf(expression("aaa"), expression("bbb"));
        assertBuilder(builder5);

        SqlBuilder builder6 = new SqlBuilder().append(expression("bbb"));
        assertBuilder(builder6);
    }

    @Test
    public void testTemplate(){
        SqlBuilder sqlBuilder1 = new SqlBuilder()
                .appendTemplate("update  person set PeopleID=5 where PeopleID = ? AND CountryID = ?",
                        Parameter.integerOf("", 3), Parameter.integerOf("", 4))
                .into(Person.class);
        assertBuilder(sqlBuilder1);

        SqlBuilder sqlBuilder2 = new SqlBuilder().appendTemplate(SegmentConstants.AND.getText());
        sqlBuilder2.appendTemplate("inserttime<date_sub(now(), INTERVAL ? SECOND)",
                Parameter.integerOf("", 5));
        assertBuilder(sqlBuilder2);
    }

    @Test
    public void testMisc(){
        Person.PersonDefinition p = Person.PERSON;

        SqlBuilder sqlBuilder1 = new SqlBuilder().withLock();
        assertBuilder(sqlBuilder1);

        SqlBuilder sqlBuilder2 = new SqlBuilder().groupBy(p.Name, p.ProvinceID).having(p.CityID.eq(7));
        assertBuilder(sqlBuilder2);

        SqlBuilder sqlBuilder3 = new SqlBuilder().atPage(3, 9);
        assertBuilder(sqlBuilder3);
    }

    @Test
    public void testOrder(){
        Person.PersonDefinition p = Person.PERSON;
        SqlBuilder builder1 = new SqlBuilder().orderBy(p.ProvinceID.asc(), p.Name.desc(), p.PeopleID.asc());
        assertBuilder(builder1);
    }

    @Test
    public void testSelect(){
        Person.PersonDefinition p = Person.PERSON;
        SqlBuilder builder1 = SqlBuilder.select(p.Name.as("TN"), p.CityID.as("TP"));
        assertBuilder(builder1);

        SqlBuilder builder2 = SqlBuilder.selectAll();
        assertBuilder(builder2);

        SqlBuilder builder3 = SqlBuilder.selectCount();
        assertBuilder(builder3);

        SqlBuilder builder4 = SqlBuilder.selectAllFrom(p);
        assertBuilder(builder4);

        SqlBuilder builder5 = SqlBuilder.selectDistinct(p.PeopleID, p.Name);
        assertBuilder(builder5);

        SqlBuilder builder6 = SqlBuilder.selectTop(3, p.ProvinceID, p.DataChange_LastTime);
        assertBuilder(builder6);
    }

    @Test
    public void testTableShard(){
        Person.PersonDefinition p = Person.PERSON;
        SqlBuilder builderX = new SqlBuilder().from(p.inShard("A").shardBy("B"));

        SqlBuilder builderY = mirror(builderX);
        Assert.assertEquals("A", ((TableDeclaration)builderY.getFilteredSegments().get(1)).getShardId());
        Assert.assertEquals("B", ((TableDeclaration)builderY.getFilteredSegments().get(1)).getShardValue());
    }

    SqlBuilder mirror(SqlBuilder sqlBuilderX){
        DasSqlBuilder dasSqlBuilder = BuilderUtils.buildSqlBuilder(sqlBuilderX);
        SqlBuilder sqlBuilderY = BuilderUtils.fromSqlBuilder(dasSqlBuilder);
        return sqlBuilderY;
    }

    void assertBuilder(SqlBuilder sqlBuilderX) {
        SqlBuilder sqlBuilderY = mirror(sqlBuilderX);
        Assert.assertEquals(sqlBuilderY.build(ctx), sqlBuilderX.build(ctx));
    }

    void assertSegments(SqlBuilder sqlBuilderX) {
        SqlBuilder sqlBuilderY = mirror(sqlBuilderX);

        List<Segment> segmentsX = sqlBuilderX.getFilteredSegments();
        List<Segment> segmentsY = sqlBuilderY.getFilteredSegments();

        Assert.assertEquals(segmentsY.size(), segmentsY.size());
        for(int i = 0; i < segmentsX.size(); i++){
            Assert.assertEquals(segmentsY.get(i).toString(), segmentsX.get(i).toString());
        }
        Assert.assertEquals(segmentsY.toString(), segmentsX.toString());
    }

    @Test
    public void testGetFilteredSegments(){
        Person.PersonDefinition p = Person.PERSON;
        SqlBuilder builder1 = SqlBuilder.select(p.Name.as("TN"), p.CityID.as("TP"))
                .set(p.CityID.eq(7))
                .append(INSERT, INTO, p)
                .from(p)
                .innerJoin(p)
                .where()
                .allOf(p.CityID.eq(2), p.Name.eq("NQ"))
                .and(p.Name.greaterThan("W"))
                .or(p.ProvinceID.lessThan(9))
                .and(p.CountryID.between(3, 10))
                .and(p.ProvinceID.greaterThanOrEqual(19))
                .and(p.ProvinceID.lessThanOrEqual(11))
                .and(p.Name.like("OE"))
                .and(p.ProvinceID.gt(24))
                .and(p.Name.isNull())
                .and(p.Name.isNotNull())
                .and(p.Name.eq("CX").nullable())
                .and(p.ProvinceID.in(1, 3, 5))
                .groupBy(p.DataChange_LastTime, p.ProvinceID)
                .having(p.Name, p.CityID)
                .orderBy(p.PeopleID.asc(), p.CountryID.desc())
                .limit(2, 4)
                .offset(3, 7)
                .top(12)
                .appendTemplate("ABC ?", Parameter.varcharOf("", "PL"));
        assertSegments(builder1);

    }

    @Test
    public void testTemplateParameters(){
        SqlBuilder builder1 = new SqlBuilder().appendTemplate(
                "ABC ? XYZ ?",
                Parameter.varcharOf("", "PL"),
                Parameter.integerOf("", 35));
        assertBuildParameters(builder1);
    }

    @Test
    public void testConditionParameters(){
        Person.PersonDefinition p = Person.PERSON;
        SqlBuilder builder1 = new SqlBuilder()
                .where(p.Name.eq("DF"), p.CountryID.lessThan(12))
                .or(p.ProvinceID.in(5, 6, 7))
                .and(p.CityID.in(Arrays.asList("A", "P")))
                .and(p.ProvinceID.between(5, 8))
                .having(p.CountryID.lessThan(16))
                .allOf(p.Name.like("SD"), p.PeopleID.in(55, 66));
        assertBuildParameters(builder1);
    }

    @Test
    @Ignore
    public void testDefinitions(){
        Person.PersonDefinition p = Person.PERSON;

        SqlBuilder builder1 = new SqlBuilder().appendTemplate("ABC")
                .offset(
                        new ParameterDefinition(ParameterDirection.Input,  "name1", JDBCType.INTEGER, true),
                        new ParameterDefinition(ParameterDirection.Output, "name2", JDBCType.VARCHAR, false));

        SqlBuilder sqlBuilderY = mirror(builder1);

        List<ParameterDefinition> definitions = sqlBuilderY.buildDefinitions();
        Assert.assertEquals(2, definitions.size());

        ParameterDefinition definition = definitions.get(1);
        Assert.assertEquals("name2", definition.getName());
        Assert.assertEquals(JDBCType.VARCHAR, definition.getType());
        Assert.assertEquals(ParameterDirection.Output, definition.getDirection());
    }

    @Test
    public void testDataTypes() {

        Person.PersonDefinition p = Person.PERSON;
        final Date date = new Date(1);
        final java.sql.Date sqlDate = new java.sql.Date(5);
        final Timestamp timestamp = new Timestamp(10);
        SqlBuilder builderX = new SqlBuilder()
                .where(p.DataChange_LastTime.eq(date),
                       p.DataChange_LastTime.eq(sqlDate),
                       p.DataChange_LastTime.eq(timestamp),
                       p.CountryID.eq(2),
                       p.CountryID.eq(2.5),
                       p.CountryID.eq(2.8f),
                       p.CountryID.eq(10L),
                       p.CountryID.eq(true),
                       p.CountryID.eq(BigInteger.valueOf(11)),
                       p.CountryID.eq(BigDecimal.valueOf(12.3456)),
                       p.Name.like("K"),
                       p.Name.like(null).nullable(),
                       p.Name.in(4, 6, 8),
                       p.Name.in(Arrays.asList(3, 7, 9)))
                       .appendTemplate("?", Parameter.varcharOf("n", "NZ"));

        SqlBuilder sqlBuilderY = mirror(builderX);
        List<Parameter> parametersX = builderX.buildParameters();
        List<Parameter> parametersY = sqlBuilderY.buildParameters();
        Assert.assertEquals(parametersY.size(), parametersY.size());
        for(int i = 0; i < parametersX.size(); i++){
            Assert.assertEquals(parametersX.get(i).toString(), parametersY.get(i).toString());
        }
        Assert.assertEquals(parametersX.toString(), parametersY.toString());
    }

    void assertBuildParameters(SqlBuilder sqlBuilderX) {
        DasSqlBuilder dasSqlBuilder = BuilderUtils.buildSqlBuilder(sqlBuilderX);
        SqlBuilder sqlBuilderY = BuilderUtils.fromSqlBuilder(dasSqlBuilder);

        List<Parameter> parametersX = sqlBuilderX.buildParameters();
        List<Parameter> parametersY = sqlBuilderY.buildParameters();

        Assert.assertEquals(parametersX.size(), parametersY.size());
        for(int i = 0; i < parametersX.size(); i++){
            Assert.assertEquals(parametersX.get(i).toString(), parametersY.get(i).toString());
        }
        Assert.assertEquals(parametersX.toString(), parametersY.toString());
    }

    @Test
    public void testBuildParameterDefinition(){
        SqlBuilder builder = new SqlBuilder();
        Person.PersonDefinition p = Person.PERSON;
        builder.where(p.CityID.equal(var("a")), AND, p.CountryID.greaterThan(var("b")), OR, p.Name.like(var("c")).when(false));
        List<ParameterDefinition> params = builder.buildDefinitions();
        ParameterDefinition d = new ParameterDefinition(ParameterDirection.InputOutput, "h", JDBCType.DATE, true);
        params.add(d);

        List<DasParameterDefinition> l = BuilderUtils.buildParameterDefinition(params);
        assertEquals(3, l.size());
        assertEquals("h", l.get(2).getName());//TODO

        List<ParameterDefinition> pds = BuilderUtils.fromDefinition(l);
        assertEquals(3, pds.size());
        assertEquals("h", pds.get(2).getName());//TODO
    }

    @Test
    public void testBuildParameters(){
        SqlBuilder builder = new SqlBuilder();
        Person.PersonDefinition p = Person.PERSON;
        builder.where(p.CityID.equal(1), AND, p.CountryID.greaterThan(2), OR, p.Name.like(null).nullable());
        List<Parameter> params =  builder.buildParameters();
        Parameter pr = new Parameter("h", JDBCType.VARCHAR, "A");
        pr.setValues(Arrays.asList("B", "C"));
        params.add(pr);

        Date date = new java.sql.Date(1L);
        Parameter pr2 = new Parameter("h", JDBCType.DATE, date);
        pr2.setValues(Arrays.asList(date,  date));
        params.add(pr2);

        List<DasParameter> ps = BuilderUtils.buildParameters(params);
        assertEquals(4, ps.size());
        assertEquals("h", ps.get(2).getName());//TODO

        List<Parameter> ps2 = BuilderUtils.fromParameters(ps);
        assertEquals(4, ps2.size());
        assertEquals("h", ps2.get(2).getName());
        assertEquals(Arrays.asList("B", "C"), ps2.get(2).getValues());
    }

    @Test
    public void testBuildCallBuilder(){
        CallBuilder cb = new CallBuilder("SP_WITH_OUT_PARAM");
        cb.registerInput("v_id", JDBCType.INTEGER, 4);
        cb.registerOutput("count", JDBCType.INTEGER);

        DasCallBuilder dasCallBuilder = BuilderUtils.buildCallBuilder(cb);
        CallBuilder cb2 = BuilderUtils.fromCallBuilder(dasCallBuilder);
        assertEquals(2, cb2.buildParameters().size());
        assertEquals(cb.buildParameters().get(0).getValue(),
                cb2.buildParameters().get(0).getValue());
    }

    @Test
    public void testBuildSqlBuilders(){
        Person.PersonDefinition p = PERSON.as("p");

        SqlBuilder sqlBuilder = select(p.PeopleID).from(p, PERSON).where(p.CityID.equal(PERSON.CityID), AND, p.PeopleID.greaterThan(PERSON.CountryID));
        List<DasSqlBuilder> dasSqlBuilders = BuilderUtils.buildSqlBuilders(Arrays.asList(sqlBuilder));

        SqlBuilder sqlBuilder2 = BuilderUtils.fromSqlBuilder(dasSqlBuilders.get(0));
        assertEquals2("SELECT p.PeopleID FROM person p, person WHERE p.CityID = person.CityID AND p.PeopleID > person.CountryID",
                sqlBuilder.build(testCtx),
                sqlBuilder2.build(testCtx));
    }

    @Test
    public void testBuildBatchCallBuilder() {
        BatchCallBuilder cb = new BatchCallBuilder("SP_WITH_OUT_PARAM");
        cb.registerInput("v_id", JDBCType.INTEGER);
        cb.registerOutput("count", JDBCType.INTEGER);
        cb.addBatch(1);
        cb.addBatch(2);
        cb.addBatch(3);

        DasBatchCallBuilder dasBatchCallBuilder = BuilderUtils.buildBatchCallBuilder(cb);
        BatchCallBuilder cb2 = BuilderUtils.fromBatchCallBuilder(dasBatchCallBuilder);
        assertArrayEquals(cb.getValuesList().get(0), cb2.getValuesList().get(0));
        assertEquals(cb.buildDefinitions().get(0).getType(), cb2.buildDefinitions().get(0).getType());
        assertEquals(cb.buildDefinitions().get(0).getName(), cb2.buildDefinitions().get(0).getName());
        assertEquals(cb.buildDefinitions().get(0).isInValues(), cb2.buildDefinitions().get(0).isInValues());
        assertEquals(cb.buildDefinitions().get(0).getDirection(), cb2.buildDefinitions().get(0).getDirection());
    }

    private BuilderContext ctx = new DefaultBuilderContext();
    private BuilderContext testCtx = new CtripBuilderContextTest("person");

    static public void assertEquals2(Object expected, Object actual1, Object actual2) {
        assertEquals(null, expected, actual1);
        assertEquals(null, expected, actual2);
    }

    BuilderContext shardedBC = new BuilderContext() {
        @Override
        public String wrapName(String name) {
            return name;
        }

        @Override
        public String locateTableName(Table table) {
            return table.getName() + "_" + table.getShardId();
        }

        @Override
        public String locateTableName(TableDefinition definition) {
            return definition.getShardId() == null ? definition.getName() : definition.getName() + "_" + definition.getShardId();
        }

        @Override
        public String declareTableName(String name) {
            return name;
        }

        @Override
        public String getPageTemplate() {
            return Page.EMPTY;
        }
    };

    static class CtripBuilderContextTest extends DasBuilderContext {

        public CtripBuilderContextTest(String logicDbName) {
            super("das-test", logicDbName);
        }

        public CtripBuilderContextTest(String logicDbName, Hints ctripHints, List<Parameter> parameters) {
            super("das-test", logicDbName, ctripHints, parameters);
        }

        public CtripBuilderContextTest(String logicDbName, Hints ctripHints, List<Parameter> parameters, SqlBuilder builder) {
            super("das-test", logicDbName, ctripHints, parameters, builder);
        }

        @Override
        public String locateTableName(TableDefinition definition) {
            return definition.getName();
        }

        @Override
        public String locateTableName(Table table) {
            return table.getName();
        }

        @Override
        public String wrapName(String name) {
            return name;
        }

        @Override
        public String declareTableName(String name) {
            return name;
        }

        @Override
        public String locate(String rawTableName, String tableShardId, Object tableShardValue) {
            if(tableShardId == null){
                return rawTableName;
            } else {
                return rawTableName + "_" + tableShardId;
            }
            // return rawTableName;// + tableShardId +tableShardValue;
        }
    }
/*
    public static void main(String[] v){
        Object[] ob = new Object[]{"A", 2, "", 4.6f, new Date(), "QQ".getBytes()};
        List<Object[]> values = Lists.<Object[]>newArrayList(ob);
        List<List<ByteBuffer>> b = BuilderUtils.buildValues(values);
        List<Object[]> p = BuilderUtils.fromValues(b);
        String sk = new String((byte[])p.get(0)[5]);
        sk.toUpperCase();

    }*/
}
