package org.hfoss.posit;

import java.util.ArrayList;
import java.util.HashMap;

import org.hfoss.posit.util.DataSet;
import org.hfoss.posit.util.SpinnerBindings;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MapperNew extends MapActivity {
/**
 * The placeholder mapper class
 * <b>Deprecated</b>
 * @author pgautam
 *
 */
	private Bundle extras;
	int latitude, longitude;
	private String Latitude_string, Longitude_string;
	protected final long MINIMUM_DISTANCECHANGE_FOR_UPDATE = 25; // in Meters
	protected final long MINIMUM_TIME_BETWEEN_UPDATE = 5500; // in Milliseconds
	private boolean doUpdates = true;
	private static final String MY_LOCATION_CHANGED_ACTION = "android.intent.action.LOCATION_CHANGED";
	private String APP = "Mapper"; // to simplify the debugging process!
	protected final IntentFilter myIntentFilter = new IntentFilter(
			MY_LOCATION_CHANGED_ACTION);
	//protected MyIntentReceiver myIntentReceiver = null;

	/* Needed for the MapView and overlaying */
	private MapView mMapView;
	protected MapController mapController;
	private Location location;
	private LocationManager locationManager;
	private LocationProvider locationProvider;
	private Boolean input = false;
	private Spinner nameSpinner;
	private DBHelper mDbHelper;
	private Long mRowId = 1L;
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

	private void fillNames() {
		Cursor c = mDbHelper
				.fetchSelectedColumns(new String[] { DBHelper.KEY_NAME });
		SpinnerBindings.bindCursor(this, c, nameSpinner, null,
				DBHelper.KEY_NAME);
	}

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		final Intent intent = getIntent();
		Cursor c = null;
		try {
			mDbHelper = new DBHelper(this);
			mDbHelper.open();
			// is the row a new one or an existing?
			// mRowId = 1L;
			mRowId = icicle != null ? icicle.getLong(DBHelper.KEY_ROWID) : null;
			if (mRowId == null) {
				extras = intent.getExtras();
				mRowId = extras != null ? extras.getLong(DBHelper.KEY_ROWID)
						: null;
			}

			if (extras != null) {
				Latitude_string = intent.getStringExtra(DBHelper.KEY_LATITUDE);
				Longitude_string = intent
						.getStringExtra(DBHelper.KEY_LONGITUDE);
				input = true;
			}
		
		// Latitude_string = "5309691";
		// Longitude_string = "8851933";
		Log.i("Mapper", Latitude_string + " " + Longitude_string);
		/* Do we want a window title? Nope :) */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.newfindmapold);
		mMapView = (MapView) findViewById(R.id.mapper);
		c = mDbHelper.fetchSelectedColumns(new String[] { DBHelper.KEY_ROWID,
				DBHelper.KEY_LATITUDE, DBHelper.KEY_LONGITUDE,
				DBHelper.KEY_NAME, DBHelper.KEY_DESCRIPTION });
		
		if (!DataSet.isMultipleInstances()) {
			((LinearLayout) findViewById(R.id.selectionBar)).removeAllViews();

			Drawable marker = getResources().getDrawable(R.drawable.bubble);
			ArrayList<OverlayItem> mPoints = mapViewItems(c);
			updateView();
			/*mPoints.add(new OverlayItem(new GeoPoint((int)location.getLatitude(),
					(int) location.getLongitude()),"",""));*/
			mMapView.getOverlays().add(new SitesOverlay(marker, mPoints));
			//mMapView.setSatellite(true);
			mapController.zoomToSpan(maxLatitude - minLatitude, maxLongitude
				- minLongitude);
		
			mMapView.displayZoomControls(true);
			/*
			 * mapController.animateTo(new GeoPoint( (maxLatitude +
			 * minLatitude)/2, (maxLongitude + minLongitude)/2 ));
			 */
			Log.i("Mapper", "" + mMapView.getMapCenter().getLatitudeE6() + " "
					+ mMapView.getMapCenter().getLongitudeE6());
		} else {
			nameSpinner = (Spinner) findViewById(R.id.nameSpinner);
			fillNames();
			// nameSpinner.setId(0);
			nameSpinner
					.setOnItemSelectedListener(this.nameSpinnerOnItemSelectedListener);
			c.requery();
			c.moveToFirst();
			/*
			 * try { Cursor c2 = mDbHelper.getInstances(1); mapViewItems(c2); }
			 * catch (Exception e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); }
			 */
			c.close();
			
		}
		} catch (Exception e) {

		}
	}

	/* private HashMap<String,String> args = new HashMap<String,String>(); */

	private ArrayList<OverlayItem> mapViewItems(Cursor c) {
		String name = "", description = "";
		mapController = mMapView.getController();
		if (c.getCount() <= 0)
			return null;
		// this.overlayController = mMapView.createOverlayController();
		/*
		 * Cursor c = mDbHelper.fetchSelectedColumns(new String[]{
		 * DBHelper.KEY_LATITUDE, DBHelper.KEY_LONGITUDE, DBHelper.KEY_NAME,
		 * DBHelper.KEY_DESCRIPTION } );
		 */

		ArrayList<OverlayItem> mPoints = new ArrayList<OverlayItem>();
		// List<DataPoint> mPoints = new ArrayList<DataPoint>();

		while (c.moveToNext()) {
			try {
				latitude = (int) (c.getDouble(c
						.getColumnIndex(DBHelper.KEY_LATITUDE)));
				longitude = (int) (c.getDouble(c
						.getColumnIndex(DBHelper.KEY_LONGITUDE)));
				name = c.getString(c.getColumnIndex(DBHelper.KEY_NAME));
				// name = (!name.equals(null))?name:"null";
				description = c.getString(c
						.getColumnIndex(DBHelper.KEY_DESCRIPTION));
				// description = (!description.equals(null))?description:"null";
				Log.i(APP, latitude + " latitude");
				Log.i(APP, longitude + " longitude");
			} catch (Exception e) {
				e.printStackTrace();
			}
			// Sometimes the longitude or latitude gathering
			// did not work so skipping the point
			// doubt anybody would be at 0 0
			if (latitude != 0 && longitude != 0) {
				HashMap<String, String> args = new HashMap<String, String>();
				// Sets the minimum and maximum latitude so we can span and zoom
				minLatitude = (minLatitude > latitude) ? latitude : minLatitude;
				maxLatitude = (maxLatitude < latitude) ? latitude : maxLatitude;

				// Sets the minimum and maximum latitude so we can span and zoom
				minLongitude = (minLongitude > longitude) ? longitude
						: minLongitude;
				maxLongitude = (maxLongitude < longitude) ? longitude
						: maxLongitude;

				// args.put(DBHelper.KEY_NAME, name);
				// args.put(DBHelper.KEY_DESCRIPTION, description);
				mPoints.add(new OverlayItem(new GeoPoint(latitude, longitude),
						name, description));
				//mMapView.getOverlays().add(new SitesOverlay(new GeoPoint(Integer.parseInt(latitude),Integer.parseInt(longitude)),name,description));
			}

		}
		
		return mPoints;
		/*
		 * Drawable marker = getResources().getDrawable(R.drawable.bubble);
		 * 
		 * mMapView.getOverlays().add(new SitesOverlay(marker,mPoints));
		 * mapController.zoomToSpan(maxLatitude - minLatitude,
		 * maxLongitude-minLongitude); mapController.animateTo(new GeoPoint(
		 * (maxLatitude + minLatitude)/2, (maxLongitude + minLongitude)/2 ));
		 * Log.i("Mapper",
		 * ""+mMapView.getMapCenter().getLatitudeE6()+" "+mMapView
		 * .getMapCenter().getLongitudeE6());
		 */
		// overlayController.add(myMapOverlay, true);
	}

	private OnItemSelectedListener nameSpinnerOnItemSelectedListener = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView parent, View v, int position,
				long id) {

			/*
			 * Log.i(APP, "Spinner id =" + id); Log.i(APP, "Spinner position ="
			 * + id); Log.i(APP, "" +
			 * nameSpinner.getAdapter().getItem(position));
			 */

			Long rowId = mDbHelper.getIdForAdapterPosition(position);
			ArrayList<OverlayItem> points=null;
			Log.i(APP, "Record Id = " + rowId);
			if (rowId != null) {
				updateView();
				Cursor c2 = mDbHelper.getInstances(rowId);
				points =mapViewItems(c2);
			}
			Drawable marker = getResources().getDrawable(R.drawable.flag);
			marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
			mMapView.getOverlays().add(new SitesOverlay(marker,points));
			mMapView.displayZoomControls(true);
			mapController.setCenter(points.get(0).getPoint());
			Log.i(APP,points.size()+"");
			mapController.zoomToSpan(points.get(0).getPoint().getLatitudeE6(), points.get(0).getPoint().getLongitudeE6());
			mapController.setZoom(18);
		}

		public void onNothingSelected(AdapterView arg0) {
			// on nothing selected, do nothing :)
		}

	};

	public void placePoint(String latitude, String longitude, String type) {
		GeoPoint x = new GeoPoint((int) (Double.parseDouble(latitude) * 1E6),
				(int) (Double.parseDouble(longitude) * 1E6));
		mapController.setCenter(x);

	}

	private void updateView() {
		// Refresh our gps-location
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		location = locationManager.getLastKnownLocation("gps");
		/*
		 * Redraws the mapView, which also makes our OverlayController redraw
		 * our Circles and Lines
		 */
		mMapView.invalidate();
	}

	public void onPause(Bundle icicle) {
		/*
		 * We don't want to update the map when we aren't using it do we? Saves
		 * some battery.
		 */
		this.doUpdates = false;
		// this.unregisterReceiver(this.myIntentReceiver);
		super.onPause();
	}

	public void onResume() {
		super.onResume();
		this.doUpdates = true;

		/*
		 * As we only want to react on the LOCATION_CHANGED intents we made the
		 * OS send out, we have to register it along with a filter, that will
		 * only "pass through" on LOCATION_CHANGED-Intents.
		 */
		//this.registerReceiver(this.myIntentReceiver, this.myIntentFilter);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_I) {
			// Zoom In
			int level = mMapView.getZoomLevel();
			mapController.zoomIn();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_O) {
			// Zoom Out
			int level = mMapView.getZoomLevel();
			mapController.zoomOut();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_S) {
			// Switch on the satellite images
			mMapView.setSatellite(true);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_T) {
			// Switch on traffic overlays
			mMapView.setTraffic(true);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			// showDialog();
		}
		return false;
	}

	/**
	 * This tiny IntentReceiver updates our stuff as we receive the intents
	 * (LOCATION_CHANGED_ACTION) we told the myLocationManager to send to us.
	 */
	/*class MyIntentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String data = intent.getAction();
			if (data.equals(MapperNew.MY_LOCATION_CHANGED_ACTION)) {
				if (MapperNew.this.doUpdates) {
					// Will simply update our list, when receiving an intent
					MapperNew.this.updateView();
				}
			}
		}

	}*/

	/*private void setupForGPSAutoRefreshing() {
		// providerList = this.locationManager.getProviders();
		// Log.e(LOG_TAG, providerList.isEmpty()?"empty":"full");
		// get the GPS device
		// locationProvider = providerList.get(0);
		locationProvider = locationManager.getProvider("gps");
		
		 * this.locationManager.requestUpdates(locationProvider,
		 * MINIMUM_TIME_BETWEEN_UPDATE, MINIMUM_DISTANCECHANGE_FOR_UPDATE, new
		 * Intent(MY_LOCATION_CHANGED_ACTION));
		 
		// So, we need to listen for the intent that we asked for our
		// locationManager to
		// send us
		this.myIntentReceiver = new MyIntentReceiver();

	}*/

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	private class SitesOverlay extends ItemizedOverlay<OverlayItem> {
		private ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
		private Drawable marker = null;

		public SitesOverlay(Drawable marker, ArrayList<OverlayItem> _items) {
			super(boundCenterBottom(marker));
			items = _items;
			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return (items.get(i));

		}

		@Override
		protected boolean onTap(int pIndex) {
			// show the description
			Toast.makeText(MapperNew.this, items.get(pIndex).getSnippet(),
					Toast.LENGTH_LONG).show();

			return true;
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return items.size();
		}

	}
}