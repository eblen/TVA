/********************************************************************
 TVA (TetraVex for Android)
 Copyright (C) 2014 John Eblen

 This file is part of TVA.

 TVA is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 TVA is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with TVA.  If not, see <http://www.gnu.org/licenses/>.
*********************************************************************/
package org.jdeblen.games.tetravex;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import android.content.Context;
import android.util.Log;

public class Tetravex {
	private static final String TAG = "TetravexModel";
	
	// Variables for providing feedback to controller when a move is attempted.
	public static final int INVALID_MOVE = 0; 
	public static final int VALID_MOVE = 1;
	public static final int WINNING_MOVE = 2;
	
	// Puzzle parameters - affects difficulty
	private int mSize; // Puzzle size
	private int mMaxValue; // Maximum number of the values on the tiles
	
	// Puzzle representation
	// Note that a puzzle may have more than one solution. Thus, it is possible
	// for the user to win but for the solution and board matrices to not match.
	private Tile [][] mSolution; // A solution, computed upon initialization
	private Tile [][] mBoard; // Tracks user progress, initially empty
	private int mNumTilesPlaced; // Tracks number of tiles placed on board
	
	// Convenience class for both the class itself and for users to encapsulate
	// the four values defining a tile and to allow for easy tile construction.
	static class Tile
	{
		public int top;
		public int left;
		public int right;
		public int bottom;
		
		public Tile()
		{
			this(0,0,0,0);
		}
		
		public Tile(int t, int l, int r, int b)
		{
			top = t;
			left = l;
			right = r;
			bottom = b;
		}
		
		public Tile(Tile t)
		{
			top = t.top;
			left = t.left;
			right = t.right;
			bottom = t.bottom;
		}
	}
	
	// Construction - initialize variables and compute a new puzzle
	// Note that startNewPuzzle is public and can be called at any time.
	// Doing so erases the old puzzle and starts a new puzzle from scratch,
	// even one with different parameters (size and maxValue).
	public Tetravex(int s, int v)
	{
		startNewPuzzle(s,v);
	}
	
	public void startNewPuzzle(int s, int v)
	{
		initVariables(s,v);
		createNewPuzzle();
	}
	
	private void initVariables(int s, int v)
	{
		mSize = s;
		mMaxValue = v;
		mNumTilesPlaced = 0;
		mSolution = new Tile[s][s];
		mBoard = new Tile[s][s];
	}
	
	private void createNewPuzzle()
	{
		Random randomNumberFactory = new Random();
		
		for (int x=0; x<mSize; x++)
		{
			for (int y=0; y<mSize; y++)
			{
				mSolution[x][y] = new Tile();
				
				if (x == 0) mSolution[x][y].left = randomNumberFactory.nextInt(mMaxValue);
				else mSolution[x][y].left = mSolution[x-1][y].right;
				
				if (y==0) mSolution[x][y].top = randomNumberFactory.nextInt(mMaxValue);
				else mSolution[x][y].top = mSolution[x][y-1].bottom;
				
				mSolution[x][y].right = randomNumberFactory.nextInt(mMaxValue);
				
				mSolution[x][y].bottom = randomNumberFactory.nextInt(mMaxValue);
			}
		}
	}
	
	// Static factory method to recreate a puzzle previously saved
	public static Tetravex restorePuzzle(String puzzleName, Context context)
	{
		String fileName = puzzleName + ".puzzle";
		FileInputStream fis = null;
		Tetravex puzzle = null;
		
		// Open the file
		try {
			fis = context.openFileInput(fileName);
			puzzle = new Tetravex();
			
			// Read puzzle metadata
			int puzzleSize = fis.read();
			int puzzleMaxValue = fis.read();
			puzzle.initVariables(puzzleSize, puzzleMaxValue);
			puzzle.mNumTilesPlaced = fis.read();
			
			// Read current board 
			for(int i=0; i<puzzle.mSize; i++)
			{
				for (int j=0; j<puzzle.mSize; j++)
				{
					int tileTopValue = fis.read();
					if (tileTopValue == Byte.MAX_VALUE) continue;
					
					puzzle.mBoard[i][j] = new Tile();
					puzzle.mBoard[i][j].top = tileTopValue;
					puzzle.mBoard[i][j].left = fis.read();
					puzzle.mBoard[i][j].right = fis.read();
					puzzle.mBoard[i][j].bottom = fis.read();
				}
			}
			
			// Read solution
			for(int i=0; i<puzzle.mSize; i++)
			{
				for (int j=0; j<puzzle.mSize; j++)
				{
					puzzle.mSolution[i][j] = new Tile();
					puzzle.mSolution[i][j].top = fis.read();
					puzzle.mSolution[i][j].left = fis.read();
					puzzle.mSolution[i][j].right = fis.read();
					puzzle.mSolution[i][j].bottom = fis.read();
				}
			}
			
		} catch(FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		} finally {
			try {
				if (fis != null) fis.close();
			} catch (IOException e) {}
		}
		
		return puzzle;
	}
	
	// Private do-nothing constructor for use by restorePuzzle
	private Tetravex()
	{
	}

	// Getter methods
	public int getSize()
	{
		return mSize;
	}
	
	public Tile getSolutionTile(int x, int y)
	{
		return new Tile(mSolution[x][y]);
	}
	
	public Tile getBoardTile(int x, int y)
	{
		if (mBoard[x][y] == null) return null;
		else return new Tile(mBoard[x][y]);
	}
	
	// Methods for building a solution
	public int placeTile(Tile t, int x, int y)
	{
		if (!isValidMove(t, x, y)) return INVALID_MOVE;
		
		if (mBoard[x][y] == null) mNumTilesPlaced++;
		mBoard[x][y] = new Tile(t);
		Log.d(TAG, "Placing tile number " + mNumTilesPlaced + " at " + mBoard[x][y].top + mBoard[x][y].left + mBoard[x][y].right + mBoard[x][y].bottom + " to " + x + y);
		
		if (mNumTilesPlaced == mSize*mSize) return WINNING_MOVE;
		else return VALID_MOVE;
	}
	
	public int removeTile(int x, int y)
	{
		if (mBoard[x][y] != null)
		{
			Log.d(TAG, "Removing tile " + mBoard[x][y].top + mBoard[x][y].left + mBoard[x][y].right + mBoard[x][y].bottom + " from " + x + y);
			mNumTilesPlaced--;
			mBoard[x][y] = null;
		}
		
		return VALID_MOVE;
	}

	// For now, this method simply checks that neighbor tiles are compatible.
	// This could be expanded to check the validity of the tiles.
	private boolean isValidMove(Tile t, int x, int y)
	{
		if (mBoard[x][y] != null) return false;
		if (y > 0 && mBoard[x][y-1] != null && t.top != mBoard[x][y-1].bottom) return false;
		if (x > 0 && mBoard[x-1][y] != null && t.left != mBoard[x-1][y].right) return false;
		if (x < (mSize-1) && mBoard[x+1][y] != null && t.right != mBoard[x+1][y].left) return false;
		if (y < (mSize-1) && mBoard[x][y+1] != null && t.bottom != mBoard[x][y+1].top) return false;
		
		return true;
	}
	
	// Methods for saving and restoring puzzles
	public boolean savePuzzle(String puzzleName, Context context)
	{
		String fileName = puzzleName + ".puzzle";
		FileOutputStream fos = null;
		
		// Open the file
		try {
			fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			
			// Write puzzle metadata
			fos.write(mSize);
			fos.write(mMaxValue);
			fos.write(mNumTilesPlaced);
			
			// Write current board 
			for(int i=0; i<mSize; i++)
			{
				for (int j=0; j<mSize; j++)
				{
					if (mBoard[i][j] == null) fos.write(Byte.MAX_VALUE);
					else
					{
						fos.write(mBoard[i][j].top);
						fos.write(mBoard[i][j].left);
						fos.write(mBoard[i][j].right);
						fos.write(mBoard[i][j].bottom);
					}
				}
			}
			
			// Write solution
			for(int i=0; i<mSize; i++)
			{
				for (int j=0; j<mSize; j++)
				{
					fos.write(mSolution[i][j].top);
					fos.write(mSolution[i][j].left);
					fos.write(mSolution[i][j].right);
					fos.write(mSolution[i][j].bottom);
				}
			}
			
		} catch(FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			context.deleteFile(fileName);
			return false;
		} finally {
			try {
				if (fos != null) fos.close();
			} catch (IOException e) {}
		}
		
		return true;
	}
}