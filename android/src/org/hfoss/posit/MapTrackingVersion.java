package org.hfoss.posit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class MapTrackingVersion extends MapActivity{

	private LocationManager lm;
	private LocationListener locationListener;
	private MapView mMapView;
	private MyLocationOverlay myLoc;
	private Integer currentZoomLevel=21;
	private Menu mMenu;
	private static final String APP = "MAPTRACKING";
	private static final int NEW_FIND_DIALOG = 1;
	private static final int MAP_MODES = 2;
	private static final int TAKE_PICTURE =3;
	@Override
	protected boolean isRouteDisplayed() {
		
		return false;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id){
		case NEW_FIND_DIALOG:
        	return new AlertDialog.Builder(MapTrackingVersion.this)
            .setTitle("Type")
            .setItems(R.array.new_find_dialog, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    /* User clicked so do some stuff */

                	switch (which){
                	case 0: /* Find Selected */

                		NewFindDialog nFD = new NewFindDialog(MapTrackingVersion.this,myLoc.getMyLocation());
                		nFD.show();
                		break;
                	case 1: /* Sighting Selected */
                		ListFindsDialog lFD = new ListFindsDialog(MapTrackingVersion.this,myLoc.getMyLocation());
                		lFD.show();
                		break;
                	}
                }
            })
            .create();
		case MAP_MODES:
        	return new AlertDialog.Builder(MapTrackingVersion.this)
            .setTitle("Map Modes")
            .setItems(R.array.map_modes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    /* User clicked so do some stuff */
                	switch (which){
                	case 0: /* Traffic*/
                		mMapView.setTraffic(!mMapView.isTraffic());
                		break;
                	case 1: /* Sighting Selected */
                		mMapView.setSatellite(!mMapView.isSatellite());
                		break;
                	case 2:
                		mMapView.setStreetView(!mMapView.isStreetView());
                		break;
                	}
                }
            })
            .create();
		case TAKE_PICTURE:
			return new ListFindsDialogCamera(MapTrackingVersion.this,myLoc.getMyLocation());
    		

		}
			return null;
	}

	@Override
	protected void onCreate(Bundle icicle) {
		
		super.onCreate(icicle);
		setContentView(R.layout.newfindmap);
		mMapView = (MapView) findViewById(R.id.mapper);
		
		
		myLoc = new MyLocationOverlay(this,mMapView);
		myLoc.enableMyLocation();
		myLoc.enableCompass();
		setupGPSTracking();
		initLocationOverlay();
        // Add zoom controls
        LinearLayout zoomLayout = (LinearLayout) findViewById(R.id.map_zoom);
        zoomLayout.addView(mMapView.getZoomControls(),
            new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
		Spinner nameSpinner = ((Spinner)findViewById(R.id.nameSpinner));
		nameSpinner.setVisibility(View.INVISIBLE);
		
	}

	protected void initLocationOverlay(){
		Location l=null;
		try {
		if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
			l = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			l = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		mMapView.getController().animateTo(new GeoPoint((int)(l.getLatitude()*1E6),(int)(l.getLongitude()*1E6)));
		mMapView.getOverlays().add(myLoc);
		mMapView.getController().setZoom(currentZoomLevel);
		} catch (NullPointerException e){
			Log.e(APP,e.getMessage());
		}
	}
	
	protected void setupGPSTracking(){
		lm = (LocationManager)
			getSystemService(Context.LOCATION_SERVICE);
		locationListener = new MyLocationListener();
		if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
			lm.requestLocationUpdates(
				LocationManager.GPS_PROVIDER, 100, 100, locationListener);
		/*else if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);*/
	}




	@Override
	protected void onStop() {

		myLoc.disableMyLocation();
		super.onStop();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyDown(keyCode, event);
	}
	
	 private class MyLocationListener implements LocationListener 
	    {

		public void onLocationChanged(Location loc) {
			if (loc != null) {
				//Overlay
				//mMapView.getOverlays().add(object);
				mMapView.getOverlays().add(myLoc);
				mMapView.getController().animateTo(myLoc.getMyLocation());
				mMapView.getController().setZoom(currentZoomLevel);
                Toast.makeText(getBaseContext(), 
                    "Location changed : Lat: " + loc.getLatitude() + 
                    " Lng: " + loc.getLongitude(), 
                    Toast.LENGTH_SHORT).show();
            }

			
		}

		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
			
		}

		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub
			
		}

		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub
			
		}
	        
	    }

	@Override
	protected void onResume() {

		myLoc.enableMyLocation();
		super.onResume();
	}        
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Hold on to this
        mMenu = menu;
        
        // Inflate the currently selected menu XML resource.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mapnewmenu, menu);
        return true;
    }

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.newFind:
        	//Toast.makeText(this, "NewFind Selected", Toast.LENGTH_SHORT).show();
        	showDialog(NEW_FIND_DIALOG);
        	//Log.i(APP,"Here");
        	return true;
        case R.id.mapMode:
        	showDialog(MAP_MODES);
        	return true;
        case R.id.takePicture:
        	showDialog(TAKE_PICTURE);
        default:
                Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            
        }
        

    }

}
