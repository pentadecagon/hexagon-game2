package com.hexagongame;

import java.util.ArrayList;


public class Hexagon {
	
	public int color;
	
	final public float x, y;
	
	final ArrayList<Hexagon> adjacent = new ArrayList<Hexagon>();
	
	public Hexagon(float x, float y, int color)
	{
		this.x = x;
		this.y = y;
		this.color = color;
	}
}
