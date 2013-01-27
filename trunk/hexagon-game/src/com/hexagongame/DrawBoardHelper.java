package com.hexagongame;

import com.hexagongame.game.Board;
import com.hexagongame.game.Hexagon;

import android.util.Log;

public class DrawBoardHelper {

	final public Board board;
	
	//x0, x-position of the top-left corner of the hexagon relative to the canvas
	private int x0;
	
	//y0, y-position of the top-left corner of the hexagon relative to the canvas
	private int y0;	
	
	private int smallHexSideLength;
	
	// vertical distance of cell rows 
	private int dyCell;
	//width of the hexagonal grid cell
	private int wCell;
	
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
		
		wCell = (int)(canvasWidth / ( xmax-xmin+2) / 2 + 0.5) * 2; // must be even
		smallHexSideLength = (int)Math.round(wCell / Math.sqrt(3.0));
		dyCell = smallHexSideLength * 3 /2;
		
		x0 = (int)(wCell * (1-xmin)); 
		final float ymid = (ymax+ymin) * 0.5f; // this should be placed at canvasHeight/2
		
		y0 = (int)(canvasHeight * 0.5 - ymid * dyCell+0.5);
		Log.d("hex","calculated position of board on canvas: x0="+x0+", y0="+y0);
	}
	
	public float[] findPositionOfCenterOfHexagonalCell( float xi, float yi )
	{
		float[] hexCellPos = {x0 + wCell * xi, y0+dyCell*yi };
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
		return dyCell;		
	}
	
	public float getWCell()
	{
		return wCell;
	}
}
