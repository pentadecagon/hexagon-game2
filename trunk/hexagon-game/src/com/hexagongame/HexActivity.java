package com.hexagongame;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HexActivity extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	  initializeLayout();
    }
    
    private void initializeLayout()
    {
    	setContentView(R.layout.main);	

    	String gameModeStr = getIntent().getStringExtra(ChooseBoardActivity.ID_GAME_MODE);
        int gameMode = (gameModeStr != null) ? Integer.valueOf(gameModeStr) : 0;
        
        String phonePlayerIdStr = getIntent().getStringExtra(ChooseBoardActivity.ID_PHONE_PLAYER_ID);
        int phonePlayerId = (phonePlayerIdStr != null) ? Integer.valueOf(phonePlayerIdStr) : 0;
        
        UiView uiView = (UiView) findViewById(R.id.boardview);

        uiView.gameMode = gameMode;
        uiView.phonePlayerId = phonePlayerId;
        
        //add a text view for the "congratulations, you have won" message, which will be hidden initially
        TextView winnerNotification = (TextView) findViewById(R.id.winnertext);
		uiView.setWinnerNotification(winnerNotification);
		
		//add a view for the "phone is thinking" message (for "play-against-phone" mode), which will be hidden initially
		LinearLayout phoneThinkingNotification = (LinearLayout) findViewById(R.id.phonethinkingtext);
		uiView.setPhoneThinkingNotification(phoneThinkingNotification);
		
		if (ChooseBoardView.boardShape == 1)
		{
			uiView.setBackgroundResource(R.drawable.square_background);
		} else
		{
			uiView.setBackgroundResource(R.drawable.hex_background);
		}
    }
}