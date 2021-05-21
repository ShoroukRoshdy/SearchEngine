/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler;

import java.io.File;
import java.io.FileWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author aliaa
 */
public class Crawler {
    // HashSet For VisitedLinks 
    // Why HashSet -> Fast Searching if the link visited before or not
    private HashSet<String> visited_Links;
    //List of URLs
    private List<String> seed_Set;
    
    public Crawler() throws IOException
    {
        // Intialize data members
        visited_Links = new HashSet<String>();

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
        File seeds_File = new File("C:\\Users\\aliaa\\Desktop\\Search Engine\\Crawler\\src\\main\\java\\crawler\\seeds.txt");
        Scanner myReader = new Scanner(seeds_File);
        while (myReader.hasNextLine()) {
            seed_Set.add(myReader.nextLine());
        }
        myReader.close();
       
    }
    
    public void crawl() throws IOException
    {
        // loop on each url in the seed_Set
        int i =0;
        FileWriter myWriter = new FileWriter("C:\\Users\\aliaa\\Desktop\\Search Engine\\Crawler\\src\\main\\java\\crawler\\seeds.txt");
        FileWriter visitedWriter = new FileWriter("visited.txt");
     
        while (i < seed_Set.size() && seed_Set.size() <500)
        {
            System.out.println(seed_Set.size());
             // check if the have been visited 
            if (visited_Links.contains(seed_Set.get(i)) == false)
            {
                // if not -> Add it to the visited set
                visited_Links.add(seed_Set.get(i));
                visitedWriter.write(seed_Set.get(i)+"\n");

            
                // Fetch the url
                Document document = Jsoup.connect(seed_Set.get(i)).get();
                // parse the HTML document to extract links to other URLs
                Elements page_Links = document.select("a[href]");

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
        try {
            crawler.crawl();
        } catch (IOException ex) {
            Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
        }           
    }
   
}
