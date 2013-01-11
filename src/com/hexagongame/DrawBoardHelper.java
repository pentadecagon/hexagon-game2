package com.hexagongame;

import android.util.Log;

public class DrawBoardHelper {

	final public Board board;
	
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
		this.board = board;
		float xmin=100000;
		float xmax=-100000;
		float ymin=100000;
		float ymax=-100000;
		for( Hexagon hex : board.hexagonList ){
			xmin = Math.min(xmin, hex.xi );
			xmax = Math.max(xmax, hex.xi );
			ymin = Math.min(ymin,  hex.yi );
			ymax = Math.max(ymax,  hex.yi );
		}
		
		wCell = canvasWidth / ( xmax-xmin+2);
		smallHexSideLength = wCell / (float)Math.sqrt(3.0);
		hCell = smallHexSideLength * 1.5f;
		
		x0 = wCell * (1-xmin); 
		final float ymid = (ymax+ymin) * 0.5f; // this should be placed at canvasHeight/2
		
		y0 = canvasHeight * 0.5f - ymid * hCell + 0.25f * smallHexSideLength;
		Log.d("hex","calculated position of board on canvas: x0="+x0+", y0="+y0);
	}
	
	public float[] findPositionOfCenterOfHexagonalCell( float xi, float yi )
	{
		float[] hexCellPos = {x0 + wCell * xi, y0+hCell*yi };
		return hexCellPos; 
	}
	
	Hexagon findHexagonFromPointOnCanvas(float x, float y){
	    Hexagon besthex = null;
	    float[] coords;
	    float hexX, hexY;
	    float besthex_dist = smallHexSideLength*smallHexSideLength;
	    for( Hexagon hex: board.hexagonList ){
	       coords = findPositionOfCenterOfHexagonalCell(hex.xi, hex.yi);
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
}
