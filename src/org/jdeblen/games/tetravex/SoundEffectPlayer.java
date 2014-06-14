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

import android.content.Context;
import android.media.MediaPlayer;

// Simple class to play sound effects - copied from "Hello, Android" by Ed
// Burnette, third edition, page 106.
public class SoundEffectPlayer {
	private static MediaPlayer mp = null;
	private static boolean enabled = true;

	public static void play(Context context, int resource)
	{
		if (!enabled) return;
		
		stop(context);
		mp = MediaPlayer.create(context, resource);
		mp.start();
	}
	
	public static void stop(Context context)
	{
		if (mp != null)
		{
			mp.stop();
			mp.release();
			mp = null;
		}
	}
	
	public static void enable()
	{
		enabled = true;
	}
	
	public static void disable()
	{
		enabled = false;
	}
}