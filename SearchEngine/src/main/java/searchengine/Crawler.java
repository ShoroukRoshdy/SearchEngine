package searchengine;

import com.mongodb.*;
import java.io.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger; 
import java.util.regex.*;  

//MONGO



public class Crawler {
   

    private DataBase database;
    
    static int seedsSize = 0;
    
    public Crawler() throws IOException
    {
       
        database = new DataBase();
        
        database.getCollection("Seeds").drop();
         database.getCollection("Robot").drop();
       
        File seeds_File = new File("seeds.txt");
        Scanner myReader = new Scanner(seeds_File);
        int i =0;
        while (myReader.hasNextLine()) {
            DBObject seeds =  new BasicDBObject("URL",myReader.nextLine() );
            seeds.put("index", i);                    
            seeds.put("Visited", false);
            database.insertDocument("Seeds",seeds );
            i++;
            seedsSize++;
        }
        myReader.close();
       
    }
    
    
    public void crawl() throws IOException
    {
        int i =0;
        DBObject url;
        do
        {
//            get URL from database by its index -> index field based on i
            url =database.getDatabase().getCollection("Seeds").findOne(new BasicDBObject("index", i));

             // check if visited before
            if( url.get("Visited").equals(false))
            {
                
//                Set Visited Value to True 
                BasicDBObject visitedField = new BasicDBObject().append("$set", new BasicDBObject().append("Visited", true));
                database.getCollection("Seeds").update(new BasicDBObject().append("URL", url.get("URL")), visitedField);
               
                
//               Fetch the url
                Document document = Jsoup.connect(url.get("URL").toString()).get();
                // parse the HTML document to extract links to other URLs
                Elements page_Links = document.select("a");
               
                
//                Check Robot.txt
            HashSet<String> Disallow = new HashSet<String>();
            HashSet<String> Allow = new HashSet<String>();
            String host=getHostName(url.get("URL").toString());
            DBObject robot =database.getDatabase().getCollection("Robot").findOne(new BasicDBObject("Host", host));
            if (robot== null)
            {
                System.out.println(host);
                getAllowDisallow(host,Disallow,Allow);
//               Add Allow ,Disallows             
               DBObject query =  new BasicDBObject("Host",host );
                query.put("Allow", Allow);                    
                query.put("Disallow", Disallow);
                database.insertDocument("Robot",query );
            }
            else
            {
                BasicDBList DisallowDB = (BasicDBList)robot.get("Disallow"); 
                for (Object d: DisallowDB)
                    Disallow.add(d.toString());
                BasicDBList AllowDB = (BasicDBList) robot.get("Allow");
                 for (Object d: AllowDB)
                    Allow.add(d.toString());
               
            }
                

                
                //Loop on all extracted links and push them in the seed_set
                int depth=0;
                for (Element newurl : page_Links) {
//                    if (depth > 10 )
//                        break;
//                                    
                    if (newurl.attr("abs:href") == "" )
                        continue;
                    else if ((checkDisallow(newurl.attr("abs:href") ,Disallow) && !checkAllow(newurl.attr("abs:href") ,Allow)))
                    {   
                        System.out.println("Disallowed : " + newurl.attr("abs:href"));
                        continue; 
                    }   
                    
                    DBObject newurlDB =database.getDatabase().getCollection("Seeds").findOne(new BasicDBObject("URL", newurl.attr("abs:href") ));

                    if (newurlDB == null)
                    {
                        DBObject newURL =  new BasicDBObject("URL",newurl.attr("abs:href") );
                        newURL.put("index", seedsSize);                    
                        newURL.put("Visited", false);
                        seedsSize++;
                        database.insertDocument("Seeds",newURL );
                    }
                    depth++;
                } 
            }
            
            i++;
        } while (url != null && seedsSize<= 1000);
    }
    
    public String getHostName (String URL) throws MalformedURLException
    {
         URL url = new URL(URL);
        return url.getProtocol()+"://"+url.getHost();     
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
    
    public static void main(String[] args) throws IOException {
     
        Crawler crawler = new Crawler();
        
        crawler.crawl();
//        HashSet<String> Disallow = new HashSet<String>();
//        HashSet<String> Allow = new HashSet<String>();
//        crawler.getAllowDisallow("https://www.amazon.com/",Disallow,Allow);
//        System.out.println("https://www.amazon.com/wishlist/universal/2313541");
//        System.out.println("Allow : "+crawler.checkAllow("https://www.amazon.com/wishlist/universal/2313541", Allow));
//        System.out.println("Disallow : "+crawler.checkDisallow("https://www.amazon.com/wishlist/universal/2313541", Disallow));
//        System.out.println("https://www.amazon.com/wishlist/");
//        System.out.println("Allow : "+crawler.checkAllow("https://www.amazon.com/wishlist/", Allow));
//        System.out.println("Disallow : "+crawler.checkDisallow("https://www.amazon.com/wishlist/", Disallow));
    }
   
  
 
   

}
