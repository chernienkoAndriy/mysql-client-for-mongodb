package com.test;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.junit.Test;

public class MongoDBQueryHelperTest {
    MongoClient client = new MongoClient();
    MongoDatabase database = client.getDatabase("users");

    @Test
    public void testFind() {

    }
}
