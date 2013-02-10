package com.hexagongame;

/**
 * Set of configuration options for the hex game.
 * These can be set in the form in the "ChooseBoardActivity" activity.
 */

public class ChooseBoardConfig implements Cloneable {
	/**
	 * Board shape
	 * 0 = hexagon
	 * 1 = square
	 */
	public int boardShape = 1;
	
	/**
	 * Board size
	 * From 0-2, 0 is the smallest, 2 is the biggest
	 */
	public int boardSize = 1;
	
	/**
	 * Game mode
	 * 0 = 2-player
	 * 1 = play against phone
	 */
	public int gameMode = 1;
	
	/**
	 * ID of the phone player, for "play against phone" mode
	 * 0 = phone goes first
	 * 1 = phone goes second
	 */
	public int phonePlayerId = 0;
	
	/**
	 * Strength of automatic opponent in "play against phone" mode
	 * From 1-6, with 1 the weakest, 6 the strongest
	 */
	public int opponentStrength = 3;
	
	/**
	 * Clone the current object
	 */
	@Override
	public ChooseBoardConfig clone()
	{
		try
	    {
			return (ChooseBoardConfig) super.clone();
	    }
		catch (CloneNotSupportedException e)
	    {
			throw new RuntimeException("failed to clone ChooseBoardConfig object: " + e.getMessage());
	    }
	}
}
