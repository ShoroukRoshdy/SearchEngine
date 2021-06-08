package com.example.search;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


import android.os.Bundle;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {

    // getting the views from the activity
    ArrayList<String> suggestlist = new ArrayList<>();
    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    TextView instrc1;
    ImageButton VoiceBttn;
    Button searchbttn;
    String FinalQuery;
    AutoCompleteTextView suggestmenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instrc1 = findViewById(R.id.text);
        VoiceBttn = findViewById(R.id.VoiceText);

        searchbttn = findViewById(R.id.button);
        suggestmenu = findViewById(R.id.autotext);
        ///////////////////////////////////////////////////////////////////////////////

        //////////////////////////////////////////////////database conection

        // getting the speech to text dialog

        VoiceBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });

        ////////////////////////////////////////////////////going to next indent in search button
        searchbttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FinalQuery = suggestmenu.getText().toString();
                Intent intent = new Intent(MainActivity.this,ResultsActivity.class);
                Bundle b = new Bundle();
                b.putString("key",FinalQuery);
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);
                finish();
                Toast.makeText(MainActivity.this, FinalQuery, Toast.LENGTH_SHORT).show();
            }
        });

        ////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////API
        suggestmenu.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FinalQuery= suggestmenu.getText().toString();
                Retrofit retrofit = new Retrofit.Builder().baseUrl("http://10.0.2.2:8080/").   // to be changed to  "10.0.2.2:portnumber/"
                        addConverterFactory(GsonConverterFactory.create()).build();

                JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

                Call<List<String>> call = jsonPlaceHolderApi.getSuggestions(FinalQuery);
                call.enqueue(new Callback<List<String>>() {
                    @Override
                    public void onResponse(Call<List<String>> call, Response<List<String>> response) {

                        if (!response.isSuccessful()) {

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, suggestlist);
                            suggestmenu.setAdapter(adapter);
                            // searchquery.setText("Code:" + response.code());
                            return;
                        }

                        List<String> theApiURLlist = response.body();
                        suggestlist.clear();
                        for (String suggest : theApiURLlist) {
                            suggestlist.add(suggest);
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, suggestlist);
                        suggestmenu.setAdapter(adapter);
                    }
                    @Override
                    public void onFailure(Call<List<String>> call, Throwable t) {
                        suggestlist.add(t.toString());
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, suggestlist);
                        suggestmenu.setAdapter(adapter);
                        //numberofresults.setText(t.toString());
                    }
                });


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

//////////////////////////////////////////////////////////////////////////////////////////////////////////
    }
    /////////////////////////////
    ///////////////////////////////function for the mic button
    private void speak() {
        //intent to show speech
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say what you want to search about");

        //the intent
        try {

            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);

        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> res = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    suggestmenu.setText(res.get(0));
                    FinalQuery = suggestmenu.getText().toString();
                }
                break;
            }
        }
    }
    /////////////////////////////////////////////end function for the mic button


    //////////////////////////////////////////////////////////////for action bar search
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater flater = getMenuInflater();
        flater.inflate(R.menu.search_menu, menu);

        MenuItem search = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) search.getActionView();
        searchView.setQueryHint("Search for anything!");
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                FinalQuery = query;
                Toast.makeText(MainActivity.this, FinalQuery, Toast.LENGTH_SHORT).show();
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                /// here pass the word to the query
                return false;
            }
        });
        return true;



    }
////////////////////////////////////////////////////////////////////////////////////////end action bar search







}
