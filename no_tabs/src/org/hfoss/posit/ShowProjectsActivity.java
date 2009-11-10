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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.hfoss.posit.web.Communicator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
 * @author Qianqian Lin
 *
 */
public class ShowProjectsActivity extends Activity implements View.OnClickListener{

	private static final String TAG = "ListActivity";
	private static final int CONFIRM_PROJECT_CHANGE = 0;
	private ArrayList<HashMap<String, Object>> list;
	private Communicator comm;
	private RadioGroup mRadio;
	private int mCheckedPosition = 0;

	/**
	 * Called when the activity is first started.  Shows a list of radio buttons, each representing
	 * a different project on the server.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_projects);
		comm = new Communicator(this);
		
		try{
		list = comm.getProjects();
		mRadio = (RadioGroup) findViewById(R.id.projectsList);
		Iterator<HashMap<String, Object>> it = list.iterator();
		
		for(int i = 0; it.hasNext(); i++) {
			RadioButton button = new RadioButton(this);
			button.setId(i);

			button.setOnClickListener(this);
			button.setText((String)(it.next().get("name")));
			mRadio.addView(button);
		}
		}catch(Exception e){
			finish();
			Utils.showNetworkErrorDialog(this);
			
		}
		
		
	}

	/**
	 * Called when the user clicks on a project in the list.  Sets the project id in the shared
	 * preferences so it can be remembered when the application is closed
	 */
	public void onClick(View v) {
		mCheckedPosition  = mRadio.getCheckedRadioButtonId();
		String projectId = (String) list.get(mCheckedPosition).get("id");
		int id  = Integer.parseInt(projectId);
		String projectName = (String) list.get(mCheckedPosition).get("name");
		
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
						+ (String) list.get(mCheckedPosition).get("name"))
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