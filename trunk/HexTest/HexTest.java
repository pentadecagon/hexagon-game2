
import java.util.ArrayList;

import com.hexagongame.game.Board;
import com.hexagongame.game.Hexagon;
import com.hexagongame.game.Hexagon.HexSet;
import com.hexagongame.game.Solver;
import com.hexagongame.game.Solver1;
import com.hexagongame.game.Solver2;
import com.hexagongame.game.Solver6;

public class HexTest {
	static Solver make1(){ return new Solver1(4.0); }
	static Solver make2(){ return new Solver6(4.0, 3); }
	
	static int compete( Solver s1, Solver s2, Board b )
	{
		Solver2.mustWin[0] = Solver2.mustWin[1] = false;
		while(true){
			int id = b.getPlayerId();
			if( b.doMove( s1.bestMove(b) )){
				if( Solver2.mustWin[ 1-id ] )
					return 1/0;
				return 0;
			}
			if( b.doMove( s2.bestMove(b))){
				if( Solver2.mustWin[id ] )
					return 1/0;
				return 1;
			}
		}
	}
	static Board createBoard(){
		return new Board(0, 1);
	}
	
	static void printHistory( Board b, String s )
	{
		System.out.print( s );
		for( Hexagon hex : b.history ){
			System.out.print(hex.xid+", ");
		}
		System.out.println();
	}
	
	static boolean compete(){
		final int nhex = createBoard().hexagonList.size();
		int w_total = 0;
		int w_clean[] = { 0, 0 };
		int nhex2 = nhex * (nhex-1);
		System.out.println("nhex="+nhex+" "+nhex2 );
		for( int i=0; i<nhex; ++i ) for( int k=0; k<nhex; ++k ) if( i != k ){
			Solver s1 = make1();
			Solver s2 = make2();
			Board b1 = createBoard();
			b1.doMove( b1.hexagonList.get(i) );
			b1.doMove( b1.hexagonList.get(k) );
			final int win1 = compete( s1, s2, b1 );
			w_total += win1;
			Board b2 = createBoard();
			b2.doMove( b2.hexagonList.get(i) );
			b2.doMove( b2.hexagonList.get(k) );
			s1 = make1();
			s2 = make2();
			final int win2 = compete( s2, s1, b2 );
			w_total += win2;
			if( win1 != win2 ){
				w_clean[win1] += 1;
				if( win1 == -1 ){
					printHistory( b1, "F1: " );
					printHistory( b2, "F2: " );
					return true;
				} 
			}
//			System.out.println("WInner: " + win1 + " " + win2 );
		}
		System.out.println("total: " + (float)(w_total) / nhex2 + " nhex: " + nhex2 + " tot: " + w_total );
		System.out.println("clean: " + w_clean[0] + ' ' + w_clean[1] );
		return true;
	}
	
	public static void main(String[] args) {
		ArrayList<Hexagon> a0 = new ArrayList<Hexagon>();
		a0.add( new Hexagon(1,1,1,1));
		HexSet hs = new HexSet(a0);
		long t0 = System.nanoTime();
		compete();
		System.out.println("Time: "+(System.nanoTime()-t0) * 1e-9 );
	}
}
