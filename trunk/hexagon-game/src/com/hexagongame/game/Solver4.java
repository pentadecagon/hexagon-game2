package com.hexagongame.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Solver4 implements Solver {

	private final double _lengthFactor;
	private final int _midSize;
	public Solver4( double f, int m ){
		_midSize = m;
		_lengthFactor = f;
	}

	static class Blob {
		final HashSet<Hexagon> members = new HashSet<Hexagon>();
		final HashSet<Hexagon> edge = new HashSet<Hexagon>();
	};
	
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

	static int xassert( boolean b ){
		if( ! b )
			return 1/0;
		else
			return 1;
	}
	
	HashMap<Hexagon, List<HexPair>> 	pathValue( Hexagon outer, HashSet<Hexagon> a, HashSet<Hexagon> a_opp, int color, HashMap<Hexagon, HashSet<Hexagon>> neighbors  ){
		final HashMap<Hexagon, List<HexPair>>  erg = new HashMap<Hexagon, List<HexPair>> ();
		HashMap<Hexagon, List<HexPair>> lastlevel = new HashMap<Hexagon, List<HexPair>>();
		for( Hexagon hex : a ){
			updateMap( lastlevel, hex, new HexPair( outer, 1.0 ) );
		}
		while (lastlevel.size() > 0){
			erg.putAll( lastlevel );
			final HashMap<Hexagon, List<HexPair>> nextlevel = new HashMap<Hexagon, List<HexPair>>();
			for( Hexagon hex : lastlevel.keySet() ) if( ! a_opp.contains(hex)){
				for( Hexagon next : neighbors.get(hex)) if( ! erg.containsKey(next)){
					double oldval = 0;
					for( HexPair hp : lastlevel.get(hex)){
						if( ! neighbors.get(next).contains(hp.hex) )
							oldval += hp.d;
					}
					double newval = oldval / _lengthFactor;
					updateMap( nextlevel, next, new HexPair( hex, newval ));
				}
			}
/*			for( Hexagon hex : nextlevel.keySet() ) if( ! a_opp.contains(hex)){
				for( Hexagon next : neighbors.get(hex)) if( nextlevel.containsKey(next)){
					double oldval = 0;
					for( HexPair hp : nextlevel.get(hex)){  // we walk hp -> hex -> next
						if( ! nextlevel.containsKey(hp.hex)  && ! neighbors.get(next).contains(hp.hex))
							oldval += hp.d;
					}
					double newval = oldval / _lengthFactor;
					nextlevel.get(next).add(new HexPair( hex, newval ));
				}
			} */
			lastlevel = nextlevel;
		}		
		return erg;
	}
		
	Blob createBlob( Hexagon hex )
	{
		Blob erg = new Blob();
		Board.addToSetSameColor(erg.members, hex );
		for( Hexagon hb : erg.members ){
			xassert( hex.owner<2 && hex.owner == hb.owner );
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
			if( edgemix.size() >= 2 ){ // join them
				middle.addAll(edgemix);
				blob.members.addAll(b1.members);
				blob.edge.addAll(b1.edge);
			}
		} 
		for( Hexagon hex : blob.members ){
			map.put( hex,  blob );
		}
	}
		
	
	HashMap<Hexagon, Double> analyze( Board board,  int owner ){
		final Blob outer0 = createBlob( board.outer[owner][0] );
		final Blob outer1 = createBlob( board.outer[owner][1] );
		
		final HashMap<Hexagon, Blob> blobs = new HashMap<Hexagon, Blob>();
		HashSet<Hexagon> middle = new HashSet<Hexagon>();
		addBlobToMap( outer0, blobs, middle );
		addBlobToMap( outer1, blobs, middle );
		findBlobs( board, owner, blobs, middle );
/*		for( Hexagon hex : outer0.members )
			blobs.remove(hex);
		for( Hexagon hex : outer1.members )
			blobs.remove(hex); */
		
		final HashMap<Hexagon, HashSet<Hexagon>> neighbors = findNeighbors( board.hexagonList, blobs, owner );
		final HashMap<Hexagon, Double> erg = new HashMap<Hexagon, Double>();
		if( blobs.get( board.outer[owner][0]) == blobs.get( board.outer[owner][1])){
			for( Hexagon hex : middle ){
				erg.put( hex, 1.0 );
			}
			Solver2.mustWin[owner] = true;
			return erg;
		} 

		final HashMap<Hexagon, List<HexPair>>  v1 = pathValue( board.outer[owner][0], blobs.get( board.outer[owner][0]).edge, blobs.get( board.outer[owner][1]).edge, owner, neighbors  ); 
		final HashMap<Hexagon, List<HexPair>>  v2 = pathValue( board.outer[owner][1], outer1.edge, outer0.edge, owner, neighbors );
		
		for( Hexagon hex : v1.keySet() ) if( v2.containsKey(hex)){
			double val = 0;
			for( HexPair hp1 : v1.get(hex)){
				final HashSet<Hexagon> xn = neighbors.get( hp1.hex );
				for( HexPair hp2 : v2.get(hex)){
//					if( hp1.hex != hp2.hex && ( xn==null || ! xn.contains(hp2.hex)))
						val += hp1.d * hp2.d;
				}
			}
//			if( middle.contains(hex))
//				val *= 0.5;
			erg.put( hex,  val );
		}
		return erg;
	} // end analyze
	
	private HashMap<Hexagon, HashSet<Hexagon>> findNeighbors(
			ArrayList<Hexagon> hexagonList, HashMap<Hexagon, Blob> blobs, int owner) {
		HashMap<Hexagon, HashSet<Hexagon>> erg = new HashMap<Hexagon, HashSet<Hexagon>>();
		for( Hexagon hex :  hexagonList ) if( hex.isEmpty() ){
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
			double val = (v1.get(hex) + v2.get(hex)) * (1.0+hex.xid*1e-13);
			if( val == bestval && val>0 ){
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
