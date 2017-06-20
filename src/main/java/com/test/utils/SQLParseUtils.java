package com.test.utils;

import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.nodes.*;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SQLParseUtils {
    private static TGSqlParser parser = new TGSqlParser(EDbVendor.dbvmysql);

    public static Map<String, String> parseSQL(String query) {
        Map<String, String> result = new HashMap<>();
        parser.sqltext = query;
        int ret = parser.parse();
        result.put("table", "");
        if (ret == 0) {
            for (int i = 0; i < parser.sqlstatements.size(); i++) {
                TSelectSqlStatement statement = (TSelectSqlStatement) parser.sqlstatements.get(i);
                TResultColumnList resultColumnList = statement.getResultColumnList();
                System.out.println(statement.getJoins().toString());
                result.put("table", statement.getTables().getTable(0).getFullName());
                result.put("columns", "");
                for (int j = 0; j < resultColumnList.size(); j++) {
                    TResultColumn resultColumn = resultColumnList.getResultColumn(j);
                    String columns = result.get("columns");
                    result.put("columns", columns + " " + resultColumn.getExpr().toString());
                    System.out.println(resultColumn.getExpr().toString());
                }

                if (statement.getWhereClause() != null) {
                    result.put("whereClause", statement.getWhereClause().getCondition().toString());
                    System.out.printf("\nwhere clause: \n\t%s\n", statement.getWhereClause().getCondition().toString());
                }

                if (statement.getGroupByClause() != null) {
                    System.out.printf("\ngroup by: \n\t%s\n", statement.getGroupByClause().toString());
                    result.put("groupBy", statement.getGroupByClause().toString());
                }

                if (statement.getOrderbyClause() != null) {
                    System.out.printf("\norder by:");
                    result.put("orderBy", "");
                    String stringSeparator = ",";
                    for (int j = 0; j < statement.getOrderbyClause().getItems().size(); j++) {
                        System.out.printf("\n\t%s", statement.getOrderbyClause().getItems().getOrderByItem(j).toString());
                        String orderBy = result.get("orderBy");
                        if ((statement.getOrderbyClause().getItems().size() - 1) == j) {
                            stringSeparator = "";
                        }
                        result.put("orderBy", orderBy + " " + statement.getOrderbyClause().getItems().getOrderByItem(j).toString() + stringSeparator);
                    }
                }

                if (statement.getTopClause() != null) {
                    result.put("topClause", statement.getTopClause().toString());
                    System.out.printf("top clause: \n%s\n", statement.getTopClause().toString());
                }

                if (statement.getLimitClause() != null) {
                    result.put("limitClause", statement.getLimitClause().toString());
                    System.out.printf("top clause: \n%s\n", statement.getLimitClause().toString());
                }
            }
        } else {
            System.out.println(parser.getErrormessage());
            return Collections.emptyMap();
        }

        return result;
    }

    public static Boolean isValidSQL(String query) {
        parser.sqltext = query;
        int ret = parser.parse();
        return ret == 0;
    }

}