package com.example.search;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class WebsiteActivity extends AppCompatActivity {

    private TextView Url;
   private  WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_website);
        Url =findViewById(R.id.TheUrl);
        web=(WebView) findViewById(R.id.webview);

 ////////////////////////////////////////////////////////////getting the url
        Intent intent = getIntent();
        Bundle b = getIntent().getExtras();
        if (b != null) {
            String query = b.getString("key");
            Url.setText(query);
        }
///////////////////////////////////// showing the back button in action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

//////////////////////////////////////////////////displaying the Url
        web.setWebViewClient(new WebViewClient());
       // web.loadUrl("http://www.google.com");  /// here we pass the url
        web.loadUrl("query");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}