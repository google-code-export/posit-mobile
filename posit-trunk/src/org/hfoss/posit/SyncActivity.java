/*
 * File: SyncActivity.java
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

import org.hfoss.posit.utilities.Utils;
import org.hfoss.posit.web.Communicator;
import org.hfoss.posit.web.SyncThread;
import org.hfoss.third.NetworkConnectivityListener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Manages synchronization between the phone and a server.  The phone
 * must be registered on the server.
 */
public class SyncActivity extends Activity  {

	private boolean syncSuccess=true;
	private ProgressDialog mProgressDialog;
	private static final String TAG = "SyncActivity";
	private NetworkConnectivityListener ncl;
	private ConnectivityManager mConman;
	private ConnectivityHandler mHandler;
	private SyncThread mSyncThread;
	private Context mContext;
	private boolean result = false;
	private long mStart = 0;
	private static final String PRESS_BACK = " Press the back key TWICE to exit.";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		if (getIntent().getAction().equals(Intent.ACTION_SYNC)) {
			mConman = 
				(ConnectivityManager) this.getSystemService(this.CONNECTIVITY_SERVICE);
			mHandler = new ConnectivityHandler();

			ncl = new NetworkConnectivityListener();
			ncl.registerHandler(mHandler, 0);
			ncl.startListening(this);
		}
	}
	

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		syncFinds();
	}

	/**
	 * Creates a progress dialog and a message handler and starts 
	 * SyncThread which handles the synchronization actions.
	 * 
	 * When SyncThread finishes or stops because of an error a
	 * message is sent to the message handler, which stops the Activity.
	 */
	private void syncFinds() {
		mProgressDialog = ProgressDialog.show(this, "Synchronizing",
				"Please wait.", true, true);
		mStart = System.currentTimeMillis();

		mSyncThread = new SyncThread(this, mHandler);
		Log.i(TAG,"SyncThread " + mSyncThread.getState().toString());
		mSyncThread.start();
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		Log.i(TAG, "TOTAL ACTIVITY TIME = " + (System.currentTimeMillis()-mStart) + " millisecs");
		Log.i(TAG, "TOTAL COMM TIME = " + Communicator.mTotalTime);

		Log.i(TAG, "Stopping listener");
		ncl.stopListening();
		ncl.unregisterHandler(mHandler);
		Log.i(TAG, "Sync thread is " + mSyncThread.getState().toString());
		mProgressDialog.dismiss();
	}

	/**
	 * Intercepts the back key (KEYCODE_BACK) and displays a confirmation dialog
	 * when the user tries to exit SynchActivity.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			mSyncThread.stopThread();
			Log.i(TAG, "Stopping thread");
			finish();
			return true;
		}
		Log.i("code", keyCode+"");
		return super.onKeyDown(keyCode, event);
	}
	

	/**
	 * Listens for messages from the ConnectivityListener and
	 * takes appropriate action.
	 * @author rmorelli
	 *
	 */
	private class ConnectivityHandler extends Handler  {

		public ConnectivityHandler () {
			super();
		}

		/**
		 * Receives and handles messages from the ConnectivityListener.
		 * The messages are sent each time the network status changes. 
		 * In its current form, we really only check before the Sync 
		 * thread starts.  A more refined SyncThread would be able to
		 * resume syncing if it loses the network connection in the middle.
		 */
		public void handleMessage(Message msg) {
			Log.i(TAG,"Message = " + msg);
			switch (msg.what) {
			case NetworkConnectivityListener.STATE_UNKNOWN:
				Log.i(TAG, "Connectivity: UNKNOWN");
				break;
			case NetworkConnectivityListener.STATE_CONNECTED_WIFI:
				mProgressDialog.setMessage("Syncing over WIFI");
				Log.i(TAG, "Connectivity: CONNECTED on WIFI");
				mSyncThread.setConnected(true);
				break;
			case NetworkConnectivityListener.STATE_CONNECTED_MOBILE:
				mProgressDialog.setMessage("Syncing over MOBILE");
				Log.i(TAG, "Connectivity: CONNECTED on MOBILE");
				mSyncThread.setConnected(true);				
				break;
			case NetworkConnectivityListener.STATE_UNCONNECTED:
				Log.i(TAG, "Connectivity: UNCONNECTED");
				mProgressDialog.setMessage("No network connection. " 
						+ PRESS_BACK);
//				Utils.showToast(mContext, "Sync Exiting: No network connection");
				mSyncThread.setConnected(false);
//				finish();
				break;						
			case SyncThread.DONE: 
				if(syncSuccess=true){
				mProgressDialog.setMessage("Sync completed successfully. " 
						+ PRESS_BACK);
				Utils.showToast(mContext, "Sync completed successfully.");
				}else{
				mProgressDialog.setMessage("Sync failed." 
						+ PRESS_BACK);
				Utils.showToast(mContext, "Sync failed.");
				}
				finish();
				break;
			case SyncThread.NETWORKERROR:
				mProgressDialog.setMessage("No network avaiable. "
						+ PRESS_BACK);
				Utils.showToast(mContext, "Sync Exiting: No network available");
				mSyncThread.setConnected(false);
				syncSuccess=false;
//				finish();
				break;
			case SyncThread.SYNCERROR:
				mProgressDialog.setMessage("Sync failed. An unknown error has occurred. "
						+ PRESS_BACK);
				mSyncThread.stopThread();
//				mSyncThread.setConnected(false);
				syncSuccess=false;
				finish();
				break;
			default:
				Log.i(TAG, "What does " + msg.what + " mean?");
			break;		
			}
		}
	}
}
