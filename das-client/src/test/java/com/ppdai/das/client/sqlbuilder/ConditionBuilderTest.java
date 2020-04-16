package com.ppdai.das.client.sqlbuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static com.ppdai.das.client.SegmentConstants.*;

import java.sql.SQLException;

import org.junit.Test;

import com.ppdai.das.client.Person;
import com.ppdai.das.client.Person.PersonDefinition;
import com.ppdai.das.client.SegmentConstants;
import com.ppdai.das.client.SqlBuilder;
import com.ppdai.das.strategy.ColumnCondition;
import com.ppdai.das.strategy.ConditionList;

public class ConditionBuilderTest {
    private static PersonDefinition p = Person.PERSON;
    
//    @Test
//    public void testIllegalArgument() {
//        SqlBuilder builder = new SqlBuilder();
//        builder.values(p.Name.of("Jerry"), p.CountryID.of(1));
//        try {
//            builder.buildUpdateConditions();
//            fail();
//        } catch (IllegalArgumentException e) {
//        }
//    }
    
    @Test
    public void testInsertValues() throws SQLException {
        SqlBuilder builder = SqlBuilder.insertInto(p, p.Name, p.CountryID);
        builder.values(p.Name.of("Jerry"), p.CountryID.of(1));
        ConditionList cl = builder.buildUpdateConditions();
        assertTrue(cl.isIntersected());
        assertEquals(2, cl.size());
    }

    @Test
    public void testInsertBySelect() throws SQLException {
        SqlBuilder builder = SqlBuilder.insertInto(p);
        builder.append(SqlBuilder.select(p.Name).from(p).where().allOf(p.Name.eq(1), p.CountryID.gt(2)));
        ConditionList cl = builder.buildUpdateConditions();
        assertTrue(cl.isIntersected());
        assertEquals(2, cl.size());
    }
    
    @Test
    public void testInsertWithColumnListBySelect() throws SQLException {
        SqlBuilder builder = SqlBuilder.insertInto(p, p.Name, p.CountryID);
        builder.append(SqlBuilder.select(p.Name).from(p).where().allOf(p.Name.eq(1), p.CountryID.gt(2)));
        ConditionList cl = builder.buildUpdateConditions();
        assertTrue(cl.isIntersected());
        assertEquals(2, cl.size());
    }
    
    @Test
    public void testInsertWithColumnListBySelectWhereBracket() throws SQLException {
        SqlBuilder builder = SqlBuilder.insertInto(p, p.Name, p.CountryID);
        builder.append(SqlBuilder.select(p.Name).from(p).where().leftBracket().allOf(p.Name.eq(1), p.CountryID.gt(2)).rightBracket());
        ConditionList cl = builder.buildUpdateConditions();
        assertTrue(cl.isIntersected());
        assertEquals(2, cl.size());
    }

    @Test
    public void testUpdate() throws SQLException {
        SqlBuilder builder = SqlBuilder.update(p).set(p.Name.eq("Jerry"), p.CountryID.eq(1));
        ConditionList cl = builder.buildUpdateConditions();
        assertTrue(cl.isIntersected());
        assertEquals(2, cl.size());
    }

    @Test
    public void testUpdateWhere() throws SQLException {
        SqlBuilder builder = SqlBuilder.update(p).set(p.Name.eq("Jerry"), p.CountryID.eq(1)).
                where().leftBracket().allOf(p.Name.eq(1), p.CountryID.gt(2)).rightBracket();
        ConditionList cl = builder.buildUpdateConditions();
        assertTrue(cl.isIntersected());
        assertEquals(4, cl.size());
    }

    @Test
    public void testDelete() throws SQLException {
        SqlBuilder builder = SqlBuilder.deleteFrom(p);
        ConditionList cl = builder.buildUpdateConditions();
        assertTrue(cl.isIntersected());
        assertEquals(0, cl.size());
    }

    @Test
    public void testDeleteWhere() throws SQLException {
        SqlBuilder builder = SqlBuilder.deleteFrom(p).where().leftBracket().allOf(p.Name.eq(1), p.CountryID.gt(2)).rightBracket();
        ConditionList cl = builder.buildUpdateConditions();
        assertTrue(cl.isIntersected());
        assertEquals(2, cl.size());
    }

    @Test
    public void testSelectWhere() throws SQLException {
        SqlBuilder builder = SqlBuilder.selectAllFrom(p).where(p.CityID.eq(1), OR, p.CountryID.eq(1), AND, p.Name.like("A"), OR, p.PeopleID.eq(1));
        ConditionList cl = builder.buildQueryConditions();
        cl = (ConditionList)cl.get(0);//get first
        
        assertFalse(cl.isIntersected());
        assertEquals(3, cl.size());
        
        cl = (ConditionList)cl.get(1);
        assertTrue(cl.isIntersected());
        assertEquals(2, cl.size());
    }

    @Test
    public void testSelectWhereIncludeAll() throws SQLException {
        SqlBuilder builder = SqlBuilder.selectAllFrom(p).where().includeAll().and().append(p.CityID.eq(1), OR, p.CountryID.eq(1), AND, p.Name.like("A"), OR, p.PeopleID.eq(1));
        ConditionList cl = builder.buildQueryConditions();
        cl = (ConditionList)cl.get(0);//get first

        assertFalse(cl.isIntersected());
        assertEquals(3, cl.size());

        ConditionList cl0;
        cl0 = (ConditionList)cl.get(0);
        assertTrue(cl0.isIntersected());
        assertEquals(2, cl0.size());

        cl0 = (ConditionList)cl.get(1);
        assertTrue(cl0.isIntersected());
        assertEquals(2, cl0.size());
    }

    @Test
    public void testSelectWhereExcludeAll() throws SQLException {
        SqlBuilder builder = SqlBuilder.selectAllFrom(p).where().excludeAll().and().append(p.CityID.eq(1), OR, p.CountryID.eq(1), AND, p.Name.like("A"), OR, p.PeopleID.eq(1));
        ConditionList cl = builder.buildQueryConditions();
        cl = (ConditionList)cl.get(0);//get first

        assertFalse(cl.isIntersected());
        assertEquals(4, cl.size());

        assertTrue(cl.get(0) instanceof ColumnCondition);

        cl = (ConditionList)cl.get(2);
        assertTrue(cl.isIntersected());
        assertEquals(2, cl.size());
    }

    @Test
    public void testSelectWhereAll() throws SQLException {
        SqlBuilder builder = SqlBuilder.selectAllFrom(p).where().includeAll().and().excludeAll().and(SegmentConstants.TRUE).or(SegmentConstants.FALSE);
        ConditionList cl = builder.buildQueryConditions();
        cl = (ConditionList)cl.get(0);//get first

        assertFalse(cl.isIntersected());
        assertEquals(3, cl.size());

        assertTrue(cl.get(1) instanceof ColumnCondition);
        assertTrue(cl.get(2) instanceof ColumnCondition);

        cl = (ConditionList)cl.get(0);
        assertTrue(cl.isIntersected());
        assertEquals(2, cl.size());
    }

    @Test
    public void testSelectWhereSimple() throws SQLException {
        SqlBuilder builder = SqlBuilder.selectAllFrom(p).where().includeAll().append(SegmentConstants.TRUE).or(SegmentConstants.FALSE);
        ConditionList cl = builder.buildQueryConditions();

        cl = (ConditionList)cl.get(0);//get first

        assertFalse(cl.isIntersected());
        assertEquals(2, cl.size());

        assertTrue(cl.get(1) instanceof ColumnCondition);

        cl = (ConditionList)cl.get(0);
        assertEquals(2, cl.size());
    }

    @Test
    public void testSelectWhereNotSimple() throws SQLException {
        SqlBuilder builder = SqlBuilder.selectAllFrom(p).where().not().includeAll().and().append(SegmentConstants.TRUE);
        ConditionList cl = builder.buildQueryConditions();

        assertTrue(cl.isIntersected());
        assertEquals(2, cl.size());

        assertTrue(cl.get(0) instanceof ColumnCondition);
    }

    @Test
    public void testSelectWhereNotOr() throws SQLException {
        SqlBuilder builder = SqlBuilder.selectAllFrom(p).where().not().includeAll().and().not().excludeAll().and(SegmentConstants.TRUE).or(SegmentConstants.FALSE);
        ConditionList cl = builder.buildQueryConditions();
        cl = (ConditionList)cl.get(0);//get first

        assertFalse(cl.isIntersected());
        assertEquals(3, cl.size());

        assertTrue(cl.get(1) instanceof ColumnCondition);
        assertTrue(cl.get(2) instanceof ColumnCondition);

        cl = (ConditionList)cl.get(0);
        assertTrue(cl.isIntersected());
        assertEquals(2, cl.size());
    }

    @Test
    public void testSelectWhereNotAnd() throws SQLException {
        SqlBuilder builder = SqlBuilder.selectAllFrom(p).where().not().excludeAll().and().not().includeAll().and(SegmentConstants.TRUE).and(SegmentConstants.FALSE);
        ConditionList cl = builder.buildQueryConditions();
        cl = (ConditionList)cl.get(0);//get first

        assertFalse(cl.isIntersected());
        assertEquals(2, cl.size());

        assertTrue(cl.get(0) instanceof ColumnCondition);

        cl = (ConditionList)cl.get(1);
        assertTrue(cl.isIntersected());
        assertEquals(3, cl.size());
        assertTrue(cl.get(0) instanceof ColumnCondition);
        assertTrue(cl.get(1) instanceof ColumnCondition);
        assertTrue(cl.get(2) instanceof ColumnCondition);
    }

    @Test
    public void testSelectWhereNotWellFormated() throws SQLException {
        SqlBuilder builder = SqlBuilder.selectAllFrom(p).where().not();
        try {
            builder.buildQueryConditions();
            fail();
        } catch (IllegalArgumentException e) {
        }

        builder = SqlBuilder.selectAllFrom(p).where().append(SegmentConstants.TRUE, SegmentConstants.AND);
        try {
            builder.buildQueryConditions();
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
