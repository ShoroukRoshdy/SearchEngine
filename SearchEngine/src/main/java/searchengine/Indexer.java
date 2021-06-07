
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Shorouk
 */

package searchengine;

import java.io.IOException;  
import org.jsoup.Jsoup;  
import org.jsoup.nodes.Document;  
import org.jsoup.nodes.Element;  
import org.jsoup.select.Elements;  
import java.util.ArrayList;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import java.io.File; 
import java.io.FileNotFoundException;  
import java.util.Scanner; 
import java.util.*;
import com.mongodb.*;
import java.lang.Math;







public class Indexer 
{
    
     
    static ArrayList<Document> documents;
    static int ThreadsNumber;                
    static DataBase db = new DataBase();
    static ArrayList<String> stopWords = new ArrayList<>();
    static int totalDocNumber;

    public void setDocuments(ArrayList<Document> documents) 
    {
        this.documents = documents;
    }

    public static void setThreadsNumber(int ThreadsNumber) 
    {
        Indexer.ThreadsNumber = ThreadsNumber;
    }
    
    
    
    
    public static class WordDetails
    {
      public  String word;
      public  int occurence;
      ArrayList<String> tags = new ArrayList<>();  
      ArrayList<String> wordTags=new ArrayList<>();

        public void setWord(String w)
           {word=w; }
        public void setOccurence(int o)
           {occurence=o; }
        public void addTag(String t)
           {
               if (wordTags.contains(t)==false) // If the tag isn't already added
               {
                     wordTags.add(t);
               }

           }
       
     }
     public static class MyPair
     {
        private  String word;
        private  String tag;
        public void setPair(String w,String t)
           {
               word=w; 
               tag=t;
           }
        public String getWord()
        {
            return word;
        }
        
         public String getTag()
        {
            return tag;
        }
        
        public void setWord(String w)
        {
            word=w;
        }

     }
      public static class WordsPair
     {
        private  String word;
        private  int id;
        public void setPair(String w,int t)
           {
               word=w;
               id=t;
           }
        public String getWord()
        {
            return word;
        }
        
         public int getdocid()
        {
            return id;
        }
        
        public void setWord(String w)
        {
            word=w;
        }

     }
    
    public static class IndexerThread implements Runnable 
    {
        
       int docNumber;

         public IndexerThread(){}
        
         void Indexing()
         {
             String tag;
             String str;
             String title = new String();

             ArrayList<MyPair> words = new ArrayList<>();  
             ArrayList<String> tags = new ArrayList<>();  
             ArrayList<WordsPair> wordsdocs = new ArrayList<>();
             ArrayList<Integer> documentIds = new ArrayList<>(); 
             ArrayList<String> all = new ArrayList<>(); 
             ArrayList<WordDetails> allWords = new ArrayList<>(); 


                   //---------------------- Iterating on documents per thread ---------------------- //

            int currentThread = Integer.parseInt(Thread.currentThread().getName());
            System.out.println("Hello from Thread " + currentThread +  " Number of Document " + documents.size());
            

            for(int r=currentThread ; r<documents.size()  ;  r = r+ ThreadsNumber )
            {
                Elements head = documents.get(r).head().select("*");
                for (Element element : head) 
                {
                    tag= element.tagName();
                    str= element.ownText();

                    if(tag.equals("title"))
                        title= str;

                    str = str.replaceAll("[^0-9a-zA-Z @!]", ""); 

                  // ---------------- Splitting the word in each sentence ---------------- //
                    for (String word : str.split(" ")) 
                    {
                       if (word!="" && word!=" ")
                            {
                                MyPair p=new MyPair();
                                p.setPair(word, tag);
//                                System.out.println(p.getWord());
                                words.add(p);
                            }

                    }

                
                }
             
              // --------------- Getting all words in the body of the document --------------- //
               Elements body = documents.get(r).body().select("*");
            for (Element element : body) 
            {
                   tag= element.tagName();
                   str= element.ownText();
                    str = str.replaceAll("[^a-zA-Z @!]", ""); // <---------- Not sure if it's accurate
                   // ---------------- Splitting the word in each sentence ---------------- //
                    for (String word : str.split(" ")) 
                    {
                        
                        if (word!="" && word!=" ")
                            {
                                MyPair p=new MyPair();
                                p.setPair(word, tag);
//                                  System.out.println(p.getWord());
                                words.add(p);
                                
                            }
                    }
               
                
            }
            
            // ------------------------ Stemming the words extracted from the document ------------------------ //
            
            SnowballStemmer snowballStemmer = new englishStemmer();
            String stemWord;
            for (int i=0;i<words.size();i++)
                {  
                           
                    snowballStemmer.setCurrent(words.get(i).getWord());
                    snowballStemmer.stem();
                    stemWord= snowballStemmer.getCurrent();
                    stemWord= stemWord.toLowerCase();
                    words.get(i).setWord(stemWord);
                }
                   
            // ------------------------ Removing stop words ------------------------ //
            for (int i=0;i<stopWords.size();i++)
            {
                    String s=stopWords.get(i);
                    words.removeIf( word -> word.getWord().equals(s));
                             
            }
                      
                      
            // ------------------------ Get occurence of each word ------------------------ //
            WordDetails w=new WordDetails();
               
            for(int i=0;i<words.size();i++)
               {
                      w.occurence=1;
                      w.tags.clear();
                      w.setWord(words.get(i).getWord());
                      w.tags.add(words.get(i).getTag());
                        for(int j=i+1;j<words.size();j++)
                        {
                            if(w.word.equals(words.get(j).getWord()))
                            {
                                if(!(w.tags.contains(words.get(j).getTag())))
                                {
                                    w.tags.add(words.get(j).getTag());
                                }
                                
                                w.occurence++;
                                
                             }
                        }
                        String s=words.get(i).getWord();
                        words.removeIf( word -> word.getWord().equals(s));
                        
                       synchronized(db)
                             { 
                                   double idf=-0;
                        // ------------------------ Creating word document to store in the database ------------------------ //
                                   DBObject Word =  new BasicDBObject("Word", w.word); 
                                    DBCursor cursor=db.findDocument("WordDetails", Word);
                                    if (cursor.count()!=0)
                                    {
                                        Map <String, Map<String,String>> parsedWord;
                                        Map<String,String> parsedTags = null;
                                        Map<String,String> parsedUrls=null;
                                        int occurrence=0;
                                        int tagCounter=1;
                                        int urlsCounter=1;
                                        while(cursor.hasNext()) 
                                        {
                                   
                                            parsedWord  =cursor.next().toMap();                                    
                                            parsedTags = parsedWord.get("Tags");
                                            parsedUrls=parsedWord.get("Document_URL");
                                            for (Map.Entry<String,String> entry : parsedUrls.entrySet())
                                                {
                                                  urlsCounter++;
                                                }

                                            for (Map.Entry<String,String> entry : parsedTags.entrySet())
                                                {
                                                  tagCounter++;
                                                }
                                            //-----> Adding new tags to the tags map
                                            for (int k=0;k<w.tags.size();k++)
                                              {
                                                if(!(parsedTags.containsValue(w.tags.get(k)))) 
                                                {
                                                   parsedTags.put("tag"+tagCounter,w.tags.get(k)) ;     
                                                   tagCounter++;
                                                }

                                              }
                                    
                                            parsedUrls.put("url"+urlsCounter, documents.get(r).location());

                                        }
                            
                          
                             
                                        cursor=db.findDocument("WordDetails", Word);
                          
                          
                                        while(cursor.hasNext())
                                        {
                                           Map<String,Integer> tf=cursor.next().toMap();
                                           occurrence=tf.get("tf");
                                           occurrence+=w.occurence;
                                        }
                          //---------------------------- IDF ---------------------- //
                          
                                        idf=Math.log(totalDocNumber/urlsCounter);

                                       // -------------------------- Updating the document   -------------------------- //
                                      BasicDBObject searchQuery = new BasicDBObject();
                                      searchQuery.append("Word", w.word);

                                      DBObject listItem = new BasicDBObject("URLs", new BasicDBObject("url",documents.get(r).location()).append("title",title));
                                      BasicDBObject updateQuery = new BasicDBObject();
                                      updateQuery.append("$push",listItem);
                                      updateQuery.append("$set",
                                      new BasicDBObject().append("tf", occurrence).append("Tags", parsedTags).append("Document_URL",parsedUrls).append("idf",idf));

                                      db.getDatabase().getCollection("WordDetails").update(searchQuery, updateQuery);
                                    }
                                    else
                                        {
                            
                                            idf=Math.log(totalDocNumber/1); //<------ only 1 document so far
                                            //---------------------> First occurence of the word

                                           Map<String,String> url=Map.of("url1",documents.get(r).location());;
                                           BasicDBObject documentDetail = new BasicDBObject();
                                           for (int k=0;k<w.tags.size();k++)
                                           {
                                             documentDetail.put("tag"+k,w.tags.get(k)) ;     
                                           }

                                            Word.put("Tags", documentDetail);
                                            Word.put("tf",w.occurence);
                                            Word.put("idf", idf);
                                            List<BasicDBObject> URLs = new ArrayList<>();
                                            URLs.add( new BasicDBObject("url",documents.get(r).location()).append("title",title) );                          
                                            Word.put("URLs",URLs);
                                            Word.put("Document_URL",url);
                                            db.insertDocument("WordDetails",Word);
                                        }
                    
                              }      
                }
  
            }
        System.out.println("Thread: " + Thread.currentThread().getName() + " Finished" );           

        }
               
               
                 
         

          @Override
        public void run() 
        {
            Indexing();
        }
    }
   public static void toRun() throws IOException, InterruptedException
    {
        db.getCollection("WordDetails").drop();
           // ------------------------ Get total no. of documents ------------------------ //
             totalDocNumber=db.getDatabase().getCollection("Seeds").find().count();
             // ------------------------ Reading stop words from the .txt file ------------------------ //
            try {
                File myObj = new File("stopwords.txt");
                Scanner myReader = new Scanner(myObj);
                while (myReader.hasNextLine()) 
                {
                 stopWords.add (myReader.nextLine());
                }
                myReader.close();
            } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
            }

        Thread[] threads = new Thread[ThreadsNumber];
        for (int j =0;j < ThreadsNumber ;j++)
        {
            Thread thread = new Thread(new IndexerThread());
            thread.setName(Integer.toString(j));
            threads[j] = thread;
            thread.start();
        }
        for (int j =0;j < ThreadsNumber;j++)
        {
            threads[j].join();
        }

    }       
   
}
        