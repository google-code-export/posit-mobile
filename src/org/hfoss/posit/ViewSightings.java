package org.hfoss.posit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hfoss.posit.util.DataPoint;
import org.hfoss.posit.util.SpinnerBindings;
import org.hfoss.posit.util.StaticOverlay;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class ViewSightings extends MapActivity {
	int latitude, longitude;
	long	recordId; /*recordId for the finds for which we are seeking sightings*/
	private Spinner nameSpinner;
	private MapView mMapView;
	private HashMap<String,String> args= new HashMap<String,String>();
	protected MapController mapController;
	//protected OverlayController overlayController;
	private DBHelper dbHelper;
	// Minimum & maximum latitude so we can span it
	// The latitude is clamped between -80 degrees and +80 degrees inclusive
	// thus we ensure that we go beyond that number
	private int minLatitude = (int) (+81 * 1E6);

	private int maxLatitude = (int) (-81 * 1E6);

	// Minimum & maximum longitude so we can span it
	// The longitude is clamped between -180 degrees and +180 degrees inclusive
	// thus we ensure that we go beyond that number
	private int minLongitude = (int) (+181 * 1E6);

	private int maxLongitude = (int) (-181 * 1E6);

	private final String APP = "ViewSightings";

	public ViewSightings() {

	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		dbHelper = new DBHelper(this);
		dbHelper.open();
		setContentView(R.layout.list_sightings);
		mapViewItems();
		fillNames();
		setListeners();
		addMarkers();
		
	}

	private void setListeners() {
		nameSpinner.setOnItemSelectedListener(nameSpinnerOnItemSelectedListener);
	}
	
	private void fillNames() {
		Cursor c = dbHelper.allNames();
		SpinnerBindings.bindCursor(this, c, nameSpinner, null);
	}
	/**
	 * As usual, maps the different View Items in the Activity
	 */
	private void mapViewItems() {
		
		mMapView = (MapView)findViewById(R.id.sightingsMapper);
		mapController = mMapView.getController();
		this.overlayController = mMapView.createOverlayController();
		nameSpinner = (Spinner)findViewById(R.id.nameSpinner);
		
	}
	/**
	 * Adds the required markers for the mapView 
	 */
	private void addMarkers() {
		overlayController.clear();
		
		String name, description,time, observed,weather;
		Cursor c = dbHelper.getSightings(recordId,new String[]{
				DBHelper.KEY_LATITUDE, DBHelper.KEY_LONGITUDE, DBHelper.KEY_OBSERVED,
				DBHelper.KEY_TIME, DBHelper.KEY_WEATHER});
		

		List<DataPoint> mPoints = new ArrayList<DataPoint>();
		while (c.moveToNext()) {
			latitude = (int) (c.getDouble(c
					.getColumnIndex(DBHelper.KEY_LATITUDE)) * 1E6);
			longitude = (int) (c.getDouble(c
					.getColumnIndex(DBHelper.KEY_LONGITUDE)) * 1E6);
			//name = c.getString(c.getColumnIndex(DBHelper.KEY_NAME));
			time = c.getString(c
					.getColumnIndex(DBHelper.KEY_TIME));
			observed = c.getString(c.getColumnIndex(DBHelper.KEY_OBSERVED));
			weather = c.getString(c.getColumnIndex(DBHelper.KEY_WEATHER));
			Log.i(APP, latitude + " latitude");
			Log.i(APP, longitude + " longitude");
			args.put(DBHelper.KEY_TIME, time);
			args.put(DBHelper.KEY_OBSERVED, observed);
			args.put(DBHelper.KEY_WEATHER, weather);
			
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
				
				mPoints.add(new DataPoint(new GeoPoint(latitude, longitude),args));
				
			}
			mapController.zoomToSpan(maxLatitude - minLatitude, maxLongitude
					- minLongitude);
			mapController.animateTo(new GeoPoint((maxLatitude + minLatitude) / 2,
				 (maxLongitude + minLongitude) / 2));
			StaticOverlay myMapOverlay = new StaticOverlay(mPoints, mMapView,
					this);

			overlayController.add(myMapOverlay, true);

		}

	}
	
	private OnItemSelectedListener nameSpinnerOnItemSelectedListener = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView parent, View v, int position,
				long id) {
			/*Gets the associated Row Id for the current item's position*/ 
			recordId = dbHelper.getIdForAdapterPosition(position);
			Log.i(APP, "Record Id = "+recordId);
			ViewSightings.this.mMapView.invalidate();
			ViewSightings.this.addMarkers();
		}

		public void onNothingSelected(AdapterView arg0) {
			//on nothing selected, do nothing :)
		}

	};

	@Override
	protected boolean isLocationDisplayed() {
		// TODO Auto-generated method stub
		return super.isLocationDisplayed();
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}
