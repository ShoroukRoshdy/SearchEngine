package searchengine;

import com.mongodb.*;
import java.io.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger; 
import java.util.regex.*;  
import org.jsoup.Connection.Response;

//MONGO



public class Crawler implements Runnable {
 
//////////////////////////////// Data Members ////////////////////////////////////////////

    static int ThreadsNumber;   
    static final Object robotLock = new Object();
    private DataBase database;
    static final Object visitedLock = new Object();
    
//    /////////////////////////////// Getters ////////////////////////////////////////////
    public static int getThreadsNumber() {
        return ThreadsNumber;
    }
    
 
    public Crawler(int num,DataBase db)  throws IOException
    {
        ThreadsNumber = num;
        database = db;
       
    }
    
    /////////////////////////////////////////////////  crawl Function ////////////////////////////////////////////////////////////////
    
    public void crawl() throws MalformedURLException 
    {
        int visited=0;
        DBObject url;
        do
        {  
            synchronized(database)
            {
                if (database.getDatabase().getCollection("Seeds").find(new BasicDBObject("Assigned", true)).count() >= 2)
                    break;
                
                url =database.getDatabase().getCollection("Seeds").findOne(new BasicDBObject("Assigned", false));
                while (url == null)
                {     
                    System.out.println("Tthread " + Thread.currentThread().getName() + " is Sleeping" );     

                    try {
                            database.wait();
                    } catch (InterruptedException ex) {
                    }
                    url =database.getDatabase().getCollection("Seeds").findOne(new BasicDBObject("Assigned", false));

                }             
                BasicDBObject assignedField = new BasicDBObject().append("$set", new BasicDBObject().append("Assigned", true));
                database.getCollection("Seeds").update(new BasicDBObject().append("URL", url.get("URL")), assignedField);
            }
            
            System.out.println("I'm thread " + Thread.currentThread().getName() + " , Working on " +url.get("URL") );     
  
//               Fetch the url
            Document document = null;
            try {
                document = Jsoup.connect(url.get("URL").toString()).timeout(500000).get();
            } catch (IOException ex) {
                Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
            }
//            Save document in database
            BasicDBObject Field = new BasicDBObject().append("$set", new BasicDBObject().append("Document", document.toString()));
            database.getCollection("Seeds").update(new BasicDBObject().append("URL", url.get("URL")), Field);
            
           
            // parse the HTML document to extract links to other URLs
            Elements page_Links = document.select("a");
                 
//                Check Robot.txt
            HashSet<String> Disallow = new HashSet<String>();
            HashSet<String> Allow = new HashSet<String>();
            String host=getHostName(url.get("URL").toString());
            DBObject robot =null;
            synchronized(robotLock){
                robot =database.getDatabase().getCollection("Robot").findOne(new BasicDBObject("Host", host));
            
                if (robot== null)
                {
                    try {
                        getAllowDisallow(host,Disallow,Allow);
                    } catch (IOException ex) {
                        System.out.println("PAGE NOT FOUND");
                    }
    //               Add Allow ,Disallows             
                    DBObject query =  new BasicDBObject("Host",host );
                    query.put("Allow", Allow);                    
                    query.put("Disallow", Disallow);
                    database.insertDocument("Robot",query );
                }  
            }
            if (robot !=null)
            {
                BasicDBList DisallowDB = (BasicDBList)robot.get("Disallow"); 
                for (Object d: DisallowDB)
                    Disallow.add(d.toString());
                BasicDBList AllowDB = (BasicDBList) robot.get("Allow");
                for (Object d: AllowDB)
                     Allow.add(d.toString()); 
            }
//          Loop on all extracted links and push them in the seed_set
            for (Element newurl : page_Links) {
                try {
                    Document doc = Jsoup.connect(url.get("URL").toString()).timeout(500000).get();
                        
                    if (newurl.attr("abs:href") == "" )
                        continue;

                    if ((checkDisallow(newurl.attr("abs:href") ,Disallow) && !checkAllow(newurl.attr("abs:href") ,Allow)))
                    {   
                        System.out.println("Disallowed : " + newurl.attr("abs:href"));
                        continue; 
                    }   
//                  Check if this link already in database
                    synchronized(database)
                    {
                        DBObject newurlDB =database.getDatabase().getCollection("Seeds").findOne(new BasicDBObject("URL", normalize(newurl.attr("abs:href")) ));

                        if (newurlDB == null)
                        {
                            DBObject newURL =  new BasicDBObject("URL",normalize(newurl.attr("abs:href")) );
                            newURL.put("Visited", false); 
                            newURL.put("Assigned", false);
                            database.insertDocument("Seeds",newURL );
                            database.notify();
                        }

                    }
                } catch (IOException ex) {
                    System.out.println("PAGE NOT FOUND");
                }
            } 
                
//          Set Visited Value to True 
            BasicDBObject visitedField = new BasicDBObject().append("$set", new BasicDBObject().append("Visited", true));
            synchronized(database)
            {
                database.getCollection("Seeds").update(new BasicDBObject().append("URL", url.get("URL")), visitedField);
                visited = database.getDatabase().getCollection("Seeds").find(new BasicDBObject("Visited", true)).count();
            }
           
            System.out.println("I'm thread " + Thread.currentThread().getName() + " , Visited " +visited );           


          
        } while (visited<= 2);
        System.out.println("Thread: " + Thread.currentThread().getName() + " Finished" );           

    }
  
    /////////////////////////////////////////////////  Functions Related to Robot.txt ////////////////////////////////////////////////////////////////

    public static String getHostName (String URL) throws MalformedURLException
    {
        URL url = new URL(URL);
        return url.getProtocol().toLowerCase()+"://"+url.getHost().toLowerCase();     
   }
    
    public boolean checkAllow (String url,HashSet<String> Allow)
    {
        for (String A : Allow)
        {
            if(url.matches(A))
                return true;
        }
        return false;
    }
    
    public boolean checkDisallow(String url,HashSet<String> Disallow)
    {
        for (String D : Disallow)
        {
            if(url.matches(D))
                return true;
        }
        return false;
    }
    
    public void getAllowDisallow(String theDomainUrl,HashSet<String> Disallow,HashSet<String> Allow) throws IOException
    {
           
        //geting the url of the robot.txt
        URL urlRobot = new URL(theDomainUrl);
        String theRobotUrl =urlRobot.getProtocol()+"://"+urlRobot.getHost()+"/robots.txt"; 

        try(BufferedReader in = new BufferedReader( new InputStreamReader(new URL(theRobotUrl).openStream())))
        {
            String line = null;
            while((line = in.readLine()) != null) 
            {
                if (line.contains("User-agent: *"))
                {

                    while (true)
                    {
                        line = in.readLine();
                         
                        if (line==null)
                             break;
                        else if( line.contains("User-agent: ") )
                            break;
                        if (line.contains("Disallow"))
                        {
                             line= line.substring(line.indexOf(":")+2,line.length());

                             int i =0;
                            while ( i< line.length())
                            {
                                if ( line.charAt(i) == '*' )
                                {
                                    line=line.substring(0, i) + '.' + line.substring(i,line.length());
                                  
                                    i++;
                                }
                                else if (line.charAt(i) == '.')
                                {
                                    line=line.substring(0 , i) + '\\' + line.substring(i,line.length());
                                    i++;
                                }
                                else if (line.charAt(i) == '+')
                                {
                                    line=line.substring(0 , i) + '\\' + line.substring(i,line.length());
                                   i++;
                                }
                                else if (line.charAt(i) == '?')
                                {
                                    line=line.substring(0, i) + '\\' + line.substring(i,line.length()); 
                                    i++;
                                }

                                i++; 

                            }  
                             
                            Disallow.add(".*" +line);
                        }
                        else if (line.contains("Allow"))
                        {              

                            line=  line.substring(line.indexOf(":")+2 ,line.length());

                            int i =0;
                            while ( i< line.length())
                            {
                                if ( line.charAt(i) == '*' )
                                {
                                    line=line.substring(0 , i) + '.' + line.substring(i,line.length());
                                    i++;
                                }
                                else if (line.charAt(i) == '.')
                                {
                                    line=line.substring(0 , i) + '\\' + line.substring(i,line.length());
                                    i++;
                                }
                                else if (line.charAt(i) == '+')
                                {
                                    line=line.substring(0 , i) + '\\' + line.substring(i,line.length());
                                    i++;

                                }
                                else if (line.charAt(i) == '?')
                                {     
                                    line=line.substring(0 , i) + '\\' + line.substring(i,line.length());
                                    i++;
                                }
                                i++;

                            }
                            
                            Allow.add(".*" +line);
                        }
                         
                    }
                }
            }  
        }
        catch(MalformedURLException e1) {
        }
           
           
    }
/////////////////////////////////////////////////  RUN  ////////////////////////////////////////////////////////////////
    public void run()
    {
        try {
            crawl();
        } catch (IOException ex) {
            Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
/////////////////////////////////////////////////  Normalize Functions Functions  ////////////////////////////////////////////////////////////////
    public static String normalize(String URL) throws MalformedURLException
    {
    //  Remove #
        if (URL.contains("#"))
        {
            URL =  URL.replace("#", "");
        }
        URL  url = new URL(URL);
    //  Add www
        if(!( URL.contains("www.") || URL.contains("WWW.") ))
        {
           URL =  url.getProtocol().toLowerCase()+"://"+"www."+url.getHost().toLowerCase()+url.getPath();   
        }
    //  Remove index.html
        if(URL.endsWith("index.html")  || URL.endsWith("index.html/"))
        {
            URL =  URL.replace("index.html", "");
        }
        if (URL.endsWith("index")  || URL.endsWith("index/"))
        {
            URL =  URL.replace("index", "");
        } 
    //    Add trailing slash    
        if (!URL.endsWith("/"))
        {
            URL =  URL+ "/";  
        }

        return URL;

    }

/////////////////////////////////////////////////  Loading Functions  ////////////////////////////////////////////////////////////////
    public static void firstTime(DataBase db) throws FileNotFoundException, MalformedURLException
    {
    //  Reading From seeds.txt into database           
        db.getCollection("Seeds").drop();
        db.getCollection("Robot").drop();

        File seeds_File = new File("seeds.txt");
        Scanner myReader = new Scanner(seeds_File);
        while (myReader.hasNextLine()) {
            DBObject seeds =  new BasicDBObject("URL",normalize(myReader.nextLine()));
            seeds.put("Visited", false); 
            seeds.put("Assigned", false);
            db.insertDocument("Seeds",seeds );
        }
        myReader.close();

    }

    public static void Continue(DataBase db)
    {
        BasicDBObject newField = new BasicDBObject().append("$set", new BasicDBObject().append("Assigned", false));
        DBObject object = new BasicDBObject("Assigned", true);
        object.put("Visited", false);
        db.getCollection("Seeds").update(object, newField);
    }

/////////////////////////////////////////////////  main Function  ////////////////////////////////////////////////////////////////

    public static void toRun(DataBase db,int numberOfThreads) throws IOException, InterruptedException
    {
       
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter 1 if First Time , 2 if Continue");
        int option = scanner.nextInt();
        if (option == 1)
            firstTime(db);
        else
            Continue(db);
        
//        get user input  --> number of threads
       
        Thread[] threads = new Thread[numberOfThreads];
        Crawler crawler = new Crawler(numberOfThreads,db);

        for (int j =0;j < numberOfThreads;j++)
        {
            Thread thread = new Thread(crawler);
            thread.setName(Integer.toString(j));
            threads[j] = thread;
            thread.start();
        }
        
        for (int j =0;j < numberOfThreads;j++)
        {
            threads[j].join();
        }
   }
}
