/*
 * File: PositMain.java
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

import org.hfoss.posit.adhoc.RWGConstants;
import org.hfoss.posit.adhoc.RWGService;
import org.hfoss.posit.extension.InstanceSettingsReader;
import org.hfoss.posit.utilities.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 *  Implements the main activity and the main screen for the POSIT application. 
 */
public class PositMain extends Activity implements OnClickListener, RWGConstants{

	private static final int CONFIRM_EXIT=0;
	private static final String TAG = "PositMain";
	//public static AdhocClient mAdhocClient;
	public static WifiManager wifiManager;
	public RWGService rwgService;
	public Intent rwg;
	
	NotificationManager mNotificationManager;
	
	/**
	 * Called when the activity is first created.  Sets the UI layout, adds
	 * the buttons, checks whether the phone is registered with a POSIT server.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		rwgService =new RWGService();
		setContentView(R.layout.main);

		final Button addFindButton = (Button)findViewById(R.id.addFindButton);
		if(addFindButton!=null)
			addFindButton.setOnClickListener(this);

		final Button listFindButton = (Button)findViewById(R.id.listFindButton);
		if(listFindButton!=null) {
			Log.i(TAG,listFindButton.getText()+"");
			listFindButton.setOnClickListener(this);
		}

//		final Button sahanaButton = (Button)findViewById(R.id.sahanaSMS);
//		if(sahanaButton!=null)
//			sahanaButton.setOnClickListener(this);

		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = sp.edit();
		Log.i(TAG, "onCreate(), Preferences= " + sp.getAll().toString());
		
		// NOTE: If the shared preferences get left in a state with the Tracker's not set to IDLE,
		//  it will be impossible to start the Tracker.  To do so, use the statements here to reset
		//  Tracker State to IDLE.  
		//  This is an area that could use a better algorithm based on Android's life cycle. 
//			editor.putInt(BackgroundTrackerActivity.SHARED_STATE, BackgroundTrackerActivity.IDLE);
//			editor.commit();

		// If this is the first run on this device, let the user register the phone.
		if(savedInstanceState==null)  {
			checkPhoneRegistrationAndInitialSync();
		}

		Utils.showToast(this, "Current Project: "+sp.getString("PROJECT_NAME", ""));
		
		/*  ******* POLICY:  RWG should not be running at start up  */
		
		if (RWGService.isRunning())  {
			Log.i(TAG, "RWG running");
			Utils.showToast(this, "RWG running");
		}
		
	}

	/**
	 * Handles clicks on PositMain's buttons.
	 */
	public void onClick(View view) {
		Intent intent = new Intent();

		switch(view.getId()) {
		case R.id.addFindButton :
			intent.setClass(this, FindActivity.class);
			intent.setAction(Intent.ACTION_INSERT);
			startActivity(intent);
			break;
		case R.id.listFindButton :
			intent.setClass(this, ListFindsActivity.class);
			startActivity(intent);
			break;
//		case R.id.sahanaSMS:
//			intent.setClass(this, SahanaSMSActivity.class);
//			startActivity(intent);
//			break;
		}	
	}

	
	/**
	 * Creates the menu options for the PositMain screen.  Menu items are inflated
	 *  from a resource file.
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.positmain_menu, menu);
		return true;
	}
	
	/**
	 * Updates the RWG Start/End menus based on whether RWG is running or not.
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (RWGService.isRunning()) {
			menu.findItem(R.id.rwg_start).setEnabled(false);
			menu.findItem(R.id.rwg_end).setEnabled(true);
		} else {
			menu.findItem(R.id.rwg_start).setEnabled(true);
			menu.findItem(R.id.rwg_end).setEnabled(false);
		}

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);	
		int trackerState = sp.getInt(BackgroundTrackerActivity.SHARED_STATE, -1);
//		Log.i(TAG, " Preferences= " + sp.getAll().toString());
//		Log.i(TAG, "onPrepareOptionsMenu trackerState = " + trackerState);
//		if (trackerState != BackgroundTrackerActivity.RUNNING &&
//				trackerState != BackgroundTrackerActivity.PAUSED) {
//			menu.findItem(R.id.track_menu_item).setEnabled(true);
//		} else {
//			menu.findItem(R.id.track_menu_item).setEnabled(false);
//		}
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Manages the selection of menu items. 
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
		case R.id.track_menu_item:
			startActivity(new Intent(this, BackgroundTrackerActivity.class));
			break;
		case R.id.rwg_start:
			wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE); 
			//mAdhocClient = new AdhocClient(this);		
			rwg = new Intent(this, RWGService.class);
	        //rwgService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        RWGService.setActivity(this);

			startService(rwg);
			break;
		case R.id.rwg_end:
			if(RWGService.isRunning() && rwg!=null)  // Kill RWG if already running
				stopService(rwg);
			try{
				rwgService.killProcessRunning("./rwgexec");
			}
			catch(Exception e) {
				Log.e(TAG,e.getClass().toString(),e);
			}
			mNotificationManager.cancel(Utils.ADHOC_ON_ID);
			Utils.showToast(this, "RWG Service Stopped");
			break;
		}
		return true;
	}
	

	/**
	 * Checks whether the phone is registered with POSIT server.
	 * The phone is registered if it has an authentication key that matches one of
	 * the projects on the server specified in the phone's preferences. If the phone is not registered, 
	 * the user will be prompted to go to the server site and register their phone.
	 * Shared preferences are also checked to see whether the phone should
	 * sync up with the server.
	 */
	private void checkPhoneRegistrationAndInitialSync() {
		loadInstanceSettings();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String AUTH_KEY = sp.getString("AUTHKEY", null);
		if (AUTH_KEY == null || AUTH_KEY.equals("") || AUTH_KEY.equals(null))
			startActivity(new Intent(this, ServerRegistrationActivity.class));
	}


	/**
	 * Reads the settings file and loads certain settings to SharedPreferences
	 * 
	 * The settings are passed as a JSON object and
	 * include serverAddress, projectId, projectName, authKey, syncOn,
	 * instanceName, instanceDescription.
	 */
	private void loadInstanceSettings() {
		InstanceSettingsReader i = new InstanceSettingsReader(this);
		i.parseSettingsFile(); 
	}

	/**
	 * Intercepts the back key (KEYCODE_BACK) and displays a confirmation dialog
	 * when the user tries to exit POSIT.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//  REMOVED:  To allow navigating back to Tracker		
//		if(keyCode==KeyEvent.KEYCODE_BACK){
//			showDialog(confirm_exit);
//			return true;
//		}
//		Log.i("code", keyCode+"");
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStop(){
		super.onStop();
	}

	/**
	 * Creates a dialog to confirm that the user wants to exit POSIT.
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case CONFIRM_EXIT:
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
					/* User clicked Cancel so do nothing */
				}
			}).create();

		default:
			return null;
		}
	}
	

	/** 
	 * Makes sure RWG is stopped before exiting the Activity
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish() {
		if(RWGService.isRunning() && rwg!=null)  // Kill RWG if already running
			stopService(rwg);
		try{
			rwgService.killProcessRunning("./rwgexec");
		}
		catch(Exception e) {
			Log.e(TAG,e.getClass().toString(),e);
		}
		mNotificationManager.cancel(Utils.ADHOC_ON_ID);
		Utils.showToast(this, "RWG Service Stopped");
		super.finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Intent svc = new Intent(this, SyncService.class);
		// stopService(svc);
	}
}