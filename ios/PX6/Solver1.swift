//
//  Solver1.swift
//  PX6
//
//  Created by Thomas Kunert on 30.07.14.
//  Copyright (c) 2014 Thomas Kunert. All rights reserved.
//

import Foundation

func removeAll( _ a : inout HexSet, b : HexSet )
{
    for (hex, _) in b {
        a[hex] = nil
    }
}

class Solver1 {
    let _ilengthFactor : Double
    
    init( f : Double ){
        _ilengthFactor = 1.0 / f
    }
    
    
/*    static HexSet allNeighbors( Hexagon a, int owner )
    {
        return a.neighbors[owner];
    }*/
    
    func pathValue( _ a: HexSet, a_opp: HexSet, color: Int ) -> [Hexagon : Double]{
        var erg = [Hexagon:Double]()
        var lastlevel = HexSet()
        for (hex, _) in a {
            erg[hex] = 1.0
            lastlevel[hex] = true
        }
        while lastlevel.count > 0 {
            var erg2 = [Hexagon:Double]()
            var nextlevel = HexSet()
            removeAll(&lastlevel, b: a_opp)
            for (hex, _) in lastlevel {
                let newval = erg[hex]! * _ilengthFactor
                for (next, _) in hex.neighbors[color] {
                    if erg[next] != nil {
                        continue
                    }
                    let curval = erg2[next]
                    erg2[next] = curval != nil ? curval! + newval : newval
                    nextlevel[next] = true
                }
            }
            for (u,v) in erg2 {
                erg[u] = v
            }
            lastlevel = nextlevel
        }
        print("pathvalue: \(erg.count)")
        return erg
    }

    func analyze( _ board: Board, p: Int ) -> [Hexagon : Double]{
        let s1: HexSet = board.outer[p][0].neighbors[p]
        let s2: HexSet = board.outer[p][1].neighbors[p]
        let v1: [Hexagon : Double] = pathValue( s1, a_opp: s2, color: p )
        let v2 = pathValue( s2, a_opp: s1, color: p );
        var erg = [Hexagon : Double]()
        for (hex, y1) in v1 {
            if (v2[hex] != nil) {
                let val = y1 * v2[hex]!
                erg[hex] = val
            }
        }
        assert( erg.count > 0 )
        return erg
    }
    
    func bestMove( _ board:Board ) ->Hexagon {
        let v1 = analyze( board, p: 0 )
        let v2 = analyze( board, p: 1 )
        var besthex: Hexagon?
        var bestval: Double = 0
        for( hex, y1) in v1 {
            if (v2[hex] != nil) {
                let val = y1 + v2[hex]! * (1.0+Double(hex.xid)*1e-13)
                if val > bestval {
                    bestval = val
                    besthex = hex
                }
            }
        }
        return besthex!
    }
}
