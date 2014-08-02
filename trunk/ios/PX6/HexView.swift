import Darwin
import UIKit

class DrawBoardHelper {
    
    let board : Board
    //x0, x-position of the top-left corner of the hexagon relative to the canvas
    let x0, y0 : Float
    let smallHexSideLength : Float
    // vertical distance of cell rows
    let dyCell : Float
    //width of the hexagonal grid cell
    let wCell : Float
    
    init( ymid: Float, canvasHeight : Float, canvasWidth : Float, board : Board ){
        self.board = board
        var xmin : Float = 100000.0
        var xmax : Float = -100000.0
        var ymin : Float = 100000.0
        var ymax : Float = -100000.0
        for hex in board.hexagonList {
            xmin = min( xmin, hex.xi )
            xmax = max( xmax, hex.xi )
            ymin = min( ymin, hex.yi )
            ymax = max( ymax, hex.yi )
        }
        wCell = round( canvasWidth / Float(xmax-xmin+2) )  // must be even
        smallHexSideLength = round( Float(wCell) / sqrt( 3.0 ) )
        dyCell = smallHexSideLength * 1.5
        x0 = wCell * (1.0-xmin)
        y0 = ymid - (ymax+ymin) * 0.5 * dyCell
    }
    
    func findPositionOfCenterOfHexagonalCell( xi : Float, yi : Float ) -> (Float, Float ){
        return (Float(x0) + Float(wCell) * xi, Float(y0)+Float(dyCell)*yi )
    }

    func findHexagon( x: Float, y: Float ) -> Hexagon? {
        var besthex : Hexagon? = nil
        var besthex_dist : Float = smallHexSideLength*smallHexSideLength
        
        for hex in board.hexagonList {
            if hex.isEmpty() {
                let (hx,hy) = findPositionOfCenterOfHexagonalCell(hex.xi, yi: hex.yi)
                let dhex =  (hx-x)*(hx-x)+(hy-y)*(hy-y)
                if dhex < besthex_dist {
                    besthex = hex
                    besthex_dist = dhex
                }
            }
        }
        return besthex
    }
}

func load_image( name:String ) -> UIImage {
    let imagePath = NSBundle.mainBundle().pathForResource(name, ofType:"png")
    let im = UIImage(contentsOfFile: imagePath)
    assert(im != nil)
    return im
}

enum GameStatus {
    case waitingForUser
    case waitingForPhone
    case busy
    case winner
}

var gameStatus = GameStatus.busy

func xrect( x:Float, y:Float, w:Float, h:Float ) -> CGRect {
    return CGRect( x:CGFloat(x), y:CGFloat(y), width:CGFloat(w), height:CGFloat(h) )
}

class HexView : UIView {
    let tiles = [UIImage]()
    let tiles_winner = [UIImage]()
    let bg_image : UIImage
    let bg_rect : CGRect
    let board = Board( boardShape: boardShape, boardSize: boardSize )
    let helper : DrawBoardHelper
    let solver = Solver1( f: 4.0 )
    let tileview: UIImageView
    let refreshview: UIImageView
    let yourmoveview: UILabel
    let phonePlayer = phonePlayerId
    var showWinnerTile = false
    
    init( frame rect : CGRect ) {
        // load all images
        for fname in ["blue_tile", "green_tile", "unused_tile"]{
            tiles.append(load_image(fname))
        }
        for fname in ["blue_tile_highlight", "green_tile_highlight"]{
            tiles_winner.append(load_image(fname))
        }
        let wid: Float = rect.width.native
        var bg_hei: Float = wid * 1.1
        let ymin = rect.minY.native
        if( board.boardShape == BOARD_GEOMETRY.RECT ){
            bg_image = load_image("square_back")
        } else {
            switch board.boardSize {
            case 1:
                bg_image = load_image("hex_back_1")
            case 2:
                bg_image = load_image("hex_back_2")
            default:
                bg_image = load_image("hex_back_3")
            }
            if board.boardSize == 4 {
                bg_hei = wid
            }
        }
        let ymid: Float = rect.minY.native+bg_hei / 2
        bg_rect = xrect( 0.0, ymin, wid, bg_hei )
        helper = DrawBoardHelper(ymid: ymid, canvasHeight: rect.height.native, canvasWidth: rect.width.native, board: board)
        tileview = UIImageView(image: tiles[0])
        refreshview = UIImageView( image: load_image("refresh"))
        //create label view
        let labelframe = xrect( 0.0, 20, wid, 40 )
        yourmoveview = UILabel(frame:labelframe)
        yourmoveview.text = "Your turn!"
        yourmoveview.textAlignment = NSTextAlignment.Center
        yourmoveview.backgroundColor = UIColor.blackColor()
        yourmoveview.textColor = UIColor.whiteColor()
        yourmoveview.alpha = 0.0
        super.init( frame: rect )
        tileview.frame = xrect( 10.0, rect.minY.native+bg_hei+5, helper.wCell, 2.0*helper.smallHexSideLength )
        refreshview.frame = xrect( wid*0.8, ymin+bg_hei+5, wid*0.12, wid*0.1 )
        
        addSubview(tileview)
        addSubview(refreshview)
        addSubview(yourmoveview)
        if board.getPlayerId() == phonePlayer {
            initPhoneMove()
        } else {
            initUserMove()
        }
    }

    func initUserMove(){
        gameStatus = .waitingForUser
        UIView.animateWithDuration( 1, animations: {() in
            self.yourmoveview.alpha = 1
            }, completion: {(Bool) in
                UIView.animateWithDuration( 1, animations: {() in
                    self.yourmoveview.alpha = 0.0
                    })
        })
    }
    
    func doPhoneMove() {
        let hex2 = solver.bestMove(board)
        dispatch_async( dispatch_get_main_queue(),
            { () in self.do_move( hex2, owner: self.phonePlayer ) }
        )
    }
    
    func initPhoneMove() {
        gameStatus = .waitingForPhone
        let queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0)
        dispatch_async( queue, { () in self.doPhoneMove() } )
    }
    
    func updateWinnerBlink(){
        if gameStatus == .winner {
            showWinnerTile = !showWinnerTile
            setNeedsDisplay()
            dispatch_after( dispatch_time(DISPATCH_TIME_NOW, 500_000_000), dispatch_get_main_queue(),
                { () in self.updateWinnerBlink() }
            )
        } else {
            showWinnerTile = false
        }
    }
    
    func doWinner(){
        gameStatus = .winner
        updateWinnerBlink()
    }
    
    func do_move( hex: Hexagon, owner: Int ){
        gameStatus = .busy
        let (px, py) = helper.findPositionOfCenterOfHexagonalCell( hex.xi, yi: hex.yi )
        let newcenter = CGPoint( x: CGFloat(px), y: CGFloat(py) )
        self.tileview.image = tiles[owner]
        let origcenter = self.tileview.center
        UIView.animateWithDuration(0.5, animations:  {() in
            self.tileview.center = newcenter
            }, completion:{(Bool)  in
                println("move animation finished")
                self.tileview.center = origcenter
                if self.board.doMove(hex) { // we have a winner
                    self.doWinner()
                    return
                }
                self.tileview.image = self.tiles[1-owner]
                self.setNeedsDisplay()
                if self.board.getPlayerId() == self.phonePlayer {
                    self.initPhoneMove()
                } else {
                    self.initUserMove()
                }
            })
    }
    
    override func drawRect(rect: CGRect) {
        let ctx = UIGraphicsGetCurrentContext();
//        CGContextDrawImage(ctx, bg_rect, bg_image.CGImage)
        bg_image.drawInRect( bg_rect )
        println("bgrect: \(bg_rect)")
        for hex in board.hexagonList {
            drawHexagon(ctx, hex: hex)
        }
    }
    
    func checkForRestartTouch( pos : CGPoint ) -> Bool {
        if CGRectContainsPoint( refreshview.frame, pos ){
            viewController.view = viewController.settingsView
//            mainApp!.window.rootViewController = ViewController()
            return true
        }
        return false
    }
    
    override func touchesEnded(touches: NSSet!, withEvent event: UIEvent!){
        if touches.count == 1 {
            let touch: UITouch = touches.anyObject() as UITouch
            let pos = touch.locationInView(self)
            if( !checkForRestartTouch(pos) && gameStatus == .waitingForUser ){
                let hex = helper.findHexagon( pos.x.native, y: pos.y.native )
                if hex? {
                    do_move( hex!, owner: board.getPlayerId() )
                    setNeedsDisplay()
                }
            }
        }
    }
    
    func drawHexagon( ctx:CGContext, hex:Hexagon ){
        let (cx, cy) = helper.findPositionOfCenterOfHexagonalCell( hex.xi, yi: hex.yi )
        let x = cx - Float(helper.wCell) / 2.0
        let y = cy - Float(helper.smallHexSideLength)
        let rect = CGRect(x:  CGFloat(x), y: CGFloat(y), width: CGFloat(helper.wCell)*1.02, height: CGFloat(helper.smallHexSideLength*2)*1.02)
        let tile = hex.owner == 3 ? tiles_winner[0] :
            (gameStatus == .winner && showWinnerTile && hex.owner == board.getPlayerId() ) ? tiles_winner[hex.owner] : tiles[hex.owner]
        CGContextDrawImage(ctx, rect, tile.CGImage)
    }
}