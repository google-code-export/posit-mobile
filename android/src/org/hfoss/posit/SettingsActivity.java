/*******************************************************************************
 * Copyright (c) 2009 .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

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