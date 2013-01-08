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

	public ArrayList<Hexagon> hexagonList = null;
	
	/* 'outer' represents the four outer regions of the board.  The first index indicates the player (0 or 1),
	 * the second index enumerates the both opposite regions of each player.  Those objects hold the set 
	 * of adjacent hexagons for each outer region   */
	
	Hexagon outer[][] = {{ new Hexagon(0, 0, UiView.BLUE), new Hexagon(0, 0, UiView.BLUE)},
								{new Hexagon(0, 0, UiView.GREEN), new Hexagon(0, 0, UiView.GREEN)}};
	
	public Board(int boardShape) {

		this.boardShape = boardShape;

		initialize();
	}

	private void initialize()
	{
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
		        color = UiView.HEX_UNUSED_COLOR;

		        hexagon = new Hexagon(i, j, color);
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

    	int iLowerLimit, iUpperLimit;

    	Hexagon hexagon;

    	for (int j = -1; j > -8; j--)
    	{
    		//corners of hexagonal board
    		iLowerLimit = (j % 2 == 0) ? 1 : 0;
			iUpperLimit = 6;
			
	    	for (int i = iLowerLimit; i < (iUpperLimit + 1); i++)
	    	{
		        hexagon = new Hexagon(i, j, UiView.HEX_UNUSED_COLOR);
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

	static void findAdjacentHexagons( ArrayList<Hexagon> a )
	{
		boolean isAdjacent;
		for( Hexagon u : a ){
			for( Hexagon v : a ){
				if( u != v ){					
					if (v.j == u.j) // if hexagons are on same row
					{
						isAdjacent = Math.abs(v.i - u.i) == 1;
					} else if (Math.abs(v.j - u.j) == 1) //if hexagons are on adjacent rows
					{
						if (v.j % 2 == 0) //if hexagons are on a row with an even value of j
						{
							isAdjacent = (v.i - u.i) > -1 && (v.i - u.i) < 2;
						} else //if hexagons are on a row with an odd value of j
						{
							isAdjacent = (v.i - u.i) > -2 && (v.i - u.i) < 1;
						}
					} else {
						isAdjacent = false;
					}
					
					if (isAdjacent)
					{
						u.adjacent.add(v);
					}
				}
			}
		}
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

