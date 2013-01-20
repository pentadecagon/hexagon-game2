package com.hexagongame.game;

import java.util.ArrayList;
import java.util.HashSet;

//import android.util.Log;


public class Board{

	//hexagonal board
	public static final int BOARD_GEOMETRY_HEX = 0;
	
	//square board
	public static final int BOARD_GEOMETRY_RECT = 1;

	//constant giving shape of board: hexagonal, square etc
	public int boardShape = BOARD_GEOMETRY_HEX;
	
	//default board size
	public int boardSize = 1;

	public final ArrayList<Hexagon> hexagonList = new ArrayList<Hexagon>();

	private int playerTurn = 0;	
	public ArrayList<Hexagon> history = new ArrayList<Hexagon>();

	/* 'outer' represents the four outer regions of the board.  The first index indicates the player (0 or 1),
	 * the second index enumerates the both opposite regions of each player.  Those objects hold the set 
	 * of adjacent hexagons for each outer region   */
	
	Hexagon outer[][] = {{ new Hexagon(0, 0, Hexagon.OWNER_FIRST, -1), new Hexagon(0, 0, Hexagon.OWNER_FIRST, -2)},
								{new Hexagon(0, 0, Hexagon.OWNER_SECOND, -3), new Hexagon(0, 0, Hexagon.OWNER_SECOND, -4)}};
	
	public Board(int boardShape, int boardSize) {

		this.boardShape = boardShape;
		this.boardSize = boardSize;
		
		//construct the list of hexagons that will make up the board
		if (boardShape == Board.BOARD_GEOMETRY_RECT)
		{
			setupRectBoardListOfHexagons();
		} else
		{
			setupHexBoardListOfHexagons();
		}
		findAdjacentHexagons(hexagonList);
	}

	public int getPlayerId(){
		return playerTurn;
	}
	
	public synchronized void undo(){
		if ( history.size() > 0 )
		{
			Hexagon lastChange = history.remove(history.size()-1);
			lastChange.owner = Hexagon.OWNER_EMPTY;
			playerTurn = 1-playerTurn;
		}
	}	
	
	public synchronized boolean haveHistory(){
		return history.size() > 0;
	}
	
	public synchronized void doMove( Hexagon move )
	{
		move.owner = playerTurn;
		playerTurn = 1 - playerTurn;
		history.add( move );
	}
		
	private void setupHexBoardListOfHexagons()
	{
		int r=2+boardSize;
		int id = 0;
		for( int i=-r; i<=r; ++i )  for( int k=-r; k<=r; ++k )
			if( Math.abs(i+k) <= r ){
				Hexagon hex = new Hexagon( i+k*0.5f, k, Hexagon.OWNER_EMPTY, id++ );
				if( Math.abs(i) == r || Math.abs(k) == r || Math.abs( i+k ) == r ){ // its at the edge
					final int x = 2*i+k; 
					if( x>=1 && k>=0 )
						outer[0][0].adjacent.add(hex);
					if( x>=-1 && k<=0 )
						outer[1][0].adjacent.add(hex);
					if( x<=1 && k>=0 )
						outer[1][1].adjacent.add(hex);
					if( x<=-1 && k<=0 )
						outer[0][1].adjacent.add(hex);
				}
		        hexagonList.add(hex);
			}
//		Log.i("hex", "out size: 0: " + outer[0][0].adjacent.size()+" "+outer[0][1].adjacent.size() + " 1: " + outer[1][0].adjacent.size() + " " + outer[1][1].adjacent.size() );
	}
	
	private void setupRectBoardListOfHexagons()
	{
		final int ymax = 4 + 2 * boardSize;
		final int xmax = 4 + 2 * boardSize;
		int id = 0;
		for( int yi=0; yi<=ymax; ++yi )
			for( float xi = (yi%2) * 0.5f; xi<=xmax; ++xi ){
				final Hexagon hex = new Hexagon( xi, yi, Hexagon.OWNER_EMPTY, id++ );
				hexagonList.add(hex);
				if( yi==0 )
					outer[1][0].adjacent.add(hex);
				if( yi == ymax )
					outer[1][1].adjacent.add(hex);
				if( xi<1 )
					outer[0][0].adjacent.add(hex);
				if( xi>xmax-1 )
					outer[0][1].adjacent.add(hex);
			}
	}

	static void findAdjacentHexagons( ArrayList<Hexagon> a )
	{
		for( Hexagon p : a ){ for( Hexagon q : a ){
			if( p != q ){
				final float dx = p.xi-q.xi;
				final float dy = p.yi-q.yi;
				if( dx*dx+dy*dy < 1.5 ){
					p.adjacent.add(q);
				}
			}}
//			Log.i("hex", "adjacent: " + p.adjacent.size() );
		}
	}

	static void addToSetSameColor( HashSet<Hexagon> s, Hexagon h ){
		if( s.contains(h))
			return;
		
		s.add(h);
		for( Hexagon u : h.adjacent ){
			if( u.owner == h.owner )
				addToSetSameColor( s, u );
		}
	}
	
	public boolean isWinner( int p )
	{
		HashSet<Hexagon> s1 = new HashSet<Hexagon>();
		HashSet<Hexagon> s2 = new HashSet<Hexagon>();
		addToSetSameColor( s1, outer[p][0] );
		addToSetSameColor( s2, outer[p][1] );
		s1.retainAll(s2 );
		if( s1.size() > 0 ){
//			Log.i("hex", "WINNER!");
			return true;
		} else {
			return false;
		}
	}
}
