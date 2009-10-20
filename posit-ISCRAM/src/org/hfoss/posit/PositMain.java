/*******************************************************************************
 * Copyright (c) 2009 .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     pgautam - initial API and implementation
 ******************************************************************************/
package org.hfoss.posit;

import org.hfoss.posit.web.SyncService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.GetChars;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class PositMain extends Activity {
    private static final String TAG = "PositMain";

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _startService();
        
        setContentView(R.layout.main);
    }

	private void _startService() {
		try {
			SyncService.setMainActivity(this);
			Intent svc = new Intent(this, SyncService.class);
			startService(svc);
		} catch(Exception e){
			Log.e(TAG, "ui creation problem",e);
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.positmain_menu, menu);
		return true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent intent;
		switch(item.getItemId()) {
			case R.id.new_find_menu_item:
				intent = new Intent(this,AddFindActivity.class);
				intent.setAction(Intent.ACTION_INSERT);
				startActivity(intent);
				break;
			case R.id.list_find_menu_item:
				intent = new Intent(this,ListFindsActivity.class);
				startActivity(intent);
				break;
			case R.id.settings_menu_item:
				intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				break;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Intent svc = new Intent(this, SyncService.class);
		stopService(svc);
	}
    
	
}
