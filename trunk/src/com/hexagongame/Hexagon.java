package com.hexagongame;

import java.util.ArrayList;


public class Hexagon {
	
	public int color;
	
	final public float x, y;
	
	final public boolean[] touches_edge;
	
	final ArrayList<Hexagon> adjacent = new ArrayList<Hexagon>();
	
	public Hexagon(float x, float y, int color, boolean[] touches_edge)
	{
		this.x = x;
		this.y = y;
		this.color = color;
		this.touches_edge = touches_edge;
	}
}
