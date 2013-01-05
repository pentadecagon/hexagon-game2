package com.hexagongame;


public class Hexagon {
	
	public int color;
	
	public float x, y;
	
	public boolean[] touches_edge = new boolean[4];
	
	public Hexagon(float x, float y, int color, boolean[] touches_edge)
	{
		this.x = x;
		this.y = y;
		this.color = color;
		this.touches_edge = touches_edge;
	}
}
