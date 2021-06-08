package com.example.search;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface JsonPlaceHolderApi {

    @GET("posts")  //api name is here tob chaned
    Call<List<UrlItem>> getUrls();

}
