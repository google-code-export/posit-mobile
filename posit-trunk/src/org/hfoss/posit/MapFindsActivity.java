/*
 * File: MapFindsActivity.java
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

import org.hfoss.posit.provider.PositDbHelper;
import org.hfoss.posit.utilities.MyItemizedOverlay;
import org.hfoss.posit.utilities.Utils;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
	private PositDbHelper mDbHelper;

	/* (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.map_finds);
		linearLayout = (LinearLayout) findViewById(R.id.zoomview);
		mMapView = (MapView) findViewById(R.id.mapFinds);
		mZoom = (ZoomControls) mMapView.getZoomControls();
		linearLayout.addView(mZoom);

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
		mapFinds();
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
		mDbHelper.close(); // NOTE WELL: Can't close while managing cursor
		mCursor.close();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mDbHelper.close();
		mCursor.close();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDbHelper.close();
		mCursor.close();
	}

	private void mapFinds() {
		mDbHelper = new PositDbHelper(this);

		String[] columns = mDbHelper.list_row_data;
		int [] views = mDbHelper.list_row_views;

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		
		mCursor = mDbHelper.fetchFindsByProjectId(sp.getInt("PROJECT_ID", 0));		
		if (mCursor.getCount() == 0) { // No finds
			Utils.showToast(this, "No Finds to display");
			finish();
			return;
		}

		mapOverlays = mMapView.getOverlays();
		mapOverlays.add(mapLayoutItems(mCursor));	
		mapController = mMapView.getController();

		mDbHelper.close();
	}

	private  MyItemizedOverlay mapLayoutItems(Cursor c) {
		int latitude = 0;
		int longitude = 0;
		int itemId = 0;

		drawable = this.getResources().getDrawable(R.drawable.androidmarker);
		MyItemizedOverlay mPoints = new MyItemizedOverlay(drawable, this, true);
		c.moveToFirst();

		do {
			latitude = (int) (c.getDouble(c
					.getColumnIndex(PositDbHelper.FINDS_LATITUDE))*1E6);
			longitude = (int) (c.getDouble(c
					.getColumnIndex(PositDbHelper.FINDS_LONGITUDE))*1E6);

//			String itemIdStr = "" + c.getString(c.getColumnIndex(PositDbHelper.FINDS_GUID));
			String itemIdStr = "" + c.getString(c.getColumnIndex(PositDbHelper.FINDS_ID));
			String description = itemIdStr + "\n" 
			+ c.getString(c.getColumnIndex(PositDbHelper.FINDS_NAME));
			description += "\n" + c.getString(c.getColumnIndex(PositDbHelper.FINDS_DESCRIPTION));

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
// 			showDialog();
		}
		
		return false;
	}
}