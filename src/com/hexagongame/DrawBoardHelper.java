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
		int imin=10000;
		int imax=-10000;
		int jmax=-10000;
		int jmin=10000;
		for( Hexagon hex : board.hexagonList ){
			imin = Math.min(imin, hex.i );
			imax = Math.max(imax, hex.i );
			jmax = Math.max(jmax,  hex.j );
			jmin = Math.min(jmin,  hex.j );
		}
		
		wCell = canvasWidth / (imax-imin+2);
		smallHexSideLength = wCell / (float)Math.sqrt(3.0);
		hCell = smallHexSideLength * 1.5f;
		
		x0 = wCell * 0.5f;  // we want half a hexagon space to the right and the left of the board
		
		float jmid = (jmax+jmin) * 0.5f; // this should be placed at canvasHeight/2
		
		y0 = canvasHeight * 0.5f - jmid * hCell + 0.25f * smallHexSideLength;
		Log.d("hex","calculated position of board on canvas: x0="+x0+", y0="+y0);
	}
	
	
	public float[] findPositionOfCenterOfHexagonalCell(int hexGridCoordX, int hexGridCoordY)
	{
		//find the position of the top-left of the hexagon relative to the (0, 0) position of the board
		float x = x0 + wCell * (hexGridCoordX + (Math.abs(hexGridCoordY) % 2) * 0.5f);
		float y = y0 + hCell * hexGridCoordY;

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
