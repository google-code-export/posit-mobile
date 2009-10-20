package org.hfoss.posit.victim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hfoss.posit.util.DataPoint;
import org.hfoss.posit.util.StaticOverlay;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentReceiver;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayController;
import com.google.android.maps.Point;

public class MapperNew extends MapActivity{
	
	private Bundle extras; 
	int latitude, longitude;          
	private String Latitude_string, Longitude_string;
	protected final long MINIMUM_DISTANCECHANGE_FOR_UPDATE = 25; // in Meters
	protected final long MINIMUM_TIME_BETWEEN_UPDATE = 5500; // in Milliseconds
	private boolean doUpdates =true;
	private static final String MY_LOCATION_CHANGED_ACTION =
		"android.intent.action.LOCATION_CHANGED";
	private String APP = "Mapper";  //to simplify the debugging process!
	protected final IntentFilter myIntentFilter =  new IntentFilter(MY_LOCATION_CHANGED_ACTION);
	protected MyIntentReceiver myIntentReceiver = null;

	/*Needed for the MapView and overlaying*/
	private MapView mMapView;
	protected MapController mapController;
	protected OverlayController overlayController;
	private Location location ;
	private LocationManager locationManager ;
	private LocationProvider locationProvider ;
	private Boolean input=false;
	private DBHelper mDbHelper;
	private Long mRowId=1L;
	  // Minimum & maximum latitude so we can span it
    // The latitude is clamped between -80 degrees and +80 degrees inclusive
    // thus we ensure that we go beyond that number
    private int minLatitude = (int) (+81 * 1E6);

    private int maxLatitude = (int) (-81 * 1E6);

    // Minimum & maximum longitude so we can span it
    // The longitude is clamped between -180 degrees and +180 degrees inclusive
    // thus we ensure that we go beyond that number
    private int minLongitude = (int) (+181 * 1E6);;

    private int maxLongitude = (int) (-181 * 1E6);;

	public MapperNew() {
	}
	
	public void onCreate(Bundle icicle){
		super.onCreate(icicle);
		final Intent intent = getIntent();
		mDbHelper = new DBHelper(this);
		mDbHelper.open();
		// is the row a new one or an existing?
		mRowId = icicle != null ? icicle.getLong(DBHelper.KEY_ROWID) : null;
	    if (mRowId == null) {
	         extras = intent.getExtras();
	         mRowId = extras != null ? extras.getLong(DBHelper.KEY_ROWID) : null;
	    }
	    
	    if (extras != null){
	    	Latitude_string = intent.getStringExtra(DBHelper.KEY_LATITUDE);
	    	Longitude_string = intent.getStringExtra(DBHelper.KEY_LONGITUDE);
	    	input = true;
	    }
	    Log.i("Mapper", Latitude_string+ " " +Longitude_string);
	    /*Do we want a window title? Nope :)*/
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.newfindmap);
		mMapView = (MapView)findViewById(R.id.mapper);
		
		mapViewItems();
		//updateView();
		//setupForGPSAutoRefreshing();
	}
	
	private void mapViewItems(){
		String name, description;
		mapController = mMapView.getController();
		this.overlayController = mMapView.createOverlayController();
		Cursor c = mDbHelper.fetchSelectedColumns(new String[]{
		DBHelper.KEY_LATITUDE, DBHelper.KEY_LONGITUDE, DBHelper.KEY_NAME, DBHelper.KEY_DESCRIPTION
		}		
		);
		HashMap<String,String> args = new HashMap<String,String>();
		List<DataPoint> mPoints = new ArrayList<DataPoint>();
		while (c.next()) {
            latitude = (int)(c.getDouble(c.getColumnIndex(DBHelper.KEY_LATITUDE))*1E6);
            longitude = (int)(c.getDouble(c.getColumnIndex(DBHelper.KEY_LONGITUDE))*1E6);
            name = c.getString(c.getColumnIndex(DBHelper.KEY_NAME));
            description = c.getString(c.getColumnIndex(DBHelper.KEY_DESCRIPTION));
            Log.i(APP,latitude+" latitude");
            Log.i(APP, longitude+" longitude");
            
            // Sometimes the longitude or latitude gathering
            // did not work so skipping the point
            // doubt anybody would be at 0 0
            if (latitude != 0 && longitude != 0) {

                    // Sets the minimum and maximum latitude so we can span and zoom
                    minLatitude = (minLatitude > latitude) ? latitude : minLatitude;
                    maxLatitude = (maxLatitude < latitude) ? latitude : maxLatitude;

                    // Sets the minimum and maximum latitude so we can span and zoom
                    minLongitude = (minLongitude > longitude) ? longitude
                                    : minLongitude;
                    maxLongitude = (maxLongitude < longitude) ? longitude
                                    : maxLongitude;

                    args.put(DBHelper.KEY_NAME, name);
                    args.put(DBHelper.KEY_DESCRIPTION, description);
                    mPoints.add(new DataPoint(new Point(latitude, longitude),args ));
            }
            mapController.zoomToSpan(maxLatitude - minLatitude, maxLongitude-minLongitude);
            mapController.animateTo(new Point(
                    (maxLatitude + minLatitude)/2,
                    (maxLongitude + minLongitude)/2 )); 
            StaticOverlay myMapOverlay = new StaticOverlay(mPoints,mMapView, this);
            
            overlayController.add(myMapOverlay, true);
            
    }

		
		
	
		
		
	}
	public void placePoint(String latitude,String longitude, String type)
	{
		Point x = new Point(
				(int) (Double.parseDouble(latitude) * 1E6),
				(int) (Double.parseDouble(longitude) * 1E6));
		mapController.centerMapTo(x, false);
		
		
	}

	private void updateView() {
		// Refresh our gps-location
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		location = locationManager.getCurrentLocation("gps");
		/* Redraws the mapView, which also makes our
		 * OverlayController redraw our Circles and Lines */
		mMapView.invalidate();
	}
	
	public void onFreeze(Bundle icicle) {
		/*We don't want to update the map when we aren't using it
		 * do we? Saves some battery.
		 */
		this.doUpdates = false;
		//this.unregisterReceiver(this.myIntentReceiver);
		super.onFreeze(icicle);
	}

	public void onResume() {
		super.onResume();
		this.doUpdates = true;

		/* As we only want to react on the LOCATION_CHANGED
		 * intents we made the OS send out, we have to
		 * register it along with a filter, that will only
		 * "pass through" on LOCATION_CHANGED-Intents.
		 */
		this.registerReceiver(this.myIntentReceiver, this.myIntentFilter);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_I) {
			// Zoom In
			int level = mMapView.getZoomLevel();
			mapController.zoomTo(level + 1);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_O) {
			// Zoom Out
			int level = mMapView.getZoomLevel();
			mapController.zoomTo(level - 1);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_S) {
			// Switch on the satellite images
			mMapView.toggleSatellite();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_T) {
			// Switch on traffic overlays
			mMapView.toggleTraffic();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK){
			finish();
		}else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER){
			//showDialog();
		}
		return false;
	}
	/**
	 * This tiny IntentReceiver updates
	 * our stuff as we receive the intents
	 * (LOCATION_CHANGED_ACTION) we told the
	 * myLocationManager to send to us.
	 */
	class MyIntentReceiver extends IntentReceiver {
		@Override
		public void onReceiveIntent(Context context, Intent intent) {
			String data = intent.getAction();
			if ( data.equals(MapperNew.MY_LOCATION_CHANGED_ACTION)){
				if(MapperNew.this.doUpdates){
					// Will simply update our list, when receiving an intent
					MapperNew.this.updateView();
				}
			}
		}
		
	}
	
	private void setupForGPSAutoRefreshing(){
		//providerList = this.locationManager.getProviders();
		//Log.e(LOG_TAG, providerList.isEmpty()?"empty":"full"); 
		//get the GPS device
		//locationProvider = providerList.get(0);
		locationProvider = locationManager.getProvider("gps"); 
		this.locationManager.requestUpdates(locationProvider, MINIMUM_TIME_BETWEEN_UPDATE,
				MINIMUM_DISTANCECHANGE_FOR_UPDATE,
				new Intent(MY_LOCATION_CHANGED_ACTION));
		//So, we need to listen for the intent that we asked for our locationManager to
		// send us
		this.myIntentReceiver = new MyIntentReceiver();

	}
}