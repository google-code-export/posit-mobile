/*******************************************************************************
 * Copyright (c) 2009 .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package org.hfoss.posit;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Shows the user specifics about the development team.
 * 
 * 
 *
 */
public class AboutActivity extends Activity{
	private static final String TAG = "AboutActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	     setContentView(R.layout.about_copyright);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.about_copyright_menu, menu);
		return true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent intent;
		switch(item.getItemId()) {
			case R.id.settings_menu_item:
				intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				break;
			case R.id.goback_menu_item:
				intent = new Intent(this,PositMain.class);
				startActivity(intent);
				break;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}

