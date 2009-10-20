/*******************************************************************************
 * Copyright (c) 2009 .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Prasanna Gautam - initial API and implementation
 *     Ralph Morelli - Supervisor
 *     Trishan deLanerolle - Director
 *     Antonio Alcorn - Summer 2009 Intern
 *     Gong Chen - Summer 2009 Intern
 *     Chris Fei - Summer 2009 Intern
 *     Qianqian Lin - Summer 2009 Intern 
 ******************************************************************************/

package org.hfoss.posit;

import java.util.List;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.ZoomControls;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 *  This class retrieves Finds from the POSIT DB and
 *  displays them as an overlay on a Google map. When clicked, 
 *  the finds start a FindActivity. Allowing them to be edited.
 *
 * @author Prasanna Gautam
 * @author Ralph Morelli
 */
public class MapFindsActivity extends MapActivity {
	
	private static final String TAG = "MapFindsActivity";
	private MapView mMapView;
	private MapController mapController;
	private ZoomControls mZoom;
	private LinearLayout linearLayout;
	private List<Overlay> mapOverlays;
	private Drawable drawable;
	
	private Cursor mCursor;  // Used for DB accesses
	private MyDBHelper mDbHelper;


	/* (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		mDbHelper = new MyDBHelper(this);
		mapFinds();
		mDbHelper.close();
	}
	
	/** 
	 * This method is called when the activity is ready to start 
	 *  interacting with the user. It is at the top of the Activity
	 *  stack.
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
//		mapFinds();
	}

	/**
	 * Called when the system is about to resume some other activity.
	 *  It can be used to save state, if necessary.  In this case
	 *  we close the cursor to the DB to prevent memory leaks.
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onPause(){
		super.onPause();
        stopManagingCursor(mCursor);
        mDbHelper.close();
        mCursor.close();
//		mDbHelper.close();  // NOTE WELL: Can't close while managing cursor
	}

	
	@Override
	protected void onStop() {
		super.onStop();
		mDbHelper.close();
		mCursor.close();

	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mDbHelper.close();
		mCursor.close();
	}
	
	private void mapFinds() {
		String[] columns = mDbHelper.list_row_data;
		int [] views = mDbHelper.list_row_views;
		
		mCursor = mDbHelper.fetchAllFinds();		
    	if (mCursor.getCount() == 0) { // No finds
			Utils.showToast(this, "No Finds to display");
			finish();
    		return;
    	}
    	
		setContentView(R.layout.map_finds);
        linearLayout = (LinearLayout) findViewById(R.id.zoomview);
		mMapView = (MapView) findViewById(R.id.mapFinds);
        mZoom = (ZoomControls) mMapView.getZoomControls();
        linearLayout.addView(mZoom);
        mapOverlays = mMapView.getOverlays();
        mapOverlays.add(mapLayoutItems(mCursor));	
		mapController = mMapView.getController();
	}
	
	private  MyItemizedOverlay mapLayoutItems(Cursor c) {
		int latitude = 0;
		int longitude = 0;
		int itemId = 0;
        drawable = this.getResources().getDrawable(R.drawable.androidmarker);
    	MyItemizedOverlay mPoints = new MyItemizedOverlay(drawable, this);
		c.moveToFirst();

		do {
			latitude = (int) (c.getDouble(c
				.getColumnIndex(MyDBHelper.KEY_LATITUDE))*1E6);
			longitude = (int) (c.getDouble(c
				.getColumnIndex(MyDBHelper.KEY_LONGITUDE))*1E6);

			String itemIdStr = "" + c.getString(c.getColumnIndex(MyDBHelper.KEY_ID));
			String description = itemIdStr + "\n" 
				+ c.getString(c.getColumnIndex(MyDBHelper.KEY_NAME));
			description += "\n" + c.getString(c.getColumnIndex(MyDBHelper.KEY_DESCRIPTION));

			Log.i(TAG, latitude+" "+longitude+" "+description);
			mPoints.addOverlay(new OverlayItem(new GeoPoint(latitude,longitude),itemIdStr,description));
		} while (c.moveToNext());
		return mPoints;
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}


	@Override
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
				mMapView.setSatellite(!mMapView.isSatellite());		
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_T) {
			// Switch on traffic overlays
			mMapView.setTraffic(!mMapView.isTraffic());
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			// showDialog();
		}
		return false;
	}
}
