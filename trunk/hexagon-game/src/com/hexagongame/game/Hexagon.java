package com.hexagongame.game;

import java.util.ArrayList;


public class Hexagon {
	public final static int OWNER_FIRST = 0;  // blue
	public final static int OWNER_SECOND = 1; // green
	public final static int OWNER_EMPTY = 2;
	
	public int owner; 
	final public int id;
	final public float xi, yi;
	
	final ArrayList<Hexagon> adjacent = new ArrayList<Hexagon>();
	
	public boolean isEmpty(){
		return owner==OWNER_EMPTY;
	}
	
	public Hexagon(float u, float v, int owner, int id)
	{
		this.xi = u;
		this.yi = v;
		this.owner = owner;
		this.id = id;
	}
}