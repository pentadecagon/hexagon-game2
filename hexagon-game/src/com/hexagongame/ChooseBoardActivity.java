package com.hexagongame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ChooseBoardActivity extends Activity implements OnSeekBarChangeListener {
	
	private int gameMode = 0;
	
	private int phonePlayerId = 0;

	public final static String ID_GAME_MODE = "com.hexagongame._ID_GAME_MODE";
	
	public final static String ID_PHONE_PLAYER_ID = "com.hexagongame._ID_PHONE_PLAYER_ID";
	
	private SeekBar bar; // declare seekbar object variable
	
	// declare text label objects
	private TextView textProgress;
	
	//display the labels for the bar that the user can move to change the board size
	private String[] sizeBarLabels = {"1", "2", "3"};
	
	private ChooseBoardView chooseBoardView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initializeLayout();
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
		
		//add onclick event to button
		Button go = (Button) findViewById(R.id.go);
		go.setOnClickListener(new View.OnClickListener()
		  {
			  public void onClick(View v)
			  {
				  Activity ac = ChooseBoardActivity.this;
				  Intent i = new Intent(ac, HexActivity.class);
				  i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				  i.putExtra(ID_GAME_MODE, String.valueOf(gameMode));
				  i.putExtra(ID_PHONE_PLAYER_ID, String.valueOf(phonePlayerId));
				  ac.startActivity(i);
			  }
		  });
		
		//initialise slider
        bar = (SeekBar)findViewById(R.id.seekBar1); // make seekbar object
        bar.setOnSeekBarChangeListener(this); // set seekbar listener.
        // since we are using this class as the listener the class is "this"        
        // make text label for progress value
        textProgress = (TextView)findViewById(R.id.textViewProgress);

		//populate the form based on initial values passed to the activity
		populateForm();
    }
    
    //set onchanged activity for sliders
    public void onProgressChanged(SeekBar seekBar, int progress,
    		boolean fromUser) {
    	// change progress text label with current seekbar value
    	textProgress.setText(sizeBarLabels[progress]);
    	ChooseBoardView.boardSize = progress;
    	chooseBoardView.postInvalidate();
    }

    //set method for when user stops dragging slider
    public void onStopTrackingTouch(SeekBar seekBar) {
    	seekBar.setSecondaryProgress(seekBar.getProgress()); // set the shade of the previous value. 	
    }

    //set method for when user starts dragging slider
    public void onStartTrackingTouch(SeekBar seekBar) {
    }
    
    //populate the form based on initial values passed to the activity
    private void populateForm()
    {
        RadioButton person = (RadioButton) findViewById(R.id.person);
        RadioButton phone = (RadioButton) findViewById(R.id.phone);
       
        //set game Mode radio button
        switch(gameMode)
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
        switch(phonePlayerId)
        {
        	case 0:
        		blue.performClick();
        		break;
        	case 1:
        		green.performClick();
        		break;
        }
        
        //set slider value
        bar.setProgress(ChooseBoardView.boardSize);
    }

}
