package com.test.utils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.test.Main;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MongoDBQueryHelper {
    public enum SkipLimitEnum {
        SKIP,
        LIMIT
    }
    public void prepareMongoQuery(Map<String, String> queryOptions, MongoCollection collection) {
        List<Document> list;
        String[] projections = getStrings(queryOptions);
        Bson filter = getQueryFilter(queryOptions);
        Bson sort = getSortDocument(queryOptions);

        int skip = getLimitOrSkipValue(queryOptions, SkipLimitEnum.SKIP);
        int limit = getLimitOrSkipValue(queryOptions, SkipLimitEnum.LIMIT);

        list = getDocuments(collection, projections, filter, sort, skip, limit);
        for (Document doc : list) {
            Main.printJson(doc);
        }
    }

    public int getLimitOrSkipValue(Map<String, String> queryOptions, SkipLimitEnum option){
        int skip = 0;
        int limit = 0;
        if (queryOptions.containsKey("limitClause")) {
            String[] limitClauseArray = queryOptions.get("limitClause").split(",");
            if (limitClauseArray.length == 2) {
                skip = Integer.valueOf(limitClauseArray[0].replaceAll("limit", "").trim());
                limit = Integer.valueOf(limitClauseArray[1].trim());
            } else if (limitClauseArray.length == 1) {
                limitClauseArray = queryOptions.get("limitClause").split("offset");
                if (limitClauseArray.length == 2) {
                    limit = Integer.valueOf(limitClauseArray[0].replaceAll("limit", "").trim());
                    skip = Integer.valueOf(limitClauseArray[1].trim());
                } else {
                    limit = Integer.valueOf(limitClauseArray[0].replaceAll("limit", "").trim());
                }
            }
        }
        return option == SkipLimitEnum.LIMIT ? limit : skip;
    }

    public Bson getQueryFilter(Map<String, String> queryOptions) {
        Bson filter = new Document();
        if (queryOptions.containsKey("whereClause")) {
            filter = getFilterFromClause(queryOptions.get("whereClause"));
        }
        return filter;
    }

    public String[] getStrings(Map<String, String> queryOptions) {
        String[] projections = {};
        if (!queryOptions.get("columns").trim().equals("*")) {
            projections = queryOptions.get("columns").split(" ");
        }
        return projections;
    }

    public Bson getSortDocument(Map<String, String> queryOptions) {
        Bson sort = new Document();
        if (queryOptions.containsKey("orderBy")) {
            String[] orderFields = queryOptions.get("orderBy").trim().split(",");
            List<Bson> orders = new ArrayList<>();
            for (String field : orderFields) {
                if (field.indexOf("asc") > -1) {
                    orders.add(Sorts.ascending(field.replaceAll("asc", "").trim()));
                } else if (field.indexOf("desc") > -1) {
                    orders.add(Sorts.descending(field.replaceAll("desc", "").trim()));
                }
            }
            sort = Sorts.orderBy(orders);
        }
        return sort;
    }

    public List<Document> getDocuments(MongoCollection collection, String[] projections, Bson filter, Bson sort, int skip, int limit) {
        List<Document> list;
        Bson projection = Projections.include(Arrays.asList(projections));
        if (limit > 0) {
            list = (List<Document>) collection.find(filter).projection(projection)
                    .sort(sort)
                    .limit(limit)
                    .skip(skip)
                    .into(new ArrayList<>());
        } else {
            list = (List<Document>) collection.find(filter).projection(projection)
                    .sort(sort)
                    .skip(skip)
                    .into(new ArrayList<>());
        }
        return list;
    }

    public Bson getFilterFromClause(String whereClause) {

        String patternString = "\\((.*\\)\\s)|\\s(\\(.*\\))";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(whereClause);

        List<Bson> groupedFilters = new ArrayList<>();

        while (matcher.find()) {
            Bson groupFilter;
            String groupClause = whereClause.substring(matcher.start(), matcher.end()).replaceAll("[\\(\\)]", "");
            whereClause = whereClause.replace(whereClause.substring(matcher.start(), matcher.end()), String.valueOf(groupedFilters.size()));
            groupFilter = getLogicalFilter(groupClause, groupedFilters);
            if (groupFilter != null) {
                groupedFilters.add(groupFilter);
            }
            matcher = pattern.matcher(whereClause);
        }
        return getLogicalFilter(whereClause, groupedFilters);
    }

    public Bson getLogicalFilter(String logicalClause, List<Bson> filters) {
        logicalClause = logicalClause.replaceAll("[\\(\\)]", "");
        String[] orConditions = logicalClause.split("or");
        if (orConditions.length > 1) {
            List<Bson> orList = new ArrayList<>();
            Bson orFilter;
            for (String condition : orConditions) {
                String[] andConditions = condition.split("and");
                List<Bson> list = new ArrayList<>();
                if (andConditions.length > 1) {
                    for (String andCondition : andConditions) {
                        Bson bson = getMongoFilterFromSqlCondition(filters, orList, andCondition);
                        list.add(bson);
                    }
                    orFilter = Filters.and(list);
                } else {
                    orFilter = getMongoFilterFromSqlCondition(filters, orList, condition);
                }
                if (orFilter != null) {
                    orList.add(orFilter);
                }
            }
            if (orList.size() > 0) {
                return Filters.or(orList);
            }
        } else {
            String[] andConditions = logicalClause.split("and");
            if (andConditions.length > 1) {
                List<Bson> andList = new ArrayList<>();
                for (String s : andConditions) {
                    getMongoFilterFromSqlCondition(filters, andList, s);
                }
                return Filters.and(andList);
            } else {
                return getSimpleFilter(logicalClause);
            }
        }
        return getSimpleFilter(logicalClause);
    }

    public Bson getMongoFilterFromSqlCondition(List<Bson> filters, List<Bson> list, String condition) {
        Bson bson = getSimpleFilter(condition);
        if (bson == null) {
            try {
                int filterIndex = Integer.valueOf(condition);
                list.add(filters.get(filterIndex));
            } catch (Exception e) {
                bson = new Document();
            }
        }
        return bson;
    }

    public Bson getSimpleFilter(String clause) {
        String[] result = clause.split("<=");
        if (result.length > 1) {
            return Filters.lte(result[0].trim(), result[1].trim());
        }
        result = clause.split("<>");
        if (result.length > 1) {
            return Filters.ne(result[0].trim(), result[1].trim());
        }
        result = clause.split(">=");
        if (result.length > 1) {
            return Filters.gte(result[0].trim(), result[1].trim());
        }
        result = clause.split("=");
        if (result.length > 1) {
            return Filters.eq(result[0].trim(), result[1].trim());
        }
        result = clause.split(">");
        if (result.length > 1) {
            return Filters.gt(result[0].trim(), result[1].trim());
        }
        result = clause.split("<");
        if (result.length > 1) {
            return Filters.lt(result[0].trim(), result[1].trim());
        }
        return null;
    }
}
