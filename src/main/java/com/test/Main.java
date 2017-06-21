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
import java.util.Scanner;

public class Main {


    public static void main(String[] args) {
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("users");
        if (args.length > 0) {
            System.out.println(args[0]);
        }
        readFromConsole(db);
    }

    public static void printJson(Document document) {
        JsonWriter jsonWriter = new JsonWriter(new StringWriter(), new JsonWriterSettings(JsonMode.SHELL, true));
        new DocumentCodec().encode(jsonWriter, document, EncoderContext.builder().isEncodingCollectibleDocument(true).build());
        System.out.println(jsonWriter.getWriter());
        System.out.flush();
    }

    static void readFromConsole(MongoDatabase db) {
        Scanner in = new Scanner(System.in);
        while (true) {
            String string = in.nextLine();
            if(string.equals("exit")){
                break;
            } else {
                Map<String, String> options = SQLParseUtils.parseSQL(string);
                String table = options.get("table");
                if (table != null) {
                    MongoCollection collection = db.getCollection(table);
                    MongoDBQueryHelper helper = new MongoDBQueryHelper();
                    helper.prepareMongoQuery(options, collection);
                }
            }
        }
    }
}
