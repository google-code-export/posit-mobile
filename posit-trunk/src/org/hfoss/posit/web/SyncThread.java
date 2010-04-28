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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.hfoss.posit.Find;
import org.hfoss.posit.provider.PositDbHelper;
import org.hfoss.posit.utilities.Utils;
import org.hfoss.third.Base64Coder;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Handles synchronization between the phone (client) and a server.
 */
public class SyncThread extends Thread {

	private static final String TAG = "SyncThread";
	public static final int DONE = 100;
	public static final int NETWORKERROR = 20;
	public volatile boolean shutdownRequested = false;
	public static final int SYNCERROR = 10;
	public static final int INTERRUPTED = 30;

	private Handler mHandler;
	private Context mContext;
	private boolean mConnected;
	private boolean mStopRequested;
		
	private PositDbHelper mdbh; 

	private SharedPreferences sp; 
	private String server;
	private String authKey;
	private int mProjectid;
	private TelephonyManager manager; 
	private String imei;
	private Communicator comm;
	
	
	/**
	 * Constructor sets up the thread environment with references to the
	 * Activity context and the message handler.  And it sets up the 
	 * control variables used to suspend and stop the thread.
	 * 
	 * It initially assumes that there is no network connection.
	 * The NetworkConnectivityListener will notify it when a 
	 * connection is obtained.
	 * 
	 * @param context
	 * @param handler
	 */
	public SyncThread(Context context, Handler handler) {
		mHandler = handler;
		mContext = context;
		mConnected = false;
		mStopRequested = false;	
	}
	
	/**
	 * Must be called when a network WIFI connection is detected
	 * in order for the synchronization to run. It notify's the
	 * waiting thread.
	 * 
	 * @param connected
	 */
	public synchronized void setConnected(boolean connected) {
		mConnected = connected;
		if (mConnected)
			notify();
		Log.i(TAG,"Set connected to " + mConnected);
	}
	
	/**
	 * Stops the thread by forcing the run() method to return.
	 * Called when there's no valid WIFI.
	 */
    public synchronized void stopThread() {
    	mStopRequested = true;
    	mConnected = true;
        notify();
		Log.i(TAG,"Requesting a stop");
    }

    /** 
     * Called repeatedly during run() to make sure each synchronization
     * step completes successfully.
     */
    private void waitHere() {
		try {
			synchronized(this) {
				while (!mConnected) {
					Log.i(TAG,"SyncThread starting its wait");
					wait();
					Log.i(TAG,"SyncThread ending its wait");
				}
				if (mStopRequested)
					return;
			}
		} catch (InterruptedException e){
			Log.i(TAG, "Interrupted thread " + e.getMessage());
			e.printStackTrace();
			mHandler.sendEmptyMessage(INTERRUPTED);
		}   	
    }
    
	/**
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
	 * TODO:  Clean up the use of "success" by coordinating return
     * messages with the server.
	 * 
	 */
	public void run() {
		boolean success = false;
		mdbh = new PositDbHelper(mContext);
 
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		//		sp.setDefaultValues(mContext, R.xml.posit_preferences, false);
		server=sp.getString("SERVER_ADDRESS", null);
		authKey = sp.getString("AUTHKEY", null);
		mProjectid = sp.getInt("PROJECT_ID", 0);
		manager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		imei = manager.getDeviceId();
		comm = new Communicator(mContext);
		Log.i(TAG,"server="+server+" key="+authKey+" pid="+mProjectid+" imei="+imei);

		// Wait here to make sure there is a WIFI connection
		waitHere();
		
		// Get finds from the server since last sync with this device
		// (NEEDED: should be made project specific)
		
		String serverFindGuIds = getServerFindsNeedingSync(); 
		
		// Get finds from the client

		String phoneFindGuIds = mdbh.getDeltaFindsIds();
		Log.i(TAG, "phoneFindsNeedingSync = " + phoneFindGuIds);

		// Send finds to the server

		success = sendFindsToServer(phoneFindGuIds);


		// Get finds from the server and store in the DB

		success = getFindsFromServer(serverFindGuIds);
		
		// Record the synchronization in the client's sync_history table

		ContentValues values = new ContentValues();
		values.put(PositDbHelper.SYNC_COLUMN_SERVER, server);
		
		success = mdbh.recordSync(values);
		if (!success) {
			Log.i(TAG, "Error recording sync stamp");
			mHandler.sendEmptyMessage(SYNCERROR);
		}

		// Record the synchronization in the server's sync_history table
		
		String url = server + "/api/recordSync?authKey=" +authKey + "&imei=" + imei;
		Log.i(TAG, "recordSyncDone URL=" + url);
		String responseString = "";
			
		try {
			responseString = comm.doHTTPGET(url);
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
			e.printStackTrace();
			mHandler.sendEmptyMessage(NETWORKERROR);
		}
		Log.i(TAG,"HTTPGet recordSync response = " + responseString);

		mHandler.sendEmptyMessage(DONE);
		return;
	}
      
	/**
     * Returns a list of guIds for server finds that need syncing.
     * @return
     */
    private String getServerFindsNeedingSync() {
   	    String response="";
		String url = "";
		url = server + "/api/getDeltaFindsIds?authKey=" +authKey + "&imei=" + imei + "&projectId=" + mProjectid;
		Log.i(TAG, "getDeltaFindsIds URL=" + url);
		try {
			response = comm.doHTTPGET(url);
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
			e.printStackTrace();
			mHandler.sendEmptyMessage(SYNCERROR);
		}
		Log.i(TAG,"serverFindsNeedingSync = " + response);
		//mServerFindsNeedingSync = response;
		return response;
    } 
    
    /**
     * Sends finds to the server.  Uses a Communicator.
     * @param phoneGuids
     * @return
     */
    private boolean sendFindsToServer(String phoneGuids) {
    	boolean success = false;
    	StringTokenizer st = new StringTokenizer(phoneGuids,",");
    	String str, guid, action;
    	while (st.hasMoreElements()) {
    		str = st.nextElement().toString();
    		int indx = str.indexOf(':');
    		guid = str.substring(0,indx);
    		action = str.substring(indx+1);
    		Log.i(TAG, "Find="+guid+" action="+action);
    		
    		if (action.equals("delete")) {
    			Log.i(TAG, "Ignoring deletions");
    		} else {
    			Find find = new Find(mContext, guid); // Create a Find object
 
    			try {
    				success = comm.sendFind(find,action); //  Send it to server
    			} catch (Exception e) {
   					Log.i(TAG, e.getMessage());
   					e.printStackTrace();
    				mHandler.sendEmptyMessage(NETWORKERROR);				
    			}
    			if (!success) {
    				mHandler.sendEmptyMessage(SYNCERROR);
    			}
    		}
    	}
    	return success;
    }
    
    /**
     * 
     * Retrieve finds from the server using a Communicator.
     * @param serverGuids
     * @return
     */
    private boolean getFindsFromServer(String serverGuids) {
		List<ContentValues> photosList = null;
		String guid;
		boolean success = false;
		StringTokenizer st = new StringTokenizer(serverGuids, ",");
		while (st.hasMoreElements()) {
			guid = st.nextElement().toString();

			ContentValues cv = comm.getRemoteFindById(guid); 
			
			if (cv == null) {
				mHandler.sendEmptyMessage(SYNCERROR);  // Shouldn't be null
			}
			else {
				cv.put(PositDbHelper.FINDS_SYNCED, PositDbHelper.FIND_IS_SYNCED);
				Log.i(TAG,cv.toString());
				PositDbHelper dbh = new PositDbHelper(mContext);
				
				// Get the images for this find
				ArrayList<HashMap<String, String>> images = comm.getRemoteFindImages(guid);

				photosList = saveImages(images);
				success = false;
				
				// Update the DB				
				if (dbh.containsFind(guid)) {
					success = dbh.updateFind(guid, cv, photosList); // Should use a Find? 
					Log.i(TAG,"Updating existing find");
				} else {
					Find newFind = new Find(mContext, guid);
					success = newFind.insertToDB(cv, photosList);
					Log.i(TAG,"Adding a new find");
				}
				if (!success) {
					Log.i(TAG, "Error recording sync stamp");
					mHandler.sendEmptyMessage(SYNCERROR);
				} else {
					Log.i(TAG, "Recorded timestamp stamp");
				}
				dbh.close();
			} 
		}
		return success;
    }
    
    /**
     * Saves downloaded images to the Database and returns their Uris in 
     * a list that can be passed to the Db.
     * @param images
     */
    private List<ContentValues> saveImages(ArrayList<HashMap<String, String>> images ){
    	List<ContentValues> photosList = null;
    	ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();

		if (images != null) {
			Log.i(TAG, "remote find images = " + images.toString());
			for (int j = 0; j < images.size(); j++) {
				Log.i(TAG, "image=" + images.get(j).toString());
				HashMap<String, String> image = images.get(j);  // Get the image
				try {
//					String guid = (String) image.get(PositDbHelper.FINDS_GUID);
					ContentValues photoCv = new ContentValues();
					photoCv.put(PositDbHelper.PHOTOS_MIME_TYPE, 
							(String) image.get(PositDbHelper.PHOTOS_MIME_TYPE));
					photoCv.put(PositDbHelper.FINDS_PROJECT_ID, 
							(String) image.get(PositDbHelper.FINDS_PROJECT_ID));
					Long identifier = Long.parseLong(image.get(PositDbHelper.PHOTOS_IDENTIFIER));
					photoCv.put(PositDbHelper.PHOTOS_IDENTIFIER, identifier.longValue());
					String fullData = (String) image.get("data_full");
					//Log.i("The IMAGE DATA", fullData);
					byte[] data = Base64Coder.decode(fullData);
					Bitmap imageBM = BitmapFactory.decodeByteArray(data, 0, data.length);
					Log.i("The Bitmap To Save", imageBM.toString());
					bitmaps.add(imageBM);
					Log.i(TAG, "bitmap saved!");	
				}
				catch (Exception e){
					Log.d(TAG, ""+e);
				}
			}
			photosList = Utils.saveImagesAndUris(mContext, bitmaps); // Utility method
		}
		return photosList;
    }
}