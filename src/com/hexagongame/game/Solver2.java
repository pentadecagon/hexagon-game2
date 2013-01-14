package com.hexagongame.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.hexagongame.game.Solver3.HexPair;

public class Solver2 implements Solver {

	private final double _lengthFactor;
	private final int _midSize;
	public Solver2( double f, int m ){
		_midSize = m;
		_lengthFactor = f;
	}

	private static class Blob {
		final HashSet<Hexagon> members = new HashSet<Hexagon>();
		final HashSet<Hexagon> edge = new HashSet<Hexagon>();
	};
	
	static HashSet<Hexagon> allNeighbors( Hexagon a, int owner, HashMap<Hexagon, Blob> blobs )
	{
		HashSet<Hexagon> scol = new HashSet<Hexagon>();
		HashSet<Hexagon> erg = new HashSet<Hexagon>();
		for( Hexagon hex : a.adjacent ){
			if( hex.isEmpty() ){
				erg.add(hex);
			} else if( hex.owner == owner ){
				erg.addAll(blobs.get(hex).edge);
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
	HashMap<Hexagon, Double>	pathValue( Hexagon outer, HashSet<Hexagon> a, HashSet<Hexagon> a_opp, int color, HashMap<Hexagon, HashSet<Hexagon>> neighbors  ){
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
				for( Hexagon next : neighbors.get(hex)) if( ! erg.containsKey(next)){
					double oldval = 0;
					for( HexPair hp : lastlevel.get(hex)){
						if( ! neighbors.get(next).contains(hp.hex) ){
							oldval += hp.d;
						}
					}
/*					if( oldval != erg.get(hex)){
						double u = erg.get(hex);
						new HexPair( null, 1/0+u );
					} */
					double newval = oldval / _lengthFactor;
					updateMap( erg2, next, newval );
					updateMap( nextlevel, next, new HexPair( hex, newval ));
				}
			}
			for( Hexagon hex : nextlevel.keySet() ) if( ! a_opp.contains(hex)){
				for( Hexagon next : hex.adjacent) if( nextlevel.containsKey(next)){
					double oldval = 0;
					for( HexPair hp : nextlevel.get(hex)){  // we walk hp -> hex -> next
						if( ! nextlevel.containsKey(hp.hex) && ! neighbors.get(next).contains(hp.hex)){
							oldval += hp.d;
						}
					}
					double newval = oldval / _lengthFactor;
					updateMap( erg2, next, newval );
					updateMap( nextlevel, next, new HexPair( hex, newval ));
				}
			}
			erg.putAll(erg2);
			lastlevel = nextlevel;
		}		
		return erg;
	}
		
	Blob createBlob( Hexagon hex )
	{
		Blob erg = new Blob();
		Board.addToSetSameColor(erg.members, hex );
		for( Hexagon hb : erg.members ){
			for( Hexagon ha : hb.adjacent ){
				if( ha.isEmpty() )
					erg.edge.add(ha);
			}
		}
		return erg;
	}
	
	private void addBlobToMap( Blob blob, HashMap<Hexagon, Blob> map, HashSet<Hexagon> middle ){
		HashSet<Blob> bset = new HashSet<Blob>( map.values());
		for( Blob b1: bset ){
			HashSet<Hexagon> edgemix = new HashSet<Hexagon>( b1.edge );
			edgemix.retainAll(blob.edge );
			edgemix.removeAll(middle);
			if( edgemix.size() >= _midSize ){ // join them
				middle.addAll(edgemix);
				blob.members.addAll(b1.members);
				blob.edge.addAll(b1.edge);
			}
		}
		for( Hexagon hex : blob.members ){
			map.put( hex,  blob );
		}
	}
	
	static public boolean mustWin[] = new boolean[2];
	
	
	HashMap<Hexagon, Double> analyze( Board board,  int owner ){
		final Blob outer0 = createBlob( board.outer[owner][0] );
		final Blob outer1 = createBlob( board.outer[owner][1] );
		
		final HashMap<Hexagon, Blob> blobs = new HashMap<Hexagon, Blob>();
		HashSet<Hexagon> middle = new HashSet<Hexagon>();
		addBlobToMap( outer0, blobs, middle );
		addBlobToMap( outer1, blobs, middle );
		findBlobs( board, owner, blobs, middle );
		final HashMap<Hexagon, HashSet<Hexagon>> neighbors = findNeighbors( board.hexagonList, blobs );
		final HashMap<Hexagon, Double> erg = new HashMap<Hexagon, Double>();
		if( blobs.get( board.outer[owner][0]) == blobs.get( board.outer[owner][1])){
			for( Hexagon hex : middle ){
				erg.put( hex, 1.0 );
			}
			mustWin[owner] = true;
			return erg;
		}

		final HashMap<Hexagon, Double> v1 = pathValue( board.outer[owner][0], outer0.edge, outer1.edge, owner, neighbors  ); 
		final HashMap<Hexagon, Double> v2 = pathValue( board.outer[owner][1], outer1.edge, outer0.edge, owner, neighbors );
		
		for( Hexagon hex : v1.keySet() ) if( v2.containsKey(hex)){
			double val = v1.get(hex) * v2.get(hex);
/*			if( middle.contains(hex)){
				val *= 0.125;
			} */
			erg.put( hex,  val );
		}
		return erg;
	} // end analyze

	private HashMap<Hexagon, HashSet<Hexagon>> findNeighbors(
			ArrayList<Hexagon> hexagonList, HashMap<Hexagon, Blob> blobs) {
		HashMap<Hexagon, HashSet<Hexagon>> erg = new HashMap<Hexagon, HashSet<Hexagon>>();
		for( Hexagon hex :  hexagonList ){
			 HashSet<Hexagon> hs = new HashSet<Hexagon>();
			 for( Hexagon h1 : hex.adjacent ) if( h1.isEmpty() ){
				 hs.add(h1);
			 }
			erg.put( hex,  hs );
		}
		for( Blob b : blobs.values() ){
			for( Hexagon h : b.edge ){
				HashSet<Hexagon> hs = erg.get(h);
				for( Hexagon h1 : b.edge ) if( h1 != h && h1.isEmpty() ){
					hs.add(h1);
				}
			}
		}
		return erg;
	}

	private void findBlobs(Board board, int owner, HashMap<Hexagon, Blob> erg, HashSet<Hexagon> middle ) {
		for( Hexagon hex : board.hexagonList ){
			if( hex.owner == owner && ! erg.containsKey(hex)){
				addBlobToMap( createBlob( hex ), erg, middle );
			}
		}
	}
	
	public Hexagon bestMove( Board board ){
		HashMap<Hexagon, Double> v1 = analyze(board, 0);
		HashMap<Hexagon, Double> v2 = analyze(board, 1);
		Hexagon besthex = null;
		double bestval = 0;
		for( Hexagon hex : v1.keySet() ) if( v2.containsKey(hex)){
			double val = (v1.get(hex) + v2.get(hex)) * (1.0+hex.xi*2e-12 + hex.yi*1e-13);
			if( val == bestval){
				bestval = 1/0;
			}
			if( val > bestval ){
				bestval = val;
				besthex = hex;
			}
		}
		return besthex;
	}
	
}
