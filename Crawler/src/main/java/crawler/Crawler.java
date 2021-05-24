package crawler;

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
//MONGO



public class Crawler {
    // HashSet For VisitedLinks 
    // Why HashSet -> Fast Searching if the link visited before or not
    private HashSet<String> visited_Links;
    //List of URLs
    private List<String> seed_Set;
    
    private DataBase database;
    
    public Crawler() throws IOException
    {
       
        database = new DataBase();
        File visited_File = new File("visited.txt");
        // if first time (there's no visited file) -> create and intialize
        if (!visited_File.createNewFile()) {
        //if crawler interrupted -> load visited urls from visited file into visited set
            Scanner myReader = new Scanner(visited_File);
            while (myReader.hasNextLine()) {
              visited_Links.add(myReader.nextLine());
            }
            myReader.close();
        }
        
        seed_Set = new ArrayList<String>();
        File seeds_File = new File("seeds.txt");
        Scanner myReader = new Scanner(seeds_File);
        while (myReader.hasNextLine()) {
            seed_Set.add(myReader.nextLine());
        }
        myReader.close();
       
    }


        /*
      getRobotDiallows funcs takes an

      Input:(empty hashset , the Url as a string )

      Return: it searches for any disallows in the robots.txt and returns true if found any disallows
      otherwise it returns false if it didnt found any disallow or it it faced any problem
    */
   
    public boolean getRobotDiallows(String theDomainUrl,HashSet<String> theDisallows ) throws IOException
    {
     try
     {
         ////geting the url of the robot.txt
        URL urlRobot = new URL(theDomainUrl);
        String theRobotUrl =urlRobot.getProtocol()+"://"+urlRobot.getHost()+"/robots.txt";
        ////opening the robot.txt 
        String line = null;
        String RobotCommands = new String("");;
        try(BufferedReader in = new BufferedReader( new InputStreamReader(new URL(theRobotUrl).openStream())))
         {
            Boolean isDisallowExist= false;
        //// saving the robot.text 
             while((line = in.readLine()) != null) 
             {
                // checking for disallow
                if (line.contains("Disallow")) 
                {
                    isDisallowExist=true;   
               /// removing "Disallow: from the line"
                    RobotCommands = line;
                    int startIndex = RobotCommands.indexOf(":");
                    int endIndex = line.length();
                    RobotCommands=RobotCommands.substring(startIndex+2 , endIndex);
                    RobotCommands.trim();
               ///saving the extentsion in the hashset 
                    theDisallows.add(urlRobot.getProtocol()+"://"+urlRobot.getHost()+RobotCommands);  
                }  
            }
             // if the robot.txt has a disallow statement this will be equal to true else false
             return isDisallowExist;
         } 
    } 
       catch (MalformedURLException e1) 
    {
        // Tthe URL is not correct 
        e1.printStackTrace();
    }
        return false;
   }

   /*
   runs a hard coded test case to test  getRobotDiallows()  func
   
   */

   public void testgetRobotDiallows()
   {
        /// robots.txt test getRobotDiallows()

    
    try {
        Crawler test = new Crawler();
        HashSet<String> DisallowLinks=new HashSet<String>();  
        Boolean ifRobotRulesExist =test.getRobotDiallows("https://moz.com/",DisallowLinks);
       
        if(ifRobotRulesExist )
        {
            Iterator<String> i=DisallowLinks.iterator();  
            while(i.hasNext())  
            {  
            System.out.println(i.next());  
            }  
        }
        else 
        {
            System.out.println("no Disallows in robots or the robots.txt doesnt exist");
        }

    } 
    
    catch (IOException e) {
        e.printStackTrace();
    }

   }
    

    
    public void crawl() throws IOException
    {
        // loop on each url in the seed_Set
        int i =0;
        FileWriter myWriter = new FileWriter("seeds.txt");
        FileWriter visitedWriter = new FileWriter("visited.txt");
     
        while (i < seed_Set.size() && seed_Set.size() <500)
        {
            System.out.println(i);
            System.out.println(seed_Set.size());
            System.out.println(seed_Set.get(i));

             // check if the have been visited 
            if (visited_Links.contains(seed_Set.get(i)) == false)
            {
                // if not -> Add it to the visited set
                visited_Links.add(seed_Set.get(i));
                visitedWriter.write(seed_Set.get(i)+"\n");

            
                // Fetch the url
                Document document = Jsoup.connect(seed_Set.get(i)).get();
                // parse the HTML document to extract links to other URLs
                Elements page_Links = document.select("a");

                //Loop on all extracted links and push them in the seed_set
                int depth=0;
                for (Element newurl : page_Links) {
                    if (depth > 10)
                        break;
                    seed_Set.add(newurl.attr("abs:href"));
                    myWriter.write(newurl.attr("abs:href")+"\n");
                    depth++;
                } 
            }
            
            i++;
        }   
        myWriter.close();
        visitedWriter.close();
    }
    
    
    public static void main(String[] args) throws IOException {
     
        Crawler crawler = new Crawler();
       System.out.println("CONNECTED");

        
        try {
            crawler.crawl();
        } catch (IOException ex) {
            Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
   
}
