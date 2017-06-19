package com.test;

import com.mongodb.MongoClient;

public class Main {
    public static void main(String[] args) {
        MongoClient client = new MongoClient();
        if(args.length > 0) {
            System.out.println(args[0]);
        }
        System.out.println("hello world");
    }
}
