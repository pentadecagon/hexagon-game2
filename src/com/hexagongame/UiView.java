package com.hexagongame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class UiView extends View{

	private Board board = null;

	//the paint object used by the canvas
	private Paint paint;

	private int playerTurn = 0;
	
	public int boardShape = Board.BOARD_GEOMETRY_HEX;
	
	private Hexagon lastChange = null;

	private long playerTurnToastStartTime = 0;
	
	public static final int BLUE = android.graphics.Color.parseColor("#00DDFF");
	
	public static final int GREEN = android.graphics.Color.parseColor("#00FF00");
	
	public static final int HEX_UNUSED_COLOR = android.graphics.Color.parseColor("#E0E0E0");
	
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
	
	public UiView(Context context, AttributeSet attrs) {
		
		super(context, attrs);

		paint = new Paint();

		viewInit();
	}
	
	public void viewInit()
	{
		board = null;
		playerTurn = 0;
		lastChange = null;	

		//show introductory message
    	Context context = getContext();
		Toast toast = Toast.makeText(context, "First player to make a path from one side to the other wins. Blue goes first.", Toast.LENGTH_SHORT);
		toast.show();
	}
	
	private void setupBoard()
	{
		//note: we have to call this method from within onDraw, rather than from within the constructor, because otherwise
		//the getHeight and getWidth methods just return 0
		float canvasWidth = getWidth();
    	float canvasHeight = getHeight();
    	
		board = new Board(canvasHeight, canvasWidth, boardShape);

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		//unfortunately, we have to do this here rather than in the constructor because the getHeight and getWidth
		//functions do not work there
		if (board == null)
		{
			setupBoard();
		}
		
		this.setOnTouchListener(touchListener);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {

		//TODO: implement complete play-against-phone functionality
		//here, the player is playing against the phone and the phone is blue (goes first), so we just
		//select the first available hexagon
		if (gameMode == 1)
		{
			if (phonePlayerId == 0 && playerTurn == 0) //phone is blue
			{
				//just select the first hexagon that's not taken
				for (Hexagon hexagon: board.hexagonList)
				{
					if (hexagon.color == HEX_UNUSED_COLOR)
					{
						hexagon.color = BLUE;
						playerTurn = 1;
						lastChange = hexagon;
						break;
					}
				}
			} else if (phonePlayerId == 1 && playerTurn == 1) //phone is blue
			{
				//just select the first hexagon that's not taken
				for (Hexagon hexagon: board.hexagonList)
				{
					if (hexagon.color == HEX_UNUSED_COLOR)
					{
						hexagon.color = GREEN;
						playerTurn = 0;
						lastChange = hexagon;
						break;
					}
				}
			}
		}
	    
		//draw the bottom nav containing turn indicator & undo functionality
		drawBottomNav(canvas);

    	for( Hexagon hex : board.hexagonList ){
    		drawHexagon(canvas, hex);
    	}			
	}
	
	private OnTouchListener touchListener = new View.OnTouchListener() {
		
    	public boolean onTouch(View v, MotionEvent event) {

   			Log.e("hex", "ontouch x="+event.getX());
    	    Log.e("hex", "ontouch y="+event.getY());
    	    	
    	    //prevent null pointer exceptions
    	    if (board == null)
    	    {
    	    	return false;
    	    }

    	    Hexagon hexagon = board.findHexagonFromPointOnCanvas((float) event.getX(), (float) event.getY());
    	    
    		if (hexagon == null) //hexagon is out of scope of board
    		{
    			Log.e("hex", "hex is out of scope of board");
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
		    				Toast toast = Toast.makeText(context, turnMessage, Toast.LENGTH_SHORT);
		    				playerTurnToastStartTime = System.currentTimeMillis();
		    				toast.show();	    					
	    				}
	    					
    				} else if (x >= 0.45 * canvasWidth && x <= 0.55 * canvasWidth)
    				{
    					Log.e("hex", "undo button clicked");
    					//undo button
    					if (lastChange != null)
    					{
    						lastChange.color = HEX_UNUSED_COLOR;
    						playerTurn = (playerTurn == 1) ? 0 : 1;
    						lastChange = null;
    						UiView.this.postInvalidate();
    					}
    				} else if (x >= 0.7 * canvasWidth && x <= 0.8 * canvasWidth)
    				{
    					Log.e("hex", "redo button clicked");

    					Activity ac = (Activity) getContext();
    					ac.startActivity(new Intent(ac, ChooseBoardActivity.class));
    				}
    			}
    				
    			//do nothing
    		} else if (hexagon.color == HEX_UNUSED_COLOR) //hexagon is on board, but unused
    		{
    			Log.e("hex", "hex is white");
    			if (playerTurn == 0)
    			{
    				hexagon.color = BLUE;
    				playerTurn = 1;
    				//save last change in case we need to undo it
    				lastChange = hexagon;
    			} else
    			{
    				hexagon.color = GREEN;
    				playerTurn = 0;
    				//save last change in case we need to undo it
    				lastChange = hexagon;
    			}

    			UiView.this.postInvalidate();				
    		}
    		return true;
    	}
    };
	
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
		
		if (playerTurn == 0)
		{
			paint.setColor(BLUE);
		} else
		{
			paint.setColor(GREEN);
		}
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
		if (lastChange != null)
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
		float hexSide = board.getSmallHexSideLength();
		
		// vx, vy represents the vector of the first edge of the hexagon
		float vx = hexSide * (float) Math.cos(Math.PI/6.0);
		float vy = - hexSide * (float) Math.sin(Math.PI/6.0);
		final float co = (float)Math.cos(Math.PI/3);
		final float si = (float)Math.sin(Math.PI/3);

		float x, y;
		Path path;
		
		for (int j = 0; j < 2; j++)
		{
			if (j == 1)
			{
				paint.setColor(android.graphics.Color.BLACK);
		    	paint.setStrokeWidth(1);
		    	paint.setStyle(Paint.Style.STROKE);
			} else
			{
				paint.setColor(hex.color);
		        paint.setStyle(Paint.Style.FILL);
			}
			
			x = hex.x - board.getWCell()/2.0f;
			y = hex.y - board.getHCell()/2.0f;

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
	}
}
