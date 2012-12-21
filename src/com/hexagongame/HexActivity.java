package com.hexagongame;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class HexActivity extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onResume()
    {
  	  Log.v("hex", "called onResume");
  	  
  	  super.onResume();
  	  
  	  initializeLayout();
  	  startWork();
    }
    
    private void initializeLayout()
    {
    	setContentView(R.layout.main);
    }
    
    private void startWork()
    {
    	findViewById(R.id.boardview);
    	
    	Context context = getApplicationContext();
		Toast toast = Toast.makeText(context, "First player to make a path from one side to the other wins. Blue goes first.", Toast.LENGTH_SHORT);
		toast.show();
    }

}