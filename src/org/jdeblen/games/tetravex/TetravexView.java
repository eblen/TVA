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

import java.util.LinkedList;
import java.util.ListIterator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

public class TetravexView extends View {
	private static final float gridToScratchAreaRatio = 0.45f;
	private static final float cushionBetweenGridAndTile = 0.05f;

	// Palette
	private final Paint mGridAreaPaint;
	private final Paint mScratchAreaPaint;
	private final Paint mGridLinesPaint;
	private final Paint mBitmapPaint;
	
	// Sound Effects
	private static final int placeTileOnGridSoundEffect = R.raw.judge_gavel;
	private static final int rejectTileSoundEffect = R.raw.frying_pan_impact;
	private static final int winSoundEffect = R.raw.computer_start_up;
	
	private final int mGridSize;
	private float mTileWidth;
	private float mTileHeight;
	private Bitmap [] mTiles;
	private LinkedList<TilePosition> mTilePositions;
	
	// Variables for tile dragging. Finger offsets record the touch position
	// relative to the tile's upper left corner.
	private TilePosition mTileBeingDragged;
	private float mFingerOffsetX;
	private float mFingerOffsetY;

	private TetravexController mController;
	private boolean mPuzzleSolved = false;
	
	// Nested class for storing tile positions
	private class TilePosition
	{
		public int tileNum;
		public float x;
		public float y;
		public boolean onGrid;
		public int gridX;
		public int gridY;
	}
	
	public TetravexView(Context context, int puzzleSize)
	{
		super(context);
		mGridSize = puzzleSize;
		mTiles = null;
		mTilePositions = new LinkedList<TilePosition>();
		mTileBeingDragged = null;
		mController = (TetravexController) context;
		
		mGridAreaPaint = new Paint();
		mGridAreaPaint.setColor(getResources().getColor(R.color.gridAreaBackground));
		
		mScratchAreaPaint = new Paint();
		mScratchAreaPaint.setColor(getResources().getColor(R.color.scratchAreaBackground));

		mGridLinesPaint = new Paint();
		mGridLinesPaint.setColor(getResources().getColor(R.color.gridLines));

		mBitmapPaint = new Paint();
		
		setFocusable(true);
		setFocusableInTouchMode(true);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		mTileWidth = w / (float) mGridSize;
		mTileHeight = h * gridToScratchAreaRatio / (float) mGridSize;
		mTiles = null;
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		// Draw background
		canvas.drawRect(0, 0, getWidth(), getHeight(), mScratchAreaPaint);
		canvas.drawRect(0, 0, getWidth(), (int) gridToScratchAreaRatio*getHeight(), mGridAreaPaint);
		
		// Draw horizontal grid lines
		for (int i=0; i<=mGridSize; i++)
		{
			canvas.drawLine(0, i*mTileHeight, getWidth(), i*mTileHeight, mGridLinesPaint);
		}
		
		// Draw vertical grid lines
		for (int i=0; i<=mGridSize; i++)
		{
			canvas.drawLine(i*mTileWidth, 0, i*mTileWidth, gridToScratchAreaRatio*getHeight(), mGridLinesPaint);
		}
		
		// Draw tiles
		if (mTiles == null)
		{
			mTiles = mController.getTileBitmaps((int) mTileWidth, (int) mTileHeight);
			setInitialPositionOfTiles();
			mTileBeingDragged = null;
		}
		
		for (ListIterator<TilePosition> it = mTilePositions.listIterator(mTilePositions.size()); it.hasPrevious();)
		{
			TilePosition tileData = it.previous();
			canvas.drawBitmap(mTiles[tileData.tileNum], tileData.x, tileData.y, mBitmapPaint);
		}
		
		if (mTileBeingDragged != null)
		{
			canvas.drawBitmap(mTiles[mTileBeingDragged.tileNum], mTileBeingDragged.x, mTileBeingDragged.y, mBitmapPaint);
		}
	}
	
	private void setInitialPositionOfTiles()
	{
		float startingHeight = getHeight() - (mGridSize * mTileHeight);
		
		// Set all tiles to an initial starting postion below the grid
		for (int i=0; i<mGridSize; i++)
		{
			for (int j=0; j<mGridSize; j++)
			{
				TilePosition newTilePosition = new TilePosition();
				newTilePosition.tileNum = i*mGridSize + j;
				newTilePosition.x = j*mTileWidth;
				newTilePosition.y = startingHeight + i*mTileHeight;
				newTilePosition.onGrid = false;
				newTilePosition.gridX = -1;
				newTilePosition.gridY = -1;
				
				mTilePositions.add(newTilePosition);
			}
		}

		// Since we may be restoring a saved game, see if any tiles should be
		// placed on the grid.
		int [][] tileGridPositions = mController.getTileLocations();
		for (int i=0; i<mGridSize; i++)
		{
			for (int j=0; j<mGridSize; j++)
			{
				if (tileGridPositions[i][j] != -1)
				{
					TilePosition tileData = findTileDataByTileNumber(tileGridPositions[i][j]);
					this.placeTileOnGrid(tileData, i, j);
				}
			}
		}
	}
	
	// Used by setInitialPositionOfTiles to locate a particular tile's data
	private TilePosition findTileDataByTileNumber(int tileNum)
	{
		for (TilePosition tileData : mTilePositions)
		{
			if (tileData.tileNum == tileNum) return tileData;
		}
		
		return null;
	}
	
	public boolean onTouchEvent(MotionEvent event)
	{
		if (mPuzzleSolved) return true;
		
		switch(event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				mTileBeingDragged = identifyTouchedTile(event.getX(), event.getY());
				if (mTileBeingDragged != null)
				{
					mTilePositions.remove(mTileBeingDragged);
					mFingerOffsetX = event.getX() - mTileBeingDragged.x;
					mFingerOffsetY = event.getY() - mTileBeingDragged.y;
					removeTileFromGrid(mTileBeingDragged);
					invalidate();
				}
				break;
				
			case MotionEvent.ACTION_MOVE:
				if (mTileBeingDragged != null)
				{
					mTileBeingDragged.x = event.getX() - mFingerOffsetX;
					mTileBeingDragged.y = event.getY() - mFingerOffsetY;
					invalidate();
				}
				break;
				
			case MotionEvent.ACTION_UP:
				if (mTileBeingDragged != null)
				{
					dropTile(mTileBeingDragged);
					mTilePositions.addFirst(mTileBeingDragged);
					mTileBeingDragged = null;
					invalidate();
				}
				break;
				
			default:
				return super.onTouchEvent(event);
		}
		
		return true;
	}
	
	private TilePosition identifyTouchedTile(float touchX, float touchY)
	{
		for (TilePosition tileData : mTilePositions)
		{
			if ((tileData.x < touchX) && (touchX < tileData.x + mTileWidth) &&
				(tileData.y < touchY) && (touchY < tileData.y + mTileHeight))
			{
				return tileData;
			}
		}
		
		return null;
	}
	
	// Note: This function should check if removing the tile is a valid move
	// and do something appropriate if not. It should also check if it is a
	// winning move. For Tetravex, we know tile removal is always okay, but
	// that is more knowledge of the game than the view should have.
	private void removeTileFromGrid(TilePosition tileData)
	{
		if (tileData.onGrid)
		{
			tileData.onGrid = false;
			mController.removeTileFromGrid(tileData.gridX, tileData.gridY);
		}
	}
	
	// Handle tile placement after it is dropped.
	// Place the tile on the grid if over grid and if the move is valid. If the
	// move is not valid, we shove the tile downward until it is entirely in
	// the scratch area.
	private void dropTile(TilePosition tileData)
	{
		gridCoordinates gridPosition = getGridCoordinates(tileData.x, tileData.y);
		if (gridPosition == null) return;
		
		int moveType = mController.placeTileOnGrid(tileData.tileNum, gridPosition.x, gridPosition.y);
		switch (moveType)
		{
			case Tetravex.WINNING_MOVE:
				mPuzzleSolved = true;
				SoundEffectPlayer.play(mController, winSoundEffect);
			case Tetravex.VALID_MOVE:
				placeTileOnGrid(tileData, gridPosition.x, gridPosition.y);
				if (moveType != Tetravex.WINNING_MOVE)
					SoundEffectPlayer.play(mController, placeTileOnGridSoundEffect);
				break;
				
			// Nothing to do but push tile downward into scratch area
			case Tetravex.INVALID_MOVE:
				tileData.y = (gridToScratchAreaRatio + cushionBetweenGridAndTile) * getHeight();
				SoundEffectPlayer.play(mController, rejectTileSoundEffect);
				break;
		}
	}
	
	// Actually place a tile on a specific grid square
	private void placeTileOnGrid(TilePosition tileData, int x, int y)
	{
		tileData.onGrid = true;
		tileData.gridX = x;
		tileData.gridY = y;
		
		// Snap tile to grid square
		tileData.x = tileData.gridX * mTileWidth;
		tileData.y = tileData.gridY * mTileHeight;
	}
	
	// Return value of getGridCoordinates
	private class gridCoordinates
	{
		int x;
		int y;
	}
	
	// If tile is over grid, compute the closest grid square. If not, return null
	private gridCoordinates getGridCoordinates(float x, float y)
	{
		if (y >= gridToScratchAreaRatio * getHeight()) return null;
		
		gridCoordinates closestGridSquare = new gridCoordinates();
		double smallestDistanceSoFar = Double.MAX_VALUE;
		
		for (int i=0; i<mGridSize; i++)
		{
			for (int j=0; j<mGridSize; j++)
			{
				float gridX = i*mTileWidth;
				float gridY = j*mTileHeight;
				double distance = computeEuclideanDistance(x, y, gridX, gridY);
				
				if (distance < smallestDistanceSoFar)
				{
					smallestDistanceSoFar = distance;
					closestGridSquare.x = i;
					closestGridSquare.y = j;
				}
			}
		}
		
		return closestGridSquare;
	}
	
	// Computes distance between two points using the Pythagorean Theorem
	private double computeEuclideanDistance(float x1, float y1, float x2, float y2)
	{
		return Math.sqrt(Math.pow((double)(x1 - x2), (double)2) + Math.pow((double)(y1 - y2), (double)2));
	}
}