package com.hexagongame;

import java.util.ArrayList;


public class Hexagon {
	public final static int OWNER_FIRST = 0;  // blue
	public final static int OWNER_SECOND = 1; // green
	public final static int OWNER_EMPTY = 2;
	
	public int owner; 

	final public float xi, yi;
	
	final ArrayList<Hexagon> adjacent = new ArrayList<Hexagon>();
	
	boolean isEmpty(){
		return owner==OWNER_EMPTY;
	}
	
	public Hexagon(float u, float v, int owner)
	{
		this.xi = u;
		this.yi = v;
		this.owner = owner;
	}
}
