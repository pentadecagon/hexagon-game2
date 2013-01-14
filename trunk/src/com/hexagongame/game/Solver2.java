package com.hexagongame.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Solver2 implements Solver {

	private final double _lengthFactor;
	
	public Solver2( double f ){
		_lengthFactor = f;
	}
	public Solver2(){
		_lengthFactor = 4.0f;
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
			
	HashMap<Hexagon, Double>	pathValue( HashSet<Hexagon> a, HashSet<Hexagon> a_opp, int color, HashMap<Hexagon, Blob> blobs ){
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
				double newval = erg.get(hex) / _lengthFactor;
				for( Hexagon next : allNeighbors(hex, color, blobs )){
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
	
	private static void addBlobToMap( Blob blob, HashMap<Hexagon, Blob> map, HashSet<Hexagon> middle ){
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
	
	static public boolean mustWin[] = new boolean[2];
	
	HashMap<Hexagon, Double> analyze( Board board,  int owner ){
		final Blob outer0 = createBlob( board.outer[owner][0] );
		final Blob outer1 = createBlob( board.outer[owner][1] );
		
		final HashMap<Hexagon, Blob> blobs = new HashMap<Hexagon, Blob>();
		HashSet<Hexagon> middle = new HashSet<Hexagon>();
		addBlobToMap( outer0, blobs, middle );
		addBlobToMap( outer1, blobs, middle );
		findBlobs( board, owner, blobs, middle );
		final HashMap<Hexagon, Double> erg = new HashMap<Hexagon, Double>();
		if( blobs.get( board.outer[owner][0]) == blobs.get( board.outer[owner][1])){
			for( Hexagon hex : middle ){
				erg.put( hex, 1.0 );
			}
			mustWin[owner] = true;
			return erg;
		}

		final HashMap<Hexagon, Double> v1 = pathValue( outer0.edge, outer1.edge, owner, blobs ); 
		final HashMap<Hexagon, Double> v2 = pathValue( outer1.edge, outer0.edge, owner, blobs );
		
		for( Hexagon hex : v1.keySet() ) if( v2.containsKey(hex)){
			double val = v1.get(hex) * v2.get(hex);
/*			if( middle.contains(hex)){
				val *= 0.125;
			} */
			erg.put( hex,  val );
		}
		return erg;
	} // end analyze

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
