/*
 * File: SettingsActivity.java
 * 
 * Copyright (C) 2009 The Humanitarian FOSS Project (http://www.hfoss.org)
 * 
 * This file is part of POSIT, Portable Open Search and Identification Tool.
 *
 * POSIT is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) as published 
 * by the Free Software Foundation; either version 3.0 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU LGPL along with this program; 
 * if not visit http://www.gnu.org/licenses/lgpl.html.
 * 
 */
package org.hfoss.posit;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

/**
 * Offers the user various options on how things should work in POSIT.  The user can choose whether or
 * not they want automatic syncing to be on and whether they want notifications about syncing.  The user
 * can also register their phone from this screen in case they need to register with a different web server.
 * Lastly, the user can also set their group size should they need to be in ad hoc mode. 
 * 
 *
 */
public class SettingsActivity extends PreferenceActivity {
	private static final String TAG = "SettingsActivity";
	protected static final int BARCODE_READER = 0;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		addPreferencesFromResource(R.xml.posit_preferences);

		//Get the custom barcode preference
		Preference barcodePref = (Preference) findPreference("APP_KEY");
		barcodePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {	
				Intent intent = new Intent(SettingsActivity.this, ServerRegistrationActivity.class);
				try {
					startActivity(intent);
				} catch(ActivityNotFoundException e) {
					Log.e(TAG, e.toString());
				}
				return true;
			}
		});
	}
}