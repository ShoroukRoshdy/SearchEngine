
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




public class Indexer {
    
       static ArrayList<MyPair> words = new ArrayList<>();  
            static ArrayList<String> tags = new ArrayList<>();  
              static ArrayList<WordsPair> wordsdocs = new ArrayList<>();
                static ArrayList<Integer> documentIds = new ArrayList<>(); 
               static ArrayList<String> all = new ArrayList<>();  
             static ArrayList<String> stopWords = new ArrayList<>(); 
            static ArrayList<WordDetails> allWords = new ArrayList<>(); 
              static DataBase db = new DataBase();
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
//        public void lowerCase()
//        {
//            word.toLowerCase();
//        }
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
//        public void lowerCase()
//        {
//            word.toLowerCase();
//        }
     }
    
    public static class IndexerThread implements Runnable {
       Document [] doc;
       int [] documentID;
         public IndexerThread(Document[] d, int [] docID)
         {
             doc=d;
             documentID=docID;
         }
         void Indexing()
         {
             String tag;
            String str;
          
          synchronized(words)
         { 
//              synchronized(documentID)
//              {
                  for(int r=0;r<doc.length;r++)
             {
                 Elements head = doc[r].head().select("*");
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
               Elements body = doc[r].body().select("*");
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
//                   if(!(all.contains(words.get(i).getWord())))
//                        {
                           
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
                        
                        
                        //--------------> Check if document exists
                        
                        
                        DBObject Word =  new BasicDBObject("Word", w.word);
                         
//                        Word.put("tags",w.tags.get(0));
                        
                        BasicDBObject documentDetail = new BasicDBObject();
                        for (int k=0;k<w.tags.size();k++)
                        {
                               documentDetail.put("tag"+k,w.tags.get(k)) ;
                               
                        }
                       Word.put("Tags", documentDetail);
                        Word.put("tf",w.occurence);
                         Word.put("Document_ID",documentID[r]);
                        // db.insertDocument("WordDetails",Word);
                        }
                             
         //                  System.out.println(words.get(j).getWord());
                          // System.out.println("document "+documentID[r]);
//                                     System.out.println(w.occurence);
//                                     for(String m : w.tags)
//                                     {
//                                         System.out.print(m+" ");
//                                         
//                                     }
//                                     System.out.println();
//                                      System.out.println("//////////////////////");
                        
                        
                        //-----> ADD IDF
                      
                     
                        
                      //  
             }
              
                         
                 }
//          }
//             // --------------- Getting all words in the head of the document --------------- //
//            
              }
//               
               
                 
         
//         }
          @Override
    public void run() {
        Indexing();
    }
    }
   public static void main(String[] args) throws IOException, InterruptedException
    {
           
             int occurrence;
            
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

             Document doc = Jsoup.connect("https://en.wikipedia.org/wiki/Document").get();
             Document doc2 = Jsoup.connect("https://en.wikipedia.org/wiki/Happiness").get();
             Document doc3 = Jsoup.connect("https://en.wikipedia.org/wiki/Happiness").get();
               Document doc4 = Jsoup.connect("https://en.wikipedia.org/wiki/Document").get();
             
                 IndexerThread t1 = new IndexerThread(new Document[]{doc,doc2},new int[]{1,2});
                 IndexerThread t2 = new IndexerThread(new Document[]{doc3,doc4},new int[]{3,4});
                 Thread thread1 = new Thread(t1,"1");
                 Thread thread2 = new Thread(t2,"2");
                  thread1.start();
                  thread2.start();
                  thread1.join();
                  thread2.join();
//                wordsdocs.forEach(p -> System.out.println(p.word));
//                 wordsdocs.forEach(p -> System.out.println(p.id));
                 
      }       
   
    }
//}


//         