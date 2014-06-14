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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public class TetravexTileFactory {
	private static final int MAX_VALUES = 10;
	private static final Paint tileBorderPaint;
	private static final Paint [] tilePaints;
	
	static
	{
		tileBorderPaint = new Paint();
		tileBorderPaint.setColor(Color.BLACK);
		tileBorderPaint.setStyle(Paint.Style.STROKE);
		tileBorderPaint.setStrokeWidth(5);
		
		tilePaints = new Paint[MAX_VALUES];
		for (int i=0; i < MAX_VALUES; i++) tilePaints[i] = new Paint();
		
		tilePaints[0].setColor(Color.WHITE);
		tilePaints[1].setColor(Color.DKGRAY);
		tilePaints[2].setColor(Color.BLUE);
		tilePaints[3].setColor(Color.RED);
		tilePaints[4].setColor(Color.YELLOW);
		tilePaints[5].setColor(Color.GREEN);
		tilePaints[6].setColor(Color.MAGENTA);
		tilePaints[7].setColor(Color.parseColor("#ffffa500")); // Orange
		tilePaints[8].setColor(Color.CYAN);
		tilePaints[9].setColor(Color.GRAY);
		
		for (Paint p : tilePaints) p.setStyle(Paint.Style.FILL);
	}

	public static Bitmap buildColorTile(Tetravex.Tile tile, int width, int height)
	{
		float midpointX = width / 2f;
		float midpointY = height / 2f;
		
		// Create bitmap, wrap in a temporary canvas for drawing
		Bitmap tileBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvasForTile = new Canvas(tileBitmap);
		
		// Create triangle paths
		Path topTriangle = new Path();
		topTriangle.moveTo(0, 0);
		topTriangle.lineTo(midpointX, midpointY);
		topTriangle.lineTo((float) width, 0);
		
		Path leftTriangle = new Path();
		leftTriangle.moveTo(0, 0);
		leftTriangle.lineTo(midpointX, midpointY);
		leftTriangle.lineTo(0, (float) height);
		
		Path rightTriangle = new Path();
		rightTriangle.moveTo((float) width, 0);
		rightTriangle.lineTo(midpointX, midpointY);
		rightTriangle.lineTo((float) width, (float) height);
		
		Path bottomTriangle = new Path();
		bottomTriangle.moveTo(0, (float) height);
		bottomTriangle.lineTo(midpointX, midpointY);
		bottomTriangle.lineTo((float) width, (float) height);

		// Draw triangles
		canvasForTile.drawPath(topTriangle, tilePaints[tile.top]);
		canvasForTile.drawPath(leftTriangle, tilePaints[tile.left]);
		canvasForTile.drawPath(rightTriangle, tilePaints[tile.right]);
		canvasForTile.drawPath(bottomTriangle, tilePaints[tile.bottom]);
		
		// Draw triangle borders
		canvasForTile.drawPath(topTriangle, tileBorderPaint);
		canvasForTile.drawPath(leftTriangle, tileBorderPaint);
		canvasForTile.drawPath(rightTriangle, tileBorderPaint);
		canvasForTile.drawPath(bottomTriangle, tileBorderPaint);
		
		// Draw edge borders
		canvasForTile.drawLine(0, 0, width, 0, tileBorderPaint);
		canvasForTile.drawLine(0, 0, 0, height, tileBorderPaint);
		canvasForTile.drawLine(width, 0, width, height, tileBorderPaint);
		canvasForTile.drawLine(0, height, width, height, tileBorderPaint);
		
		return tileBitmap;
	}
}
