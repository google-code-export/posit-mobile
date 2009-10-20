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
 *     Qianqian Lin - Summer 2009 Intern 
 ******************************************************************************/
package org.hfoss.posit;

import java.util.ArrayList;
import java.util.HashMap;

import org.hfoss.posit.web.Communicator;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class ShowProjectsActivity extends ListActivity {

	private static final String TAG = "ListActivity";
	private ArrayList<HashMap<String, Object>> list;
	private String appKey;
	private String imei;
	private String deviceName="";
	private Communicator comm;
	private static AlertDialog myAlertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_projects);
		comm = new Communicator(this);
		list = comm.getProjects();
		SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.projects_list_row,
				new String[]{ "name","app_key" },
				new int[]{ R.id.name_id, R.id.description_id });
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		HashMap<String, Object> map = list.get(position);
		Log.i(TAG, "App Key "+map.get("app_key").toString());
		appKey = map.get("app_key").toString();
		imei = Utils.getIMEI(this);

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("APP_KEY", map.get("app_key").toString());
		editor.commit();
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
		myAlertDialog = new AlertDialog.Builder(this)
		.setTitle(R.string.alert_dialog_title)
		.setView(textEntryView)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				EditText et = (EditText) ShowProjectsActivity.myAlertDialog.findViewById(R.id.devicename_edit);
				deviceName = et.getText().toString();
				Log.i(TAG, deviceName);
				if (comm.registerDevice(appKey, imei, deviceName)) {
					Utils.showToast(ShowProjectsActivity.this, R.string.device_registered);
				} else {
					Utils.showToast(ShowProjectsActivity.this, R.string.device__not_registered);
				}
				ShowProjectsActivity.this.finish();
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				/* User clicked cancel so do nothing*/
			}
		}).create();
		myAlertDialog.show();
	}
}
