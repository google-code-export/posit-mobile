/*******************************************************************************
 * Copyright (c) 2009 .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Prasanna Gautam - initial API and implementation
 *     Ralph Morelli - Supervisor
 *     Trishan deLanerolle - Director
 *     Antonio Alcorn - Summer 2009 Intern
 *     Gong Chen - Summer 2009 Intern
 *     Chris Fei - Summer 2009 Intern
 *     Phil Fritzsche - Summer 2009 Intern
 *     James Jackson - Summer 2009 Intern
 *     Qianqian Lin - Summer 2009 Intern 
 *     Khanh Pham - Summer 2009 Intern
 ******************************************************************************/

package org.hfoss.posit;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * @author rmorelli
 */
public class PositMain extends Activity {
	private static final int confirm_exit=0;
//	public static AdhocClient mAdhocClient;
	public static WifiManager wifiManager;
	/**
	 * Called when the application is first created.  If there is no wireless or data connection, starts the 
	 * AdhocClient to work in ad hoc mode.  Also, if this is the first time that the application is run, 
	 * the application will check the phone's registration.  Also saves the
	 * selected project id so that when the application is restored, the right project is showed.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//if (!Utils.isConnected(this)) 
		//	startActivity(new Intent(this, AdhocClientActivity.class));
		//else 
		if(savedInstanceState==null) {
			checkPhoneRegistrationAndInitialSync();
			
		}
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String projectName = sp.getString("PROJECT_NAME", "");
		if(!projectName.equals(""))
			Utils.showToast(this, projectName);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("notFirst", true);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		setContentView(R.layout.main);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setContentView(R.layout.main);
	}

	@Override
	protected void onPause(){
		super.onPause();
	}	

	
	/**
     * The phone is registered if it has an authentication key that matches one of
     * the projects on the server specified in the phone's preferences. If the phone is not registered, 
     * the user will be prompted to go to the server site and register their phone.
     * Shared preferences are also checked to see whether the phone should
     * sync up with the server.
     */
    private void checkPhoneRegistrationAndInitialSync() {
       SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
       
       String AUTH_KEY = sp.getString("AUTHKEY", null);
       
	   if (AUTH_KEY == null || AUTH_KEY.equals("")||AUTH_KEY.equals(null))
		   	startActivity(new Intent(this, ServerRegistrationActivity.class));
	   else if (sp.getBoolean("SYNC_ON_OFF", true)){	
       		Intent intent = new Intent(this, SyncActivity.class);
			intent.setAction(Intent.ACTION_SYNC);
			//startActivity(intent);
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

	/* When hit the back key, there should be a confirmation dialog pop-out 
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case R.id.settings_menu_item:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		case R.id.about_menu_item:
			startActivity(new Intent(this,AboutActivity.class));
			break;
		case R.id.projects_menu_item:
			startActivity(new Intent(this, ShowProjectsActivity.class));
			break;
			
/*		case R.id.rwg_start_activity:
			startActivity(new Intent(this, AdhocClientActivity.class));
			break;
*/
		case R.id.track_menu_item:
			startActivity(new Intent(this, TrackerActivity.class));
			break;
		/*
		case R.id.rwg_start:

			wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE); 
			mAdhocClient = new AdhocClient(this);
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
	        Editor edit = sp.edit();
	        edit.putBoolean("IS_ADHOC", true);
	        edit.commit();
	        break;
		case R.id.rwg_end:

			mAdhocClient.end();
			stopService(new Intent(this,RWGService.class));
			Utils.showToast(this, "RWG Service Stopped");
			break;
		*/
		}
		return true;
	}

	/**
	 * Makes sure that the user did not press the back key by accident by showing a confirmation dialog.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			showDialog(confirm_exit);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStop(){
		super.onStop();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case confirm_exit:
			return new AlertDialog.Builder(this)
			.setIcon(R.drawable.alert_dialog_icon)
			.setTitle(R.string.exit)
			.setPositiveButton(R.string.alert_dialog_ok, 
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// User clicked OK so do some stuff 
					finish();
				}
			}).setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					setContentView(R.layout.main);
					/* User clicked Cancel so do nothing */
				}
			}).create();

		default:
			return null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Intent svc = new Intent(this, SyncService.class);
		// stopService(svc);
	}
}