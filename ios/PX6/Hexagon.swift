let OWNER_FIRST = 0  // blue
let OWNER_SECOND = 1 // green
let OWNER_EMPTY = 2

class Hexagon : Hashable {
    var owner : Int
    let xid : Int
    let xi : Float
    let yi : Float
    let hashValue : Int
    
    var adjacent = [Hexagon]()
    
    var neighbors = [HexSet] ( repeating: HexSet(), count: 2 )
    init( u : Float, v : Float, owner : Int, id : Int){
        self.xi = u
        self.yi = v
        self.owner = owner
        self.xid = id
        self.hashValue = id
    }

    func isEmpty() -> Bool {
        return owner == OWNER_EMPTY
    }

    var stack = [HexSet]()
    func push( _ n:Int ){
        stack.append( neighbors[n] )
    }
    func pop( _ n:Int ){
        neighbors[n] = stack.removeLast()
    }
}

func==(lhs:Hexagon, rhs:Hexagon)->Bool {
    return lhs.xid == rhs.xid
}

typealias HexSet = [Hexagon:Bool]

func hexSetMerge( _ a : inout HexSet, b : HexSet ){
    for( u, v ) in b {
        a[u] = v
    }
}

func array2set( _ a : [Hexagon] ) -> HexSet
{
    var b = HexSet()
    for x in a {
        b[x] = true;
    }
    return b
}
