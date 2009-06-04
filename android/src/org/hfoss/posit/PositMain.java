/*******************************************************************************
 * Copyright (c) 2009 .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Prasanna Gautam <prasanna.gautam@trincoll.edu>
 *     Ralph Morelli <ralph.morelli@trincoll.edu>
 *     Trishan de Lanerolle <trishan.delanerolle@trincoll.edu>
 *     
 ******************************************************************************/
package org.hfoss.posit;

import org.hfoss.posit.web.SyncThread;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * @author rmorelli
 *
 */
public class PositMain extends Activity {
    private static final String TAG = "PositMain";	
    private ProgressDialog pd;
    
	/**
	 * Called with the application is first created. 
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 //       PreferenceManager.setDefaultValues(this, R.xml.posit_preferences, true);
        setContentView(R.layout.main);
        checkPhoneRegistrationAndInitialSync();
    }

    /**
     * The phone is registered if it has an APP_KEY that matches one of
     * the projects on the server specified in the phone's preferences. 
     * Shared preferences are also checked to see whether the phone should
     * sync up with the server.
     */
    private void checkPhoneRegistrationAndInitialSync() {
       SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
       String APP_KEY = sp.getString("APP_KEY", null);
       
       // If not APP_KEY, register the phone, otherwise display is server address.
        if (APP_KEY == null || APP_KEY.equals("")||APP_KEY.equals(null)){
        	Intent intent = new Intent(this, ShowProjectsActivity.class);
        	startActivity(intent);
        } else {
			Utils.showToast(this, "This phone is registered with " +
					sp.getString("SERVER_ADDRESS",null) + ".");
        }
        boolean syncIsOn = sp.getBoolean("SYNC_ON_OFF", true);
        if (syncIsOn)
            syncFinds();
    }


	/**
	 * syncFinds creates the SyncThread and starts the synchronization 
	 *  process.
	 */
	private void syncFinds() {
		pd = ProgressDialog.show(this, "Synchronizing", "Please wait.", true,false);
		Thread syncThread = new SyncThread(this,handler);
		syncThread.start();
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
				intent = new Intent(this,FindActivity.class);
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
//		Intent svc = new Intent(this, SyncService.class);
//		stopService(svc);
	}

    
	/**
	 * The handler object dismisses the SyncThread when it
	 *  receives a message that the thread has completed.
	 */
	private Handler handler = new Handler() {

		/* (non-Javadoc)
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			if (msg.what==SyncThread.DONE) {
				pd.dismiss();
			}
		}
	};
	
}
