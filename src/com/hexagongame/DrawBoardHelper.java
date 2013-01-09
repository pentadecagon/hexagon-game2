package com.hexagongame;

import android.util.Log;

public class DrawBoardHelper {

	public Board board = null;
	
	//width of canvas
	private static float canvasWidth;

	//height of canvas
	private static float canvasHeight;
		
	//width of board
	private static float boardWidth;

	//x0, x-position of the top-left corner of the hexagon relative to the canvas
	private static float x0;
	
	//y0, y-position of the top-left corner of the hexagon relative to the canvas
	private static float y0;	
	
	private static float smallHexSideLength;
	
	//height of the rectangular grid cell
	private static float hCell;
	
	//width of the rectangular grid cell
	private static float wCell;
	
	public DrawBoardHelper(float canvasHeight, float canvasWidth, Board board)
	{
		DrawBoardHelper.canvasWidth = canvasWidth;
		DrawBoardHelper.canvasHeight = canvasHeight;
		this.board = board;
		
		initialize();
	}
	

	private void initialize()
	{
		//calculate width of board
		boardWidth = 0.8f * canvasWidth;
		
		Log.d("hex","calculated boardWidth="+boardWidth);

		//calculate the length of the side of one of the small hexagons that fills up the board
		calculateSmallHexagonSideLength();
		
		//calculate height and width of one grid cell
		calculateDimensionsOfGridCell();
		
		//calculate (x0, y0) position of the board (position of the top-left point of the hexagon)
		//relative to the canvas
		calculatePositionOfBoardOnCanvas();
	}
	

	private void calculateSmallHexagonSideLength()
	{
		smallHexSideLength = boardWidth/ (14.0f * (float) Math.cos(Math.PI/6.0));
		Log.d("hex","smallHexSideLength: "+smallHexSideLength);
	}
	
	private void calculateDimensionsOfGridCell()
	{
		wCell = 2.0f * smallHexSideLength * (float) Math.cos((Math.PI/ 6.0));
		hCell = smallHexSideLength * (1.0f + (float) Math.cos((Math.PI/ 3.0)));
	}
	
	
	private void calculatePositionOfBoardOnCanvas()
	{
		
		x0 = 0.1f * canvasWidth;
		
		float boardHeight;
		if (this.board.boardShape == Board.BOARD_GEOMETRY_RECT)
		{
			boardHeight = hCell * 7.0f - smallHexSideLength * (float) Math.sin((Math.PI/ 6.0));
		} else
		{
			boardHeight = hCell * 9.0f - smallHexSideLength * (float) Math.sin((Math.PI/ 6.0));
		}
		
		y0 = canvasHeight/2.0f - boardHeight/2.0f;	
				
		Log.d("hex","calculated position of board on canvas: x0="+x0+", y0="+y0);
	}
	
	
	public float[] findPositionOfCenterOfHexagonalCell(int hexGridCoordX, int hexGridCoordY)
	{
		//find the position of the top-left of the hexagon relative to the (0, 0) position of the board
		float yRel = hCell * (-(float) hexGridCoordY - 0.5f);
		float xRel;
		//check if this is an even or odd row in the grid
		if (hexGridCoordY % 2 == 0)
		{
			xRel = wCell * (float) hexGridCoordX;
		} else
		{
			xRel = wCell * ((float) hexGridCoordX + 0.5f);
		}
		
		float x = xRel + getXpositionOfBoardOnCanvas();
		float y = yRel + getYpositionOfBoardOnCanvas();
		
		float[] hexCellPos = {x, y};
		return hexCellPos;
	}
	
	
	Hexagon findHexagonFromPointOnCanvas(float x, float y){
	    Hexagon besthex = null;
	    float[] coords;
	    float hexX, hexY;
	    float besthex_dist = smallHexSideLength*smallHexSideLength;
	    for( Hexagon hex: board.hexagonList ){
	       coords = findPositionOfCenterOfHexagonalCell(hex.i, hex.j);
	       hexX = coords[0];
	       hexY = coords[1];
	       float dhex =  (hexX-x)*(hexX-x)+(hexY-y)*(hexY-y);
	       if( dhex < besthex_dist){
	           besthex = hex;
	           besthex_dist = dhex;
	       }
	    }
	    return besthex;
	}
	

	public float getXpositionOfBoardOnCanvas()
	{
		return x0;
	}
	
	public float getYpositionOfBoardOnCanvas()
	{
		return y0;
	}
	
	public float getSmallHexSideLength()
	{
		return smallHexSideLength;
	}
	
	public float getHCell()
	{
		return hCell;		
	}
	
	public float getWCell()
	{
		return wCell;
	}
	
	public float getCanvasWidth()
	{
		return canvasWidth;
	}
	
	public float getCanvasHeight()
	{
		return canvasHeight;
	}
}
