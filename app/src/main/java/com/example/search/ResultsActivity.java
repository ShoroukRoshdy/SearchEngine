package com.example.search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ResultsActivity extends AppCompatActivity {
      TextView searchquery;
      TextView numberofresults;
      private RecyclerView recycleUrl;
      private RecyclerView.Adapter myAdapter;
      private RecyclerView.LayoutManager MLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        searchquery = findViewById(R.id.textword);
        numberofresults=findViewById(R.id.sizeofsearch);
        /////////////////////////////////////////////// ////////calling the action bar
        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);
        /////////////////////////////////////////////////////////Getting query
        Intent intent = getIntent();
        Bundle b = getIntent().getExtras();
        if (b != null) {
            String query = b.getString("key");
            searchquery.setText(query);
        }
        ///////////////////////////////////////////////////recycleView
        ArrayList<UrlItem> urllist = new ArrayList<>();
        ///////////////////////////////////////////////////////////////////////////////// get api
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://jsonplaceholder.typicode.com/").
                addConverterFactory(GsonConverterFactory.create()).build();

        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        Call<List<UrlItem>> call = jsonPlaceHolderApi.getUrls();
        call.enqueue(new Callback<List<UrlItem>>() {
            @Override
            public void onResponse(Call<List<UrlItem>> call, Response<List<UrlItem>> response) {


                if (!response.isSuccessful()) {
                    searchquery.setText("Code:" + response.code());
                    return;
                }

                List<UrlItem> theApiURLlist = response.body();
                for (UrlItem url : theApiURLlist) {
                    urllist.add(new UrlItem(1, 2, url.getTitle(), url.getText()));
                }

                numberofresults.setText(" "+urllist.size()+" ");

                recycleUrl = findViewById(R.id.recycleView);
                recycleUrl.setHasFixedSize(true);
                MLayout = new LinearLayoutManager(ResultsActivity.this);
                myAdapter = new UrlAdapter(urllist);
                recycleUrl.setLayoutManager(MLayout);
                recycleUrl.setAdapter(myAdapter);
            }
            @Override
            public void onFailure(Call<List<UrlItem>> call, Throwable t) {

               numberofresults.setText(t.toString());

            }
        });
        /////////////////////////////////////////////////////////////////////////////////////////
    }

}