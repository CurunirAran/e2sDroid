package com.arcurunir.e2s;

import java.io.IOException;
import java.util.Locale;

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
import android.view.inputmethod.EditorInfo;
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
    
    // Initialize the EditText inputText and the SQLite Database.
    inputTextInit();
    sqlDBInit();
    
    return true;
  }
  
  /**
   * Initialize the inputText field.
   */
  
  private void inputTextInit() {
    EditText inputText = (EditText) findViewById(R.id.inputText);
    inputText.setOnEditorActionListener(inputTextALInit());
  }
  
  /**
   * Initialize the OneEditorActionListener for inputText.
   * @return the initialized OnEditorActionListener
   */
  private OnEditorActionListener inputTextALInit() {
    
    OnEditorActionListener returnValue = 
      new OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
          if (actionId == EditorInfo.IME_ACTION_GO) {          
            lookupAndOutput();
            return true;
          }
          return false;
        }
      };
    return returnValue;
  }
  
  /**
   * Initialize the SQLite Database.
   */
  private void sqlDBInit() {
    this.sqlDB = new DataBaseHelper(this);
    try {
      sqlDB.createDataBase();
      sqlDB.openDataBase();
    } catch(IOException ioe) {
      System.out.println(ioe);
    } catch (SQLException sqle) {
      System.out.println(sqle);
    }
  }
  
  /**
   * Set the action done when enterButton is pressed.
   * @param v is the View to which the button belongs.
   */
  public void enterClick(View v) {
    lookupAndOutput();
  }
  
  /**
   * lookupAndOutput() is responsible for looking up the input. Generates error toasts when 
   * the input is invalid or if there is no translation for the input.
   * Otherwise, it appends the output to the TextView translations. 
   */
  private void lookupAndOutput() {
    
    // Hides keyboard upon querying.
    InputMethodManager imm = (InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(((EditText) findViewById(R.id.inputText)).getWindowToken(), 0);
    
    // Finds the required elements.
    EditText enterText = (EditText) findViewById(R.id.inputText);
    TextView translations = (TextView) findViewById(R.id.translations);
    
    String input = enterText.getText().toString().toLowerCase(Locale.UK);
    
    if (enterText.getText().length() == 0) {
      // If the input is invalid, generate an error Toast.
      errorToast("Please enter a valid word");
    } else {
      try {
        Cursor queryResult = sqlDB.executeQry("select * from ensd where word = ?", input);
        if (!queryResult.moveToNext()) {
          
          // If there exists no translation for the input, generate an error Toast.
          errorToast("Word not found in dictionary. Sorry!");
        } else {
          
          translations.setText(new SpannedString(""));
          // Iteratively look up the various fields of the result and append them 
          // to the TextView 'translations'.
          do {
            String translation = queryResult.getString(queryResult.getColumnIndex("translation"));
            String pos = queryResult.getString(queryResult.getColumnIndex("pos"));
            String tense = queryResult.getString(queryResult.getColumnIndex("tense"));
            String usage = queryResult.getString(queryResult.getColumnIndex("usage"));
            
            appendResult(translations, translation, pos, tense, usage);
          } while(queryResult.moveToNext());
        }
      } catch(SQLException sqle) {
        System.err.println(sqle);
      }
    }
  }
  
  /**
   * appendResult() appends the various linguistic data to the 'translations' TextView.
   * @param translations the TextView to which the data is to be appended.
   * @param tr the Sindarin translation of the input.
   * @param p the Part of Speech to which the translation belongs.
   * @param te the tense of the translation.
   * @param u the usage of the translation
   */
  
  private void appendResult(TextView translations, String tr, String p, String te, String u) {
    
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
  
  /**
   * errorToast() generates a Toast containing text passed as a parameter.
   * @param message the text to be displayed.
   */
  private void errorToast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }

}
