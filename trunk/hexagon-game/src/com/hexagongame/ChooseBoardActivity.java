package com.hexagongame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ChooseBoardActivity extends Activity {

	public final static String ID_GAME_MODE = "com.hexagongame._ID_GAME_MODE";
	
	public final static String ID_PHONE_PLAYER_ID = "com.hexagongame._ID_PHONE_PLAYER_ID";
	
	private SeekBar bar; // declare seekbar object variable
	
	private SeekBar bar2;
	
	// declare text label objects
	private TextView boardSizeTextProgress;
	
	private TextView opponentStrengthTextProgress;
	
	//display the labels for the bar that the user can move to change the board size
	private String[] sizeBarLabels = {"1", "2", "3"};
	
	//display the labels for the bar that the user can move to change the automatic phone AI opponent strength
	private String[] opponentStrengthBarLabels = {"1", "2", "3", "4"};
	
	private ChooseBoardView chooseBoardView;
	
	//global copy of config
	public static ChooseBoardConfig config = new ChooseBoardConfig();
	
	//local copy of config: settings will be discarded if user exits activity without confirming choices
	static ChooseBoardConfig _configLocal = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d("hex", "ChooseBoardActivity.onCreate called");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //initialize the local config object
        initConfig();
        //initialize the layout
        initializeLayout();
		//populate the form based on initial values passed to the activity
		populateForm();
    }

    //initialize the local config object
    private void initConfig()
    {
    	//copy the global config to the local config. this is so that if the user makes local modifications but does not
    	//save them, they are discarded the next time the activity is called
    	ChooseBoardActivity._configLocal = ChooseBoardActivity.config.clone();
    }

    private void initializeLayout()
    {
    	setContentView(R.layout.chooseboard);
    	
    	chooseBoardView = (ChooseBoardView) findViewById(R.id.chooseboardview);

		//add listeners to radio buttons
		RadioGroup gameModeRadioGroup = (RadioGroup) findViewById(R.id.game_mode);
		gameModeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
	        public void onCheckedChanged(RadioGroup radioGroup, int i) {
	        	LinearLayout gameModeLayout = (LinearLayout) ChooseBoardActivity.this.findViewById(R.id.player_order);
	        	LinearLayout opponentStrengthSeekbar = (LinearLayout) ChooseBoardActivity.this.findViewById(R.id.opponent_strength_seekbar);
	        	switch (i) {
		        	case R.id.person:
		        		ChooseBoardActivity._configLocal.gameMode = 0;
		        		gameModeLayout.setVisibility(LinearLayout.GONE);
		        		opponentStrengthSeekbar.setVisibility(LinearLayout.GONE);
		        		break;
		        	case R.id.phone:
		        		ChooseBoardActivity._configLocal.gameMode = 1;
		        		gameModeLayout.setVisibility(LinearLayout.VISIBLE);
		        		opponentStrengthSeekbar.setVisibility(LinearLayout.VISIBLE);
		        		break;
	        	}
	        }
	    });
		
		RadioGroup phonePlayerIdRadioGroup = (RadioGroup) findViewById(R.id.phone_player_id);
		phonePlayerIdRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
	        public void onCheckedChanged(RadioGroup radioGroup, int i) {
	        	switch (i) {
		        	case R.id.blue:
		        		ChooseBoardActivity._configLocal.phonePlayerId = 0;
		        		break;
		        	case R.id.green:
		        		ChooseBoardActivity._configLocal.phonePlayerId = 1;
		        		break;
	        	}
	        }
	    });
		
		//add onclick event to button
		Button go = (Button) findViewById(R.id.go);
		go.setOnClickListener(new View.OnClickListener()
		  {
			  public void onClick(View v)
			  {
				  Activity ac = ChooseBoardActivity.this;
				  Intent i = new Intent(ac, HexActivity.class);
				  i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				  //update global config with the local config
				  ChooseBoardActivity.config = ChooseBoardActivity._configLocal;
				  ac.startActivity(i);
			  }
		  });
		
		//initialise slider
        bar = (SeekBar)findViewById(R.id.seekBar1); // make seekbar object
        bar.setOnSeekBarChangeListener(boardSizeSeekBarListener); // set seekbar listener.
        // since we are using this class as the listener the class is "this"        
        // make text label for progress value
        boardSizeTextProgress = (TextView)findViewById(R.id.textViewProgress);
        
        //set up seek bar for the opponent strength
        bar2 = (SeekBar)findViewById(R.id.seekBar2); // make seekbar object
        bar2.setOnSeekBarChangeListener(opponentStrengthSeekBarListener); // set seekbar listener.
        
        opponentStrengthTextProgress = (TextView)findViewById(R.id.textViewProgressOpponentStrength);
    }
    
    private BoardSizeSeekBarListener boardSizeSeekBarListener = new BoardSizeSeekBarListener();
    
    private class BoardSizeSeekBarListener implements OnSeekBarChangeListener {
    	//set onchanged activity for sliders
        public void onProgressChanged(SeekBar seekBar, int progress,
        		boolean fromUser) {
        	// change progress text label with current seekbar value
        	boardSizeTextProgress.setText(sizeBarLabels[progress]);
        	ChooseBoardActivity._configLocal.boardSize = progress;
        	chooseBoardView.postInvalidate();
        }

        //set method for when user stops dragging slider
        public void onStopTrackingTouch(SeekBar seekBar) {
        	seekBar.setSecondaryProgress(seekBar.getProgress()); // set the shade of the previous value. 	
        }

        //set method for when user starts dragging slider
        public void onStartTrackingTouch(SeekBar seekBar) {
        }	
    }
    
    private OpponentStrengthSeekBarListener opponentStrengthSeekBarListener = new OpponentStrengthSeekBarListener();
    
    private class OpponentStrengthSeekBarListener implements OnSeekBarChangeListener {
    	//set onchanged activity for sliders
        public void onProgressChanged(SeekBar seekBar, int progress,
        		boolean fromUser) {
        	// change progress text label with current seekbar value
        	opponentStrengthTextProgress.setText(opponentStrengthBarLabels[progress]);
        	ChooseBoardActivity._configLocal.opponentStrength = progress + 1;
        	chooseBoardView.postInvalidate();
        }

        //set method for when user stops dragging slider
        public void onStopTrackingTouch(SeekBar seekBar) {
        	seekBar.setSecondaryProgress(seekBar.getProgress()); // set the shade of the previous value. 	
        }

        //set method for when user starts dragging slider
        public void onStartTrackingTouch(SeekBar seekBar) {
        }	
    }
    
    //populate the form based on initial values passed to the activity
    private void populateForm()
    {
        RadioButton person = (RadioButton) findViewById(R.id.person);
        RadioButton phone = (RadioButton) findViewById(R.id.phone);
       
        //set game Mode radio button
        switch(ChooseBoardActivity._configLocal.gameMode)
        {
        	case 0:
        		person.performClick();
        		break;
        	case 1:
        		phone.performClick();
        		break;
        }

        RadioButton blue = (RadioButton) findViewById(R.id.blue);
        RadioButton green = (RadioButton) findViewById(R.id.green);
        
        //set phone ID radio button
        switch(ChooseBoardActivity._configLocal.phonePlayerId)
        {
        	case 0:
        		blue.performClick();
        		break;
        	case 1:
        		green.performClick();
        		break;
        }
        
        //set slider values
        
        //board size bar
        bar.setProgress(ChooseBoardActivity._configLocal.boardSize);
        
        //opponent strength bar
        bar2.setProgress(ChooseBoardActivity._configLocal.opponentStrength - 1);
    }

}
