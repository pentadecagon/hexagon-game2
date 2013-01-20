package com.hexagongame;

import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hexagongame.game.Board;
import com.hexagongame.game.Hexagon;
import com.hexagongame.game.Solver;
import com.hexagongame.game.Solver1;


public class UiView extends View{

	private Board board = null;
	Solver solver = new Solver1(4);
	
	private DrawBoardHelper drawBoardHelper;

	//the paint object used by the canvas
	private Paint paint;

	private long playerTurnToastStartTime = 0;
	
	public static final int BLUE_BG = android.graphics.Color.parseColor("#0000A0");
	public static final int GREEN_BG = android.graphics.Color.parseColor("#208020");
	
	int HEX_COLORS_HIGHLIGHT[] = {
			android.graphics.Color.parseColor("#00FFFF"), // Blue
			android.graphics.Color.parseColor("#FFF380")  // Green
	};
	
	public static final int HEX_UNUSED_COLOR = android.graphics.Color.parseColor("#E0E0E0");

	int HEX_COLORS[] = {
			android.graphics.Color.parseColor("#1010FF"), // Blue
			android.graphics.Color.parseColor("#00FF00"), // Green
			android.graphics.Color.parseColor("#E0E0E0") // empty
	};
	
	private boolean inWinnerMode = false;
	private int winnerModeTickCount = 0;
	private int winner = 0;
	
	private TextView winnerNotification = null;
	
	/**
	 * Game mode
	 * 0 = 2-player (default)
	 * 1 = play against phone
	 */
	public int gameMode = 0;
	
	/**
	 * Phone player ID
	 * only used if gameMode = 1 (play against phone)
	 * 0 = player goes first
	 * 1 = phone goes first
	 */
	public int phonePlayerId = 0;
	
	//when phone is calculating next move
	AtomicBoolean phoneIsThinking = new AtomicBoolean(false);
	
	//thread in which phone's next move is calculated in "play against phone" mode
	Thread phoneMoveThread = null;
	  
	
	public UiView(Context context, AttributeSet attrs) {
		
		super(context, attrs);

		paint = new Paint();
		board = new Board( ChooseBoardView.boardShape, ChooseBoardView.boardSize );
	}
			
	public void setWinnerNotification(TextView winnerNotification)
	{
		this.winnerNotification = winnerNotification;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		float canvasWidth = getWidth();
    	float canvasHeight = getHeight();
		drawBoardHelper = new DrawBoardHelper(canvasHeight, canvasWidth, board);
		//if we are in "play against phone" mode and the phone has to go first, calculate the phone's first move
		if (gameMode == 1 && phonePlayerId == 0)
		{
			doPhoneMove();
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {

		if (inWinnerMode && winnerModeTickCount == 0)
		{
			showWinnerCongratulationsMessage(canvas);
		}
		
		drawBackground(canvas);
	    
		//draw the bottom nav containing turn indicator & undo functionality
		drawBottomNav(canvas);

    	for( Hexagon hex : board.hexagonList ){
    		drawHexagon(canvas, hex);
    	}		

    	if (inWinnerMode)
		{
    		winnerModeTickCount ++;
    		countdownTimer.start();
		}
	}
	
	  /**
	   * Runnable with which the phone will calculate its next move (in "play against phone" mode) in a separate thread.
	   */
	  
	  private Runnable doPhoneMoveTask = new Runnable()
	  {

		  /**
		   * Calculate the phone's next move in "play against phone" mode.
		   * 
		   */
		  
		  public void run()
		  {
			  //set a flag showing that the phone is planning its next move
			  phoneIsThinking.set(true);
			  Hexagon move = solver.bestMove(board);
			  if( move != null ){
				  board.doMove( move );
				  if (board.isWinner(1-board.getPlayerId() ))
				  {
					  inWinnerMode = true;
					  winner = 1-board.getPlayerId();
				  }
			  }
			  //set a flag showing that the phone has finished planning its next move
			  phoneIsThinking.set(false);
			  //update the view
			  HexActivity ac = (HexActivity) UiView.this.getContext();
			  ac.runOnUiThread(new Runnable(){
				  public void run()
				  {
					  UiView.this.invalidate();
				  }
			  });
		  }
	  };
	
	private CountDownTimer countdownTimer = new CountDownTimer(250, 250){

        @Override
        public void onTick(long miliseconds){}

        @Override
        public void onFinish(){
        	invalidate();
        }
    };
	
	protected void showWinnerCongratulationsMessage(Canvas canvas)
	{
		//someone has won: congratulate the winner
		winnerNotification.setText(((winner == 1) ? "Green" : "Blue") + " wins!");
		winnerNotification.setVisibility(View.VISIBLE);		
	}
	
	void undo(){
		//if we're in winner mode, revert it
		if (inWinnerMode)
		{
			inWinnerMode = false;
		}
		for( int i=0; i<gameMode+1; ++i ){ // need to undo twice when playi8ng against the computer
			board.undo();
		}
		invalidate();
	}
	
	private void tappedOutsideBoard( MotionEvent event){
		//check if user has clicked on the nav
		float canvasWidth = getWidth();
		float canvasHeight = getHeight();
		float x = (float) event.getX();
		float y = (float) event.getY();
		if (y > 0.9f * canvasHeight)
		{
			if (x > 0.2 * canvasWidth && x < 0.3 * canvasWidth)
			{
				//turn indicator: if user taps on the circle, show a message showing whose turn it is next
				Context context = getContext();
				String turnMessage = "";
				if (board.getPlayerId() == 0)
				{
					turnMessage = "Blue's turn!";
				} else
				{
					turnMessage = "Green's turn!";
				}
				//make sure toast is not triggered multiple times
				if ((System.currentTimeMillis() - playerTurnToastStartTime) > 2000)
				{
    				Toast toast = Toast.makeText(context, turnMessage, Toast.LENGTH_SHORT);
    				playerTurnToastStartTime = System.currentTimeMillis();
    				toast.show();	    					
				}
					
			} else if (x >= 0.45 * canvasWidth && x <= 0.55 * canvasWidth)
			{
				//undo button clicked
				undo();
			} else if (x >= 0.7 * canvasWidth && x <= 0.8 * canvasWidth)
			{
				//redo button clicked
				doRedoButtonOnClick();
			}
		}
			
		//do nothing
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN ){
        	return false;
        }
        
        //if we are in "play against phone" mode and the phone is calculating its next move, board is deactivated
        if (phoneIsThinking.get())
        {
        	return false;
        }
        
        //if someone has already won and we are just showing the "congratulations" screen, just show a dialog to restart the game
        if (inWinnerMode)
        {
        	doWinnerModeOnTouch(event);
    		return true;
        }

		Log.d("hex", "ontouch x="+event.getX());
	    Log.d("hex", "ontouch y="+event.getY());
	    	
	    Hexagon hexagon = drawBoardHelper.findHexagonFromPointOnCanvas((float) event.getX(), (float) event.getY());
	    
		if (hexagon == null) //hexagon is out of scope of board
		{
			Log.d("hex", "hex is out of scope of board");
			tappedOutsideBoard(event);
		} else if (hexagon.isEmpty() ) //hexagon is on board, but unused
		{
			Log.d("hex", "hex is white");
			final int player = board.getPlayerId();
			board.doMove( hexagon );
			if (board.isWinner( player ))
			{
				inWinnerMode = true;
				winner = player;
				//update the view
				invalidate();
			} else if (gameMode == 1)
			{
				//if it's phone's turn, do phone's move
				doPhoneMove();
			} else {
				//update the view
				invalidate();
			}					
		}
		return true;
    };
    
    private void doPhoneMove()
    {
		//if it's phone's turn, do phone's move
		if (phoneMoveThread != null)
		{
			phoneMoveThread.interrupt();
			phoneMoveThread = null;
		}
		
		phoneMoveThread = new Thread(doPhoneMoveTask);
		phoneMoveThread.start();    	
    }
    
    private void doWinnerModeOnTouch(MotionEvent event)
    {
		float canvasWidth = getWidth();
		float canvasHeight = getHeight();
		float x = (float) event.getX();
		float y = (float) event.getY();
		if (x >= 0.45 * canvasWidth && x <= 0.55 * canvasWidth)
		{
			//undo button is still active in winner mode
			undo();
		} else if ((y > 0.9f * canvasHeight) && (x >= 0.7 * canvasWidth && x <= 0.8 * canvasWidth))
		{
    		//redo button is still active in winner mode
			doRedoButtonOnClick();
		} else
		{
			//do nothing: rest of screen is deactivated
		}
    }
    
    //handle a click on the redo button, which takes the user to the preferences screen
    private void doRedoButtonOnClick()
    {
		Log.d("hex", "redo button clicked");

		Activity ac = (Activity) getContext();
		Intent i = new Intent(ac, ChooseBoardActivity.class);
		i.putExtra(ChooseBoardActivity.ID_GAME_MODE, String.valueOf(gameMode));
		i.putExtra(ChooseBoardActivity.ID_PHONE_PLAYER_ID, String.valueOf(phonePlayerId));
		ac.startActivity(i);
		ac.finish();
    }
    
    private void drawBackground(Canvas canvas)
    {
    	if (board.boardShape == Board.BOARD_GEOMETRY_RECT)
		{
    		drawSquareBackground(canvas);
		} else
		{
			drawHexBackground(canvas);
		}
    }
	
    private void drawHexBackground(Canvas canvas)
    {
    	float canvasHeight = getHeight();
    	float canvasWidth = getWidth();

    	float padding = 0.01f * canvasHeight;
    	
    	paint.setColor(BLUE_BG);
    	paint.setStyle(Paint.Style.FILL);
    	Path path = new Path();
    	path.moveTo(0.0f, 0.1f * canvasHeight);
    	path.lineTo(0.4f * canvasWidth - padding, 0.1f * canvasHeight);
    	path.lineTo(0.6f * canvasWidth + padding, 0.9f * canvasHeight);
    	path.lineTo(canvasWidth, 0.9f * canvasHeight);
    	path.lineTo(canvasWidth, 0.5f * canvasHeight + padding);
    	path.lineTo(0.0f, 0.5f * canvasHeight - padding);
    	path.lineTo(0.0f, 0.1f * canvasHeight);
    	canvas.drawPath(path, paint);
    	
    	paint.setColor(GREEN_BG);
    	paint.setStyle(Paint.Style.FILL);
    	path = new Path();
    	path.moveTo(0.5f * canvasWidth + padding, 0.1f * canvasHeight);
    	path.lineTo(canvasWidth, 0.1f * canvasHeight);
    	path.lineTo(canvasWidth, 0.5f * canvasHeight - padding);
    	path.lineTo(0.0f, 0.5f * canvasHeight + padding);
    	path.lineTo(0.0f, 0.9f * canvasHeight);
    	path.lineTo(0.6f * canvasWidth - padding, 0.9f * canvasHeight);
    	path.lineTo(0.4f * canvasWidth + padding, 0.1f * canvasHeight);
    	canvas.drawPath(path, paint);
    }
    
    private void drawSquareBackground(Canvas canvas)
    {
    	float canvasHeight = getHeight();
    	float canvasWidth = getWidth();
	
    	paint.setColor(BLUE_BG);
    	paint.setStyle(Paint.Style.FILL);
    	Path path = new Path();
    	path.moveTo(0.0f, 0.23f * canvasHeight);
    	path.lineTo(0.0f, 0.77f * canvasHeight);
    	path.lineTo(canvasWidth, 0.23f * canvasHeight);
    	path.lineTo(canvasWidth, 0.77f * canvasHeight);
    	path.lineTo(0.0f, 0.23f * canvasHeight);
    	canvas.drawPath(path, paint);
    	
    	paint.setColor(GREEN_BG);
    	paint.setStyle(Paint.Style.FILL);
    	path = new Path();
    	path.moveTo(0.0f, 0.19f * canvasHeight);
    	path.lineTo(canvasWidth, 0.19f * canvasHeight);
    	path.lineTo(0.0f, 0.81f * canvasHeight);
    	path.lineTo(canvasWidth, 0.81f * canvasHeight);
    	path.lineTo(0.0f, 0.19f * canvasHeight);
    	canvas.drawPath(path, paint);
    }
    
	private void drawBottomNav(Canvas canvas)
	{
		drawTurnIndicator(canvas);
		drawUndoIcon(canvas);
		drawRefreshIcon(canvas);
	}
	
	private void drawTurnIndicator(Canvas canvas)
	{
		//indicate whose turn it is next
		float canvasWidth = getWidth();
		float canvasHeight = getHeight();
		
		paint.setColor( HEX_COLORS[board.getPlayerId()] );
    	paint.setStyle(Paint.Style.FILL);
    	
    	float cx = 0.25f * canvasWidth;
    	float cy = 0.95f * canvasHeight;
    	
		canvas.drawCircle(cx, cy, 0.15f * cx, paint);	
	}
	
	private void drawUndoIcon(Canvas canvas) {
		//indicate whose turn it is next
		float canvasWidth = getWidth();
		float canvasHeight = getHeight();

		Bitmap bmp;
		if ( board.haveHistory() )
		{
			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.undo);
		} else {
			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.undo_black);
		}
		
		float cx = 0.5f * canvasWidth - bmp.getWidth()/2.0f;
		float cy = 0.95f * canvasHeight - bmp.getHeight()/2.0f;

		canvas.drawBitmap(bmp, cx, cy, paint);
	}
	
	public void drawRefreshIcon(Canvas canvas)
	{
		//indicate whose turn it is next
		float canvasWidth = getWidth();
		float canvasHeight = getHeight();

		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.refresh);
	
		float cx = 0.75f * canvasWidth - bmp.getWidth()/2.0f;
		float cy = 0.95f * canvasHeight - bmp.getHeight()/2.0f;

		canvas.drawBitmap(bmp, cx, cy, paint);
	}
	
	
	public void drawHexagon(Canvas canvas, Hexagon hex)
	{
		float hexSide = drawBoardHelper.getSmallHexSideLength();
		
		// vx, vy represents the vector of the first edge of the hexagon
		float vx = hexSide * (float) Math.cos(Math.PI/6.0);
		float vy = - hexSide * (float) Math.sin(Math.PI/6.0);
		final float co = (float)Math.cos(Math.PI/3);
		final float si = (float)Math.sin(Math.PI/3);

		float x, y;
		Path path;
		
		final float[] coords = drawBoardHelper.findPositionOfCenterOfHexagonalCell(hex.xi, hex.yi);
		for (int j = 0; j < 2; j++)
		{
			if (j == 1)
			{
				paint.setColor(android.graphics.Color.BLACK);
		    	paint.setStrokeWidth(1);
		    	paint.setStyle(Paint.Style.STROKE);
			} else
			{
				int color;
				//if we are in "congratulations, winner" mode, every second tick we show the winner's rectangles in an alternative color
				if (inWinnerMode && winnerModeTickCount % 2 == 0
						&& winner == hex.owner )
				{
					color = HEX_COLORS_HIGHLIGHT[winner];
				} else
				{
					color = HEX_COLORS[hex.owner];
				}
				paint.setColor(color);
		        paint.setStyle(Paint.Style.FILL);
			}
			
			x = coords[0] - drawBoardHelper.getWCell()/2.0f;
			y = coords[1] - drawBoardHelper.getHCell()/2.0f;

			path = new Path();
			
			path.moveTo( x,  y );
			for( int i=0; i<6; ++i ){
				x += vx;
				y += vy;
				path.lineTo( x, y );
				// now rotate the edge vector by Pi/3
				float vx_temp = co * vx - si * vy;
				vy = si * vx + co * vy;
				vx=vx_temp;
			}
			
			canvas.drawPath(path, paint);
		}
        paint.setStyle(Paint.Style.FILL);
		canvas.drawText( ""+hex.id, coords[0], coords[1], paint);
	}
}
