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
	
	//flag saying whether ontouch function is initialized
	private boolean onTouchInit = false;
	
	private long playerTurnToastStartTime = 0;
	
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
	protected void onDraw(Canvas canvas) {

		//unfortunately, we have to do this here rather than in the constructor because the getHeight and getWidth
		//functions do not work there
		if (board == null)
		{
			setupBoard();
		}
	    if (!onTouchInit)
	    {
	    	//initialize the ontouch event
		    this.setOnTouchListener(new View.OnTouchListener() {
		    		
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
		    			float x = (float) event.getX();
		    			float y = (float) event.getY();
		    			if (y > 0.85f * canvasWidth)
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
		    						lastChange.color = android.graphics.Color.WHITE;
		    						playerTurn = (playerTurn == 1) ? 0 : 1;
		    						lastChange = null;
		    						UiView.this.postInvalidate();
		    					}
		    				}  else if (x >= 0.7 * canvasWidth && x <= 0.8 * canvasWidth)
		    				{
		    					Log.e("hex", "redo button clicked");

		    					Activity ac = (Activity) getContext();
		    					ac.startActivity(new Intent(ac, ChooseBoardActivity.class));
		    				}
		    			}
		    				
		    			//do nothing
		    		} else if (hexagon.color == android.graphics.Color.WHITE) //hexagon is on board, but unused
		    		{
		    			Log.e("hex", "hex is white");
		    			if (playerTurn == 0)
		    			{
		    				hexagon.color = android.graphics.Color.BLUE;
		    				playerTurn = 1;
		    				//save last change in case we need to undo it
		    				lastChange = hexagon;
		    			} else
		    			{
		    				hexagon.color = android.graphics.Color.GREEN;
		    				playerTurn = 0;
		    				//save last change in case we need to undo it
		    				lastChange = hexagon;
		    			}
		    			
		    				
		    			UiView.this.postInvalidate();				
		    		}
		    		return true;
		    	}
		    });
		    	
		    onTouchInit = true;
	    }

		//TODO: implement complete play-against-phone functionality
		//here, the player is playing against the phone and the phone is blue (goes first), so we just
		//select the first hexagon
		if (gameMode == 1 && phonePlayerId == 0)
		{
			Hexagon hexagon = board.hexagonList.get(0);
			if (hexagon.color == android.graphics.Color.WHITE)
			{
				hexagon.color = android.graphics.Color.BLUE;
				playerTurn = 1;
			}
		}
	    
		//draw the bottom nav containing turn indicator & undo functionality
		drawBottomNav(canvas);

		if (board.boardShape == Board.BOARD_GEOMETRY_RECT)
		{
			drawSquareBoard(canvas);
		} else
		{
			drawHexBoard(canvas);
		}
		
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
		
		if (playerTurn == 0)
		{
			paint.setColor(android.graphics.Color.BLUE);
		} else
		{
			paint.setColor(android.graphics.Color.GREEN);
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
	
	private void drawHexBoard(Canvas canvas) {   	
    	for( Hexagon hex : board.hexagonList ){
    		drawHexagon(canvas, hex);
    	}
    	
    	decorateBoardTopLeft(canvas, android.graphics.Color.BLUE);
    	decorateBoardTopRight(canvas, android.graphics.Color.GREEN);
    	decorateBoardBottomLeft(canvas, android.graphics.Color.GREEN);
    	decorateBoardBottomRight(canvas, android.graphics.Color.BLUE);
    	decorateBoardLeft(canvas, android.graphics.Color.BLUE, android.graphics.Color.GREEN);
    	decorateBoardRight(canvas, android.graphics.Color.GREEN, android.graphics.Color.BLUE);	
	}
	
	private void drawSquareBoard(Canvas canvas) {
		for( Hexagon hex : board.hexagonList ){
    		drawHexagon(canvas, hex);
    	}
    	
    	decorateRectBoardHorizontalEdges(canvas, android.graphics.Color.GREEN);
    	decorateRectBoardVerticalEdges(canvas, android.graphics.Color.BLUE);
	}
	
	public void drawHexagon(Canvas canvas, Hexagon hex)
	{
		paint.setColor(hex.color);
        paint.setStyle(Paint.Style.FILL);
        
		Path path = new Path();
		
		float x = hex.x - board.getWCell()/2.0f;
		float y = hex.y - board.getHCell()/2.0f;
		
		float hexSide = board.getSmallHexSideLength();
		
		// vx, vy represents the vector of the first edge of the hexagon
		float vx = hexSide * (float) Math.cos(Math.PI/6.0);
		float vy = - hexSide * (float) Math.sin(Math.PI/6.0);
		final float co = (float)Math.cos(Math.PI/3);
		final float si = (float)Math.sin(Math.PI/3);

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
		
	public void decorateBoardTopLeft(Canvas canvas, int color)
	{
		float xOrigin = board.getXpositionOfBoardOnCanvas();
    	float yOrigin = board.getYpositionOfBoardOnCanvas();

    	float[] gridCoords = board.findPositionOfTopLeftOfHexagonalCell(0,-3);  	
		
    	float x0 = xOrigin + gridCoords[0];
    	float y0 = yOrigin + gridCoords[1];
    	
		paint.setColor(color);
    	paint.setStrokeWidth(10);
    	paint.setStyle(Paint.Style.STROKE);
    	Path path = new Path();
    	path.moveTo(x0, y0);
		path.lineTo(x0 + board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 - board.getSmallHexSideLength() * (float) Math.sin(Math.PI/6.0));
		path.lineTo(x0 + 2.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0);
		path.lineTo(x0 + 3.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 - board.getSmallHexSideLength() * (float) Math.sin(Math.PI/6.0));
		path.lineTo(x0 + 3.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 - board.getSmallHexSideLength() * (1.0f + (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 + 4.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 - board.getSmallHexSideLength() * (1.0f + 2.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 + 5.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 - board.getSmallHexSideLength() * (1.0f + (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 + 6.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 - board.getSmallHexSideLength() * (1.0f + 2.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 + 6.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 - board.getSmallHexSideLength() * (2.0f + 2.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 + 7.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 - board.getSmallHexSideLength() * (2.0f + 3.0f * (float) Math.sin(Math.PI/6.0)));
		canvas.drawPath(path, paint);		
	}
	
	public void decorateBoardTopRight(Canvas canvas, int color)
	{
		float xOrigin = board.getXpositionOfBoardOnCanvas();
    	float yOrigin = board.getYpositionOfBoardOnCanvas();

    	float[] gridCoords = board.findPositionOfTopLeftOfHexagonalCell(3,-1);  	
		
    	float x0 = xOrigin + gridCoords[0] + board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0);
    	float y0 = yOrigin + gridCoords[1] - board.getSmallHexSideLength() * (float) Math.sin(Math.PI/6.0);
    	
		paint.setColor(color);
    	paint.setStrokeWidth(10);
    	paint.setStyle(Paint.Style.STROKE);
    	Path path = new Path();
    	path.moveTo(x0, y0);
		path.lineTo(x0 + board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (float) Math.sin(Math.PI/6.0));
		path.lineTo(x0 + board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + (1.0f + (float) Math.sin(Math.PI/6.0)) * board.getSmallHexSideLength());
		path.lineTo(x0 + 2.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + (1.0f + 2.0f * (float) Math.sin(Math.PI/6.0)) * board.getSmallHexSideLength());
		path.lineTo(x0 + 3.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + (1.0f + 1.0f * (float) Math.sin(Math.PI/6.0)) * board.getSmallHexSideLength());
		path.lineTo(x0 + 4.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + (1.0f + 2.0f * (float) Math.sin(Math.PI/6.0)) * board.getSmallHexSideLength());
		path.lineTo(x0 + 4.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + (2.0f + 2.0f * (float) Math.sin(Math.PI/6.0)) * board.getSmallHexSideLength());
		path.lineTo(x0 + 5.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + (2.0f + 3.0f * (float) Math.sin(Math.PI/6.0)) * board.getSmallHexSideLength());
		path.lineTo(x0 + 6.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + (2.0f + 2.0f * (float) Math.sin(Math.PI/6.0)) * board.getSmallHexSideLength());
		path.lineTo(x0 + 7.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + (2.0f + 3.0f * (float) Math.sin(Math.PI/6.0)) * board.getSmallHexSideLength());

		canvas.drawPath(path, paint);
	}
	
	public void decorateBoardBottomRight(Canvas canvas, int color)
	{
		float xOrigin = board.getXpositionOfBoardOnCanvas();
    	float yOrigin = board.getYpositionOfBoardOnCanvas();

    	float[] gridCoords = board.findPositionOfTopLeftOfHexagonalCell(3,-9);  	
		
    	float x0 = xOrigin + gridCoords[0] + 1.0f * (float) Math.cos(Math.PI/6.0) * board.getSmallHexSideLength();
    	float y0 = yOrigin + gridCoords[1] + (1.0f + (float) Math.sin(Math.PI/6.0)) * board.getSmallHexSideLength();
    	
		paint.setColor(color);
    	paint.setStrokeWidth(10);
    	paint.setStyle(Paint.Style.STROKE);
    	Path path = new Path();
    	path.moveTo(x0, y0);
		path.lineTo(x0 + board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 - board.getSmallHexSideLength() * (float) Math.sin(Math.PI/6.0));
		path.lineTo(x0 + board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 - board.getSmallHexSideLength() * (1.0f + (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 + 2.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 - board.getSmallHexSideLength() * (1.0f + 2.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 + 3.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 - board.getSmallHexSideLength() * (1.0f + (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 + 4.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 - board.getSmallHexSideLength() * (1.0f + 2.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 + 4.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 - board.getSmallHexSideLength() * (2.0f + 2.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 + 5.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 - board.getSmallHexSideLength() * (2.0f + 3.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 + 6.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 - board.getSmallHexSideLength() * (2.0f + 2.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 + 7.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 - board.getSmallHexSideLength() * (2.0f + 3.0f * (float) Math.sin(Math.PI/6.0)));

		canvas.drawPath(path, paint);		
	}
	
	public void decorateBoardBottomLeft(Canvas canvas, int color)
	{
		float xOrigin = board.getXpositionOfBoardOnCanvas();
    	float yOrigin = board.getYpositionOfBoardOnCanvas();

    	float[] gridCoords = board.findPositionOfTopLeftOfHexagonalCell(0,-7);  	
		
    	float x0 = xOrigin + gridCoords[0];
    	float y0 = yOrigin + gridCoords[1] + board.getSmallHexSideLength();
    	
		paint.setColor(color);
    	paint.setStrokeWidth(10);
    	paint.setStyle(Paint.Style.STROKE);
    	Path path = new Path();
    	path.moveTo(x0, y0);
		path.lineTo(x0 + board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (float) Math.sin(Math.PI/6.0));
		path.lineTo(x0 + 2.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0);
		path.lineTo(x0 + 3.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (float) Math.sin(Math.PI/6.0));
		path.lineTo(x0 + 3.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (1.0f + (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 + 4.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (1.0f + 2.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 + 5.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (1.0f + (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 + 6.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (1.0f + 2.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 + 6.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (2.0f + 2.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 + 7.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (2.0f + 3.0f * (float) Math.sin(Math.PI/6.0)));
		canvas.drawPath(path, paint);		
	}
	
	public void decorateBoardLeft(Canvas canvas, int color1, int color2)
	{
		float xOrigin = board.getXpositionOfBoardOnCanvas();
    	float yOrigin = board.getYpositionOfBoardOnCanvas();

    	float[] gridCoords = board.findPositionOfTopLeftOfHexagonalCell(0,-3);  	
		
    	float x0 = xOrigin + gridCoords[0];
    	float y0 = yOrigin + gridCoords[1];
    	
		paint.setColor(color1);
    	paint.setStrokeWidth(10);
    	paint.setStyle(Paint.Style.STROKE);
    	Path path = new Path();
    	path.moveTo(x0, y0);
		path.lineTo(x0, y0 + board.getSmallHexSideLength());
		path.lineTo(x0 + board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (1.0f + (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 + board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (2.0f + (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0, y0 + board.getSmallHexSideLength() * (2.0f + 2.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0, y0 + board.getSmallHexSideLength() * (2.5f + 2.0f * (float) Math.sin(Math.PI/6.0)));
		canvas.drawPath(path, paint);	
		
		paint.setColor(color2);
		path = new Path();
		path.moveTo(x0, y0 + board.getSmallHexSideLength() * (2.5f + 2.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0, y0 + board.getSmallHexSideLength() * (3.0f + 2.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 + board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (3.0f + 3.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 + board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (4.0f + 3.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0, y0 + board.getSmallHexSideLength() * (4.0f + 4.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0, y0 + board.getSmallHexSideLength() * (5.0f + 4.0f * (float) Math.sin(Math.PI/6.0)));
		
		canvas.drawPath(path, paint);		
	}
	
	public void decorateBoardRight(Canvas canvas, int color1, int color2)
	{
		float xOrigin = board.getXpositionOfBoardOnCanvas();
    	float yOrigin = board.getYpositionOfBoardOnCanvas();

    	float[] gridCoords = board.findPositionOfTopLeftOfHexagonalCell(7,-3);  	
		
    	float x0 = xOrigin + gridCoords[0];
    	float y0 = yOrigin + gridCoords[1];
    	
		paint.setColor(color1);
    	paint.setStrokeWidth(10);
    	paint.setStyle(Paint.Style.STROKE);
    	Path path = new Path();
    	path.moveTo(x0, y0);
		path.lineTo(x0, y0 + board.getSmallHexSideLength());
		path.lineTo(x0 - board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (1.0f + (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 - board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (2.0f + (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0, y0 + board.getSmallHexSideLength() * (2.0f + 2.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0, y0 + board.getSmallHexSideLength() * (2.5f + 2.0f * (float) Math.sin(Math.PI/6.0)));
		canvas.drawPath(path, paint);
		
		paint.setColor(color2);
		path = new Path();
		path.moveTo(x0, y0 + board.getSmallHexSideLength() * (2.5f + 2.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0, y0 + board.getSmallHexSideLength() * (3.0f + 2.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 - board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (3.0f + 3.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0 - board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (4.0f + 3.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0, y0 + board.getSmallHexSideLength() * (4.0f + 4.0f * (float) Math.sin(Math.PI/6.0)));
		path.lineTo(x0, y0 + board.getSmallHexSideLength() * (5.0f + 4.0f * (float) Math.sin(Math.PI/6.0)));
		
		canvas.drawPath(path, paint);		
	}
	
	public void decorateRectBoardHorizontalEdges(Canvas canvas, int color)
	{
		float xOrigin = board.getXpositionOfBoardOnCanvas();
    	float yOrigin = board.getYpositionOfBoardOnCanvas();

    	float[] gridCoords = board.findPositionOfTopLeftOfHexagonalCell(0,-1);  	
		
    	float x0 = xOrigin + gridCoords[0];
    	float y0 = yOrigin + gridCoords[1];
    	
    	paint.setColor(color);
    	paint.setStrokeWidth(10);
    	paint.setStyle(Paint.Style.STROKE);
    	
    	//decorate the top edge of the board
    	Path pathTop = new Path();
    	pathTop.moveTo(x0, y0);
    	
    	float boardBottomOffset = (7.0f + 6.0f * (float) Math.sin(Math.PI/6.0)) * board.getSmallHexSideLength();
    	
    	//decorate the bottom edge of the board
    	Path pathBottom = new Path();
    	pathBottom.moveTo(x0, y0 + boardBottomOffset);
    	
    	for (int i = 0; i < 15; i++)
    	{
    		if (i % 2 == 0)
    		{
    			pathTop.lineTo(x0 + i * 1.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0);
    			pathBottom.lineTo(x0 + i * 1.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + boardBottomOffset);
    		} else
    		{
    			pathTop.lineTo(x0 + i * 1.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 - board.getSmallHexSideLength() * (float) Math.sin(Math.PI/6.0));
    			pathBottom.lineTo(x0 + i * 1.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + boardBottomOffset + board.getSmallHexSideLength() * (float) Math.sin(Math.PI/6.0));
    		}
    	}

    	//draw the top edge of the board
		canvas.drawPath(pathTop, paint);
		
		//draw the bottom edge of the board
		canvas.drawPath(pathBottom, paint);
	}
	
	public void decorateRectBoardVerticalEdges(Canvas canvas, int color)
	{
		float xOrigin = board.getXpositionOfBoardOnCanvas();
    	float yOrigin = board.getYpositionOfBoardOnCanvas();

    	float[] gridCoords = board.findPositionOfTopLeftOfHexagonalCell(0,-1);  	
		
    	float x0 = xOrigin + gridCoords[0];
    	float y0 = yOrigin + gridCoords[1];
    	
    	paint.setColor(color);
    	paint.setStrokeWidth(10);
    	paint.setStyle(Paint.Style.STROKE);
    	
    	//decorate the left edge of the board
    	Path pathLeft = new Path();
    	pathLeft.moveTo(x0, y0);

    	pathLeft.lineTo(x0, y0 + board.getSmallHexSideLength());
    	pathLeft.lineTo(x0 + board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (1.0f + (float) Math.sin(Math.PI/6.0)));
    	pathLeft.lineTo(x0 + board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (2.0f + (float) Math.sin(Math.PI/6.0)));
    	pathLeft.lineTo(x0, y0 + board.getSmallHexSideLength() * (2.0f + 2.0f * (float) Math.sin(Math.PI/6.0)));
    	pathLeft.lineTo(x0, y0 + board.getSmallHexSideLength() * (3.0f + 2.0f * (float) Math.sin(Math.PI/6.0)));
    	pathLeft.lineTo(x0 + board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (3.0f + 3.0f * (float) Math.sin(Math.PI/6.0)));
    	pathLeft.lineTo(x0 + board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (4.0f + 3.0f * (float) Math.sin(Math.PI/6.0)));
    	pathLeft.lineTo(x0, y0 + board.getSmallHexSideLength() * (4.0f + 4.0f * (float) Math.sin(Math.PI/6.0)));
    	pathLeft.lineTo(x0, y0 + board.getSmallHexSideLength() * (5.0f + 4.0f * (float) Math.sin(Math.PI/6.0)));
    	pathLeft.lineTo(x0 + board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (5.0f + 5.0f * (float) Math.sin(Math.PI/6.0)));
    	pathLeft.lineTo(x0 + board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y0 + board.getSmallHexSideLength() * (6.0f + 5.0f * (float) Math.sin(Math.PI/6.0)));
    	pathLeft.lineTo(x0, y0 + board.getSmallHexSideLength() * (6.0f + 6.0f * (float) Math.sin(Math.PI/6.0)));
    	pathLeft.lineTo(x0, y0 + board.getSmallHexSideLength() * (7.0f + 6.0f * (float) Math.sin(Math.PI/6.0)));
    	
    	//draw the left edge of the board
    	canvas.drawPath(pathLeft, paint);
    	
    	//decorate the right edge of the board
    	Path pathRight = new Path();
    	float y1 = y0;
    	float x1 = x0 + 14.0f * board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0);
    	pathRight.moveTo(x1, y1);
    	
    	pathRight.lineTo(x1, y1 + board.getSmallHexSideLength());
    	pathRight.lineTo(x1 - board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y1 + board.getSmallHexSideLength() * (1.0f + (float) Math.sin(Math.PI/6.0)));
    	pathRight.lineTo(x1 - board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y1 + board.getSmallHexSideLength() * (2.0f + (float) Math.sin(Math.PI/6.0)));
    	pathRight.lineTo(x1, y1 + board.getSmallHexSideLength() * (2.0f + 2.0f * (float) Math.sin(Math.PI/6.0)));
    	pathRight.lineTo(x1, y1 + board.getSmallHexSideLength() * (3.0f + 2.0f * (float) Math.sin(Math.PI/6.0)));
    	pathRight.lineTo(x1 - board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y1 + board.getSmallHexSideLength() * (3.0f + 3.0f * (float) Math.sin(Math.PI/6.0)));
    	pathRight.lineTo(x1 - board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y1 + board.getSmallHexSideLength() * (4.0f + 3.0f * (float) Math.sin(Math.PI/6.0)));
    	pathRight.lineTo(x1, y1 + board.getSmallHexSideLength() * (4.0f + 4.0f * (float) Math.sin(Math.PI/6.0)));
    	pathRight.lineTo(x1, y1 + board.getSmallHexSideLength() * (5.0f + 4.0f * (float) Math.sin(Math.PI/6.0)));
    	pathRight.lineTo(x1 - board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y1 + board.getSmallHexSideLength() * (5.0f + 5.0f * (float) Math.sin(Math.PI/6.0)));
    	pathRight.lineTo(x1 - board.getSmallHexSideLength() * (float) Math.cos(Math.PI/6.0), y1 + board.getSmallHexSideLength() * (6.0f + 5.0f * (float) Math.sin(Math.PI/6.0)));
    	pathRight.lineTo(x1, y1 + board.getSmallHexSideLength() * (6.0f + 6.0f * (float) Math.sin(Math.PI/6.0)));
    	pathRight.lineTo(x1, y1 + board.getSmallHexSideLength() * (7.0f + 6.0f * (float) Math.sin(Math.PI/6.0)));
    	
    	//draw the left edge of the board
    	canvas.drawPath(pathRight, paint);
	}
}
