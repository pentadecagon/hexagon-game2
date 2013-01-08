package com.hexagongame;

import java.util.ArrayList;


public class Hexagon {
	
	public int color;

	final public int i, j;
	
	final ArrayList<Hexagon> adjacent = new ArrayList<Hexagon>();
	
	public Hexagon(int i, int j, int color)
	{
		this.i = i;
		this.j = j;
		this.color = color;
	}
}
