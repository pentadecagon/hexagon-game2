package com.hexagongame;

import java.util.HashMap;

import android.content.Context;
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
	
	private HashMap<String, Integer> gameConfig;

	private int playerTurn = 0;
	
	private int boardShape = Board.BOARD_GEOMETRY_HEX;
	
	//flag saying whether ontouch is initialized
	private boolean onTouchInit = false;
	
	public UiView(Context context, AttributeSet attrs) {
		
		super(context, attrs);

		paint = new Paint();	

		viewInit();
	}
	
	public void viewInit()
	{
		board = null;
		gameConfig = new HashMap<String, Integer>();
		playerTurn = 0;
		
		//show introductory message
    	Context context = getContext();
		Toast toast = Toast.makeText(context, "First player to make a path from one side to the other wins. Blue goes first.", Toast.LENGTH_SHORT);
		toast.show();
	}
	
	public void setBoard(Board board)
	{
		this.board = board;
	}
	
	public Board getBoard()
	{
		return board;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
		if (board == null)
		{
			float canvasWidth = getWidth();
	    	float canvasHeight = getHeight();
	    	
	    	Log.e("hex", "canvasWidth="+canvasWidth);
	    	Log.e("hex", "canvasHeight="+canvasHeight);
	    	
	    	board = new Board(canvasHeight, canvasWidth, boardShape);
	    	
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
		    	    	
		    			int[] coords = board.findHexagonalGridCoordinatesOfPointOnCanvas((float) event.getX(), (float) event.getY());
	
		    			if (gameConfig.get(coords[0]+"_"+coords[1]) == null) //hexagon is out of scope of board
		    			{	    
		    				Log.e("hex", "hex is out of scope of board");
		    				//check if user has clicked on the nav
		    				float canvasWidth = getWidth();
		    				float x = (float) event.getX();
		    				float y = (float) event.getY();
		    				if (y < 0.15f * canvasWidth)
		    				{
		    					if (x > 0.15f * canvasWidth && x < 0.4f * canvasWidth)
		    					{
		    						//if user has clicked on the hexagonal shape in the nav, redraw the board as a hexagon
		    						boardShape = Board.BOARD_GEOMETRY_HEX;
		    						viewInit();
		    						UiView.this.postInvalidate();
		    					} else if (x >= 0.4f * canvasWidth && x < 0.5f * canvasWidth)
		    					{
		    						//if user has clicked on the rectangular shape in the nav, redraw the board as a rectangle
		    						boardShape = Board.BOARD_GEOMETRY_RECT;
		    						viewInit();
		    						UiView.this.postInvalidate();
		    					}
		    				}
		    				
		    				//do nothing
		    			} else if (gameConfig.get(coords[0]+"_"+coords[1]) == android.graphics.Color.WHITE) //hexagon is on board, but unused
		    			{
		    				Log.e("hex", "hex is white");
		    				if (playerTurn == 0)
		    				{
		    					gameConfig.put(coords[0]+"_"+coords[1], android.graphics.Color.BLUE);
		    					playerTurn = 1;
		    				} else
		    				{
		    					gameConfig.put(coords[0]+"_"+coords[1], android.graphics.Color.GREEN);
		    					playerTurn = 0;
		    				}
		    			
		    				
		    				UiView.this.postInvalidate();				
		    			}
		    			return true;
		    		}
		    	});
		    	
		    	onTouchInit = true;
	    	}
		}
		
		//draw the navigation allowing the user to select a shape
		drawNav(canvas);

		if (board.boardShape == Board.BOARD_GEOMETRY_RECT)
		{
			drawSquareBoard(canvas);
		} else
		{
			drawHexBoard(canvas);
		}
	}

	protected void drawNav(Canvas canvas) {
		drawNavHexagon(canvas);
		drawNavSquare(canvas);
	}
	
	protected void drawNavHexagon(Canvas canvas) {
		float canvasWidth = getWidth();

		float hexSide, x0, y0, lineWidth;
		if (board.boardShape == Board.BOARD_GEOMETRY_HEX)
		{
			hexSide = 0.07f * canvasWidth;
			x0 = 0.15f * canvasWidth;
			y0 = 0.06f * canvasWidth;
			lineWidth = 10;
		} else
		{
			hexSide = 0.05f * canvasWidth;
			x0 = 0.15f * canvasWidth;
			y0 = 0.07f * canvasWidth;
			lineWidth = 2;
		}

		float xNext = x0;
		float yNext = y0;

		paint.setColor(android.graphics.Color.WHITE);
    	paint.setStrokeWidth(lineWidth);
    	paint.setStyle(Paint.Style.STROKE);
    	Path path = new Path();
    	
		path.moveTo(xNext, yNext);

		xNext = xNext + hexSide * (float) Math.cos(Math.PI/6.0);
		yNext = yNext - hexSide * (float) Math.sin(Math.PI/6.0);
		
		path.lineTo(xNext, yNext);
		
		xNext = xNext + hexSide * (float) Math.cos(Math.PI/6.0);
		yNext = yNext + hexSide * (float) Math.sin(Math.PI/6.0);
		
		path.lineTo(xNext, yNext);
		
		yNext = yNext + hexSide;
		
		path.lineTo(xNext, yNext);
		
		xNext = xNext - hexSide * (float) Math.cos(Math.PI/6.0);
		yNext = yNext + hexSide * (float) Math.sin(Math.PI/6.0);
		
		path.lineTo(xNext, yNext);
		
		xNext = xNext - hexSide * (float) Math.cos(Math.PI/6.0);
		yNext = yNext - hexSide * (float) Math.sin(Math.PI/6.0);
		
		path.lineTo(xNext, yNext);
		
		xNext = x0;
		yNext = y0;
		
		path.lineTo(xNext, yNext - 0.07f * hexSide);
    	
    	canvas.drawPath(path, paint);
	}
	
	protected void drawNavSquare(Canvas canvas) {
		float canvasWidth = getWidth();

		float squareWidth, x0, y0, lineWidth;
		if (board.boardShape == Board.BOARD_GEOMETRY_RECT)
		{
			squareWidth = 0.1f * canvasWidth;
			x0 = 0.4f * canvasWidth;
			y0 = 0.05f * canvasWidth;		
			lineWidth = 10;	
		} else
		{
			squareWidth = 0.08f * canvasWidth;
			x0 = 0.4f * canvasWidth;
			y0 = 0.06f * canvasWidth;				
			lineWidth = 2;
		}

		paint.setColor(android.graphics.Color.WHITE);
    	paint.setStrokeWidth(lineWidth);
    	paint.setStyle(Paint.Style.STROKE);
    	Path path = new Path();
    	
    	path.moveTo(x0, y0);
    	path.lineTo(x0 + squareWidth, y0);
    	path.lineTo(x0 + squareWidth, y0 + squareWidth);
    	path.lineTo(x0, y0 + squareWidth);
    	path.lineTo(x0, y0 - 0.07f * squareWidth);
    	
    	canvas.drawPath(path, paint);
	}
	
	protected void drawHexBoard(Canvas canvas) {
    	float xOrigin = board.getXpositionOfBoardOnCanvas();
    	float yOrigin = board.getYpositionOfBoardOnCanvas();
    	
    	Log.e("hex", "board.getXpositionOfBoardOnCanvas()="+xOrigin);
    	Log.e("hex", "board.getYpositionOfBoardOnCanvas()="+yOrigin);

    	float xHexPos, yHexPos;

    	float[] hexCellPos;

    	int color;
    	int iLowerLimit, iUpperLimit;

    	for (int j = -1; j > -10; j--)
    	{
    		//corners of hexagonal board
    		if (j == -1 || j == -9)
    		{
    			iLowerLimit = 3;
    			iUpperLimit = 3;
    		} else if (j == -2 || j == -8)
    		{
    			iLowerLimit = 2;
    			iUpperLimit = 5;
    		} else
    		{
    			iLowerLimit = (j % 2 == 0) ? 1 : 0;
    			iUpperLimit = 6;
    		}
	    	for (int i = iLowerLimit; i < (iUpperLimit + 1); i++)
	    	{
	        	hexCellPos = board.findPositionOfTopLeftOfHexagonalCell(i, j);

		        xHexPos = xOrigin + hexCellPos[0];
		        yHexPos = yOrigin + hexCellPos[1];	

		        if (gameConfig.get(i+"_"+j) == null)
		        {
		        	color = android.graphics.Color.WHITE;
		        	gameConfig.put(i+"_"+j, color);		        	
		        } else
		        {
		        	color = gameConfig.get(i+"_"+j);
		        }
		        
		        drawHexagon(canvas, xHexPos, yHexPos, board.getSmallHexSideLength(), color);
	        	
	    	}
    	}
    	
    	decorateBoardTopLeft(canvas, android.graphics.Color.BLUE);
    	decorateBoardTopRight(canvas, android.graphics.Color.GREEN);
    	decorateBoardBottomLeft(canvas, android.graphics.Color.GREEN);
    	decorateBoardBottomRight(canvas, android.graphics.Color.BLUE);
    	decorateBoardLeft(canvas, android.graphics.Color.BLUE, android.graphics.Color.GREEN);
    	decorateBoardRight(canvas, android.graphics.Color.GREEN, android.graphics.Color.BLUE);	
	}
	
	protected void drawSquareBoard(Canvas canvas) {
    	float xOrigin = board.getXpositionOfBoardOnCanvas();
    	float yOrigin = board.getYpositionOfBoardOnCanvas();
    	
    	Log.e("hex", "board.getXpositionOfBoardOnCanvas()="+xOrigin);
    	Log.e("hex", "board.getYpositionOfBoardOnCanvas()="+yOrigin);

    	float xHexPos, yHexPos;

    	float[] hexCellPos;

    	int color;
    	int iLowerLimit, iUpperLimit;

    	for (int j = -1; j > -8; j--)
    	{
    		//corners of hexagonal board
    		iLowerLimit = (j % 2 == 0) ? 1 : 0;
			iUpperLimit = 6;
			
	    	for (int i = iLowerLimit; i < (iUpperLimit + 1); i++)
	    	{	
	        	hexCellPos = board.findPositionOfTopLeftOfHexagonalCell(i, j);

		        xHexPos = xOrigin + hexCellPos[0];
		        yHexPos = yOrigin + hexCellPos[1];	

		        if (gameConfig.get(i+"_"+j) == null)
		        {
		        	color = android.graphics.Color.WHITE;
		        	gameConfig.put(i+"_"+j, color);		        	
		        } else
		        {
		        	color = gameConfig.get(i+"_"+j);
		        }
		        
		        drawHexagon(canvas, xHexPos, yHexPos, board.getSmallHexSideLength(), color);
	        	
	    	}
    	}
    	
    	decorateRectBoardHorizontalEdges(canvas, android.graphics.Color.GREEN);
    	decorateRectBoardVerticalEdges(canvas, android.graphics.Color.BLUE);
	}
	
	public void drawHexagon(Canvas canvas, float x, float y, float hexSide, int color)
	{
		paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        
		Path path = new Path();
		
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
