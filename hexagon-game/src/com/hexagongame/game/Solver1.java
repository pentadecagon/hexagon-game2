package com.hexagongame.game;

import java.util.HashMap;
import java.util.HashSet;

import com.hexagongame.game.Hexagon.HexSet;

public class Solver1 implements Solver {

	private final double _ilengthFactor;
	
	public Solver1( double f ){
		_ilengthFactor = 1.0 / f;
	}
	
	static HexSet allNeighbors( Hexagon a, int owner )
	{
		return a.neighbors[owner];
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
		HashMap<Hexagon, Double> v1 = pathValue( s1, s2, p ); 
		HashMap<Hexagon, Double> v2 = pathValue( s2, s1, p );
		HashMap<Hexagon, Double> erg = new HashMap<Hexagon, Double>();		
		for( Hexagon hex : v1.keySet() ) if( v2.containsKey(hex)){
			double val = v1.get(hex) * v2.get(hex);
			erg.put( hex,  val );
		}
		return erg;
	} // end analyze

	public Hexagon bestMove( Board board ){
		HashMap<Hexagon, Double> v1 = analyze(board, 0);
		HashMap<Hexagon, Double> v2 = analyze(board, 1);
		Hexagon besthex = null;
		double bestval = 0;
		for( Hexagon hex : v1.keySet() ) if( v2.containsKey(hex)){
			double val = (v1.get(hex) + v2.get(hex)) * (1.0+hex.xid*1e-13);
			
			if( val > bestval ){
				bestval = val;
				besthex = hex;
			}
		}
		return besthex;
	}
	
}
