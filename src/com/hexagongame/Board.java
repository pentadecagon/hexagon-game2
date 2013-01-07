package com.hexagongame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import android.util.Log;


public class Board{

	//hexagonal board
	public static final int BOARD_GEOMETRY_HEX = 0;
	
	//square board
	public static final int BOARD_GEOMETRY_RECT = 1;

	//constant giving shape of board: hexagonal, square etc
	public int boardShape = BOARD_GEOMETRY_HEX;

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

	public ArrayList<Hexagon> hexagonList = null;
	
	/* 'outer' represents the four outer regions of the board.  The first index indicates the player (0 or 1),
	 * the second index enumerates the both opposite regions of each player.  Those objects hold the set 
	 * of adjacent hexagons for each outer region   */
	
	Hexagon outer[][] = {{ new Hexagon(0, 0, UiView.BLUE), new Hexagon(0, 0, UiView.BLUE)},
								{new Hexagon(0, 0, UiView.GREEN), new Hexagon(0, 0, UiView.GREEN)}};
	
	public Board(float canvasHeight, float canvasWidth, int boardShape) {

		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;
		this.boardShape = boardShape;

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
		
		setupListOfHexagons();
		findAdjacentHexagons(hexagonList);
	}
	
	private void setupListOfHexagons()
	{
		Log.d("hex","called setupListOfHexagons");
		
		hexagonList = new ArrayList<Hexagon>();
		
		//construct the list of hexagons that will make up the board
		if (boardShape == Board.BOARD_GEOMETRY_RECT)
		{
			setupRectBoardListOfHexagons();
		} else
		{
			setupHexBoardListOfHexagons();
		}
	}
	
	private void setupHexBoardListOfHexagons()
	{
		Log.d("hex","called setupHexBoardListOfHexagons");
		
		//create a list of hexagons that will be used to populate the hexagonally-shaped board
    	float xOrigin = this.getXpositionOfBoardOnCanvas();
    	float yOrigin = this.getYpositionOfBoardOnCanvas();
    	
    	//Log.e("hex", "board.getXpositionOfBoardOnCanvas()="+xOrigin);
    	//Log.e("hex", "board.getYpositionOfBoardOnCanvas()="+yOrigin);

    	float xHexPos, yHexPos;

    	float[] hexCellPos;

    	int color;
    	int iLowerLimit, iUpperLimit;
    	
    	Hexagon hexagon;

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
	    		//Log.e("hex", "setupHexBoardListOfHexagons: finding position of cell: "+i+", "+j);
	    		
	        	hexCellPos = this.findPositionOfCenterOfHexagonalCell(i, j);
	        	
	        	//Log.e("hex", "setupHexBoardListOfHexagons: found position of cell: "+hexCellPos[0]+", "+hexCellPos[1]);

		        xHexPos = xOrigin + hexCellPos[0];
		        yHexPos = yOrigin + hexCellPos[1];
	
		        color = UiView.HEX_UNUSED_COLOR;

		        hexagon = new Hexagon(xHexPos, yHexPos, color);
		        //tell if one of the hexagons is touching an edge
				if (j >= -5)
				{
				  if (i == iLowerLimit || ((j == -3 || j == -2) && i == (iLowerLimit + 1)))
				  {
					  outer[0][0].adjacent.add(hexagon);
				  }
				  if (i == iUpperLimit || ((j == -3 || j == -2) && i == (iUpperLimit - 1)))
				  {
					  outer[1][0].adjacent.add(hexagon);
				  }
				}
				if (j <= -5)
				{
					if (i == iLowerLimit || ((j == -7 || j == -8) && i == (iLowerLimit + 1)))
					{
						  outer[1][1].adjacent.add(hexagon);
					}
					if (i == iUpperLimit || ((j == -7 || j == -8) && i == (iUpperLimit - 1)))
					{
						  outer[0][1].adjacent.add(hexagon);
					}
				}
				
		        
		        hexagonList.add(hexagon);
	    	}
    	}
		
	}
	
	private void setupRectBoardListOfHexagons()
	{
		Log.d("hex","called setupHexBoardListOfHexagons");
		
		//create a list of hexagons that will be used to populate the rectangular board
    	float xOrigin = this.getXpositionOfBoardOnCanvas();
    	float yOrigin = this.getYpositionOfBoardOnCanvas();
    	
    	Log.d("hex", "board.getXpositionOfBoardOnCanvas()="+xOrigin);
    	Log.d("hex", "board.getYpositionOfBoardOnCanvas()="+yOrigin);

    	float xHexPos, yHexPos;

    	float[] hexCellPos;

    	int iLowerLimit, iUpperLimit;

    	Hexagon hexagon;

    	for (int j = -1; j > -8; j--)
    	{
    		//corners of hexagonal board
    		iLowerLimit = (j % 2 == 0) ? 1 : 0;
			iUpperLimit = 6;
			
	    	for (int i = iLowerLimit; i < (iUpperLimit + 1); i++)
	    	{	
	        	hexCellPos = this.findPositionOfCenterOfHexagonalCell(i, j);

		        xHexPos = xOrigin + hexCellPos[0];
		        yHexPos = yOrigin + hexCellPos[1];	

		        hexagon = new Hexagon(xHexPos, yHexPos, UiView.HEX_UNUSED_COLOR);
		        //tell if one of the hexagons is touching an edge
				if (j == -1)
				{
					  outer[1][0].adjacent.add(hexagon);
				} else if (j == -7)
				{
					  outer[1][1].adjacent.add(hexagon);
				}
				if (i == iLowerLimit)
				{
					  outer[0][0].adjacent.add(hexagon);
				} else if (i == iUpperLimit)
				{
					  outer[0][1].adjacent.add(hexagon);
				}

		        
		        hexagonList.add(hexagon);
	    	}
    	}
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
		if (this.boardShape == Board.BOARD_GEOMETRY_RECT)
		{
			boardHeight = hCell * 7.0f - smallHexSideLength * (float) Math.sin((Math.PI/ 6.0));
		} else
		{
			boardHeight = hCell * 9.0f - smallHexSideLength * (float) Math.sin((Math.PI/ 6.0));
		}
		
		y0 = canvasHeight/2.0f - boardHeight/2.0f;	
				
		Log.d("hex","calculated position of board on canvas: x0="+x0+", y0="+y0);
	}
	
	Hexagon findHexagonFromPointOnCanvas(float x, float y){
	    Hexagon besthex = null;
	    float besthex_dist = smallHexSideLength*smallHexSideLength;
	    for( Hexagon hex: hexagonList ){
	       float dhex =  (hex.x-x)*(hex.x-x)+(hex.y-y)*(hex.y-y);
	       if( dhex < besthex_dist){
	           besthex = hex;
	           besthex_dist = dhex;
	       }
	    }
	    return besthex;
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
		float[] hexCellPos = {xRel, yRel};
		return hexCellPos;
	}
	static double sqr(double x ){
		return x*x;
	}
	static void findAdjacentHexagons( ArrayList<Hexagon> a )
	{
		final double dmax = 3.1 * sqr( smallHexSideLength );
		for( Hexagon u : a ){
			for( Hexagon v : a ){
				if( u != v && sqr( u.x-v.x ) + sqr( u.y - v.y ) < dmax ){
					u.adjacent.add(v);
				}
			}
		}
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
	
	static void addToSetSameColor( HashSet<Hexagon> s, Hexagon h ){
		if( s.contains(h))
			return;
		
		s.add(h);
		for( Hexagon u : h.adjacent ){
			if( u.color == h.color )
				addToSetSameColor( s, u );
		}
	}
	
	boolean isWinner( int p )
	{
		HashSet<Hexagon> s1 = new HashSet<Hexagon>();
		HashSet<Hexagon> s2 = new HashSet<Hexagon>();
		addToSetSameColor( s1, outer[p][0] );
		addToSetSameColor( s2, outer[p][1] );
		s1.retainAll(s2 );
		if( s1.size() > 0 ){
			Log.i("hex", "WINNER!");
			return true;
		} else {
			return false;
		}
	}
	
	static HashSet<Hexagon> allNeighbors( Hexagon a, int color )
	{
		HashSet<Hexagon> scol = new HashSet<Hexagon>();
		for( Hexagon hex : a.adjacent ){
			if( hex.color == color ){
				addToSetSameColor(scol, hex );
			}
		}
		scol.add(a);

		HashSet<Hexagon> erg = new HashSet<Hexagon>();
		for( Hexagon hex : scol ){
			for( Hexagon u : hex.adjacent ){
				if( u.color == UiView.HEX_UNUSED_COLOR ){
					erg.add(u);
				}
			}
		}
		return erg;
	}
			
	static HashMap<Hexagon, Double>	pathValue( HashSet<Hexagon> a, HashSet<Hexagon> a_opp, int color ){
		HashMap<Hexagon, Double> erg = new HashMap<Hexagon, Double>();
		HashSet<Hexagon> lastlevel = new HashSet<Hexagon>();
		for( Hexagon hex : a ){
			erg.put(hex,  1.0 );
			lastlevel.add(hex);
		}
		while (lastlevel.size() > 0){
			HashMap<Hexagon, Double> erg2 = new HashMap<Hexagon, Double>();
			HashSet<Hexagon> nextlevel = new HashSet<Hexagon>();
			lastlevel.removeAll(a_opp);
			for( Hexagon hex : lastlevel ){
				double newval = erg.get(hex) / 4.0;
				for( Hexagon next : allNeighbors(hex, color )){
					if( erg.containsKey(next))
						continue;
				
					if( erg2.containsKey(next))
						erg2.put( next, erg2.get(next)+newval );
					else
						erg2.put( next,  newval);
					nextlevel.add(next);
				}
			}
			erg.putAll(erg2);
			lastlevel = nextlevel;
		}		
		return erg;
	}
	
	HashMap<Hexagon, Double> analyze( int p ){
		final HashSet<Hexagon> swhite[] = new HashSet[2];
		final int thiscolor = outer[p][0].color;
		for( int i=0; i<2; ++i ){
			final HashSet<Hexagon> sblue= new HashSet<Hexagon>();
			addToSetSameColor( sblue, outer[p][i] );
			swhite[i]= new HashSet<Hexagon>();
			for( Hexagon hb : sblue ){
				for( Hexagon hex: hb.adjacent ){
					if( hex.color == UiView.HEX_UNUSED_COLOR )
						swhite[i].add(hex);
				}
			}
		}
		HashMap<Hexagon, Double> v1 = pathValue( swhite[0], swhite[1], thiscolor ); 
		HashMap<Hexagon, Double> v2 = pathValue( swhite[1], swhite[0], thiscolor );
		HashMap<Hexagon, Double> erg = new HashMap<Hexagon, Double>();		
		for( Hexagon hex : v1.keySet() ) if( v2.containsKey(hex)){
			double val = v1.get(hex) * v2.get(hex);
			erg.put( hex,  val );
		}
		return erg;
	} // end analyze

	Hexagon analyzeAll(){
		HashMap<Hexagon, Double> v1 = analyze(0);
		HashMap<Hexagon, Double> v2 = analyze(1);
		Hexagon besthex = null;
		double bestval = 0;
		for( Hexagon hex : v1.keySet() ) if( v2.containsKey(hex)){
			double val = v1.get(hex) + v2.get(hex);
			if( val > bestval ){
				bestval = val;
				besthex = hex;
			}
		}
		return besthex;
	}
}

