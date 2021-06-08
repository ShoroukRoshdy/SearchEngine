package com.example.search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ResultsActivity extends AppCompatActivity  {
      TextView searchquery;
      TextView numberofresults;
      TextView numberOfPage;
      private RecyclerView recycleUrl;
      private UrlAdapter myAdapter;
      private RecyclerView.LayoutManager MLayout;
      private int currentPage=1;
      Button nextbttn;
      Button backbttn;
    ArrayList<UrlItem> urllist;
    ArrayList<UrlItem> pagelist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        searchquery = findViewById(R.id.textword);
        numberofresults=findViewById(R.id.sizeofsearch);
        numberOfPage=findViewById(R.id.textwordd2);
        nextbttn=findViewById(R.id.next);
        backbttn=findViewById(R.id.prev);
        ArrayList<UrlItem> urllist = new ArrayList<>();
        ArrayList<UrlItem> pagelist = new ArrayList<>();
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

        ///////////////////////////////////////////////////////////////////////////////// get api
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://jsonplaceholder.typicode.com/").
                addConverterFactory(GsonConverterFactory.create()).build();

        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        Call<List<UrlItem>> call = jsonPlaceHolderApi.getUrls();
        call.enqueue(new Callback<List<UrlItem>>() {
            @Override
            public void onResponse(Call<List<UrlItem>> call, Response<List<UrlItem>> response)
            {

                if (!response.isSuccessful()) {
                    searchquery.setText("Code:" + response.code());
                    return;
                }
                List<UrlItem> theApiURLlist = response.body();
                for (UrlItem url : theApiURLlist) {
                    urllist.add(new UrlItem(1, 2, url.getTitle(), url.getText()));
                }
                numberofresults.setText(" "+urllist.size()+" found");
                //page of zero
                for(int i=0 ;i<10 ;i++)
                {
                    pagelist.add(urllist.get(i));
                }

                numberOfPage.setText("P"+(currentPage)+" of P"+urllist.size()/10+"");
                recycleUrl = findViewById(R.id.recycleView);
                recycleUrl.setHasFixedSize(true);
                MLayout = new LinearLayoutManager(ResultsActivity.this);
                myAdapter = new UrlAdapter(pagelist);
                recycleUrl.setLayoutManager(MLayout);
                recycleUrl.setAdapter(myAdapter);
                myAdapter.setOnItemClickListener(new UrlAdapter.OnItemClickListenser() {
                    @Override
                    public void onItemClick(int position) {
                        String url= pagelist.get(position).getText();
                        Intent intent = new Intent(ResultsActivity.this,WebsiteActivity.class);
                        Bundle b = new Bundle();
                        b.putString("key",url);
                        intent.putExtras(b); //Put your id to your next Intent
                        startActivity(intent);
                        finish();
                    }
                });

            }
            @Override
            public void onFailure(Call<List<UrlItem>> call, Throwable t)
            {
               numberofresults.setText(t.toString());
            }
        });
        ////////////////////////////////////////////////////////////////pages handling
        nextbttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentPage<10)
                {
                    pagelist.clear();
                    currentPage=currentPage+1;
                    numberOfPage.setText("P"+(currentPage)+" of P"+urllist.size()/10+"");
                    pagelist.add(urllist.get(((currentPage-1)*10)-1));
                    for(int i=0+(currentPage-1)*10 ;i<10*currentPage ;i++)
                    {
                        pagelist.add(urllist.get(i));
                    }

                    numberOfPage.setText("P"+(currentPage)+" of P"+urllist.size()/10+"");
                    recycleUrl = findViewById(R.id.recycleView);
                    recycleUrl.setHasFixedSize(true);
                    MLayout = new LinearLayoutManager(ResultsActivity.this);
                    myAdapter = new UrlAdapter(pagelist);
                    recycleUrl.setLayoutManager(MLayout);
                    recycleUrl.setAdapter(myAdapter);

                    myAdapter.setOnItemClickListener(new UrlAdapter.OnItemClickListenser() {
                        @Override
                        public void onItemClick(int position) {
                            String url= pagelist.get(position).getText();
                            Intent intent = new Intent(ResultsActivity.this,WebsiteActivity.class);
                            Bundle b = new Bundle();
                            b.putString("key",url);
                            intent.putExtras(b); //Put your id to your next Intent
                            startActivity(intent);
                            finish();
                        }
                    });


                }

            }
        });
      //////////////////////////////////////////////////////////////////////////// Back button
        backbttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(currentPage>1)
                {
                    pagelist.clear();
                    currentPage=currentPage-1;
                    numberOfPage.setText("P"+(currentPage)+" of P"+urllist.size()/10+"");



                    for(int i=0+(currentPage-1)*10 ;i<10*currentPage ;i++)
                    {
                        pagelist.add(urllist.get(i));
                    }



                    numberOfPage.setText("P"+(currentPage)+" of P"+urllist.size()/10+"");
                    recycleUrl = findViewById(R.id.recycleView);
                    recycleUrl.setHasFixedSize(true);
                    MLayout = new LinearLayoutManager(ResultsActivity.this);
                    myAdapter = new UrlAdapter(pagelist);
                    recycleUrl.setLayoutManager(MLayout);
                    recycleUrl.setAdapter(myAdapter);

                    myAdapter.setOnItemClickListener(new UrlAdapter.OnItemClickListenser() {
                        @Override
                        public void onItemClick(int position) {
                            String url= pagelist.get(position).getText();
                            Intent intent = new Intent(ResultsActivity.this,WebsiteActivity.class);
                            Bundle b = new Bundle();
                            b.putString("key",url);
                            intent.putExtras(b); //Put your id to your next Intent
                            startActivity(intent);

                        }
                    });


                }


            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}