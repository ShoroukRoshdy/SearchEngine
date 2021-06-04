package com.example.search;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    // getting the views from the activity
    private static final int REQUEST_CODE_SPEECH_INPUT=1000;
    TextView instrc1;
    ImageButton VoiceBttn;
    EditText searchedittext;
    Button searchbttn;
    String FinalQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState)

    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instrc1=findViewById(R.id.text);
        VoiceBttn=findViewById(R.id.VoiceText);
        searchedittext=findViewById(R.id.editTextsearch);
        searchbttn=findViewById(R.id.button);
        // getting the speech to text dialog

        VoiceBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });
        searchbttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FinalQuery= searchedittext.getText().toString();
                Toast.makeText(MainActivity.this,FinalQuery,Toast.LENGTH_SHORT).show();
            }
        });
    }

    ///////////////////////////////function for the mic button
    private void speak()
    {
          //intent to show speech
        Intent intent =new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say what you want to search about");

        //the intent
        try {

            startActivityForResult(intent,REQUEST_CODE_SPEECH_INPUT);

        }
        catch (Exception e)
        {
           Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
        }

    }

///////////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case REQUEST_CODE_SPEECH_INPUT:{
                if (resultCode ==RESULT_OK && null!=data)
                {
                    ArrayList<String> res = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    searchedittext.setText(res.get(0));
                    FinalQuery=searchedittext.getText().toString();
                }
                break;
            }
        }
    }
  /////////////////////////////////////////////end function for the mic button


//////////////////////////for action bar search
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater flater =getMenuInflater();
        flater.inflate(R.menu.search_menu,menu);

        MenuItem search = menu.findItem(R.id.action_search);
        SearchView searchView =(SearchView) search.getActionView();
        searchView.setQueryHint("Search for anything!");
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                FinalQuery=query;
                Toast.makeText(MainActivity.this,FinalQuery,Toast.LENGTH_SHORT).show();
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                /// here pass the word to the query
                return false;
            }
        });
        return true;
      }
////////////////////////////////////////////////////////////////////////////////////////end action bar search
}

