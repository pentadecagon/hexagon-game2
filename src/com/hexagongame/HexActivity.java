package com.hexagongame;

import android.app.Activity;
import android.os.Bundle;
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
        
        String boardShapeStr = getIntent().getStringExtra(ChooseBoardActivity.ID_BOARD_VIEW);
        int boardShape = (boardShapeStr != null) ? Integer.valueOf(boardShapeStr) : 0;
        
        UiView uiView = (UiView) findViewById(R.id.boardview);

        uiView.gameMode = gameMode;
        uiView.phonePlayerId = phonePlayerId;
        uiView.boardShape = boardShape;
        
        TextView winnerNotification = (TextView) findViewById(R.id.winnertext);
		uiView.setWinnerNotification(winnerNotification);
    }
}