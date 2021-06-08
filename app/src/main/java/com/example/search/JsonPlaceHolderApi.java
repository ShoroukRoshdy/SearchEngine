package com.example.search;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface JsonPlaceHolderApi {

    @GET("search/{word}")  //api name is here tob chaned
    Call<List<UrlItem>> getUrls(@Path("word") String Searchword );


    @GET("suggestion/{word}")  //api name is here tob chaned
    Call<List<String>> getSuggestions(@Path("word") String Searchletter );

}
