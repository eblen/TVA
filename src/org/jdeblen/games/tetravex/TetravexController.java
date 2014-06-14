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

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class TetravexController extends Activity {
	private final String saved_puzzle = "saved_puzzle";
	
	private Tetravex mTetravexModel;
	private TetravexView mTetravexView;
	private ArrayList<Tetravex.Tile> mTiles;
		
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.initPuzzleOnCreate();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }
 
    @Override
    public void onPause()
    {
    	super.onPause();
    	mTetravexModel.savePuzzle(saved_puzzle, this);
    	SoundEffectPlayer.stop(this);
    }
    
    private void initPuzzleOnCreate()
    {
    	// Try restoring saved puzzle
    	mTetravexModel = Tetravex.restorePuzzle(saved_puzzle, this);
    	if (mTetravexModel != null)
    	{
    		getTiles();
    		mTetravexView = new TetravexView(this, mTetravexModel.getSize());
    		setContentView(mTetravexView);
    	}
    
    	// If not, create a new puzzle
    	else initNewPuzzle();
    }
    
    private void initNewPuzzle()
    {
    	mTetravexModel = new Tetravex(Preferences.getPuzzleSize(this), Preferences.getNumberOfEdgeTypes(this));
    	
        getTiles();
        
        mTetravexView = new TetravexView(this, Preferences.getPuzzleSize(this));
        setContentView(mTetravexView);
    }
    
    private void getTiles()
    {
    	int puzzleSize = mTetravexModel.getSize();
    	mTiles = new ArrayList<Tetravex.Tile>();
    	
    	for (int i=0; i < puzzleSize; i++)
    	{
    		for (int j=0; j < puzzleSize; j++)
    		{
    			mTiles.add(mTetravexModel.getSolutionTile(i,j));
    		}
    	}
    	
    	Collections.shuffle(mTiles);
    }
 
    // Public methods for the view to get tile information - the tile bitmaps
    // and the current tile locations on the grid. The latter is necessary to
    // restore saved puzzles.
    public Bitmap [] getTileBitmaps(int tileWidth, int tileHeight)
    {
    	Bitmap [] tileBitmaps = new Bitmap[mTiles.size()];
    	
    	for (int i=0; i < mTiles.size(); i++)
    	{
    		tileBitmaps[i] = TetravexTileFactory.buildColorTile(mTiles.get(i), tileWidth, tileHeight);
    	}

    	return tileBitmaps;
    }
    
    // Extra level of indirection to support changes in how tile data is stored
    // or computed
    public int [][] getTileLocations()
    {
    	return computeTileLocations();
    }
    
    // Return a 2D array listing the tile (by id number) residing in each grid
    // square. If no tile is in a square, the array entry is -1.
    // If a puzzle has two identical tiles, we have to be careful that we don't
    // place the same tile twice. This is the purpose of tileHasBeenPlaced.
    private int [][] computeTileLocations()
    {
    	int puzzleSize = mTetravexModel.getSize();
    	int [][] tileLocations = new int[puzzleSize][puzzleSize];
    	boolean [] tileHasBeenPlaced = new boolean[mTiles.size()];
    	
    	for (int i=0; i<puzzleSize; i++)
    	{
    		for (int j=0; j<puzzleSize; j++)
    		{
    			tileLocations[i][j] = -1;
    			
    			if (!emptyGridSquare(i,j))
    			{
    				for (int k=0; k<mTiles.size(); k++)
    				{
    					if (tileHasBeenPlaced[k]) continue;
    					
    					if (tilesMatch(mTetravexModel.getBoardTile(i,j), mTiles.get(k)))
    					{
    						tileLocations[i][j] = k;
    						tileHasBeenPlaced[k] = true;
    						break;
    					}
    				}
    			}
    		}
    	}
    	
    	return tileLocations;
    }
    	
    // Helper functions for computeTileLocations method
    private boolean emptyGridSquare(int i, int j)
    {
    	if (mTetravexModel.getBoardTile(i,j) == null) return true;
    	else return false;
    }
    
   	private boolean tilesMatch(Tetravex.Tile tile1, Tetravex.Tile tile2)
    {
   		if (tile1.top != tile2.top) {return false;}
   		if (tile1.left != tile2.left) {return false;}
   		if (tile1.right != tile2.right) {return false;}
   		if (tile1.bottom != tile2.bottom) {return false;}
   		
   		return true;
    }
    
    // Controller just forwards communication between the model and view about
    // tile placements. It's appropriate that these methods are almost trivial.
    // (The only nontrivial part is converting tile numbers to actual tiles.)
    public int removeTileFromGrid(int x, int y)
    {
    	return mTetravexModel.removeTile(x, y);
    }
    
    public int placeTileOnGrid(int tileNum, int x, int y)
    {
    	return mTetravexModel.placeTile(mTiles.get(tileNum), x, y);
    }
    
    // Menu handling
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
    		case R.id.preferences:
    			startActivity(new Intent(this, Preferences.class));
    			return true;
    		case R.id.start_new_puzzle:
    			this.initNewPuzzle();
    			return true;
    	}
    	
    	return false;
    }
}