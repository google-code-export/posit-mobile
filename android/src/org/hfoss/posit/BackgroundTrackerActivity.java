/*
 * File: TrackerActivity.java
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

import java.util.List;

//import org.hfoss.posit.TrackActivity.MyThreadRunner;
import org.hfoss.posit.utilities.MyItemizedOverlay;
import org.hfoss.posit.utilities.Utils;
import org.hfoss.posit.web.Communicator;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ZoomControls;

/**
 * Tracks the phone's location in a background thread, sending period updates to 
 *  the registered server and displaying the points in a MapView.
 *  
 *  TODO: Incorporate a check for Network connectivity before starting the Tracker. 
 *  
 */

public class BackgroundTrackerActivity extends MapActivity implements LocationListener {
	private static final String TAG = "TrackerActivity";

	public static final String SHARED_STATE = "TrackerState";
	public static final String NO_PROVIDER = "No location service";
	
	public static final int UPDATE_LOCATION = 2;
	public static final boolean ENABLED_ONLY = true;
	public static final int CONFIRM_EXIT=0;

	public static final int IDLE = 0;
	public static final int RUNNING = 1;
	public static final int PAUSED = 2;

	private static final int SLEEP_INTERVAL = 5000; // milliseconds
	private static final int UPDATES_INTERVAL = 5000; // 
	private static final int DEFAULT_SWATH_WIDTH = 50;  // 50 meters
	private static final String PROVIDER = "gps";

	private double mLongitude = 0;
	private double mLatitude = 0;
	private double mAltitude = 0;
	private long mSwath = DEFAULT_SWATH_WIDTH;
	
	private Communicator mCommunicator;

	private TextView mLocationTextView;
	private TextView mStatusTextView;
	private TextView mExpeditionTextView;
	private TextView mPointsTextView;
	private TextView mSwathTextView;

    private SharedPreferences mPreferences ;
    private SharedPreferences.Editor spEditor;
    
	private MapView mMapView;
	private MapController mapController;
	private List<Overlay> mapOverlays;
	private ZoomControls mZoom;
	private LinearLayout linearLayout;

	private Thread mThread;
	private LocationManager mLocationManager;
	private Location mLocation;
	private String mProvider = NO_PROVIDER;
	
	private ConnectivityManager mConnectivityMgr;
	private int mNetworkType;
	
	private NotificationManager mNotificationManager;
	public static final int NOTIFY_TRACKER_ID = 1001;
	
	private int mState; 
	private int mExpeditionNumber;
	private int mProjId;
	private int mPoints;
	private boolean hasNetworkService;
	private boolean hasGpsService;

	/**
	 * Handles GPS updates.  The handleMessage() method is called repeatedly from
	 *  the GPS thread with location updates.  This method calls post(udateDisplay)
	 *  to cause the updateDisplay() method to be run in the UI thread, rather than
	 *  in the background.  This is necessary because the background thread cannot
	 *  access the View. The handler's post() method puts the updateDisplay() method
	 *  in the queue, giving it a turn to run.
	 *  
	 *  <P>Each time it is called it sends the GPS points to the server and updates
	 *  the View.   
	 *  
	 *  TODO: Should it also store the point in the phone's memory??
	 *  TODO: Should it be possible to download and display an Expedition?? 
	 *  
	 * @see http://www.androidph.com/2009/02/app-10-beer-radar.html
	 */
	final Handler updateHandler = new Handler() {

		/** Gets called on every message that is received */
		// @Override
		public void handleMessage(Message msg) {
			mLocation = mLocationManager.getLastKnownLocation(mProvider);
			if (mLocation == null) {
				Log.e(TAG, "handleMessage(), Null location returned");
				updateState(PAUSED);
				post(updateDisplay);
				return;
			}

			mLatitude = mLocation.getLatitude();
			mLongitude = mLocation.getLongitude();
			mAltitude = mLocation.getAltitude();
			
			// Try to handle a change in network connectivity
			// We may lose a few points, but try not to crash
			try {
				mNetworkType = mConnectivityMgr.getActiveNetworkInfo().getType();
				
				String result = mCommunicator.registerExpeditionPoint(mLatitude, mLongitude, mAltitude, mSwath, mExpeditionNumber);
				mPoints++;

				// Calls updateDisplay in the main (UI) thread to update the View
				// The tracking thread cannot update the view directly
				post(updateDisplay); 

				Log.i(TAG, "handleMessage() " + mLongitude + " " + mLatitude);		
				
			} catch (Exception e) {
				Log.i(TAG, "Error handleMessage " + e.getMessage());
				e.printStackTrace();
				finish();
			}
			
			

			switch (msg.what) {
			case UPDATE_LOCATION:
				break;
			}
			super.handleMessage(msg);
		}

		/**
		 * Runs in the UI thread to update the View. Invoked indirectly by 
		 *  the updateHandler, which posts the method to the queue.
		 */
		final Runnable updateDisplay = new Runnable() {
			public void run() {
				updateView();
			}
		};
	};

	/**
	 * Sets up the initial state of the Tracker.  This method should only be
	 *  reached when Tracker starts up.  Tracker saves its state in SharedPreferences
	 *  when the user selects PositMain from its menu. This allows the user to perform
	 *  any POSIT actions while the Tracker is running in the background. 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate()");
			
		setContentView(R.layout.tracker);
		mLocationTextView = (TextView)findViewById(R.id.trackerLocation);
		mStatusTextView = (TextView)findViewById(R.id.trackerStatus);
		mExpeditionTextView = (TextView)findViewById(R.id.trackerExpedition);
		mPointsTextView = (TextView)findViewById(R.id.trackerPoints);
		mSwathTextView = (TextView)findViewById(R.id.trackerSwath);
		linearLayout = (LinearLayout) findViewById(R.id.zoomview);
		mMapView = (MapView) findViewById(R.id.mapFinds);
		mZoom = (ZoomControls) mMapView.getZoomControls();
		linearLayout.addView(mZoom);
		if (!doSetup()) {
			finish();
			return;
		}
		else if (!checkAllServices()) {
			finish();
			return;
		}
		else 
			updateView();
	}
	
	
	
	/**
	 * Intercepts configuration change notices so the Activity does not restart when the
	 *  phone's orientation (horizontal, vertical) is changed.
	 * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.i(TAG,"onConfigChanged()");
//		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
//			setContentView(R.layout-land.tracker);
//		else
//			setContentView(R.layout.tracker);
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	/**
	 * Checks for network service, location provider, and that the phone's current
	 *  location can be determined, returning false if any of those fail.
	 * @return
	 */
	private boolean checkAllServices() {
		Log.i(TAG,"checkAllServices()");
		hasNetworkService = setNetworkType();
		if (!hasNetworkService)
			return false;
		else if (!(hasGpsService = setLocationProvider()))
			return false;
//		else if ((mLocation = setInitialLocation()) == null){
//			return false;
//		} 
		else {
			mLocation = setInitialLocation();
			return true;
		}
	}
	
	
	/* (non-Javadoc)
	 * Deal here with the user leaving the Tracker. Most importantly, 
	 * any changes made by the user should at this point be committed
	 * @see com.google.android.maps.MapActivity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}




	/* (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onResume()
	 */
	@Override
	protected void onResume() {
		Log.i(TAG,"onResume()");
		// TODO Auto-generated method stub
		super.onResume();
		updateView();
	}

//  NOTE: Couldn't get this to work properly at starting/stopping the background
//  thread. Instead we implement onConfigurationChanged() to intercept orientation
//  changes. 	
//	/**
//	 * Saves the Tracker's state when the keyboard is open or the orientation
//	 *  is changed (horizontal, vertical) on Nexus.
//	 */
//	@Override
//	protected void onSaveInstanceState(Bundle outState) {
//		Log.i(TAG, "onSave()");
//
//		outState.putDouble("longitude", mLongitude);
//		outState.putDouble("latitude", mLatitude);
//		outState.putDouble("altitude", mAltitude);
//		outState.putInt("projid", mProjId);
//		outState.putInt("points", mPoints);
//		outState.putInt("expedition", mExpeditionNumber);
//		outState.putInt("state",mState);
//		outState.putBoolean("service", hasNetworkService);
//		outState.putBoolean("gps", hasGpsService);
//		outState.putParcelable("location", mLocation);
//		updateState(PAUSED);
//
//		//stopTracking();
//		super.onSaveInstanceState(outState);
//	}
//	
//	/**
//	 * Restores the Tracker's state after the keyboard is opened/closed or
//	 *  orientation (horizontal, vertical) is changed in Nexus.
//	 */
//	@Override
//	protected void onRestoreInstanceState(Bundle savedInstanceState) {
//		super.onRestoreInstanceState(savedInstanceState);
//		Log.i(TAG, "onRestore()");
//		mLongitude = savedInstanceState.getDouble("longitude");
//		mLatitude = savedInstanceState.getDouble("latitude");
//		mAltitude = savedInstanceState.getDouble("altitude");
//		mProjId = savedInstanceState.getInt("projid");
//		mPoints = savedInstanceState.getInt("points");
//		mState = savedInstanceState.getInt("state");
//		hasNetworkService = savedInstanceState.getBoolean("service");
//		hasGpsService = savedInstanceState.getBoolean("gps");
//		mExpeditionNumber = savedInstanceState.getInt("expedition");
//		mLocation = (Location)savedInstanceState.getParcelable("location");
//	    mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//		updateState(RUNNING);
//		startTracking();
//	} 

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.tracker_menu, menu);
		return true;
	}

	/**
	 * Updates the Tracker Start/stop menus based on whether Tracker is running or not.
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mState == RUNNING) {
			menu.findItem(R.id.start_tracking_menu_item).setEnabled(false);
			menu.findItem(R.id.stop_tracking_menu_item).setEnabled(true);
		} else {
			menu.findItem(R.id.start_tracking_menu_item).setEnabled(true);
			menu.findItem(R.id.stop_tracking_menu_item).setEnabled(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case R.id.start_tracking_menu_item:
			mExpeditionNumber = registerExpedition();
			startTracking();
			//Utils.showToast(this, "Expedition number " + mExpeditionNumber);
			break;
//		case R.id.back_to_main_menu_item:
//			// TODO: This should be handled in a better way, utilizing the Android lifecycle
//			// This approach appears to add another instance of PositMain to the Activity stack
//			// rather than returning to the parent.  Possible approach:  Have the Tracker save
//			// it's state and then return.
//			startActivity(new Intent(this,PositMain.class));
//			break;
			//			case R.id.new_expedition_menu_item:
			////				registerExpedition();
			//				break;
			//				
		case R.id.stop_tracking_menu_item:
			updateState(PAUSED);
			stopTracking();
			updateView();
			break;
		}
		return true;
	}
	
	/**
	 * Attempts to communicate with the server.
	 * @return
	 */
	private int registerExpedition() {
		mCommunicator = new Communicator(this);
		return mCommunicator.registerExpeditionId(mProjId);
	}

	/**
	 * Places the Tracker notification icon in the status bar.
	 */
	private void notifyUser() {
		int icon = R.drawable.radar;        // icon from resources
		CharSequence tickerText = "Tracking";              // ticker-text
		long when = System.currentTimeMillis();         // notification time
		Context context = getApplicationContext();      // application Context
		CharSequence contentTitle = "Tracker notification";  // expanded message title
		CharSequence contentText = "Phone's location is being tracked.";      // expanded message text

		Intent notificationIntent = new Intent(this, BackgroundTrackerActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		// the next two lines initialize the Notification, using the configurations above
		Notification notification = new Notification(icon, tickerText, when);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		
		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) getSystemService(ns);
		mNotificationManager.notify(NOTIFY_TRACKER_ID, notification);  // 1 = ID for this notification
	}

	/** 
	 * The only way to exit Tracker and destroy its state is to use the back 
	 *  key.  Tracker will exit back to PositMain if the user confirms the exit.
	 *  If the exit is confirmed, Tracker's state will be saved in Preferences
	 *  as IDLE.
	 *  
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			showDialog(CONFIRM_EXIT);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Creates a dialog to confirm that the user wants to exit POSIT. If
	 * confirmed the finish() method is called, which sets Tracker's state
	 * to IDLE, stops the broadcast service and exits.
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
	 * Sets Tracker's state to IDLe and stops the location broadcast manager
	 * before exiting.  It is important that the state be set to IDLE. If it
	 * is set to some other value, he Tracker menu in PositMain will be disabled.
	 * 
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish() {
		Log.i(TAG,"finish()");
		super.finish();
		if (mState != IDLE)
			stopTracking();
	}

	/**
	 * Shuts down the location service and notification manager.
	 */
	private void stopTracking() {
		updateState(IDLE);
		
		if (mLocationManager != null) {
			Log.i(TAG, "Stopping tracking thread");
			Utils.showToast(this, "Stopped tracking thread");
//			this.unregisterReceiver(this);  // Unsuccessful attempt to get rid of Intent leak
			mLocationManager.removeUpdates(this); // Stop location manager updates
		} else
			Log.i(TAG,"stopTacking() mLocationManager = null");
		if (mNotificationManager != null) {
			mNotificationManager.cancel(NOTIFY_TRACKER_ID);
		}
	}
	
	/**
	 * Create the background thread and starts it. Note the call to Looper.prepare().
	 * This is necessary in order for the thread to invoke the Handler.  
	 *  
	 * @see http://developerlife.com/tutorials/?p=290
	 * @param backgroundTrackerActivity
	 */
	private void startTracking() {
		// Check that we have service
		if (!checkAllServices()) {
			finish();
			return;
		}

		startLocationUpdateService(mProvider);
		notifyUser();
		updateState(RUNNING);
		updateView();

		new Thread() {
			@Override public void run() {
				Looper.prepare();
				//Looper.loop();
				trackInBackground();
			}
		}.start();

		Utils.showToast(this, "Tracking the phone's location");
	}

	/** 
	 * Performs the periodic updates of the phone's location. Whenever it wakes
	 * up, it obtain's the current location and sends a message to the Handler
	 * object back in the UI thread.  The Handler object invokes the updateView()
	 * method to display the location on the MapView.
	 *  
	 */
	private void trackInBackground () {
		Log.i(TAG, "trackInBackground, Starting tracking");
		updateState(RUNNING);
		while (mState == RUNNING  && !Thread.currentThread().isInterrupted()) {
			Message m = Message.obtain();
			m.what = 0;
			BackgroundTrackerActivity.this.updateHandler.sendMessage(m);
			try {
				Thread.sleep(SLEEP_INTERVAL);	
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		Log.i(TAG, "Thread completed");
	}

	/**
	 * Updates the Tracker's state and saves the state in SharedPreferences.  It is 
	 * important that this method be called with argument IDLE before exiting in order
	 *  to enable the Tracker menu when PositMain starts up again.
	 *  
	 * @param state an integer representation of the state (IDLE, PAUSED or RUNNING)
	 */
	private void updateState(int state) {
		mState = state;
		int sharedState = mPreferences.getInt(SHARED_STATE, -1);
		try {
			if (sharedState == -1) {
				throw new Exception("ERROR: Shared Preference for " + SHARED_STATE + " invalid");
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		spEditor = mPreferences.edit();
		spEditor.putInt(SHARED_STATE, mState);
		spEditor.commit();	
		Log.i(TAG, "Preferences= " + mPreferences.getAll().toString());
	}
	
	
	/**
	 * Updates the Tracker's View, including the MapView portion. 
	 */
	private void updateView() {
		Log.i(TAG,"updateView(), mPoints = " + mPoints);
		mExpeditionTextView.setText(" "+mExpeditionNumber);
		String s = " Idle ";
		if (mState == RUNNING) 
			s = " Running ";
		else if (mState == PAUSED)
			s = " Paused ";
		String netStr = (mNetworkType == ConnectivityManager.TYPE_WIFI) ? " WIFI" : " MOBILE";
 		mStatusTextView.setText(s + " (GPS = " + mProvider 
				+ ", Ntwk = " + netStr + ")");
		mPointsTextView.setText("  " + mPoints);
		mSwathTextView.setText(" " + mSwath);
		mLocationTextView.setText(mLatitude + "," + mLongitude + "," + mAltitude);
		
		int lat = (int)(mLatitude * 1E6);
		int lon = (int)(mLongitude * 1E6);
		
		MyItemizedOverlay points = new MyItemizedOverlay(this.getResources().getDrawable(R.drawable.redbutton),this,false);
		points.addOverlay(new OverlayItem(new GeoPoint(lat,lon),null,null));
		
		mapOverlays = mMapView.getOverlays();
		mapOverlays.add(points);
	
		mapController = mMapView.getController();
	}

	/**
	 * Establishes the initial state of the Tracker and sets up and starts the
	 * Location service. Called from onCreate().
	 * 
	 */
	private boolean doSetup() {
		Log.i(TAG, "doSetup()");
	    mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	    spEditor = mPreferences.edit();
		mProjId = mPreferences.getInt("PROJECT_ID", 0);	
		if (mProjId == 0) {
			Utils.showToast(this, "Aborting Tracker:\nDevice must be registered with a project.");
			return false;
		}

		updateState(IDLE);
		mPoints = 0;
		mSwath = DEFAULT_SWATH_WIDTH;
		return true;
	}

	/**
	 * Sets the connectivity to WIFI or MOBILE or returns false.
	 * @return
	 */
	private boolean setNetworkType() {
		// Check for network connectivity
		mConnectivityMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo info = mConnectivityMgr.getActiveNetworkInfo();
		if (info == null) {
			Log.e(TAG, "setNetworkType() unable to acquire CONNECTIVITY_SERVICE");
			Utils.showToast(this, "Aborting Tracker: No Active Network.\nYou must have WIFI or MOBILE enabled.");
			return false;
		}
		mNetworkType = info.getType();
		Log.i(TAG, "setNetworkType(), active network type = " + mConnectivityMgr.getActiveNetworkInfo().getTypeName());
		return true;
	}
	
	/**
	 * Sets the Find's location to the last known location and sets the
	 * provider to either GPS, if enabled, or network, if enabled.
	 */
	private boolean setLocationProvider() {
		Log.i(TAG, "setLocationProvider...()");
	
		// Check for Location service
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		
		List<String> providers = mLocationManager.getProviders(ENABLED_ONLY);
		mProvider = NO_PROVIDER;
		if (providers.contains(LocationManager.GPS_PROVIDER)) {
			mProvider = LocationManager.GPS_PROVIDER;
		} 
		else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
			mProvider = LocationManager.NETWORK_PROVIDER;
		}
		if (mProvider.equals(NO_PROVIDER)) {
			Utils.showToast(this, "Aborting Tracker: " +  
					NO_PROVIDER +  "\nYou must have GPS enabled. ");
			return false;
		}
		Log.i(TAG, "setLocationProvider()= " + mProvider);
		return true;
	}

	/**
	 * Sets the phone's last known location.
	 * @return
	 */
	private Location setInitialLocation() {
		Log.i(TAG, "setInitialLocation() " + mProvider);
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		mLocation = mLocationManager.getLastKnownLocation(mProvider);
		if (mLocation != null) {
			mLongitude = mLocation.getLongitude();
			mLatitude = mLocation.getLatitude();
			mAltitude = mLocation.getAltitude();
			Log.i(TAG, "Location= " + mLatitude + "," + mLongitude);
		} else {
			Log.e(TAG, "Location= NULL" + mLatitude + "," + mLongitude);
//			Utils.showToast(this,"Aborting Tracker:\nUnable to obtain current location");
		}
		return mLocation;
	}

	/**
	 * Starts the update service unless there is no provider available.
	 */
	private boolean startLocationUpdateService(String provider) {
		Log.i(TAG, "startLocationUpdateService()");
		if (provider.equals(NO_PROVIDER))
			return false;

		try {
			mLocationManager.requestLocationUpdates(provider, UPDATES_INTERVAL, 0, this);	
		} catch (Exception e) {
			Log.e(TAG, "Error starting location services " + e.getMessage());
			e.printStackTrace();
			return false;
		} 
		Log.i(TAG, "startLocationUpdateService started");
		return true;
	}
	
	/**
	 * Sends a message to the update handler with either the current location or 
	 *  the last known location. This method is called with null on its first 
	 *  invocation.  
	 *  
	 * @param location is either null or the current location
	 */
	private void setCurrentGpsLocation(Location location) {
		if (location == null) {
			mLocation = setInitialLocation(); // This sets the global mLocation used below
		}
		if (Utils.debug) Log.i(TAG, "setCurrentGpsLocation , Provider = |" + mProvider + "|");

		try {
			mLongitude = mLocation.getLongitude();
			mLatitude = mLocation.getLatitude();
			mAltitude = mLocation.getAltitude();
			Message msg = Message.obtain();
			msg.what = UPDATE_LOCATION;
			if (Utils.debug) Log.i(TAG, "setCurrentGpsLocation msg= " + msg);
			updateHandler.sendMessage(msg);
		} catch (NullPointerException e) {
				Log.e(TAG, e.toString());
				e.printStackTrace();
		}
	}
	
	// ------------------------ LocationListener Methods ---------------------------------//

	/**
	 * Invoked by the location service when phone's location changes.
	 */
	public void onLocationChanged(Location newLocation) {
		setCurrentGpsLocation(newLocation);  	
	}
	/**
	 * Resets the GPS location whenever the provider is enabled.
	 */
	public void onProviderEnabled(String provider) {
		setCurrentGpsLocation(null);  	
	}
	/**
	 * Resets the GPS location whenever the provider is disabled.
	 */
	public void onProviderDisabled(String provider) {
		//		Log.i(TAG, provider + " disabled");
		setCurrentGpsLocation(null);  	
	}
	/**
	 * Resets the GPS location whenever the provider status changes. We
	 * don't care about the details.
	 */
	public void onStatusChanged(String provider, int status, Bundle extras) {
		setCurrentGpsLocation(null);  	
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}		
}

