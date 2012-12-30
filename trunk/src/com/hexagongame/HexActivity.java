package com.hexagongame;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

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
    }

}