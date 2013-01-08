package com.hexagongame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

public class ChooseBoardActivity extends Activity {
	
	private int gameMode = 0;
	
	private int phonePlayerId = 0;
	
	private ChooseBoardView chooseBoardView = null;
	
	public final static String ID_GAME_MODE = "com.hexagongame._ID_GAME_MODE";
	
	public final static String ID_PHONE_PLAYER_ID = "com.hexagongame._ID_PHONE_PLAYER_ID";
	
	public final static String ID_BOARD_VIEW = "com.hexagongame._ID_BOARD_VIEW";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onResume()
    {
  	  Log.v("hex", "called ChooseBoardActivity.onResume");
  	  
  	  super.onResume();
  	  
  	  initializeLayout();
    }
    
    private void initializeLayout()
    {
    	setContentView(R.layout.chooseboard);
    	
    	chooseBoardView = (ChooseBoardView) findViewById(R.id.chooseboardview);
    	
		//add listeners
		RadioGroup gameModeRadioGroup = (RadioGroup) findViewById(R.id.game_mode);
		gameModeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
	        public void onCheckedChanged(RadioGroup radioGroup, int i) {
	        	LinearLayout gameModeLayout = (LinearLayout) ChooseBoardActivity.this.findViewById(R.id.player_order);
	        	switch (i) {
		        	case R.id.person:
		        		gameMode = 0;
		        		gameModeLayout.setVisibility(LinearLayout.GONE);
		        		break;
		        	case R.id.phone:
		        		gameMode = 1;
		        		gameModeLayout.setVisibility(LinearLayout.VISIBLE);
		        		break;
	        	}
	        }
	    });
		
		RadioGroup phonePlayerIdRadioGroup = (RadioGroup) findViewById(R.id.phone_player_id);
		phonePlayerIdRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
	        public void onCheckedChanged(RadioGroup radioGroup, int i) {
	        	switch (i) {
		        	case R.id.blue:
		        		phonePlayerId = 0;
		        		break;
		        	case R.id.green:
		        		phonePlayerId = 1;
		        		break;
	        	}
	        }
	    });
		
		Button go = (Button) findViewById(R.id.go);
		go.setOnClickListener(new View.OnClickListener()
		  {
			  public void onClick(View v)
			  {
				  Activity ac = ChooseBoardActivity.this;
				  Intent i = new Intent(ac, HexActivity.class);
				  i.putExtra(ID_GAME_MODE, String.valueOf(gameMode));
				  i.putExtra(ID_PHONE_PLAYER_ID, String.valueOf(phonePlayerId));
				  i.putExtra(ID_BOARD_VIEW, String.valueOf(chooseBoardView.boardShape));
				  ac.startActivity(i);
				  ac.finish();
			  }
		  });
    }
}
