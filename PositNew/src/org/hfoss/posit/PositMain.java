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

import java.util.HashMap;
import java.util.List;

import org.hfoss.posit.web.Communicator;
import org.hfoss.posit.web.SyncService;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
        PreferenceManager.setDefaultValues(this, R.xml.posit_preferences, true);
        //_startService();
        deleteLater();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String APP_KEY = sp.getString("APP_KEY", null);
        setContentView(R.layout.main);
        // FIXME there has to be a better way to detect this, but its okay for now
        if (APP_KEY.equals("")||APP_KEY.equals(null)){
        	Intent intent = new Intent(this, ShowProjectsActivity.class);
        	startActivity(intent);
        }
        
    }

	private void deleteLater() {
		Communicator comm = new Communicator(this);
		MyDBHelper mDBHelper  = new MyDBHelper(this);
		List<Integer> findsList = mDBHelper.getAllUnsyncedIds();
		for (int id: findsList) {
			Find find = new Find(this, id);
			comm.sendFind(find);
		}
		
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
