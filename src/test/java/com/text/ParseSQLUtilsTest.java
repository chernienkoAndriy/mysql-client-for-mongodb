package com.text;

import com.test.utils.SQLParseUtils;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class ParseSQLUtilsTest  {
    @Test
    public void testIsValidSQL() {
        boolean actual = SQLParseUtils.isValidSQL("select * from users;");
        assertTrue(actual);

        actual = SQLParseUtils.isValidSQL("select from users;");
        assertFalse(actual);

        actual = SQLParseUtils.isValidSQL("select id from users where id =3;");
        assertTrue(actual);

        actual = SQLParseUtils.isValidSQL("select group id from users where id =7;");
        assertTrue(actual);
    }

    @Test
    public void testParseQuery() {
        boolean actual = SQLParseUtils.parseSQL(" select * from users where user.id=3;");
        assertTrue(actual);

        actual = SQLParseUtils.parseSQL(" select * fom users;");
        assertFalse(actual);

        actual = SQLParseUtils.parseSQL(" select id from users where users.id = 44;");
        assertTrue(actual);
    }
}
