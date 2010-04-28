/*
 * File: ShowProjectsActivity.java
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.hfoss.posit.utilities.Utils;
import org.hfoss.posit.web.Communicator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * This activity shows a list of all the projects on the server that the phone is registered with,
 * and allows the user to pick one from the list.  When the user picks one, the phone automatically
 * syncs with the server to get all the finds from that project
 * 
 *
 */
public class ShowProjectsActivity extends Activity implements View.OnClickListener{

	private static final String TAG = "ShowProjectsActivity";
	private static final int CONFIRM_PROJECT_CHANGE = 0;
	private int mCheckedPosition = 0;

	private ArrayList<HashMap<String, Object>> projectList;
	private RadioGroup mRadio;	
	private int currentProjectId;

	/**
	 * Called when the activity is first started.  Shows a list of 
	 * radio buttons, each representing
	 * a different project on the server.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		currentProjectId = sp.getInt("PROJECT_ID", 0);
		setContentView(R.layout.list_projects); 
		tryToRegister();
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
	//	tryToRegister();
	}

	private void tryToRegister() {
		if (!Utils.isConnected(this)) {
			reportNetworkError("No Network connection ... exiting");
			return;
		} 
		Communicator comm = new Communicator(this);
		try{
			projectList = comm.getProjects();
		} catch(Exception e){
			Log.i(TAG, "Communicator error " + e.getMessage());
			e.printStackTrace();
			this.reportNetworkError(e.getMessage());
			finish();
		}
		if (projectList != null) {
			mRadio = (RadioGroup) findViewById(R.id.projectsList);
			Iterator<HashMap<String, Object>> it = projectList.iterator();
			for(int i = 0; it.hasNext(); i++) {
				HashMap<String,Object> next = it.next();
				RadioButton button = new RadioButton(this);
				button.setId(i);
				button.setOnClickListener(this);
				button.setText((String)(next.get("name")));
				int buttonProjectId = Integer.parseInt((String)next.get("id"));
				// Toggle the radio button if it is the current project
				if (buttonProjectId==currentProjectId)
					button.toggle(); 
				mRadio.addView(button);
			}
		} else {
			this.reportNetworkError("Null project list returned.\nCheck network connection.");
		}
	}
	


	/**
	 * Reports as much information as it can about the error.
	 * @param str
	 */
	private void reportNetworkError(String str) {
		Log.i(TAG, "Registration Failed: " + str);
		Utils.showToast(this, "Registration Failed: " + str);
		finish();
	}

	/**
	 * Called when the user clicks on a project in the list.  Sets the project id in the shared
	 * preferences so it can be remembered when the application is closed
	 */
	public void onClick(View v) {
		mCheckedPosition  = mRadio.getCheckedRadioButtonId();
		String projectId = (String) projectList.get(mCheckedPosition).get("id");
		int id  = Integer.parseInt(projectId);
		String projectName = (String) projectList.get(mCheckedPosition).get("name");

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = sp.edit();

		editor.putInt("PROJECT_ID", id);
		editor.putString("PROJECT_NAME", projectName);
		editor.commit();

		showDialog(CONFIRM_PROJECT_CHANGE);
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 * Confirms with the user that they have changed their project and automatically syncs with the server
	 * to get all the project finds
	 */
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case CONFIRM_PROJECT_CHANGE:
			return new AlertDialog.Builder(this)
			.setIcon(R.drawable.alert_dialog_icon)
			.setTitle("You have changed your project to: " 
					+ (String) projectList.get(mCheckedPosition).get("name"))
					.setPositiveButton(R.string.alert_dialog_ok, 
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(
									ShowProjectsActivity.this);
							boolean syncIsOn = sp.getBoolean("SYNC_ON_OFF", true);
							if (syncIsOn) {
								Intent intent = new Intent(ShowProjectsActivity.this, SyncActivity.class);
								intent.setAction(Intent.ACTION_SYNC);
								startActivity(intent);
							}
							finish();
						}
					}).create();

		default:
			return null;
		}
	}


}