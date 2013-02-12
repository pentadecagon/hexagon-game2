package com.hexagongame.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import android.os.SystemClock;

import com.hexagongame.game.Hexagon.HexSet;

public class Solver6 implements Solver {

	private final double _ilengthFactor;
	final int depth0;
	private long _endTime; 
	final private long _maxTime; // maximum milliseconds for analysis

	public Solver6( double f, int d0, long maxt ){
		_ilengthFactor = 1.0/f;
		depth0 = d0;
		_maxTime = maxt;
	}
	
	static HexSet allNeighbors( Hexagon a, int owner )
	{
		HexSet erg = a.neighbors[owner];
		Board.xassert( erg != null );
		return erg;
	}
			
	HashMap<Hexagon, Double>	pathValue( HexSet a, HexSet a_opp, int color ){
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
				double newval = erg.get(hex) * _ilengthFactor;
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
	
	HashMap<Hexagon, Double> analyze( Board board,  int p ){
		final HexSet s1 = board.outer[p][0].neighbors[p];
		final HexSet s2 = board.outer[p][1].neighbors[p];
		final HashMap<Hexagon, Double> v1 = pathValue( s1, s2, p ); 
		final HashMap<Hexagon, Double> v2 = pathValue( s2, s1, p );
		HashMap<Hexagon, Double> erg = new HashMap<Hexagon, Double>();		
		for( Hexagon hex : v1.keySet() ) if( v2.containsKey(hex)){
			double val = v1.get(hex) * v2.get(hex);
			erg.put( hex,  val );
		}
		return erg;
	} // end analyze

	static class ValHex implements Comparable<ValHex> {
		final double _v;
		final Hexagon _h;
		ValHex( double v, Hexagon h ){ _v=v; _h=h; }
		public int compareTo( ValHex b ){
			return  _v > b._v ? -1 : 1;
		}
	}
	
	
	final HashMap<ArrayList<Integer>, ArrayList<ValHex> > cache = new HashMap<ArrayList<Integer>, ArrayList<ValHex> >(); 
	
	int k1=0;
	int k2=0;
	public ArrayList<ValHex> calcValues( Board board ){
		ArrayList<Integer> hashkey = board.getHashKey();
		if( cache.containsKey(hashkey)){
			k1 +=1;
			return cache.get(hashkey);
		}
		k2 += 1;
		ArrayList<ValHex> vh = new ArrayList<ValHex>();
		final int n = board.getPlayerId();
		HashMap<Hexagon, Double> v1 = analyze(board, n);
		HashMap<Hexagon, Double> v2 = analyze(board, 1-n);
		for( Hexagon hex : v1.keySet() ) if( v2.containsKey(hex)){
			double val = (v1.get(hex) + v2.get(hex)*1.0) * (1.0+hex.xid*1e-13);
			vh.add( new ValHex( val, hex ));
		}
		Collections.sort(vh);
		if( vh.size() > depth0 )
			vh = new ArrayList<ValHex>( vh.subList( 0,  depth0 ));
		cache.put( hashkey, vh);
		return vh;
	}
		
		
	public ValHex canWin( Board board, int recursion ){
		final int dmax =  recursion < depth0 ? depth0-recursion : 1;
		final 	ArrayList<ValHex> vh = calcValues( board );
		final ArrayList<ValHex> rh = new ArrayList<ValHex>();
		for( int i=0; i<Math.min(dmax, vh.size()); ++i ){
			if( board.doMove(vh.get(i)._h) ){
				board.undo();
				return new ValHex( 1.0, vh.get(i)._h );
			}
			rh.add( new ValHex( -0.5*canWin(board, recursion+1)._v, vh.get(i)._h));
			board.undo();
			if(  System.currentTimeMillis()  > _endTime ){
				break;
			}
		}
		Collections.sort(rh);
		
		return rh.get(0);
	}
	
	public Hexagon bestMove( Board board ){
		if( board.haveHistory() )
			_endTime =  System.currentTimeMillis() + _maxTime;
		else // the phone should make the first move quickly
			_endTime =  System.currentTimeMillis() + _maxTime / 3;
		Hexagon erg =  canWin(board, 0)._h;
//		System.out.println(k1+" "+k2);
		return erg;
	}
}
