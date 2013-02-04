package com.hexagongame;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class HexActivity extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
          requestWindowFeature(Window.FEATURE_NO_TITLE);
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

		//add a view for the "phone is thinking" message (for "play-against-phone" mode), which will be hidden initially
		LinearLayout phoneThinkingNotification = (LinearLayout) findViewById(R.id.phonethinkingtext);
		uiView.setPhoneThinkingNotification(phoneThinkingNotification);
		
		setBackground(uiView);
		
		ImageView [] imageViews = {(ImageView) findViewById(R.id.tileimage0), (ImageView) findViewById(R.id.tileimage1)};
		uiView.setTurnImageViews(imageViews);
    }
    
    private void setBackground(UiView uiView)
    {
		if (ChooseBoardView.boardShape == 1)
		{
			uiView.setBackgroundResource(R.drawable.square_background);
		} else
		{
			switch (ChooseBoardView.boardSize)
			{
				case 0:
					uiView.setBackgroundResource(R.drawable.hex_background_1);
					break;
				case 1:
					uiView.setBackgroundResource(R.drawable.hex_background_2);
					break;
				case 2:
					uiView.setBackgroundResource(R.drawable.hex_background_3);
					break;
			}			
		}	
    }
}