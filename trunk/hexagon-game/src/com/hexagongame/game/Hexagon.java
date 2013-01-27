package com.hexagongame.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;


public class Hexagon {
	public final static int OWNER_FIRST = 0;  // blue
	public final static int OWNER_SECOND = 1; // green
	public final static int OWNER_EMPTY = 2;
	
	public int owner; 
	final public int xid;
	final public float xi, yi;
	
	final ArrayList<Hexagon> adjacent = new ArrayList<Hexagon>();
	final HexSet[] neighbors = new HexSet[2];
	public static class HexSet extends HashSet<Hexagon>{
		public HexSet( Collection<?extends Hexagon> c ) { super(c); }
		HexSet(){ super(); }
	}
	
	public boolean isEmpty(){
		return owner==OWNER_EMPTY;
	}
	
	public Hexagon(float u, float v, int owner, int id)
	{
		this.xi = u;
		this.yi = v;
		this.owner = owner;
		this.xid = id;
	}

	final ArrayList<HexSet> stack = new ArrayList<HexSet>();
	
	public void push(int n) {
		stack.add( new HexSet(neighbors[n]));
	}
	
	public void pop(int n){
		neighbors[n] = stack.remove(stack.size()-1);
	}
}
