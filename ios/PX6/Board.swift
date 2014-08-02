enum BOARD_GEOMETRY {
    case HEX
    case RECT
}

class Board {
    //constant giving shape of board: hexagonal, square etc
    let boardShape : BOARD_GEOMETRY = .HEX
    
    //default board size
    let boardSize = 1
    
    var hexagonList =  [Hexagon]()
    
    // _player that must do the next move
    private var _player : Int = 0
    
    /* _hasWinner indicates if we have a winner.
    If _hasWinner is true, no move must be done
    and _player indicates who won. */
    private  var _hasWinner : Bool = false
    var history = [Hexagon]()
    
    /* 'outer' represents the four outer regions of the board.  The first index indicates the player (0 or 1),
    * the second index enumerates the both opposite regions of each player.  Those objects hold the set
    * of adjacent hexagons for each outer region   */
    
    let outer = [
        [ Hexagon(u: 0, v: 0, owner: OWNER_FIRST, id: -1),
            Hexagon(u: 0, v: 0, owner: OWNER_FIRST, id: -2)],
        [Hexagon(u: 0, v: 0, owner: OWNER_SECOND, id: -3),
            Hexagon(u: 0, v: 0, owner: OWNER_SECOND, id: -4)]]
    
    func showEdge(){
        for u in [0,1] {
            for v in [0,1] {
                for hex in outer[u][v].adjacent {
                    if hex.isEmpty() {
                        hex.owner = u
                    } else {
                        assert( hex.owner + u == 1 )
                        hex.owner = 3
                    }
                }
            }
        }
    }
    
    init( boardShape : BOARD_GEOMETRY, boardSize : Int) {
        self.boardShape = boardShape
        self.boardSize = boardSize
        
        //construct the list of hexagons that will make up the board
        if (boardShape == .RECT){
            setupRectBoardListOfHexagons()
        } else {
            setupHexBoardListOfHexagons()
        }
        findAdjacentHexagons()
        findNeighbors()
//        showEdge()
        for u in [0,1] {
            for v in [0,1]{
                for nei in [0,1]{
//                    println("outer:\(u)\(v) \(nei)\(outer[u][v].neighbors[nei].count)")
                }
            }
        }
        for hex in hexagonList {
//            println("hex=\(hex.xid) \(hex.neighbors[0].count) \(hex.neighbors[1].count)")
        }
    }
    
    func getPlayerId() -> Int {
        return _player
    }
    
    func undo(){
        if history.count > 0 {
            let lastChange : Hexagon = history.removeLast()
            
            if( _hasWinner ){
                lastChange.owner = OWNER_EMPTY
                _hasWinner=false
                return
            }
            
            undoNeighbors(lastChange)
            lastChange.owner = OWNER_EMPTY
            _player = 1-_player
            //			consistency();
        }
    }
    
    func haveHistory() -> Bool {
        return history.count > 0;
    }
    
    func consistency(){
        for n in [0,1] {
            for h in hexagonList {
                if h.isEmpty() {
                    for (h1, _) in h.neighbors[n]{
                        assert( h1.isEmpty() || h1.xid<0 )
                        assert( h1.neighbors[n][h])
                        assert( h1 != h );
                    }
                }
            }
        }
        for k in 0..<hexagonList.count {
            assert( hexagonList[k].xid == k )
        }
    }
    
    func updateNeighbors( hex : Hexagon ){
        let n = hex.owner
        let myNeighbors : HexSet = hex.neighbors[n]
        for (h1, _) in myNeighbors {
            h1.push(n)
            hexSetMerge( &h1.neighbors[n], myNeighbors )
            h1.neighbors[n][h1] = nil
        }
        //	hex isn't a neighbor of anything anymore, so we have to remove it everywhere
        for i in [0,1] {
            for (h1, _) in hex.neighbors[i] {
                h1.neighbors[i][hex] = nil
            }
        }
        consistency();
    }
    
    private func undoNeighbors( hex : Hexagon ){
        let n = hex.owner
        let myNeighbors = hex.neighbors[n];
        for (h1, _) in  myNeighbors {
            h1.pop(n);
        }
        for (h1, _) in hex.neighbors[1-n] {
            h1.neighbors[1-n][hex] = true
        }
    }
    
    func doMove( move0 : Hexagon ) -> Bool {
        assert( !_hasWinner )
        assert( move0.isEmpty() )
        let move=hexagonList[move0.xid]
        move.owner = _player
        history.append( move )
        if( move.neighbors[_player][ outer[_player][0] ] && move.neighbors[_player][ outer[_player][1] ] ){
            _hasWinner = true
            return true
        }
        _player = 1 - _player
        updateNeighbors(move)
        return false
    }
    
    func setupHexBoardListOfHexagons(){
        let r : Int = 1+boardSize
        var id : Int = 0
        let xlim = boardSize-1
        for i in -r...r  { for k in -r...r {
            if abs(i+k) <= r {
                let f : Float = Float(i) + Float(k) * 0.5
                let hex = Hexagon( u: f, v: Float(k), owner: OWNER_EMPTY, id: id )
                id += 1
                if abs(i) == r || abs(k) == r || abs(i+k) == r { // at the edge
                    let x = 2 * i + k
                    if x >= xlim && k>=0 {
                        outer[0][0].adjacent.append(hex)
                    }
                    if x >= -xlim && k<=0 {
                        outer[1][0].adjacent.append(hex)
                    }
                    if x <= xlim && k>=0 {
                        outer[1][1].adjacent.append(hex)
                    }
                    if x <= -xlim && k<=0 {
                        outer[0][1].adjacent.append(hex)
                    }
                }
                hexagonList.append(hex)
            }
        }}
//        Log.i("hex", "out size: 0: " + outer[0][0].adjacent.size()+" "+outer[0][1].adjacent.size() + " 1: " + outer[1][0].adjacent.size() + " " + outer[1][1].adjacent.size() );
        consistency()
    }
    
    private func setupRectBoardListOfHexagons(){
        let ymax = 2 + 2 * boardSize
        let xmax : Int = ymax
        var id = 0
        for yi in 0...ymax {
            for var xi : Float = Float(yi%2)*0.5; xi<=Float(xmax); ++xi {
                let hex = Hexagon(u: xi, v:Float(yi), owner:OWNER_EMPTY, id:id++ )
                hexagonList.append(hex)
                if yi == 0 {
                    outer[1][0].adjacent.append(hex)
                }
                if yi == ymax {
                    outer[1][1].adjacent.append(hex)
                }
                if xi<1 {
                    outer[0][0].adjacent.append(hex)
                }
                if xi>Float(xmax-1) {
                    outer[0][1].adjacent.append(hex)
                }
            }
        }
        consistency()
    }
    
    func findNeighbors(){
        for p in hexagonList {
            p.neighbors[0] = array2set(p.adjacent)
            p.neighbors[1] = p.neighbors[0]
        }
        for p in [0,1] {
            for k in [0,1]{
                outer[p][k].neighbors[1-p] = HexSet()
                outer[p][k].neighbors[p] = array2set( outer[p][k].adjacent )
                for hex in outer[p][k].adjacent {
                    hex.neighbors[p][ outer[p][k] ] = true
                }
            }
        }
        consistency()
    }
    
    func findAdjacentHexagons(){
        for p in hexagonList {
            for q in hexagonList {
                if( p != q ){
                    let dx = p.xi-q.xi
                    let dy = p.yi-q.yi
                    if( dx*dx+dy*dy < 1.5 ){
                        p.adjacent.append(q);
                    }
                }
            }
        }
    }
}
    /*
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
*/