package com.text;

import com.test.utils.SQLParseUtils;
import org.junit.Test;

import java.util.Map;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class ParseSQLUtilsTest  {
    @Test
    public void testIsValidSQL() {
        boolean actual = SQLParseUtils.isValidSQL("select * from users;");
        assertTrue(actual);

        actual = SQLParseUtils.isValidSQL("select from users;");
        assertFalse(actual);

        actual = SQLParseUtils.isValidSQL("select id, email from users where id =3;");
        assertTrue(actual);

        actual = SQLParseUtils.isValidSQL("select id, email from users where asdfasdf and 2;");
        assertTrue(actual);

        actual = SQLParseUtils.isValidSQL("select group id from users where id =7;");
        assertTrue(actual);
    }

    @Test
    public void testParseQuery() {
        Map result;
        result = SQLParseUtils.parseSQL(" select id, email from users where user.id=3;");
        assertFalse(result.isEmpty());

        result = SQLParseUtils.parseSQL(" select * fom users;");
        assertTrue(result.isEmpty());

        result = SQLParseUtils.parseSQL("select id, email from users where users.id = 44;");
        assertFalse(result.isEmpty());

        result = SQLParseUtils.parseSQL("select * from users limit 0, 50");
        assertFalse(result.isEmpty());

        result = SQLParseUtils.parseSQL("select * from users limit 0 offset 50");
        assertTrue(result.containsKey("limitClause"));
        assertFalse(result.isEmpty());
    }
}
