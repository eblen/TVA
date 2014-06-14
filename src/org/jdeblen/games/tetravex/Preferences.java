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
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Preferences extends PreferenceActivity {
	private static final String OPT_SOUND_EFFECTS = "sound_effects";
	private static final boolean OPT_SOUND_EFFECTS_DEF = true;
	private static final String OPT_PUZZLE_SIZE = "puzzle_size";
	private static final String OPT_PUZZLE_SIZE_DEF = "3";
	private static final String OPT_NUMBER_OF_EDGE_TYPES = "number_of_edge_types";
	private static final String OPT_NUMBER_OF_EDGE_TYPES_DEF = "8";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		
		// Automatically toggle sound effects on/off when setting is changed
		Preference soundEffectsPreference = findPreference(OPT_SOUND_EFFECTS);
		soundEffectsPreference.setOnPreferenceChangeListener(
			new Preference.OnPreferenceChangeListener()
			{
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue)
				{
					Boolean setting = (Boolean)newValue;
					if (setting.equals(Boolean.TRUE)) SoundEffectPlayer.enable();
					else SoundEffectPlayer.disable();
					
					return true;
				}
			});
	}
	
	// Getters for Preferences
	public static boolean getSoundEffects(Context context)
	{
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_SOUND_EFFECTS, OPT_SOUND_EFFECTS_DEF);
	}
	
	public static int getPuzzleSize(Context context)
	{
		String s = PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_PUZZLE_SIZE, OPT_PUZZLE_SIZE_DEF);
		return Integer.parseInt(s);
	}
	
	public static int getNumberOfEdgeTypes(Context context)
	{
		String s = PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_NUMBER_OF_EDGE_TYPES, OPT_NUMBER_OF_EDGE_TYPES_DEF);
		return Integer.parseInt(s);
	}
	
	
}
