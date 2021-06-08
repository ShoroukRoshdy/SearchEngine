/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchengine;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import static searchengine.Indexer.documents;

/**
 *
 * @author aliaa
 */
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;



public class SearchEngine {
    
    static Indexer indexer;
    static DataBase db;
    static Crawler crawler;
    
    
    public static ArrayList<Document> LoadDoc()
    {
//        load visited fields only !
        ArrayList<Document> documents = new ArrayList<Document>();
        
        DBCursor cursor = db.getDatabase().getCollection("Seeds").find(new BasicDBObject("Visited", true).append("$exists", new BasicDBObject("Document", true)));
        List<DBObject> DBARRAY = cursor.toArray();
        for(int i =0 ; i  <DBARRAY.size();i++)
        {
            Document doc = Jsoup.parse(DBARRAY.get(i).get("Document").toString(),DBARRAY.get(i).get("URL").toString());
            documents.add(doc);
        }    
        System.out.println(" Number of Document " + documents.size());

        return documents;
    }
    
    public static void main(String[] args) throws IOException, InterruptedException
    {
        Scanner scanner = new Scanner(System.in);
        db = new DataBase();
        System.out.println("Enter Number of Threads : ");
        int numberOfThreads =  scanner.nextInt();
        indexer = new Indexer();
        crawler = new Crawler(numberOfThreads,db);
//        Run Crawler
        crawler.toRun(db,numberOfThreads);
        
        ArrayList<Document> documents  = LoadDoc();
//       Pass Documents and ThreadNumbers
        indexer.setThreadsNumber(numberOfThreads);
        indexer.setDocuments(documents);
        
//      Run Indexer
        indexer.toRun();
    }
    
    
    static Logger root = (Logger) LoggerFactory
            .getLogger(Logger.ROOT_LOGGER_NAME);

    static {
        root.setLevel(Level.INFO);
    }
    
}
