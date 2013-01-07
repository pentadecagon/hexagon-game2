package com.hexagongame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class ChooseBoardView extends View {
	
	//the paint object used by the canvas
	private Paint paint;
	
	public int boardShape = 0;
	
	public ChooseBoardView(Context context, AttributeSet attrs) {
		
		super(context, attrs);
		
		paint = new Paint();
		
		this.setOnTouchListener(new View.OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {

				Log.d("cbv", "ontouch x="+event.getX());
		    	
				float canvasWidth = getWidth();

				if ((float) event.getX() < 0.5f * canvasWidth)
				{
					//hex
					boardShape = Board.BOARD_GEOMETRY_HEX;
				} else
				{
					//square
					boardShape = Board.BOARD_GEOMETRY_RECT;
				}
				
				ChooseBoardView.this.postInvalidate();

		    	return true;
			}
		});
	}
	
	public void onDraw(Canvas canvas) {	
		drawNavHexagon(canvas);
		drawNavSquare(canvas);
	}

	protected void drawNavHexagon(Canvas canvas) {
		float canvasWidth = getWidth();
		final float ybase=canvas.getHeight() * 0.5f; // 0.25f * canvasWidth
		float hexSide, x0, y0, lineWidth;
		if (boardShape == Board.BOARD_GEOMETRY_HEX)
		{
			hexSide = 0.07f * canvasWidth;
			x0 = 0.25f * canvasWidth - hexSide * (float) Math.cos(Math.PI/6.0);
			y0 = ybase - hexSide * 0.5f;
			lineWidth = 10;
		} else
		{
			hexSide = 0.05f * canvasWidth;
			x0 = 0.25f * canvasWidth - hexSide * (float) Math.cos(Math.PI/6.0);
			y0 = ybase - hexSide * 0.5f;
			lineWidth = 2;
		}

		float xNext = x0;
		float yNext = y0;

		paint.setColor(android.graphics.Color.WHITE);
    	paint.setStrokeWidth(lineWidth);
    	paint.setStyle(Paint.Style.STROKE);
    	Path path = new Path();
    	
		path.moveTo(xNext, yNext);

		xNext = xNext + hexSide * (float) Math.cos(Math.PI/6.0);
		yNext = yNext - hexSide * (float) Math.sin(Math.PI/6.0);
		
		path.lineTo(xNext, yNext);
		
		xNext = xNext + hexSide * (float) Math.cos(Math.PI/6.0);
		yNext = yNext + hexSide * (float) Math.sin(Math.PI/6.0);
		
		path.lineTo(xNext, yNext);
		
		yNext = yNext + hexSide;
		
		path.lineTo(xNext, yNext);
		
		xNext = xNext - hexSide * (float) Math.cos(Math.PI/6.0);
		yNext = yNext + hexSide * (float) Math.sin(Math.PI/6.0);
		
		path.lineTo(xNext, yNext);
		
		xNext = xNext - hexSide * (float) Math.cos(Math.PI/6.0);
		yNext = yNext - hexSide * (float) Math.sin(Math.PI/6.0);
		
		path.lineTo(xNext, yNext);
		
		xNext = x0;
		yNext = y0;
		
		if (boardShape == Board.BOARD_GEOMETRY_HEX)
		{
			path.lineTo(xNext, yNext - 0.07f * hexSide);
		} else
		{
			path.lineTo(xNext, yNext);
		}
    	
    	canvas.drawPath(path, paint);
	}
	
	protected void drawNavSquare(Canvas canvas) {
		float canvasWidth = getWidth();

		float squareWidth, x0, y0, lineWidth;
		final float ybase=canvas.getHeight() * 0.5f; // 0.25f * canvasWidth
		if (boardShape == Board.BOARD_GEOMETRY_RECT)
		{
			squareWidth = 0.1f * canvasWidth;
			x0 = 0.65f * canvasWidth - squareWidth/2.0f;
			y0 = ybase - squareWidth/2.0f;
			lineWidth = 10;	
		} else
		{
			squareWidth = 0.1f * canvasWidth;
			x0 = 0.65f * canvasWidth - squareWidth/2.0f;
			y0 = ybase - squareWidth/2.0f;		
			lineWidth = 2;
		}

		paint.setColor(android.graphics.Color.WHITE);
    	paint.setStrokeWidth(lineWidth);
    	paint.setStyle(Paint.Style.STROKE);
    	Path path = new Path();
    	
    	path.moveTo(x0, y0);
    	path.lineTo(x0 + squareWidth, y0);
    	path.lineTo(x0 + squareWidth, y0 + squareWidth);
    	path.lineTo(x0, y0 + squareWidth);
    	if (boardShape == Board.BOARD_GEOMETRY_RECT)
		{
    		path.lineTo(x0, y0 - 0.07f * squareWidth);
		} else
		{
			path.lineTo(x0, y0);
		}
    	
    	canvas.drawPath(path, paint);
	}
	
}
