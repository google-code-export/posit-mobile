package org.hfoss.posit;

import java.util.List;
import java.util.Random;

import org.hfoss.posit.web.Communicator;

import android.app.Activity;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class TrackerActivity extends Activity implements LocationListener {
	private static final String TAG = "TrackerActivity";
	public static final int STATE_EDIT = 1;
	public static final int STATE_INSERT= 2;
	public static final int BARCODE_READER= 3;
	public static final int CAMERA_ACTIVITY= 4;
	public static final int SYNC_ACTIVITY= 5;
	public static final int IMAGE_VIEW = 6;
	private static final int CONFIRM_DELETE_DIALOG = 0;
	private static final int CONFIRM_DELETE_DIALOG_2=1;
	private static final int NON_UNIQUE_ID = 3;
	private static final int UPDATE_LOCATION = 2;
	private static final boolean ENABLED_ONLY = true;
	private static final int THUMBNAIL_TARGET_SIZE = 320;
	
	private double mLongitude = 0;
	private double mLatitude = 0;
	private double mAltitude = 0;
	private Communicator communicator;
	
	private TextView mLocationTextView;

	private Thread mThread;
	private LocationManager mLocationManager;
	private Location mLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	     setContentView(R.layout.tracker);
	     mLocationTextView = (TextView)findViewById(R.id.trackerLocation);
	     
	     communicator = new Communicator(this);
	     communicator.expedition = "auto" + new Random().nextInt(9999);
	     
	     //LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	    // Location location = locationManager.getLastKnownLocation("gps");
	     
	     //Utils.showToast(this, "" + location.getLatitude());
	     
	     //initializeLocationAndStartGpsThread();
	     //Utils.showToast(this, Double.toString(mLongitude));
	     //Utils.showToast(this, c.logPoint(0.0, 0.0));
	     
	}
	


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.tracker_menu, menu);
		return true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent intent;
		switch(item.getItemId()) {
			case R.id.start_tracking_menu_item:
				initializeLocationAndStartGpsThread();
				Utils.showToast(this, communicator.expedition);
				break;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		Intent svc = new Intent(this, SyncService.class);
//		stopService(svc);
	}

	/**
	 * Sets the Find's location to the last known location and starts 
	 *  a separate thread to update GPS location.
	 */
	private void initializeLocationAndStartGpsThread() {
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		List<String> providers = mLocationManager.getProviders(ENABLED_ONLY);
		Log.i(TAG, "Enabled providers = " + providers.toString());
		String provider = mLocationManager.getBestProvider(new Criteria(),ENABLED_ONLY);
		Log.i(TAG, "Best provider = " + provider);

		setCurrentGpsLocation(null);   
		mThread = new Thread(new MyThreadRunner());
		mThread.start();
	}

	/**
	 * Repeatedly attempts to update the Find's location.
	 */
	class MyThreadRunner implements Runnable {
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				Message m = Message.obtain();
				m.what = 0;
				TrackerActivity.this.updateHandler.sendMessage(m);
				try {
					Thread.sleep(5000);	// sleep 10 seconds
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
		
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

	/**
	 * Sends a message to the update handler with either the current location or 
	 *  the last known location. 
	 * @param location is either null or the current location
	 */
	private void setCurrentGpsLocation(Location location) {
		String bestProvider = "";
		if (location == null) {
			mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			List<String> providers = mLocationManager.getProviders(ENABLED_ONLY);
			Log.i(TAG, "Enabled providers = " + providers.toString());
			bestProvider = mLocationManager.getBestProvider(new Criteria(),ENABLED_ONLY);
			if (bestProvider != null && bestProvider.length() != 0) {
				mLocationManager.requestLocationUpdates(
						"gps", 1000, 0, this);	 // Every 5000 millisecs	
				location = mLocationManager.getLastKnownLocation(bestProvider);				
			}	
		}
		Log.i(TAG, "Best provider = |" + bestProvider + "|");

		try {
			mLongitude = location.getLongitude();
			mLatitude = location.getLatitude();
			mAltitude = location.getAltitude();
			Message msg = Message.obtain();
			msg.what = UPDATE_LOCATION;
			TrackerActivity.this.updateHandler.sendMessage(msg);
		} catch (NullPointerException e) {
			//	Avoid crashing Phone when no service is available.
		}
	}
	
	/**
	 * Handles GPS updates.  
	 * Source: Android tutorials
	 * @see http://www.androidph.com/2009/02/app-10-beer-radar.html
	 */
	Handler updateHandler = new Handler() {
		/** Gets called on every message that is received */
		// @Override
		public void handleMessage(Message msg) {
			//mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		    mLocation = mLocationManager.getLastKnownLocation("gps");
		    mLatitude = mLocation.getLatitude();
		    mLongitude = mLocation.getLongitude();
		    mAltitude = mLocation.getAltitude();
			String result = communicator.logPoint(mLatitude, mLongitude, mAltitude);
			mLocationTextView.setText("" + mLatitude + ", " + mLongitude + "; " + result);
			
			switch (msg.what) {
				case UPDATE_LOCATION:
					//mLatitudeTextView.setText(" " + mLatitude);
					//mLongitudeTextView.setText(" " + mLongitude);
					break;
			}
			super.handleMessage(msg);
		}
	};
}

