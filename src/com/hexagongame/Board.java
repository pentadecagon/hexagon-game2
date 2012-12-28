package com.hexagongame;

import java.util.HashMap;

import android.util.Log;


public class Board{

	//width of canvas
	private static float canvasWidth;

	//height of canvas
	private static float canvasHeight;
		
	//width of board
	private static float boardWidth;
	
	//length of one side of the board
	private static float boardSideLength;
	
	//x0, x-position of the top-left corner of the hexagon relative to the canvas
	private static float x0;
	
	//y0, y-position of the top-left corner of the hexagon relative to the canvas
	private static float y0;	
	
	private static float smallHexSideLength;
	
	//height of the rectangular grid cell
	private static float hCell;
	
	//width of the rectangular grid cell
	private static float wCell;

	private HashMap<Integer, int[]> boardConfig;

	public Board(float canvasHeight, float canvasWidth) {

		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;

		initialize();
	}

	private void initialize()
	{
		//calculate width of board
		boardWidth = 0.8f * canvasWidth;
		
		Log.e("hex","calculated boardWidth="+boardWidth);
			
		//calculate length of one side of the hexagonal board
		calculateBoardSideLength();

		//calculate the length of the side of one of the small hexagons that fills up the board
		calculateSmallHexagonSideLength();
		
		//calculate height and width of one grid cell
		calculateDimensionsOfGridCell();
		
		//calculate (x0, y0) position of the board (position of the top-left point of the hexagon)
		//relative to the canvas
		calculatePositionOfBoardOnCanvas();
	}
	
	
	private void calculateBoardSideLength()
	{
		//calculate the length of one side of the board
		boardSideLength = boardWidth/ (2.0f * (float) Math.cos((Math.PI/6.0)));
		
		Log.e("hex","calculated boardSideLength="+boardSideLength);
	}
	

	
	private void calculateSmallHexagonSideLength()
	{
		smallHexSideLength = boardWidth/ (14.0f * (float) Math.cos(Math.PI/6.0));
		Log.e("hex","smallHexSideLength: "+smallHexSideLength);
	}
	
	private void calculateDimensionsOfGridCell()
	{
		wCell = 2.0f * smallHexSideLength * (float) Math.cos((Math.PI/ 6.0));
		hCell = smallHexSideLength * (1.0f + (float) Math.cos((Math.PI/ 3.0)));
	}
	

	private void calculatePositionOfBoardOnCanvas()
	{
		
		x0 = 0.1f * boardWidth;
		
		float boardHeight = hCell * 9.0f;
		
		y0 = canvasHeight/2.0f - boardHeight/2.0f;	
				
		Log.e("hex","calculated position of board on canvas: x0="+x0+", y0="+y0);
	}

	public int[] findHexagonalGridCoordinatesOfPointOnCanvas(float x, float y)
	{
		//find position relative to top-left point of hexagon
		float xRel = x - x0;
		float yRel = -(y - y0);
		
		//find coordinates of rectangular cell
		int rectGridPosY = (int) Math.floor(yRel/hCell) +1;
		int rectGridPosX;
		if (rectGridPosY % 2 == 0)
		{
			rectGridPosX = (int) Math.floor((xRel/wCell + 0.5));
		} else
		{
			rectGridPosX = (int) Math.floor(xRel/wCell);
		}
		
		//get position of point relative to rectangular cell
		//get y-position of point relative to rectangular cell
		float yPosRelRect = yRel % hCell;
		//get x-position of point relative to rectangular cell
		float xPosRelRect;
		if (rectGridPosY % 2 == 0)
		{
			xPosRelRect = (xRel + 0.5f * wCell) % wCell;
		} else
		{
			xPosRelRect = xRel % wCell;
		}
		
		//if the point is in either of the bottom two corners, it's in the hex corresponding directly
		//to this rectangular cell, otherwise it's in one of the adjacent cells
		int hexGridPosX, hexGridPosY;
		if (xPosRelRect >= wCell/2.0)
		{
			if (yPosRelRect > xPosRelRect * (float) Math.sin(Math.PI/6.0))
			{
				//point is in the main part of the rectangular grid (not the bottom two corners) and is
				//therefore in the corresponding hexagonal cell
				hexGridPosX = rectGridPosX;
				hexGridPosY = rectGridPosY;
			} else
			{
				//point is in the bottom right corner of the rectangular grid cell and is therefore in
				//the hexagonal cell below right
				hexGridPosY = rectGridPosY - 1; 
				if (rectGridPosY % 2 == 0)
				{
					hexGridPosX = rectGridPosX;
				} else
				{
					hexGridPosX = rectGridPosX + 1;
				}
			}
		} else
		{
			if (yPosRelRect > (wCell/2.0 - xPosRelRect) * (float) Math.sin(Math.PI/6.0))
			{
				//point is in the main part of the rectangular grid (not the bottom two corners) and is
				//therefore in the corresponding hexagonal cell
				hexGridPosX = rectGridPosX;
				hexGridPosY = rectGridPosY;
			} else
			{
				//point is in the bottom left corner of the rectangular grid cell and is therefore in
				//the hexagonal cell below left
				hexGridPosY = rectGridPosY - 1; 
				if (rectGridPosY % 2 == 0)
				{
					hexGridPosX = rectGridPosX - 1;
				} else
				{
					hexGridPosX = rectGridPosX;
				}
			}	
		}
		
		int[] hexGridCoords = {hexGridPosX, hexGridPosY};
		return hexGridCoords;	
	}
	
	public float[] findPositionOfTopLeftOfHexagonalCell(int hexGridCoordX, int hexGridCoordY)
	{
		//find the position of the top-left of the hexagon relative to the (0, 0) position of the board
		float yRel = hCell * (-(float) hexGridCoordY - 1.0f);
		float xRel;
		//check if this is an even or odd row in the grid
		if (hexGridCoordY % 2 == 0)
		{
			xRel = wCell * ((float) hexGridCoordX - 0.5f);
		} else
		{
			xRel = wCell * (float) hexGridCoordX;
		}
		float[] hexCellPos = {xRel, yRel};
		return hexCellPos;
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
	
	public HashMap<Integer, int[]> getBoardConfig()
	{
		return boardConfig;
	}
}