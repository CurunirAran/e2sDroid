package com.arcurunir.e2s;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannedString;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.arcurunir.e2s.dbutil.DataBaseHelper;

public class MainActivity extends Activity {
  
  DataBaseHelper sqlDB;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    ((EditText) findViewById(R.id.inputText)).setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (event != null) {
          InputMethodManager imm = 
              (InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);
           imm.hideSoftInputFromWindow(((EditText) findViewById(R.id.inputText)).getWindowToken(), 0);
          lookupAndOutput();
          return true;
        }
        return false;
      }
    });
    sqlDB = new DataBaseHelper(this);
    try {
      sqlDB.createDataBase();
      sqlDB.openDataBase();
    } catch(IOException ioe) {
      System.out.println(ioe);
    } catch (SQLException sqle) {
      System.out.println(sqle);
    }
    return true;
  }
  
  
  public void enterClick(View v) {
    lookupAndOutput();
    InputMethodManager imm = 
        (InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);
     imm.hideSoftInputFromWindow(((EditText) findViewById(R.id.inputText)).getWindowToken(), 0);
  }
  
  public void lookupAndOutput() {
    EditText enterText = (EditText) findViewById(R.id.inputText);
    TextView translations = (TextView) findViewById(R.id.translations);
    String input = enterText.getText().toString();
    if (enterText.getText().length() == 0) {
      errorToast("Please enter a valid word");
    } else {
      try {
        Cursor queryResult = sqlDB.executeQry("select * from ensd where word = ?", input);
        if (!queryResult.moveToNext()) {
          errorToast("Word not found in dictionary. Sorry!");
        } else {
          translations.setText(new SpannedString(""));
          do {
            String translation = queryResult.getString(queryResult.getColumnIndex("translation"));
            String pos = queryResult.getString(queryResult.getColumnIndex("pos"));
            String tense = queryResult.getString(queryResult.getColumnIndex("tense"));
            String usage = queryResult.getString(queryResult.getColumnIndex("usage"));
            output(translations, translation, pos, tense, usage);
          } while(queryResult.moveToNext());
        }
      } catch(Exception e) {
        System.err.println(e);
      }
    }
  }
  
  void output(TextView translations, String tr, String p, String te, String u) {
    translations.append(Html.fromHtml("Sindarin translation: <b>" + tr + "</b><br>\n"));
    translations.append("Part of Speech: " + p + "\n");
    if (!te.equals("-")) {
      translations.append("Tense: " + te + "\n");  
    }
    if (!u.equals("-")) {
      translations.append("Usage: " + u + "\n");
    }
    translations.append("\n");
  }
  
  public void errorToast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }

}
