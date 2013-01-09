package com.hexagongame;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public class HexActivity extends Activity {

	//for pause/ resume facility
	private AtomicBoolean isActive = new AtomicBoolean(true);
	
	private boolean changesDetected = true;
	
	private UiView uiView;
	
	private ArrayList<Hexagon> history = new ArrayList<Hexagon>();
	
	private Board board = null;

	private int playerTurn;
	private boolean inWinnerMode;
	private int winnerModeTickCount;
	private int winner;
	private int gameMode;
	private int phonePlayerId;
	private int boardShape;
	private long playerTurnToastStartTime;
	  
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    	String gameModeStr = getIntent().getStringExtra(ChooseBoardActivity.ID_GAME_MODE);
        gameMode = (gameModeStr != null) ? Integer.valueOf(gameModeStr) : 0;
        
        String phonePlayerIdStr = getIntent().getStringExtra(ChooseBoardActivity.ID_PHONE_PLAYER_ID);
        phonePlayerId = (phonePlayerIdStr != null) ? Integer.valueOf(phonePlayerIdStr) : 0;
        
        String boardShapeStr = getIntent().getStringExtra(ChooseBoardActivity.ID_BOARD_VIEW);
        boardShape = (boardShapeStr != null) ? Integer.valueOf(boardShapeStr) : 0;
    }
    
    private void initializeLayout()
    {
    	setContentView(R.layout.main);	

        uiView = (UiView) findViewById(R.id.boardview);

        TextView winnerNotification = (TextView) findViewById(R.id.winnertext);
		uiView.setWinnerNotification(winnerNotification);
    }
    

    /**
     * 
     * Method called when the activity is paused.
     * 
     * Stop recording and re-initialize so the user starts from the beginning next time.
     * 
     */
    
    @Override
    public void onPause()
    {
  	  Log.v("hex", "called onPause");
  	  isActive.set(false);
  	  
  	  super.onPause();
    }
    
    /**
     * 
     * Method called when the activity is restarted.
     * 
     * Re-initialize so the user starts from the beginning.
     * 
     */
    
    @Override
    public void onResume()
    {
      Log.v("hex", "called onResume");
      isActive.set(false);
  	  
  	  super.onResume();
  	  
  	  initializeLayout();
  	  startWork();
    }
    
    /**
     * Start the work of showing the hex game
     */

    private void startWork()
    {
    	board = null;
    	
    	playerTurn = 0;
    	inWinnerMode = false;
    	winnerModeTickCount = 0;
    	winner = 0;
    	playerTurnToastStartTime = 0;
    	history = new ArrayList<Hexagon>();

		setupBoard();

		//show introductory message
		Toast toast = Toast.makeText(this, "First player to make a path from one side to the other wins. Blue goes first.", Toast.LENGTH_SHORT);
		toast.show();
		
		uiView.setBoard(board);
		uiView.updateParams(playerTurn, inWinnerMode, winner, winnerModeTickCount, history.size());
    	
    	//start the thread
    	isActive.set(true);
    	new Thread(runHexTask).start();
    }
    
	private void setupBoard()
	{
		board = new Board(boardShape);
	}
    
    /**
     * Thread on which the work of displaying data on screen and reacting to user interaction will be done.
     */
    
    private Runnable runHexTask = new Runnable()
    {    	
  	  /**
  	   * Do the actual work.
  	   *
  	   * Loop over the number of iterations defined by the variable max and, on each iteration, analyze the interaction
  	   * of the user and update the data displayed on screen.
  	   * 
  	   */
  	  
  	  public void run()
  	  {
  		  //on each iteration process user interaction and draw the view
  		  while (isActive.get())
  		  {	  
  			  //on each iteration, process user interaction and draw the view
  			  doHex();
  		  }
  	  }
    };
    
    /**
     * 
     * Method to be called on each iteration of the run method.
     * 
     * Read in data from the user and display the results in graphic form on screen. 
     */

    private void doHex()
    {
		//TODO: implement complete play-against-phone functionality
		//here, the player is playing against the phone and the phone is blue (goes first), so we just
		//select the first available hexagon
		if (gameMode == 1)
		{
			if (phonePlayerId == playerTurn ) 
			{
				final int newcolor = phonePlayerId == 1 ? UiView.GREEN : UiView.BLUE;
				Hexagon move = board.analyzeAll();
				if( move != null ){
					move.color = newcolor;
					playerTurn = 1 - playerTurn;
					changesDetected = true;
					history.add( move );
					if (board.isWinner(1-playerTurn ))
					{
						inWinnerMode = true;
						winner = 1-playerTurn;
					}
				}
			}
		}
		
    	//TODO: detect if something has changed  
    	if (changesDetected || inWinnerMode)
    	{
	    	//if something has changed, update the view
	    	runOnUiThread(new Runnable(){
	    		public void run()
				{
	    			uiView.updateParams(playerTurn, inWinnerMode, winner, winnerModeTickCount, history.size());
	    			uiView.postInvalidate();
				}
			});

		    changesDetected = false;
		    if (inWinnerMode)
			{
		    	winnerModeTickCount = (winnerModeTickCount == 1) ? 0 : 1;
		    	try{ Thread.sleep(500); }catch(InterruptedException e){ }
			}
    	}
    }
    
    public boolean handleOnTouch(float x, float y) {
        
        //if someone has already won and we are just showing the "congratulations" screen, just show a dialog to restart the game
        if (inWinnerMode)
        {
        	showRestartDialog();
    		return true;
        }
        
        //only allow one change at a time
        if (changesDetected)
        {
        	return false;
        }

		Log.d("hex", "ontouch x="+x);
	    Log.d("hex", "ontouch y="+y);
	    	
	    Hexagon hexagon = uiView.drawBoardHelper.findHexagonFromPointOnCanvas(x, y);
	    
		if (hexagon == null) //hexagon is out of scope of board
		{
			Log.d("hex", "hex is out of scope of board");
			tappedOutsideBoard(x, y);
		} else if (hexagon.color == UiView.HEX_UNUSED_COLOR) //hexagon is on board, but unused
		{
			changesDetected = true;
			Log.d("hex", "hex is white");
			if (playerTurn == 0)
			{
				hexagon.color = UiView.BLUE;
				playerTurn = 1;
			} else
			{
				hexagon.color = UiView.GREEN;
				playerTurn = 0;
			}
			if (board.isWinner(1-playerTurn ))
			{
				inWinnerMode = true;
				winner = 1-playerTurn;
			}
			//save last change in case we need to undo it
			history.add( hexagon );			
		}
		return true;
    };
    
	private void tappedOutsideBoard(float x, float y){

		if (y > 0.9f * uiView.drawBoardHelper.getCanvasHeight())
		{
			if (x > 0.2 * uiView.drawBoardHelper.getCanvasWidth() && x < 0.3 * uiView.drawBoardHelper.getCanvasWidth())
			{
				//turn indicator: if user taps on the circle, show a message showing whose turn it is next
				String turnMessage = "";
				if (playerTurn == 0)
				{
					turnMessage = "Blue's turn!";
				} else
				{
					turnMessage = "Green's turn!";
				}
				//make sure toast is not triggered multiple times
				if ((System.currentTimeMillis() - playerTurnToastStartTime) > 2000)
				{
    				Toast toast = Toast.makeText(this, turnMessage, Toast.LENGTH_SHORT);
    				playerTurnToastStartTime = System.currentTimeMillis();
    				toast.show();	    					
				}
					
			} else if (x >= 0.45 * uiView.drawBoardHelper.getCanvasWidth() && x <= 0.55 * uiView.drawBoardHelper.getCanvasWidth())
			{
				Log.d("hex", "undo button clicked");
				undo();
			} else if (x >= 0.7 * uiView.drawBoardHelper.getCanvasWidth() && x <= 0.8 * uiView.drawBoardHelper.getCanvasWidth())
			{
				Log.d("hex", "redo button clicked");

				Activity ac = HexActivity.this;
				ac.startActivity(new Intent(ac, ChooseBoardActivity.class));
				ac.finish();
			}
		}
			
		//do nothing
	}

    private void showRestartDialog()
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	TextView myMsg = new TextView(this);
    	myMsg.setText("Restart?");
    	myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
    	myMsg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25.0f);
    	myMsg.setPadding(15, 15, 15, 15);
		builder.setView(myMsg)
	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	    	   public void onClick(DialogInterface dialog, int id) {
	    		   //restart the game
	    		   Activity ac = HexActivity.this;
				   Intent i = new Intent(ac, HexActivity.class);
				   i.putExtra(ChooseBoardActivity.ID_GAME_MODE, String.valueOf(gameMode));
				   i.putExtra(ChooseBoardActivity.ID_PHONE_PLAYER_ID, String.valueOf(phonePlayerId));
				   i.putExtra(ChooseBoardActivity.ID_BOARD_VIEW, String.valueOf(boardShape));
				   ac.startActivity(i);
				   ac.finish();
	    	   } 	    	
	       })
	       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialog, int id) {}
	       });
		
		AlertDialog dialog = builder.create();
		dialog.show();
    }
	
	void undo(){
		for( int i=0; i<gameMode+1; ++i ){ // need to undo twice when playi8ng against the computer
			if ( history.size() > 0 )
			{
				Hexagon lastChange = history.remove(history.size()-1);
				lastChange.color = UiView.HEX_UNUSED_COLOR;
				playerTurn = 1-playerTurn;
			}
		}
		changesDetected = true;
	}
	
}