package com.hexagongame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class UiView extends View{

	private Board board;
	
	public DrawBoardHelper drawBoardHelper;

	//the paint object used by the canvas
	private Paint paint;

	private int playerTurn;
	private boolean inWinnerMode;
	private int winnerModeTickCount;
	private int winner;

	private int historyLength;
	
	private TextView winnerNotification;

	public static final int BLUE = android.graphics.Color.parseColor("#1010FF");
	public static final int GREEN = android.graphics.Color.parseColor("#00FF00");

	public static final int BLUE_BG = android.graphics.Color.parseColor("#0000A0");
	public static final int GREEN_BG = android.graphics.Color.parseColor("#208020");
	
	public static final int BLUE_WINNER_ALT = android.graphics.Color.parseColor("#00FFFF");
	public static final int GREEN_WINNER_ALT = android.graphics.Color.parseColor("#FFF380");
	
	public static final int HEX_UNUSED_COLOR = android.graphics.Color.parseColor("#E0E0E0");
	
	public UiView(Context context, AttributeSet attrs) {
		
		super(context, attrs);

		paint = new Paint();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		//unfortunately, we have to do this here rather than in the constructor because the getHeight and getWidth
		//functions do not work there
		if (drawBoardHelper == null)
		{
			setupBoardHelper();
		}
	}

	private void setupBoardHelper()
	{
		//note: we have to call this method from within onDraw, rather than from within the constructor, because otherwise
		//the getHeight and getWidth methods just return 0
		float canvasWidth = getWidth();
    	float canvasHeight = getHeight();

		drawBoardHelper = new DrawBoardHelper(canvasHeight, canvasWidth, board);
	}
	
	public void setWinnerNotification(TextView winnerNotification)
	{
		this.winnerNotification = winnerNotification;
	}

	protected void setBoard(Board board)
	{
		this.board = board;
	}
	
	protected void updateParams(int playerTurn, boolean inWinnerMode, int winner, int winnerModeTickCount, int historyLength)
	{
		this.playerTurn = playerTurn;
		this.inWinnerMode = inWinnerMode;
		this.winner = winner;
		this.winnerModeTickCount = winnerModeTickCount;
		this.historyLength = historyLength;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {

		if (inWinnerMode && winnerNotification.getVisibility() != View.VISIBLE)
		{
			showWinnerCongratulationsMessage(canvas);
		}
		
		drawBackground(canvas);
	    
		//draw the bottom nav containing turn indicator & undo functionality
		drawBottomNav(canvas);

    	for( Hexagon hex : board.hexagonList ){
    		drawHexagon(canvas, hex);
    	}
	}
	

	protected void showWinnerCongratulationsMessage(Canvas canvas)
	{
		//someone has won: congratulate the winner
		winnerNotification.setText(((winner == 1) ? "Green" : "Blue") + " wins!");
		winnerNotification.setVisibility(View.VISIBLE);		
	}

    
    private void drawBackground(Canvas canvas)
    {
    	if (board.boardShape == Board.BOARD_GEOMETRY_RECT)
		{
    		drawSquareBackground(canvas);
		} else
		{
			drawHexBackground(canvas);
		}
    }
	
    private void drawHexBackground(Canvas canvas)
    {
    	float canvasHeight = getHeight();
    	float canvasWidth = getWidth();

    	float padding = 0.02f * canvasHeight;
    	
    	paint.setColor(BLUE_BG);
    	paint.setStyle(Paint.Style.FILL);
    	Path path = new Path();
    	path.moveTo(0.0f, 0.1f * canvasHeight);
    	path.lineTo(0.5f * canvasWidth - padding, 0.1f * canvasHeight);
    	path.lineTo(0.5f * canvasWidth + padding, 0.9f * canvasHeight);
    	path.lineTo(canvasWidth, 0.9f * canvasHeight);
    	path.lineTo(canvasWidth, 0.5f * canvasHeight + padding);
    	path.lineTo(0.0f, 0.5f * canvasHeight - padding);
    	path.lineTo(0.0f, 0.1f * canvasHeight);
    	canvas.drawPath(path, paint);
    	
    	paint.setColor(GREEN_BG);
    	paint.setStyle(Paint.Style.FILL);
    	path = new Path();
    	path.moveTo(0.5f * canvasWidth + padding, 0.1f * canvasHeight);
    	path.lineTo(canvasWidth, 0.1f * canvasHeight);
    	path.lineTo(canvasWidth, 0.5f * canvasHeight - padding);
    	path.lineTo(0.0f, 0.5f * canvasHeight + padding);
    	path.lineTo(0.0f, 0.9f * canvasHeight);
    	path.lineTo(0.5f * canvasWidth - padding, 0.9f * canvasHeight);
    	path.lineTo(0.5f * canvasWidth + padding, 0.1f * canvasHeight);
    	canvas.drawPath(path, paint);
    }
    
    private void drawSquareBackground(Canvas canvas)
    {
    	float canvasHeight = getHeight();
    	float canvasWidth = getWidth();
	
    	paint.setColor(BLUE_BG);
    	paint.setStyle(Paint.Style.FILL);
    	Path path = new Path();
    	path.moveTo(0.0f, 0.23f * canvasHeight);
    	path.lineTo(0.0f, 0.77f * canvasHeight);
    	path.lineTo(canvasWidth, 0.23f * canvasHeight);
    	path.lineTo(canvasWidth, 0.77f * canvasHeight);
    	path.lineTo(0.0f, 0.23f * canvasHeight);
    	canvas.drawPath(path, paint);
    	
    	paint.setColor(GREEN_BG);
    	paint.setStyle(Paint.Style.FILL);
    	path = new Path();
    	path.moveTo(0.0f, 0.19f * canvasHeight);
    	path.lineTo(canvasWidth, 0.19f * canvasHeight);
    	path.lineTo(0.0f, 0.81f * canvasHeight);
    	path.lineTo(canvasWidth, 0.81f * canvasHeight);
    	path.lineTo(0.0f, 0.19f * canvasHeight);
    	canvas.drawPath(path, paint);
    }
    
	private void drawBottomNav(Canvas canvas)
	{
		drawTurnIndicator(canvas);
		drawUndoIcon(canvas);
		drawRefreshIcon(canvas);
	}
	
	private void drawTurnIndicator(Canvas canvas)
	{
		//indicate whose turn it is next
		float canvasWidth = getWidth();
		float canvasHeight = getHeight();
		
		if (playerTurn == 0)
		{
			paint.setColor(BLUE);
		} else
		{
			paint.setColor(GREEN);
		}
    	paint.setStyle(Paint.Style.FILL);
    	
    	float cx = 0.25f * canvasWidth;
    	float cy = 0.95f * canvasHeight;
    	
		canvas.drawCircle(cx, cy, 0.15f * cx, paint);	
	}
	
	private void drawUndoIcon(Canvas canvas) {
		//indicate whose turn it is next
		float canvasWidth = getWidth();
		float canvasHeight = getHeight();

		Bitmap bmp;
		if ( historyLength > 0 )
		{
			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.undo);
		} else {
			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.undo_black);
		}
		
		float cx = 0.5f * canvasWidth - bmp.getWidth()/2.0f;
		float cy = 0.95f * canvasHeight - bmp.getHeight()/2.0f;

		canvas.drawBitmap(bmp, cx, cy, paint);
	}
	
	public void drawRefreshIcon(Canvas canvas)
	{
		//indicate whose turn it is next
		float canvasWidth = getWidth();
		float canvasHeight = getHeight();

		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.refresh);
	
		float cx = 0.75f * canvasWidth - bmp.getWidth()/2.0f;
		float cy = 0.95f * canvasHeight - bmp.getHeight()/2.0f;

		canvas.drawBitmap(bmp, cx, cy, paint);
	}
		
	public void drawHexagon(Canvas canvas, Hexagon hex)
	{
		float hexSide = drawBoardHelper.getSmallHexSideLength();
		
		// vx, vy represents the vector of the first edge of the hexagon
		float vx = hexSide * (float) Math.cos(Math.PI/6.0);
		float vy = - hexSide * (float) Math.sin(Math.PI/6.0);
		final float co = (float)Math.cos(Math.PI/3);
		final float si = (float)Math.sin(Math.PI/3);

		float x, y;
		Path path;
		int color;
		
		for (int j = 0; j < 2; j++)
		{
			if (j == 1)
			{
				paint.setColor(android.graphics.Color.BLACK);
		    	paint.setStrokeWidth(1);
		    	paint.setStyle(Paint.Style.STROKE);
			} else
			{
				//if we are in "congratulations, winner" mode, every second tick we show the winner's rectangles in an alternative color
				if (inWinnerMode && winnerModeTickCount % 2 == 0
						&& ((winner == 1 && hex.color == GREEN) || (winner == 0 && hex.color == BLUE)))
				{
					color = (winner == 1) ? GREEN_WINNER_ALT : BLUE_WINNER_ALT;
				} else
				{
					color = hex.color;
				}
				paint.setColor(color);
		        paint.setStyle(Paint.Style.FILL);
			}
			
			float[] coords = drawBoardHelper.findPositionOfCenterOfHexagonalCell(hex.i, hex.j);
			x = coords[0] - drawBoardHelper.getWCell()/2.0f;
			y = coords[1] - drawBoardHelper.getHCell()/2.0f;

			path = new Path();
			
			path.moveTo( x,  y );
			for( int i=0; i<6; ++i ){
				x += vx;
				y += vy;
				path.lineTo( x, y );
				// now rotate the edge vector by Pi/3
				float vx_temp = co * vx - si * vy;
				vy = si * vx + co * vy;
				vx=vx_temp;
			}
			
			canvas.drawPath(path, paint);
		}
	}
	
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN ){
        	return false;
        }

		float x = (float) event.getX();
		float y = (float) event.getY();
		
		HexActivity ac = (HexActivity) getContext();
		ac.handleOnTouch(x, y);
        
        return true;
    }
}
