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
    
    
    public static void LoadDoc(ArrayList<Document> docs )
    {
//        load visited fields only !
        Document doc;
        try {
            DBCursor cursor = db.getDatabase().getCollection("Seeds").find(new BasicDBObject("Visited", true));
            List<DBObject> DBARRAY = cursor.toArray();
            
            for(int i =0 ; i  <DBARRAY.size();i++)
            {
                doc = Jsoup.connect(DBARRAY.get(i).get("URL").toString()).get();
                docs.add(doc);
            }  
            System.out.println("Start Loading " + docs.size());

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SearchEngine.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
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
        
        ArrayList<Document> document2 = new ArrayList<Document>();
        LoadDoc(document2);
        
        System.out.println(" Number of Document 2 " + document2.size());

//       Pass Documents and ThreadNumbers

        indexer.setThreadsNumber(numberOfThreads);
        
        indexer.setDocuments(document2);
        
//      Run Indexer
        indexer.toRun();
    }
    
    
    static Logger root = (Logger) LoggerFactory
            .getLogger(Logger.ROOT_LOGGER_NAME);

    static {
        root.setLevel(Level.INFO);
    }
    
}
