package com.test;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.test.utils.MongoDBQueryHelper;
import com.test.utils.SQLParseUtils;
import org.bson.Document;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriter;
import org.bson.json.JsonWriterSettings;

import java.io.StringWriter;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("users");
        Map<String, String> options = SQLParseUtils.parseSQL("select adress.asf from user order by age desc, adress.asf desc limit 2,1;");
        String table = options.get("table");
        if (table != null) {
            MongoCollection collection = db.getCollection(table);
            MongoDBQueryHelper.find(options, collection);
        }
        if (args.length > 0) {
            System.out.println(args[0]);
        }
    }

    public static void printJson(Document document) {
        JsonWriter jsonWriter = new JsonWriter(new StringWriter(), new JsonWriterSettings(JsonMode.SHELL, true));
        new DocumentCodec().encode(jsonWriter, document, EncoderContext.builder().isEncodingCollectibleDocument(true).build());
        System.out.println(jsonWriter.getWriter());
        System.out.flush();
    }
}
