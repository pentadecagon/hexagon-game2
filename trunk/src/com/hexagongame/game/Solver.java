package com.hexagongame.game;

import java.util.HashMap;
import java.util.HashSet;

public class Solver {
	
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
		HashMap<Hexagon, Double> v1 = pathValue( swhite[0], swhite[1], thiscolor ); 
		HashMap<Hexagon, Double> v2 = pathValue( swhite[1], swhite[0], thiscolor );
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
			double val = v1.get(hex) + v2.get(hex);
			if( val > bestval ){
				bestval = val;
				besthex = hex;
			}
		}
		return besthex;
	}
	
}
