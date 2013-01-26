package com.hexagongame.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Solver3 implements Solver {

	private final float _lengthFactor;
	
	public Solver3( float f ){
		_lengthFactor = f;
	}
	public Solver3(){
		_lengthFactor = 4.0f;
	}
	
	static HashSet<Hexagon> allNeighbors( Hexagon a, int owner )
	{
		HashSet<Hexagon> scol = new HashSet<Hexagon>();
		for( Hexagon hex : a.adjacent ){
			if( hex.owner == owner ){
				Board.addToSetSameColor(scol, hex );
			}
		}
		scol.add(a);

		HashSet<Hexagon> erg = new HashSet<Hexagon>();
		for( Hexagon hex : scol ){
			for( Hexagon u : hex.adjacent ){
				if( u.isEmpty() ){
					erg.add(u);
				}
			}
		}
		return erg;
	}
	
	static void updateMap( HashMap<Hexagon, Double>map, Hexagon a, double b ){
		if( map.containsKey(a)){
			map.put( a, map.get(a) + b );
		} else {
			map.put( a,  b );
		}
	}
	static void updateMap( HashMap<Hexagon, List<HexPair> > map, Hexagon a, HexPair b ){
		if( ! map.containsKey(a)){
			map.put( a,  new ArrayList<HexPair>() );
		}
		map.get(a).add(b);
	}
	static class HexPair {
		HexPair( Hexagon h0, double d0 ){
			hex=h0;
			d=d0;
		}
		final Hexagon hex;
		final double d;
	}
	HashMap<Hexagon, Double>	pathValue( Hexagon outer, HashSet<Hexagon> a, HashSet<Hexagon> a_opp, int color ){
		final HashMap<Hexagon, Double> erg = new HashMap<Hexagon, Double>();
		HashMap<Hexagon, List<HexPair>> lastlevel = new HashMap<Hexagon, List<HexPair>>();
		for( Hexagon hex : a ){
			erg.put(hex,  1.0 );
			updateMap( lastlevel, hex, new HexPair( outer, 1.0 ) );
		}
		while (lastlevel.size() > 0){
			final HashMap<Hexagon, Double> erg2 = new HashMap<Hexagon, Double>();
			final HashMap<Hexagon, List<HexPair>> nextlevel = new HashMap<Hexagon, List<HexPair>>();
			for( Hexagon hex : lastlevel.keySet() ) if( ! a_opp.contains(hex)){
				for( Hexagon next : allNeighbors(hex, color )) if( ! erg.containsKey(next)){
					double oldval = 0;
					for( HexPair hp : lastlevel.get(hex)){
						if( ! hp.hex.adjacent.contains(next)){
							oldval += hp.d;
						}
					}
					double newval = oldval / _lengthFactor;
					updateMap( erg2, next, newval );
					updateMap( nextlevel, next, new HexPair( hex, newval ));
				}
			}
/*			for( Hexagon hex : nextlevel.keySet() ) if( ! a_opp.contains(hex)){
				for( Hexagon next : hex.adjacent) if( nextlevel.containsKey(next)){
					double oldval = 0;
					for( HexPair hp : nextlevel.get(hex)){  // we walk hp -> hex -> next
						if( ! nextlevel.containsKey(hp.hex) && ! hp.hex.adjacent.contains(next)){
							oldval += hp.d;
						}
					}
					double newval = oldval / _lengthFactor;
					updateMap( erg2, next, newval );
					updateMap( nextlevel, next, new HexPair( hex, newval ));
				}
			} */
			erg.putAll(erg2);
			lastlevel = nextlevel;
		}		
		return erg;
	}
	
	HashMap<Hexagon, Double> analyze( Board board,  int p ){
		final HashSet<Hexagon> swhite[] = new HashSet[2];
		final int thiscolor = board.outer[p][0].owner;
		for( int i=0; i<2; ++i ){
			final HashSet<Hexagon> sblue= new HashSet<Hexagon>();
			Board.addToSetSameColor( sblue, board.outer[p][i] );
			swhite[i]= new HashSet<Hexagon>();
			for( Hexagon hb : sblue ){
				for( Hexagon hex: hb.adjacent ){
					if( hex.isEmpty() )
						swhite[i].add(hex);
				}
			}
		}
		HashMap<Hexagon, Double> v1 = pathValue( board.outer[p][0], swhite[0], swhite[1], thiscolor ); 
		HashMap<Hexagon, Double> v2 = pathValue( board.outer[p][1], swhite[1], swhite[0], thiscolor );
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
			double val = (v1.get(hex) + v2.get(hex)) * (1.0+hex.xi*1e-10 + hex.yi*1e-11);
			
			if( val > bestval ){
				bestval = val;
				besthex = hex;
			}
		}
		return besthex;
	}
	
}
