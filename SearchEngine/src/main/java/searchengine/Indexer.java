
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







public class Indexer {
    
     
    static ArrayList<Document> documents;
    static int ThreadsNumber;                
    static DataBase db = new DataBase();
    static ArrayList<String> stopWords = new ArrayList<>();
    static int totalDocNumber;

    public void setDocuments(ArrayList<Document> documents) {
        this.documents = documents;
    }

    public static void setThreadsNumber(int ThreadsNumber) {
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
           {word=w; tag=t;}
        public String getWord()
        {return word;}
        
         public String getTag()
        {return tag;}
        
        public void setWord(String w){
            word=w;
        }

     }
      public static class WordsPair
     {
        private  String word;
        private  int id;
        public void setPair(String w,int t)
           {word=w; id=t;}
        public String getWord()
        {return word;}
        
         public int getdocid()
        {return id;}
        
        public void setWord(String w){
            word=w;
        }

     }
    
    public static class IndexerThread implements Runnable {
//       Document [] doc;
//       ArrayList<Sring> stopWords; 
        
       int docNumber;
         public IndexerThread()
         {
         }
         void Indexing()
         {
             String tag;
             String str;
             ArrayList<MyPair> words = new ArrayList<>();  
             ArrayList<String> tags = new ArrayList<>();  
             ArrayList<WordsPair> wordsdocs = new ArrayList<>();
             ArrayList<Integer> documentIds = new ArrayList<>(); 
             ArrayList<String> all = new ArrayList<>(); 
             ArrayList<WordDetails> allWords = new ArrayList<>(); 
             
        
             
              System.out.print("-------------------------------------> ANA NEW THREAD");

//              0 -> 0 , 0+3 = 3 , 3+3 = 6, 
//              1 -> 1 , 1+3 = 4 , 4+3 =7,
                   //---------------------- Iterating on documents per thread ---------------------- //

            int currentThread = Integer.parseInt(Thread.currentThread().getName());
            System.out.println("Hello from Thread " + currentThread +  " Number of Document " + documents.size());
            

            for(int r=currentThread ; r<documents.size()  ;  r = r+ ThreadsNumber )
            {
                Elements head = documents.get(r).head().select("*");
                for (Element element : head) {
                    tag= element.tagName();
                    str= element.ownText();
                    str = str.replaceAll("[^0-9a-zA-Z @!]", ""); // <---------- Not sure if it's accurate

                  // ---------------- Splitting the word in each sentence ---------------- //
                    for (String word : str.split(" ")) {
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
            for (Element element : body) {
                   tag= element.tagName();
                   str= element.ownText();
                    str = str.replaceAll("[^a-zA-Z @!]", ""); // <---------- Not sure if it's accurate
//                  str = str.replaceAll("[~`!@#$%^&*()_-/?\\|]", "");   
                   // ---------------- Splitting the word in each sentence ---------------- //
                    for (String word : str.split(" ")) {
                        
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
                            //System.out.println(stemWord);
                            words.get(i).setWord(stemWord);
                            //System.out.println(words.get(i).getWord());
                           

                       }
                   
            // ------------------------ Removing stop words ------------------------ //
//             words.removeIf(s -> s.matches("(,|\\.|\\?|/|\"|\')+"));
//               words.replaceAll(MyPair::lowerCase);
//               words.replaceAll(MyPair::stopWords);
//                 words.removeAll(stopWords);

                      for (int i=0;i<stopWords.size();i++){
                            String s=stopWords.get(i);
                            words.removeIf( word -> word.getWord().equals(s));
                             
                      }
                      
                      
            // ------------------------ Get occurence of each word ------------------------ //
                //Iterator<MyPair> iter= words.iterator();
                WordDetails w=new WordDetails();
               
                for(int i=0;i<words.size();i++)
               {

                           
                     w.occurence=1;
//                     
                      w.tags.clear();
                      w.setWord(words.get(i).getWord());
                      w.tags.add(words.get(i).getTag());
                        for(int j=i+1;j<words.size();j++){
//                         System.out.println(w.word);
//                          System.out.println("//////////////////////");
//                           System.out.println(words.get(j).getWord());

                            if(w.word.equals(words.get(j).getWord()))
                            {
                                if(!(w.tags.contains(words.get(j).getTag())))
                                {w.tags.add(words.get(j).getTag());}
                                
                                w.occurence++;
                                
                            }
                        }
//                           WordsPair p=new WordsPair();
//                                p.setPair(w.word, documentID[r]);
                        // System.out.println(w.word);
                          
//                                System.out.println(p.getWord());
//                            System.out.println(w.word);
//                             System.out.println(w.occurence);
//                                wordsdocs.add(p);
//                                all.add(w.word);
                                String s=words.get(i).getWord();
                        words.removeIf( word -> word.getWord().equals(s));
                        
                        
                       synchronized(db)
                             { 
                       
                                 double idf=-0;
                        // ------------------------ Creating word document to store in the database ------------------------ //
                        
                             
                         
                        DBObject Word =  new BasicDBObject("Word", w.word); 
                                    
                        DBCursor cursor=db.findDocument("WordDetails", Word);
                       
                         //-----------------------------> If document exists
//                        -----------> EL mfrood dayman yb2a = 1
                        if (cursor.count()!=0)
                        {
                            System.out.println("-----------------------> Documeting Already exists");
                            Map <String, Map<String,String>> parsedWord;
                            Map<String,String> parsedTags = null;
                            Map<String,String> parsedUrls=null;
                            int occurrence=0;
                            int tagCounter=1;
                            int urlsCounter=1;
                             while(cursor.hasNext()) {
                                   
//                                          BasicDBObject field= new BasicDBObject().append("Word","Word");
//                                          db.getCollection("WordDetails").update(new BasicDBObject().append(s, all));
//                                    System.out.println("-------------------------> In Loop");
                                    parsedWord  =cursor.next().toMap();
                                                 
//                                                 
                                   
                                    parsedTags = parsedWord.get("Tags");
                                    parsedUrls=parsedWord.get("Document_URL");
//                                    System.out.println(urls);
//                                    System.out.println("-------------------------> Urls");
                                    for (Map.Entry<String,String> entry : parsedUrls.entrySet())
                                        {
                                          urlsCounter++;
                                        }

//                                    System.out.println("------------------------->Tags "); 
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
//                             System.out.println("------------------------------> In Loop 2");
                             Map<String,Integer> tf=cursor.next().toMap();
                             occurrence=tf.get("tf");
                             occurrence+=w.occurence;
//                             System.out.println("-------------------------> Occurrence");
//                             System.out.println(occurrence);
                          }
                          //---------------------------- IDF ---------------------- //
                          
                          idf=Math.log(totalDocNumber/urlsCounter);
                          
                         // -------------------------- Updating the document   -------------------------- //
                        BasicDBObject searchQuery = new BasicDBObject();
                        searchQuery.append("Word", w.word);

                        BasicDBObject updateQuery = new BasicDBObject();
                        updateQuery.append("$set",
                        new BasicDBObject().append("tf", occurrence).append("Tags", parsedTags).append("Document_URL",parsedUrls).append("idf",idf));
                        
        //                DB database = mongoClient.getDB("myFirstDatabase");
                        db.getDatabase().getCollection("WordDetails").update(searchQuery, updateQuery);
                        }
                        else
                        {
                            
                            idf=Math.log(totalDocNumber/1); //<------ only 1 document so far
                            //---------------------> First occurence of the word
                            
                           Map<String,String> url=Map.of("url1",documents.get(r).location());;
//                            MapInitializer.url.put( );
                            BasicDBObject documentDetail = new BasicDBObject();
                           for (int k=0;k<w.tags.size();k++)
                           {
                             documentDetail.put("tag"+k,w.tags.get(k)) ;     
                           }
                           Word.put("Tags", documentDetail);
                           Word.put("tf",w.occurence);
                           Word.put("idf", idf);
                           Word.put("Document_URL",url);
                           
                           db.insertDocument("WordDetails",Word);
   //                        System.out.println("-------------------> Done inserting Document");
                        }
                    
                      }      
             }
  
                 }

              }
               
               
                 
         

          @Override
    public void run() {
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
                while (myReader.hasNextLine()) {
                 stopWords.add (myReader.nextLine());
                }
                myReader.close();
            } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
            }

        Thread[] threads = new Thread[ThreadsNumber];
        
//        IndexerThread t1 = new IndexerThread();
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
        