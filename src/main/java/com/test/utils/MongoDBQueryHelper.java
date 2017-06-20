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
    public static void find(Map<String, String> queryOptions, MongoCollection collection) {
        List<Document> list;
        String[] projections = {};
        if (!queryOptions.get("columns").trim().equals("*")) {
            projections = queryOptions.get("columns").split(" ");
        }
        Bson filter = new Document();
        if (queryOptions.containsKey("whereClause")) {
            filter = getFilterFromClause(queryOptions.get("whereClause"));
        }
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
            System.out.println(queryOptions.get("limitClause"));
        }

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
        for (Document doc : list) {
            Main.printJson(doc);
        }
    }

    private static Bson getFilterFromClause(String whereClause) {

        String patternString = "\\((.*\\)\\s)|\\s(\\(.*\\))";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(whereClause);

        List<Bson> groupedFilters = new ArrayList<>();

        while (matcher.find()) {
            Bson groupFilter = null;

            String groupClause = whereClause.substring(matcher.start(), matcher.end()).replaceAll("[\\(\\)]", "");
            whereClause = whereClause.replace(whereClause.substring(matcher.start(), matcher.end()), String.valueOf(groupedFilters.size()));
            System.out.println(groupClause);

            groupFilter = getLogicalFilter(groupClause, groupedFilters);

            if (groupFilter != null) {
                groupedFilters.add(groupFilter);
            }
            matcher = pattern.matcher(whereClause);
        }

        return getLogicalFilter(whereClause, groupedFilters);
    }

    private static Bson getLogicalFilter(String logicalClause, List<Bson> filters) {
        logicalClause = logicalClause.replaceAll("[\\(\\)]", "");
        String[] orLogical = logicalClause.split("or");
        if (orLogical.length > 1) {
            List<Bson> orList = new ArrayList<>();
            Bson orFilter = null;
            for (String condition : orLogical) {
                String[] andLogical = condition.split("and");
                List<Bson> list = new ArrayList<>();
                if (andLogical.length > 1) {
                    for (String andCondition : andLogical) {
                        Bson bson = getSimpleFilter(andCondition);
                        if (bson == null) {
                            try {
                                int filterIndex = Integer.valueOf(andCondition);
                                orList.add(filters.get(filterIndex));
                            } catch (Exception e) {
                                bson = new Document();
                            }
                        }
                        list.add(bson);
                    }
                    orFilter = Filters.and(list);
                } else {
                    Bson bson = getSimpleFilter(condition);
                    if (bson == null) {
                        try {
                            int filterIndex = Integer.valueOf(condition);
                            orList.add(filters.get(filterIndex));
                        } catch (Exception e) {
                            bson = new Document();
                        }
                    }
                    orFilter = bson;
                }
                if (orFilter != null) {
                    orList.add(orFilter);
                }
            }
            if (orList.size() > 0) {
                return Filters.or(orList);
            }
        } else {
            String[] andLogical = logicalClause.split("and");
            if (andLogical.length > 1) {
                List<Bson> andList = new ArrayList<>();
                for (String s : andLogical) {
                    Bson bson = getSimpleFilter(s);
                    if (bson == null) {
                        try {
                            int filterIndex = Integer.valueOf(s);
                            bson = filters.get(filterIndex);
                        } catch (Exception e) {
                            bson = new Document();
                        }
                    }
                    andList.add(bson);
                }
                return Filters.and(andList);
            } else {
                return getSimpleFilter(logicalClause);
            }
        }
        return getSimpleFilter(logicalClause);
    }

    private static Bson getSimpleFilter(String clause) {
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
