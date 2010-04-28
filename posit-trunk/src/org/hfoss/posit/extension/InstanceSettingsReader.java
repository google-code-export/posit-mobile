/*
 * File: InstanceSettingsReader.java
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

package org.hfoss.posit.extension;

import java.io.IOException;
import java.io.InputStream;

import org.hfoss.posit.utilities.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.util.Log;
/**
 * This is used to read the assets/settings.json file and update the application 
 * preferences
 * @author pgautam
 *
 */
public class InstanceSettingsReader {
	private static final String TAG = "InstanceSettingsReader";
	private String serverAddress;
	private int projectId;
	private Context mContext;
	private String authKey;
	private boolean syncOn;
	private String projectName;
	private String instanceName;
	private String instanceDescription;
	
	public InstanceSettingsReader(Context context)  {
		mContext = context;
	}
	

	/**
	 * Parse the json object in the file
	 * @return
	 */
	public  boolean parseSettingsFile(){
//		FileInputStream fIs = new FileInputStream(new File(SETTINGS_FILE));
//		BufferedInputStream bS = new BufferedInputStream(fIs);
		
		AssetManager assetManager = mContext.getAssets();
		try {
			InputStream inputStream = assetManager.open("settings.json");

			JSONObject jsonObject = new JSONObject(Utils
					.readInputStreamToString(inputStream));
			serverAddress = jsonObject.getString("serverAddress");
			projectId = jsonObject.getInt("projectId");
			projectName = jsonObject.getString("projectName");
			authKey = jsonObject.getString("authKey");
			syncOn = jsonObject.getBoolean("syncOn");
			instanceName = jsonObject.getString("instanceName");
			instanceDescription = jsonObject.getString("instanceDescription");
			loadValuesToSharedPreferences();
			return true;
		} catch (IOException e) {
			Log.e(TAG, "settings.json not found, falling back");
		} catch (JSONException e) {
			Log.e(TAG, "settings.json file isn't formatted properly, falling back to register");
			
		}
		return false;
	}


	/**
	 * Load all the values to the global shared preferences
	 */
	private void loadValuesToSharedPreferences() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor editor = sp.edit();
		editor.putString("AUTHKEY", authKey);
		editor.putInt("PROJECT_ID", projectId);
		editor.putString("PROJECT_NAME", projectName);
		editor.putString("SERVER_ADDRESS", serverAddress);
		editor.putBoolean("SYNC_ON_OFF", syncOn);
		editor.putString("INSTANCE_NAME", instanceName);
		editor.putString("INSTANCE_DESCRIPTION", instanceDescription);
		editor.commit();
	}


	/**
	 * sets the projectId
	 * @param projectId
	 */
	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}
	/**
	 * returns the projectId
	 * @return
	 */
	public int getProjectId() {
		return projectId;
	}
	/**
	 * sets the server address from the file
	 * @param serverAddress
	 */
	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public String getServerAddress() {
		return serverAddress;
	} 
}
