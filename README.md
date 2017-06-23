# mysqlclientformongodb

This is CLI that use mysql syntax to work with mongoDb.

You need to have gradle installed on your machine.

You need download it using command ```git clone https://github.com/chernienkoAndriy/mysqlclientformongodb```

After project is loaded , you need navigate into root directory and run "gradle build"

When gradle finished, result .jar file is in build/libs directory.

To run it you need specify in terminal ```java -jar mysql-mongo-0.1.0.jar dbUrl=<dbUrl> dbName=<dbName>```

dbUrl - is url to your mongoDb, it not specified app will try to connect to localhost:27017.

dbName - is name of database to work with. If not specified, app will work with 'test' db.
