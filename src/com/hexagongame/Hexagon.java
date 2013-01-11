package com.hexagongame;

import java.util.ArrayList;


public class Hexagon {
	
	public int color;

	final public float xi, yi;
	
	final ArrayList<Hexagon> adjacent = new ArrayList<Hexagon>();
	
	public Hexagon(float u, float v, int color)
	{
		this.xi = u;
		this.yi = v;
		this.color = color;
	}
}
