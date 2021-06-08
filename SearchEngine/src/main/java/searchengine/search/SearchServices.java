/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchengine.search;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import searchengine.DataBase;

/**
 *
 * @author Shorouk
 */

@Service
public class SearchServices {
    
    DataBase db = new DataBase();

    public ArrayList<String> getSuggestions(String word)
    {
        word = word.replaceAll("[^a-zA-Z @!]", ""); // <---------- Not sure if it's accurate
        SnowballStemmer snowballStemmer = new englishStemmer();
        String stemWord;
        snowballStemmer.setCurrent(word);
        snowballStemmer.stem();
        stemWord= snowballStemmer.getCurrent();
        stemWord= stemWord.toLowerCase();
        
        String search =  '^'+ stemWord + ".*" ;
        BasicDBObject regex = new BasicDBObject().append("$regex",search);

        BasicDBObject findQuery = new BasicDBObject().append("Word", regex);
        
        DBCursor cursor =  db.getDatabase().getCollection("WordDetails").find(findQuery); 
        
        ArrayList <DBObject> temp = (ArrayList <DBObject>) cursor.toArray();
        
        ArrayList<String> results = new ArrayList<String>();
        for (DBObject object : temp)
        {
            results.add( object.get("Word").toString());
        }

        return results;
    }
    public ArrayList<Results> getResults(String word)
    {
        word = word.replaceAll("[^a-zA-Z @!]", ""); // <---------- Not sure if it's accurate
        SnowballStemmer snowballStemmer = new englishStemmer();
        String stemWord;
        snowballStemmer.setCurrent(word);
        snowballStemmer.stem();
        stemWord= snowballStemmer.getCurrent();
        stemWord= stemWord.toLowerCase();
        
        BasicDBObject findQuery = new BasicDBObject().append("Word", stemWord);

                
        DBObject object =  db.getDatabase().getCollection("WordDetails").findOne(findQuery); 
        
        System.out.println(object);
        
        
        BasicDBList temp = (BasicDBList) object.get("URLs");
       
        System.out.println(temp);

        ArrayList <Results> results;
        results = new ArrayList<Results>();
        
        for (Object doc : temp)
        {
            Results result = new Results(((BasicDBObject)doc).get("url").toString(),((BasicDBObject)doc).get("title").toString());
            results.add(result);
        }
        System.out.println(results);

        return results;
        
    }
    
}
