package com.hexagongame.game;

import java.util.ArrayList;
import java.util.HashSet;

import android.util.Log;

import com.hexagongame.game.Hexagon.HexSet;

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

	public final ArrayList<Hexagon> hexagonList=  new ArrayList<Hexagon>();

	// _player that must do the next move
	private int _player = 0;	
	
	/* _hasWinner indicates if we have a winner.
		If _hasWinner is true, no move must be done 
		and _player indicates who won. */
	private boolean _hasWinner = false;
	public final ArrayList<Hexagon> history = new ArrayList<Hexagon>();

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
		findNeighbors();
	}

	public int getPlayerId(){
		return _player;
	}
	
	public synchronized void undo(){
		if ( history.size() > 0 )
		{
			Hexagon lastChange = history.remove(history.size()-1);
			if( _hasWinner ){
				lastChange.owner = Hexagon.OWNER_EMPTY;
				_hasWinner=false;
				return;
			}
			undoNeighbors(lastChange);
			lastChange.owner = Hexagon.OWNER_EMPTY;
			_player = 1-_player;
//			consistency();
		}
	}	
	
	public synchronized boolean haveHistory(){
		return history.size() > 0;
	}
	
	static void xassert( boolean b ){
		if( ! b ){
			int ii = 1/0;
		}
	}
	
	void consistency(){
		for( int n=0; n<2; ++n ){
			for( Hexagon h : hexagonList ) if( h.isEmpty()){
				for( Hexagon h1 : h.neighbors[n]){
					xassert( h1.isEmpty() || h1.xid<0 );
					xassert( h1.neighbors[n].contains(h));
					xassert( h1 != h );
				}
			}
		}
	}
	
	private void updateNeighbors( Hexagon hex )
	{
		final int n = hex.owner;
		final HexSet myNeighbors = hex.neighbors[n];
/*		Hexagon edge = null;
		for( int k=0; k<2; ++k )
			if( myNeighbors.contains(outer[n][k]) )
				edge = outer[n][k];
		
		if( edge != null ){
			for( Hexagon h1: myNeighbors ){
				h1.push(n);
				 if( h1 != edge ){
					 h1.neighbors[n].removeAll(myNeighbors);
					 h1.neighbors[n].add( edge );
				 } else {
						h1.neighbors[n].addAll(myNeighbors);
						h1.neighbors[n].remove(h1);
				 }
			}
		} else {  */
			for( Hexagon h1: myNeighbors ){
				h1.push(n);
				h1.neighbors[n].addAll(myNeighbors);
				h1.neighbors[n].remove(h1);
			}
//		}
		for( int i=0; i<2; ++i ) for( Hexagon h1: hex.neighbors[i] ){
			h1.neighbors[i].remove(hex);
		}
//		consistency();
	}
	
	private void undoNeighbors( Hexagon hex )
	{
		final int n = hex.owner;
		final HexSet myNeighbors = hex.neighbors[n];
		for( Hexagon h1: myNeighbors ){
			h1.pop(n);
		}		
		for( Hexagon h1: hex.neighbors[1-n] ){
			h1.neighbors[1-n].add(hex);
		}
	}
	
	public synchronized boolean doMove( Hexagon move )
	{
		xassert( !_hasWinner );
		move=hexagonList.get(move.xid);
		move.owner = _player;
		history.add( move );
		if( move.neighbors[_player].contains( outer[_player][0] ) && move.neighbors[_player].contains( outer[_player][1] ) ){
			_hasWinner = true;
			return true;
		}
		_player = 1 - _player;
		updateNeighbors(move);
		return false;
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
					if( x>=boardSize && k>=0 )
						outer[0][0].adjacent.add(hex);
					if( x>=-boardSize && k<=0 )
						outer[1][0].adjacent.add(hex);
					if( x<=boardSize && k>=0 )
						outer[1][1].adjacent.add(hex);
					if( x<=-boardSize && k<=0 )
						outer[0][1].adjacent.add(hex);
				}
		        hexagonList.add(hex);
			}
		Log.i("hex", "out size: 0: " + outer[0][0].adjacent.size()+" "+outer[0][1].adjacent.size() + " 1: " + outer[1][0].adjacent.size() + " " + outer[1][1].adjacent.size() );
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

	void findNeighbors()
	{
		for( Hexagon p : hexagonList ){ 
			p.neighbors[0] = new HexSet( p.adjacent );
			p.neighbors[1] = new HexSet( p.adjacent );
		}
		for( int p =0; p<=1; ++p ) for( int k=0; k<=1; ++k ){
			outer[p][k].neighbors[1-p] = new HexSet();
			outer[p][k].neighbors[p] = new HexSet( outer[p][k].adjacent );
			for( Hexagon hex :outer[p][k].adjacent ){
				hex.neighbors[p].add( outer[p][k]);
			}
		}
		consistency();
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

	public ArrayList<Integer> getHashKey() {
		ArrayList<Integer> a = new ArrayList<Integer>();
		for( Hexagon h : hexagonList ){
			if( h.owner == 0 )
				a.add(h.xid);
			else if( h.owner == 1 )
				a.add(-h.xid-1);
		}
		return a;
	}
}

