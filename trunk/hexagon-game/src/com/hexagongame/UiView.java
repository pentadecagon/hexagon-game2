package com.hexagongame;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hexagongame.game.Board;
import com.hexagongame.game.Hexagon;
import com.hexagongame.game.Solver;
import com.hexagongame.game.Solver6;


public class UiView extends View{

	private Board board = null;

/* board2 is a "shadow", a copy of the normal board, except it's used by the solver to try different positions. 
 * The Solver cannot use the normal "board" here, because then we would see on the display
 * whatever the solver is considering at the moment */
	
	private Board board2 = null;
	Solver solver;
	
	private DrawBoardHelper drawBoardHelper;

	//the paint object used by the canvas
	private Paint paint;

	private long playerTurnToastStartTime = 0;

	int HEX_COLORS[] = {
			android.graphics.Color.parseColor("#1010FF"), // Blue
			android.graphics.Color.parseColor("#00FF00"), // Green
	};
		
	private boolean inWinnerMode = false;
	private int winnerModeTickCount = 0;
	private int winner = 0;

	private LinearLayout phoneThinkingNotification = null;
	
	private ImageView [] turnImageViews = null;
	
	private TranslateAnimation slide;
	
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
	
	//thread in which phone's next move is calculated in "play against phone" mode
	Thread phoneMoveThread = null;
	
	private Bitmap[] TILES = new Bitmap[3];
	
	private Bitmap[] TILES_HIGHLIGHT = new Bitmap[2];
	
	//images used in the bottom navigation
	private Bitmap undoImageWhite, undoImageBlack, refreshImage; 
	
	 //for highlighting the currently touched hexagon: -1 means no hexagon is touched
	public int hexSelected = -1;
	
	//filter for highlighting a hexagon
	ColorFilter[] highlightFilters = {
			new LightingColorFilter(android.graphics.Color.parseColor("#ff8000"), 1),
			new LightingColorFilter(android.graphics.Color.parseColor("#ffC000"), 1)
	};
	
	String[] playerNames = {
		"Red",
		"Yellow"
	};
	
	//point, as a fraction of the height, where the bottom nav starts
	private static final float bottomNavTop = 0.844f;
	
	//point, as a fraction of the height, where the bottom nav midpoint is
	private static final float bottomNavMidPoint = 0.922f;

	public UiView(Context context, AttributeSet attrs) {
		
		super(context, attrs);

		paint = new Paint();
		board = new Board( ChooseBoardView.boardShape, ChooseBoardView.boardSize );
		board2 = new Board( ChooseBoardView.boardShape, ChooseBoardView.boardSize );
		Log.d("hex", "setting AI strength to "+ChooseBoardView.opponentStrength);
		solver = new Solver6(4.0, ChooseBoardView.opponentStrength);		
	}
	
	protected void setPhoneThinkingNotification(LinearLayout phoneThinkingNotification)
	{
		this.phoneThinkingNotification = phoneThinkingNotification;
	}
	
	protected void setTurnImageViews(ImageView [] turnImageViews)
	{
		this.turnImageViews = turnImageViews;
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
		
		//initialize the images used for the individual tiles on the board
		initializeTileImages();

		//initialize the images used for the "x's turn next" icon
		initializeTurnIndicatorImages();
		
		//initialize images used in the bottom nav
		initializeBottomNavImages();
	}
	
	//initialize the images used for the individual tiles on the board
	private void initializeTileImages()
	{
		TILES[0] = initializeTileImage(R.drawable.blue_tile);
		TILES[1] = initializeTileImage(R.drawable.green_tile);
		TILES[2] = initializeTileImage(R.drawable.unused_tile);

		TILES_HIGHLIGHT[0] = initializeTileImage(R.drawable.blue_tile_highlight);
		TILES_HIGHLIGHT[1] = initializeTileImage(R.drawable.green_tile_highlight);
			
	}
	
	  /**
	   * Create and resize the tile image.
	   */
	
	private Bitmap initializeTileImage(int id)
	{
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), id);
		Matrix matrix = new Matrix();
		float scale = (drawBoardHelper.getWCell() + 1)/ bmp.getWidth();
		matrix.postScale(scale, scale);
		Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
		return resizedBitmap;
	}
	
	//initialize the images used for the "x's turn next" icon
	private void initializeTurnIndicatorImages()
	{
		for (int i = 0; i <2; i++)
		{
			turnImageViews[i].setImageBitmap(TILES[i]);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)turnImageViews[i].getLayoutParams();
			params.setMargins((int) (0.2f * getWidth()), 0, 0, (int) ((1.0f - bottomNavMidPoint) * getHeight() - TILES[i].getHeight()/2.0));
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			turnImageViews[i].setLayoutParams(params); //causes layout update
		}
	}
	
	//initialize images used in the bottom nav
	private void initializeBottomNavImages()
	{
		undoImageWhite = BitmapFactory.decodeResource(getResources(), R.drawable.undo);
		undoImageBlack = BitmapFactory.decodeResource(getResources(), R.drawable.undo_black);
		refreshImage = BitmapFactory.decodeResource(getResources(), R.drawable.refresh);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {

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
			  HexActivity ac = (HexActivity) UiView.this.getContext();
			  //show "phone is thinking" message
			  ac.runOnUiThread(new Runnable(){ public void run() {
				  phoneThinkingNotification.setVisibility(View.VISIBLE);
			  }});
			  
			  final Hexagon move = solver.bestMove(board2);
			  
			  //highlight the hexagon chosen
			  hexSelected = move.xid;
			  postInvalidate();
			  
			  //hide "phone is thinking" message
			  ac.runOnUiThread(new Runnable(){ public void run() {
				  phoneThinkingNotification.setVisibility(View.GONE);
				  //animate the tile at the bottom of the page moving to its final position (the hexagon chosen)
				  animateNextTurnTileToFinalHexagonPosition(move, phoneMoveAnimationListener);
			  }});
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
	
	void undo(){
		//if we're in winner mode, revert it
		if (inWinnerMode)
		{
			inWinnerMode = false;	
		}
		for( int i=0; i<gameMode+1; ++i ){ // need to undo twice when playing against the computer
			board.undo();
			board2.undo();
		}
		invalidate();
	}
	
	private void tappedOutsideBoard( MotionEvent event){
		//check if user has clicked on the nav
		float canvasWidth = getWidth();
		float canvasHeight = getHeight();
		float x = (float) event.getX();
		float y = (float) event.getY();
		//if the user has clicked in the bottom navigation
		if (y > bottomNavTop * canvasHeight)
		{
			if (!inWinnerMode && tappedOnTurnIndicatorImage(canvasWidth, canvasHeight, x, y))
			{
				//turn indicator: if user taps on the circle, show a message showing whose turn it is next
				Context context = getContext();
				String turnMessage = playerNames[board.getPlayerId()] + "'s turn! Pick a hexagon.";
				
				//make sure toast is not triggered multiple times
				if ((System.currentTimeMillis() - playerTurnToastStartTime) > 2000)
				{
    				Toast toast = Toast.makeText(context, turnMessage, Toast.LENGTH_SHORT);
    				playerTurnToastStartTime = System.currentTimeMillis();
    				toast.show();	    					
				}
					
			} else if (tappedOnTurnUndoImage(canvasWidth, canvasHeight, x, y))
			{
				//undo button clicked
				undo();
			} else if (tappedOnRefreshImage(canvasWidth, canvasHeight, x, y))
			{
				//restart button clicked
				doRestartButtonOnClick();
			}
		}
			
		//do nothing
	}
	
	//if the user has tapped on the "whose turn is it next" indicator
	private boolean tappedOnTurnIndicatorImage(float canvasWidth, float canvasHeight, float x, float y)
	{
		return (x > 0.2 * canvasWidth
				&& x < 0.3 * canvasWidth
				&& y > (bottomNavMidPoint * canvasHeight - TILES[0].getHeight()/2.0)
				&& y < (bottomNavMidPoint * canvasHeight + TILES[0].getHeight()/2.0));
	}
	
	//if the user has tapped on the undo image
	private boolean tappedOnTurnUndoImage(float canvasWidth, float canvasHeight, float x, float y)
	{
		return (x >= 0.45 * canvasWidth
				&& x <= 0.55 * canvasWidth
				&& y > (bottomNavMidPoint * canvasHeight - undoImageWhite.getHeight()/2.0)
				&& y < (bottomNavMidPoint * canvasHeight + undoImageWhite.getHeight()/2.0));
	}
	
	//if the user has tapped on the refresh image
	private boolean tappedOnRefreshImage(float canvasWidth, float canvasHeight, float x, float y)
	{
		return (x >= 0.7 * canvasWidth
				&& x <= 0.8 * canvasWidth
				&& y > (bottomNavMidPoint * canvasHeight - refreshImage.getHeight()/2.0)
				&& y < (bottomNavMidPoint * canvasHeight + refreshImage.getHeight()/2.0));
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent event) {
        //if we are in "play against phone" mode and the phone is calculating its next move, board is deactivated
        if ( phoneMoveThread != null )
        {
        	return false;
        }
        	    	
		Log.d("hex", "ontouch x="+event.getX());
	    Log.d("hex", "ontouch y="+event.getY());
	    	
	    Hexagon hexagon = drawBoardHelper.findHexagonFromPointOnCanvas((float) event.getX(), (float) event.getY());
	    
	    //handle different actions
	    switch (event.getAction())
	    {
	    	case MotionEvent.ACTION_DOWN:
	    		//just highlight the hexagon the user has touched if appropriate
	    		hexSelected = (hexagon == null || inWinnerMode) ? -1 : hexagon.xid; //for highlighting the currently touched hexagon
	    		invalidate();
	    		break;
	    	case MotionEvent.ACTION_MOVE:
	    		//if user moves to touch a different hexagon, highlight this one    		
	    		int hexSelectedNew = (hexagon == null || inWinnerMode) ? -1 : hexagon.xid;
	    		if (hexSelectedNew != hexSelected) //for highlighting the currently touched hexagon
	    		{	    	
	    			Log.d("hex", "onTouch ACTION_MOVE: hex id = "+hexSelectedNew);
	    			hexSelected = hexSelectedNew;
	    			invalidate();
	    		}
	    		break;
	    	case MotionEvent.ACTION_UP:
	    		//respond to user's touch on the screen
	    		if (hexagon == null) //hexagon is out of scope of board
	    		{
	    			Log.d("hex", "hex is out of scope of board");
	    			hexSelected = -1; //for highlighting the currently touched hexagon: : -1 means no hexagon is touched
	    			tappedOutsideBoard(event);
	    		} else if (hexagon.isEmpty() && ! inWinnerMode ) //hexagon is on board, but unused
	    		{
	    			Log.d("hex", "hex is white");

	    			
	    			//animate the tile at the bottom of the page moving to its final position (the hexagon chosen)
	    			animateNextTurnTileToFinalHexagonPosition(hexagon, playerMoveAnimationListener);
	    		}
	    		break;
	    	default:
	    		return false;
	    }
	    
		return true;
    };
    
    private void animateNextTurnTileToFinalHexagonPosition(Hexagon hexagon, AnimationListener listener)
    {
		//animate the tile at the bottom of the page moving to its final position (the hexagon chosen)
		float[] coords = drawBoardHelper.findPositionOfCenterOfHexagonalCell(hexagon.xi, hexagon.yi);
    	
    	RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) turnImageViews[0].getLayoutParams();
    	Bitmap turnIndicatorBitmap = ((BitmapDrawable)turnImageViews[0].getDrawable()).getBitmap();
    	
		float xDisplacement = (int) (coords[0] - params.leftMargin - turnIndicatorBitmap.getWidth()/2.0);
		float yDisplacement = (int) (coords[1] - (getHeight() - params.bottomMargin - turnIndicatorBitmap.getHeight() + turnIndicatorBitmap.getHeight()/2.0));

		slide = new HexagonAnimation(0, xDisplacement, 0, yDisplacement, hexagon);
		slide.setAnimationListener(listener);
		slide.setDuration(500);
		turnImageViews[board.getPlayerId()].startAnimation(slide);

    }
    
    private class HexagonAnimation extends TranslateAnimation {
    	
    	public Hexagon hexagon;
    	
    	public HexagonAnimation(float x1, float x2, float y1, float y2, Hexagon hexagon)
    	{
    		super(x1, x2, y1, y2);
    		this.hexagon = hexagon;
    	}
    }
    
    private AnimationListener playerMoveAnimationListener = new AnimationListener() {
		public void onAnimationStart(Animation anim){};
        public void onAnimationRepeat(Animation anim){};
        public void onAnimationEnd(Animation anim)
        {
        	HexagonAnimation hexAnim = (HexagonAnimation) anim; 
			final int player = board.getPlayerId();
			board2.doMove( hexAnim.hexagon );
			if( board.doMove( hexAnim.hexagon ) )
			{
				inWinnerMode = true;
				winner = player;
			}
			
			hexSelected = -1; //for highlighting the currently touched hexagon: : -1 means no hexagon is touched
        	invalidate();
        	
        	if (!inWinnerMode && gameMode == 1)
        	{
				//if it's phone's turn, do phone's move
				doPhoneMove();
        	}
        };
	};
	
	private AnimationListener phoneMoveAnimationListener = new AnimationListener() {
		public void onAnimationStart(Animation anim){};
        public void onAnimationRepeat(Animation anim){};
        public void onAnimationEnd(Animation anim)
        {
        	HexagonAnimation hexAnim = (HexagonAnimation) anim;
        	Hexagon move = hexAnim.hexagon;
			if( move != null ){
				board2.doMove(move);
				if( board.doMove( move ) ){
					inWinnerMode = true;
					winner = board.getPlayerId();
				}
				  
			} else {
				Log.e("hex", "move is null");
			}

			hexSelected = -1; //for highlighting the currently touched hexagon: : -1 means no hexagon is touched
			
			//update the view
			postInvalidate();
			phoneMoveThread = null;
        };
	};
    
    private void doPhoneMove()
    {
		//if it's phone's turn, do phone's move
		if (phoneMoveThread != null)
		{
			Log.e("hex", "phone move is running unexpectedly");
			phoneMoveThread.interrupt();
			phoneMoveThread = null;
		}
		
		phoneMoveThread = new Thread(doPhoneMoveTask);
		phoneMoveThread.start();    	
    }
    
    //handle a click on the restart button, which takes the user to the preferences screen
    private void doRestartButtonOnClick()
    {
		Log.d("hex", "restart button clicked");

		//as we are in "singleInstance" mode, this will go to the existing instance of the "ChooseBoardActivity" mode,
		//so we don't have to tell it the preferences: it will remember the existing ones
		Activity ac = (Activity) getContext();
		Intent i = new Intent(ac, ChooseBoardActivity.class);
		ac.startActivity(i);
    }

	private void drawBottomNav(Canvas canvas)
	{
		drawTurnIndicator(canvas);
		drawUndoIcon(canvas);
		drawRestartIcon(canvas);
	}
	
	private void drawTurnIndicator(Canvas canvas)
	{
		//indicate whose turn it is next
		turnImageViews[((board.getPlayerId() == 1) ? 0 : 1)].setVisibility(View.GONE);
		if (inWinnerMode)
		{
			turnImageViews[board.getPlayerId()].setVisibility(View.GONE);
		} else
		{
			turnImageViews[board.getPlayerId()].setVisibility(View.VISIBLE);
		}
	}
	
	private void drawUndoIcon(Canvas canvas) {
		//indicate whose turn it is next
		float canvasWidth = getWidth();
		float canvasHeight = getHeight();

		Bitmap bmp;
		if ( board.haveHistory() )
		{
			bmp = undoImageWhite;
		} else {
			bmp = undoImageBlack;
		}
		
		float cx = 0.5f * canvasWidth - bmp.getWidth()/2.0f;
		float cy = bottomNavMidPoint * canvasHeight - bmp.getHeight()/2.0f;

		canvas.drawBitmap(bmp, cx, cy, paint);
	}
	
	public void drawRestartIcon(Canvas canvas)
	{
		//indicate whose turn it is next
		float canvasWidth = getWidth();
		float canvasHeight = getHeight();

		Bitmap bmp = refreshImage;
	
		float cx = 0.75f * canvasWidth - bmp.getWidth()/2.0f;
		float cy = bottomNavMidPoint * canvasHeight - bmp.getHeight()/2.0f;

		canvas.drawBitmap(bmp, cx, cy, paint);
	}
	
	
	public void drawHexagon(Canvas canvas, Hexagon hex)
	{
		float hexSide = drawBoardHelper.getSmallHexSideLength();

		final float[] coords = drawBoardHelper.findPositionOfCenterOfHexagonalCell(hex.xi, hex.yi);
		Bitmap bmp;
		if (inWinnerMode && winnerModeTickCount % 2 == 0
				&& winner == hex.owner )
		{
			bmp = TILES_HIGHLIGHT[winner];
		} else
		{
			bmp = TILES[hex.owner];
		}
		
		//if this hexagon is currently highlighted, add an effect
		if (hex.xid == hexSelected && hex.isEmpty())
		{
			paint.setColorFilter(highlightFilters[board.getPlayerId()]);
		}
		
		canvas.drawBitmap(bmp, coords[0] - drawBoardHelper.getWCell()/2.0f ,coords[1] - hexSide, paint);
			
    	//cancel the color filter that was used for drawing the highlighted hexagon
		if (paint.getColorFilter() != null)
		{
			paint.setColorFilter(null);
		}
//		paint.setStrokeWidth(1);
//		canvas.drawText( ""+hex.xid, coords[0], coords[1], paint);
	}
}
