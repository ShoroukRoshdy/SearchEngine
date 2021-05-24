/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchengine;

/**
 *
 * @author aliaa
 */
import com.mongodb.*;

/**
 *
 * @author aliaa
 */
public class DataBase {
    static MongoClient mongoClient;
    DB database;
    public DataBase() {    
        MongoClientURI uri = new MongoClientURI(
        "mongodb+srv://SearchEngine:searchengines2021@cluster0.doy0t.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
        mongoClient = new MongoClient(uri);   
        
        database = mongoClient.getDB("myFirstDatabase");

    }
    
    public DBCursor findDocument(String Collection,DBObject query)
    {
        DBCollection collection = database.getCollection("TEST");
        DBCursor cursor = collection.find(query);
        return cursor;
//        query:
        //  DBObject test =      new BasicDBObject("Message", "HI FROM DATABASE")
        //  test.put("Shosho", "HI LOLO") 
//         while(cursor.hasNext()) {
//            System.out.println(cursor.next());
//        }
    }
    
    
    public void insertDocument(String Collection,DBObject query)
    {
        DBCollection collection = database.getCollection("TEST");
        collection.insert(query);
    }
          


    public static MongoClient getMongoClient() {
        return mongoClient;
    }


}
