package com.test.utils;

import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.TBaseType;
import gudusoft.gsqlparser.TCustomSqlStatement;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.nodes.*;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;

public class SQLParseUtils {
    public static final String SQL_SELECT = "SELECT";
    public static final String SQL_FROM = "FROM";
    public static final String SQL_WHERE = "WHERE";
    public static final String SQL_GROUP_BY = "GROUP BY";
    public static final String SQL_ORDER_BY = "ORDER_BY";
    public static final String SQL_SKIP = "SKIP";
    public static final String SQL_LIMIT = "LIMIT";
    private static TGSqlParser parser = new TGSqlParser(EDbVendor.dbvmysql);

    public static Boolean parseSQL(String query) {
        parser.sqltext = query;
        int ret = parser.parse();
        if (ret == 0) {
            for (int i = 0; i < parser.sqlstatements.size(); i++) {
                TSelectSqlStatement statement = (TSelectSqlStatement) parser.sqlstatements.get(i);
                TResultColumnList resultColumnList = statement.getResultColumnList();
                for (int j = 0; j < resultColumnList.size(); j++) {
                    TResultColumn resultColumn = resultColumnList.getResultColumn(i);
                    System.out.println(resultColumn.getExpr().toString());
                }

                if (statement.getWhereClause() != null) {
                    System.out.printf("\nwhere clause: \n\t%s\n", statement.getWhereClause().getCondition().toString());
                }

                if (statement.getGroupByClause() != null) {
                    System.out.printf("\ngroup by: \n\t%s\n", statement.getGroupByClause().toString());
                }

                if (statement.getOrderbyClause() != null) {
                    System.out.printf("\norder by:");
                    for (int j = 0; j < statement.getOrderbyClause().getItems().size(); j++) {
                        System.out.printf("\n\t%s", statement.getOrderbyClause().getItems().getOrderByItem(j).toString());
                    }
                }

                if (statement.getLimitClause() != null) {
                    System.out.printf("top clause: \n%s\n", statement.getLimitClause().toString());
                }
            }
        } else {
            System.out.println(parser.getErrormessage());
            return false;
        }


        query = query.trim();
        if (query.toUpperCase().indexOf(SQL_SELECT) != 0) {
            return false;
        }
        int indexFrom = query.toUpperCase().indexOf(SQL_FROM);
        if (indexFrom < 0) {
            return false;
        }
//        String[] projections = query.substring(SQL_SELECT.length(), indexFrom).trim().split(",");
//        System.out.println(projections[0]);
//        int indexWhere = query.toUpperCase().indexOf(SQL_WHERE);
//        String condition = "";
//        if (indexWhere > 0) {
//            condition = query.substring(indexWhere + SQL_WHERE.length());
//        }
//        System.out.println(condition);
        return true;
    }

    public static Boolean isValidSQL(String query) {
        parser.sqltext = query;
        int ret = parser.parse();
        return ret == 0;
    }

}