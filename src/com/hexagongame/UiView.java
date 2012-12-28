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

public class UiView extends View{

	private Board board = null;
	
	//the paint object used by the canvas
	private Paint paint;
	
	private HashMap<String, Integer> gameConfig;

	private int playerTurn = 0;
	
	public UiView(Context context, AttributeSet attrs) {
		
		super(context, attrs);

		paint = new Paint();	

		gameConfig = new HashMap<String, Integer>();
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
	    	
	    	board = new Board(canvasHeight, canvasWidth);
	    	
	    	this.setOnTouchListener(new View.OnTouchListener() {
	    		
	    		public boolean onTouch(View v, MotionEvent event) {

	    			int[] coords = board.findHexagonalGridCoordinatesOfPointOnCanvas((float) event.getX(), (float) event.getY());

	    			if (gameConfig.get(coords[0]+"_"+coords[1]) == null) //hexagon is out of scope of board
	    			{	    
	    				//do nothing
	    			} else if (gameConfig.get(coords[0]+"_"+coords[1]) == android.graphics.Color.WHITE) //hexagon is on board, but unused
	    			{
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
		}
  
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
	

	
	
	public void drawHexagon(Canvas canvas, float xStart, float yStart, float hexSide, int color)
	{
		paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        
		Path path = new Path();
		
		float xOrig, yOrig, xNext, yNext;
		xOrig = xStart;
		yOrig = yStart;
		xNext = xOrig;
		yNext = yOrig;
		
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
		
		xNext = xOrig;
		yNext = yOrig;
		
		path.lineTo(xNext, yNext);
		
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
}
