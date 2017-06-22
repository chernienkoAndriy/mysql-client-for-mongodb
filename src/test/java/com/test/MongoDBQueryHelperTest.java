package com.test;

import com.mongodb.client.model.Filters;
import com.test.utils.MongoDBQueryHelper;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class MongoDBQueryHelperTest {
    MongoDBQueryHelper helper = new MongoDBQueryHelper();

    @Test
    public void testGetSimpleFilter() {
        Bson actual = helper.getSimpleFilter("a=3");
        assertNotNull(actual);

        actual = helper.getSimpleFilter("a <= 3");
        assertNotNull(actual);

        actual = helper.getSimpleFilter("a >= 3");
        assertNotNull(actual);

        actual = helper.getSimpleFilter("a < 3");
        assertNotNull(actual);

        actual = helper.getSimpleFilter("a > 3");
        assertNotNull(actual);

        actual = helper.getSimpleFilter("a <> 3");
        assertNotNull(actual);

        actual = helper.getSimpleFilter("asdfasdf");
        assertNull(actual);

    }

    @Test
    public void testGetMongoFilterFromSqlCondition() {
        List<Bson> list = new ArrayList<>();

        Bson actual = helper.getLogicalFilter("a=3", list);
        assertNotNull(actual);

        actual = helper.getLogicalFilter("a=3 and b>3", list);
        assertNotNull(actual);

        actual = helper.getLogicalFilter("a=3 or b<>3", list);
        assertNotNull(actual);

        actual = helper.getLogicalFilter("a=3 or a<>3 and b=1", list);
        assertNotNull(actual);

        actual = helper.getLogicalFilter("a=3 or a<>3 and (b=1 and c>3)", list);
        assertNotNull(actual);

        actual = helper.getLogicalFilter("asdfasdf", list);
        assertNull(actual);

    }

    @Test
    public void testGetFilterFromClause() {
        Bson actual = helper.getFilterFromClause("a=3 or a<>3 and (b=1 and c>3)");
        assertNotEquals(actual, new Document());

        actual = helper.getFilterFromClause("asdfasdf");
        assertNull(actual);
    }

    @Test
    public void testGetSortDocument() {
        Map<String, String> options = new HashMap<>();

        Bson actual = helper.getSortDocument(options);
        assertEquals(actual, new Document());

        options.put("orderBy", "name");
        actual = helper.getSortDocument(options);
        assertNotEquals(actual, new Document());

        options.put("orderBy", "name asc");
        actual = helper.getSortDocument(options);
        assertNotEquals(actual, new Document());

        options.put("orderBy", "name desc");
        actual = helper.getSortDocument(options);
        assertNotEquals(actual, new Document());
    }

    @Test
    public void testGetFields() {
        Map<String, String> options = new HashMap<>();
        options.put("columns", "*");
        String[] fields = helper.getFields(options);
        assertArrayEquals(fields, new String[]{});

        options.put("columns", "name");
        fields = helper.getFields(options);
        assertArrayEquals(fields, new String[]{"name"});

        options.put("columns", "name email");
        fields = helper.getFields(options);
        assertArrayEquals(fields, new String[]{"name", "email"});
    }

    @Test
    public void testGetLimitOrSkipValue() {
        Map<String, String> options = new HashMap<>();
        int actualSkip = helper.getLimitOrSkipValue(options, MongoDBQueryHelper.SkipLimitEnum.SKIP);
        int actualLimit = helper.getLimitOrSkipValue(options, MongoDBQueryHelper.SkipLimitEnum.LIMIT);
        assertEquals(actualSkip, 0);
        assertEquals(actualLimit, 0);

        options.put("limitClause","limit 10,20");
        actualSkip = helper.getLimitOrSkipValue(options, MongoDBQueryHelper.SkipLimitEnum.SKIP);
        actualLimit = helper.getLimitOrSkipValue(options, MongoDBQueryHelper.SkipLimitEnum.LIMIT);
        assertEquals(actualSkip, 10);
        assertEquals(actualLimit, 20);

        options.put("limitClause","limit 10");
        actualSkip = helper.getLimitOrSkipValue(options, MongoDBQueryHelper.SkipLimitEnum.SKIP);
        actualLimit = helper.getLimitOrSkipValue(options, MongoDBQueryHelper.SkipLimitEnum.LIMIT);
        assertEquals(actualSkip, 0);
        assertEquals(actualLimit, 10);

        options.put("limitClause","limit 10 offset 20");
        actualSkip = helper.getLimitOrSkipValue(options, MongoDBQueryHelper.SkipLimitEnum.SKIP);
        actualLimit = helper.getLimitOrSkipValue(options, MongoDBQueryHelper.SkipLimitEnum.LIMIT);
        assertEquals(actualSkip, 20);
        assertEquals(actualLimit, 10);

    }

}
