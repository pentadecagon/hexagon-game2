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
    
    func findPositionOfCenterOfHexagonalCell( _ xi : Float, yi : Float ) -> (Float, Float ){
        return (Float(x0) + Float(wCell) * xi, Float(y0)+Float(dyCell)*yi )
    }

    func findHexagon( _ x: Float, y: Float ) -> Hexagon? {
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

func load_image( _ name:String ) -> UIImage {
    let imagePath = Bundle.main.path(forResource: name, ofType:"png")
    let im = UIImage(contentsOfFile: imagePath!)
    assert(im != nil)
    return im!
}

enum GameStatus {
    case waitingForUser
    case waitingForPhone
    case busy
    case winner
}

var gameStatus = GameStatus.busy

func xrect( _ x:Float, y:Float, w:Float, h:Float ) -> CGRect {
    return CGRect( x:CGFloat(x), y:CGFloat(y), width:CGFloat(w), height:CGFloat(h) )
}

class HexView : UIView {
    var tiles = [UIImage]()
    var tiles_winner = [UIImage]()
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
    
    override init( frame rect : CGRect ) {
        // load all images
        for fname in ["blue_tile", "green_tile", "unused_tile"]{
            tiles.append(load_image(fname))
        }
        for fname in ["blue_tile_highlight", "green_tile_highlight"]{
            tiles_winner.append(load_image(fname))
        }
        let wid = Float(rect.width)
        var bg_hei: Float = wid * 1.1
        let ymin = Float(rect.minY)
        if( board.boardShape == BOARD_GEOMETRY.rect ){
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
        let ymid = Float(rect.minY)+bg_hei / 2
        bg_rect = xrect( 0.0, y: ymin, w: wid, h: bg_hei )
        helper = DrawBoardHelper(ymid: ymid, canvasHeight: Float(rect.height),
            canvasWidth: Float(rect.width), board: board)
        tileview = UIImageView(image: tiles[0])
        refreshview = UIImageView( image: load_image("refresh"))
        //create label view
        let labelframe = xrect( 0.0, y: 20, w: wid, h: 40 )
        yourmoveview = UILabel(frame:labelframe)
        yourmoveview.text = "Your turn!"
        yourmoveview.textAlignment = NSTextAlignment.center
        yourmoveview.backgroundColor = UIColor.black
        yourmoveview.textColor = UIColor.white
        yourmoveview.alpha = 0.0
        super.init( frame: rect )
        tileview.frame = xrect( 20.0, y: Float(rect.minY)+bg_hei+15, w: Float(helper.wCell), h: 2.0*helper.smallHexSideLength )
        refreshview.frame = xrect( wid*0.8, y: ymin+bg_hei+15, w: wid*0.12, h: wid*0.1 )
        
        addSubview(tileview)
        addSubview(refreshview)
        addSubview(yourmoveview)
        if board.getPlayerId() == phonePlayer {
            initPhoneMove()
        } else {
            initUserMove()
        }
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func initUserMove(){
        gameStatus = .waitingForUser
        UIView.animate( withDuration: 1, animations: {() in
            self.yourmoveview.alpha = 1
            }, completion: {(Bool) in
                UIView.animate( withDuration: 1, animations: {() in
                    self.yourmoveview.alpha = 0.0
                    })
        })
    }
    
    func doPhoneMove() {
        let hex2 = solver.bestMove(board)
        DispatchQueue.main.async(execute: { () in self.do_move( hex2, owner: self.phonePlayer ) }
        )
    }
    
    func initPhoneMove() {
        gameStatus = .waitingForPhone
        let queue = DispatchQueue.global()
        queue.async(execute: { () in self.doPhoneMove() } )
    }
    
    var restartAng: CGFloat = 2
    
    func updateWinnerBlink(){
        if gameStatus == .winner {
            showWinnerTile = !showWinnerTile
            setNeedsDisplay()
            restartAng += 2
            let transform = CGAffineTransform(rotationAngle: CGFloat(restartAng))
            UIView.animate(withDuration: 0.5, animations:  {() in
                self.refreshview.transform = transform
                }, completion:{(Bool)  in
                    self.updateWinnerBlink()
                }
            )
        } else {
            showWinnerTile = false
        }
    }
    
    func doWinner(){
        gameStatus = .winner
        updateWinnerBlink()
    }
    
    func do_move( _ hex: Hexagon, owner: Int ){
        gameStatus = .busy
        let (px, py) = helper.findPositionOfCenterOfHexagonalCell( hex.xi, yi: hex.yi )
        let newcenter = CGPoint( x: CGFloat(px), y: CGFloat(py) )
        self.tileview.image = tiles[owner]
        let origcenter = self.tileview.center
        UIView.animate(withDuration: 0.5, animations:  {() in
            self.tileview.center = newcenter
            }, completion:{(Bool)  in
                print("move animation finished")
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
    
    override func draw(_ rect: CGRect) {
        let ctx = UIGraphicsGetCurrentContext();
//        CGContextDrawImage(ctx, bg_rect, bg_image.CGImage)
        bg_image.draw( in: bg_rect )
        print("bgrect: \(bg_rect)")
        for hex in board.hexagonList {
            drawHexagon(ctx!, hex: hex)
        }
    }
    
    func checkForRestartTouch( _ pos : CGPoint ) -> Bool {
        if refreshview.frame.contains(pos ){
            viewController.view = viewController.settingsView
//            mainApp!.window.rootViewController = ViewController()
            return true
        }
        return false
    }
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent!){
        if touches.count == 1 {
            let touch: UITouch = touches.first!
            let pos = touch.location(in: self)
            if( !checkForRestartTouch(pos) && gameStatus == .waitingForUser ){
                let hex = helper.findHexagon( Float(pos.x), y: Float(pos.y) )
                if (hex != nil) {
                    do_move( hex!, owner: board.getPlayerId() )
                    setNeedsDisplay()
                }
            }
        }
    }
    
    func drawHexagon( _ ctx:CGContext, hex:Hexagon ){
        let (cx, cy) = helper.findPositionOfCenterOfHexagonalCell( hex.xi, yi: hex.yi )
        let x = cx - Float(helper.wCell) / 2.0
        let y = cy - Float(helper.smallHexSideLength)
        let rect = CGRect(x:  CGFloat(x), y: CGFloat(y), width: CGFloat(helper.wCell)*1.02, height: CGFloat(helper.smallHexSideLength*2)*1.02)
        let tile = hex.owner == 3 ? tiles_winner[0] :
            (gameStatus == .winner && showWinnerTile && hex.owner == board.getPlayerId() ) ? tiles_winner[hex.owner] : tiles[hex.owner]
        ctx.draw(tile.cgImage!, in: rect)
    }
}
