/*
 * File: SyncThread.java
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
package org.hfoss.posit.web;

import org.hfoss.posit.utilities.Utils;
import java.util.StringTokenizer;

import org.hfoss.posit.Find;
import org.hfoss.posit.provider.MyDBHelper;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Handles synchronization between the phone (client) and a server.
 */
public class SyncThread extends Thread {

	private static final String TAG = "SyncThread";
	public static final int DONE = 0;
	public static final int NETWORKERROR = 2;
	public volatile boolean shutdownRequested = false;
	public static final int SYNCERROR = 1;
	private Handler mHandler;
	private Context mContext;
	
	public SyncThread(Context context, Handler handler) {
		mHandler = handler;
		mContext = context;
	}

	/**
	 * NOTE:  This method should be broken into sub methods.
	 * Handles all syncing steps using the following algorithm:
	 * 1) Get a list of GUIDs of all finds on the (registered) server 
	 *    that have changed since last sync with this device
	 * 2) Get a list of GUIDs of all finds on the device that have
	 *    changed since the last sync
	 * 3) Send device Finds to the server to either create or update
	 *    Finds on the server 
	 * 4) Get Finds from the server and either create or update Finds
	 *    in the device's DB.
	 * 5) Record the synchronization timestamp in the device's sync_history table.
	 * 6) Record the synchronization timestamp in the server's sync_history table.
	 * 
	 * This algorithm depends on two tables:
	 * 1) The find_history table records all changes to Finds on the device,
	 *    including create, update, delete.
	 * 2) The sync_history table records the timestamp of each sync with the server.
	 * 
	 * 
	 */
	public void run() {
		MyDBHelper mdbh = new MyDBHelper(mContext);

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		//		sp.setDefaultValues(mContext, R.xml.posit_preferences, false);
		String server=sp.getString("SERVER_ADDRESS", null);
		String authKey = sp.getString("AUTHKEY", null);
		int projectid = sp.getInt("PROJECT_ID", 0);
		TelephonyManager manager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = manager.getDeviceId();
		Communicator comm = new Communicator(mContext);

		if (!Utils.isConnected(mContext)){
			Utils.showToast(mContext, "Phone not connected to network");
			Log.i(TAG, "Phone not connected to network");
			mHandler.sendEmptyMessage(NETWORKERROR);
		}

		// Get finds from the server since last sync with this device
		// (NEEDED: should be made project specific)

		String url = "";
		String serverFindsNeedingSync = "";
		url = server + "/api/getDeltaFindsIds?authKey=" +authKey + "&imei=" + imei;
		Log.i(TAG, "getDeltaFindsIds URL=" + url);

		try {
			serverFindsNeedingSync = comm.doHTTPGET(url);
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
			Utils.showToast(mContext, e.getMessage());
			mHandler.sendEmptyMessage(NETWORKERROR);
		}
		Log.i(TAG,"serverFindsNeedingSync = " + serverFindsNeedingSync);

		// Get finds from the client

		String phoneFindsNeedingSync = mdbh.getDeltaFindsIds();
		Log.i(TAG, "phoneFindsNeedingSync = " + phoneFindsNeedingSync);

		// Send finds to the server

		StringTokenizer st = new StringTokenizer(phoneFindsNeedingSync,",");
		String str, find_guid, action;
		while (st.hasMoreElements()) {
			str = st.nextElement().toString();
			int indx = str.indexOf(':');
			find_guid = str.substring(0,indx);
			action = str.substring(indx+1);
			Log.i(TAG, "Find="+find_guid+" action="+action);
			if (action.equals("delete")) {
				Log.i(TAG, "Ignoring deletions");
			} else {
				Find find = new Find(mContext, find_guid);   // Create a Find object
				try {
					comm.sendFind(find,action);                  //  Send it to server
				} catch (Exception e) {
					Log.i(TAG, e.getMessage());
					//				Utils.showToast(mContext, e.getMessage());
					mHandler.sendEmptyMessage(NETWORKERROR);				
				}
			}
		}
		
		// Get finds from the server and store in the DB
		
		st = new StringTokenizer(serverFindsNeedingSync, ",");
		while (st.hasMoreElements()) {
			find_guid = st.nextElement().toString();
			ContentValues cv = comm.getRemoteFindById(find_guid); 
			if (cv != null) {
				Log.i(TAG,cv.toString());
				Find find = new Find(mContext, find_guid);
				MyDBHelper dbh = new MyDBHelper(mContext);
				
				boolean success = false;
				if (dbh.containsFind(find_guid)) {
					dbh.updateFind(find_guid, cv);       // Update the DB
					Log.i(TAG,"Updating existing find");
				} else {
					success = dbh.addNewFind(cv, null);  // Add find to DB w/o images
					Log.i(TAG,"Adding a new find");
				}
				if (!success) {
					 Log.i(TAG, "Error recording sync stamp");
//					 Utils.showToast(mContext, "Error recording timestamp");
					 mHandler.sendEmptyMessage(SYNCERROR);
				} else {
					Log.i(TAG, "Recorded timestamp stamp");
				}
			}
		}
		
		// Record the synchronization in the client's sync_history table
		
		ContentValues values = new ContentValues();
		values.put(MyDBHelper.SYNC_COLUMN_SERVER, server);
		boolean success = mdbh.recordSync(values);
		if (!success) {
			Log.i(TAG, "Error recording sync stamp");
//			Utils.showToast(mContext, "Error recording timestamp");
			mHandler.sendEmptyMessage(SYNCERROR);
		}

		// Record the synchronization in the server's sync_history table
		
		url = server + "/api/recordSync?authKey=" +authKey + "&imei=" + imei;
		Log.i(TAG, "recordSyncDone URL=" + url);
		String responseString = "";
		try {
			responseString = comm.doHTTPGET(url);
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
//			Utils.showToast(mContext, e.getMessage());
			mHandler.sendEmptyMessage(NETWORKERROR);
		}
		Log.i(TAG,"HTTPGet recordSync response = " + responseString);

		mHandler.sendEmptyMessage(DONE);
	}
}