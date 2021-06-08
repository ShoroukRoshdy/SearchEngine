/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package searchengine.search;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.DataBase;

/**
 *
 * @author Shorouk
 */

@RestController
public class QueryProcessorController {
   
    @Autowired
    private SearchServices service;
    @RequestMapping("/hello") 
    public String sayHi()
    {
        return "Hi";
    }
    
    @RequestMapping("/search/{word}") 
    public ArrayList<Results> getResults(@PathVariable String word)
    {
        ArrayList<Results> results = service.getResults(word);
        if (results.isEmpty())
           
            return null;
        else
            return results;
    }
    
    @RequestMapping("/suggestion/{word}") 
    public ArrayList<String> getSuggestions(@PathVariable String word)
    {
        ArrayList<String> results = service.getSuggestions(word);
        if (results.isEmpty())
           
            return null;
        else
            return results;
    }
    
}
