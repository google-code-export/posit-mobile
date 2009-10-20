package org.hfoss.posit;
import java.util.ArrayList;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/*******************************************************************************
 * Copyright (c) 2009 .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     pgautam - initial API and implementation
 ******************************************************************************/
/**
 * @author pgautam
 *
 */
public class MapFindsActivity extends MapActivity {
	
	private static final String TAG = "MapFindsActivity";
	private DBHelper mDBHelper;
	private MapView mMapView;
	private MapController mapController;

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		mDBHelper = new DBHelper(this);
		mDBHelper.open();
		String[] projection = getResources().getStringArray(R.array.TABLE_FINDS_core_fields);
		Cursor c = mDBHelper.fetchSelectedColumns(R.array.TABLE_FINDS, projection);
		if (c.getCount()==0) {
			Utils.showToast(this, "No Finds to display");
			finish();
			return;
		}
		setContentView(R.layout.map_finds);
		mMapView = (MapView) findViewById(R.id.mapFinds);
		ArrayList<OverlayItem> mPoints = mapLayoutItems(c);
/*		ArrayList<OverlayItem> mPoints = new ArrayList<OverlayItem>();
		mPoints.add(new OverlayItem(new GeoPoint(6700000,-4400000),"x","X"));*/
		Drawable marker = getResources().getDrawable(android.R.drawable.btn_star_big_on);
		//updateView();
		mMapView.displayZoomControls(true);

		mMapView.getOverlays().add(new SitesOverlay(marker ,mPoints));
		mDBHelper.close();
	}
	
	private ArrayList<OverlayItem> mapLayoutItems(Cursor c) {
		c.moveToFirst();
		if (c.getCount() <= 0)
			return null;
		mapController = mMapView.getController();
		ArrayList<OverlayItem> mPoints = new ArrayList<OverlayItem>();
		int maxLong = Integer.MIN_VALUE;
		int maxLat = Integer.MIN_VALUE;
		int minLat = Integer.MAX_VALUE;
		int minLong = Integer.MAX_VALUE;
		while (c.moveToNext()) {
			int latitude = (int) (c.getDouble(c
				.getColumnIndex(DBHelper.KEY_LATITUDE))*1E6);
			int longitude = (int) (c.getDouble(c
				.getColumnIndex(DBHelper.KEY_LONGITUDE))*1E6);
			if (longitude>maxLong) maxLong = longitude;
			if (latitude>maxLat ) maxLat = latitude;
			if (latitude<minLat) minLat = latitude;
			if (longitude<minLong) minLong = longitude;
			String description = c.getString(c
				.getColumnIndex(DBHelper.KEY_DESCRIPTION));
			Log.i(TAG, latitude+" "+longitude+" "+description);
			mPoints.add(new OverlayItem(new GeoPoint(latitude,longitude),"",description));
		}
		Log.i(TAG, maxLat+" "+maxLong);
		mapController.setZoom(10);

		mapController.zoomToSpan(maxLat	, maxLong);
		mapController.animateTo(new GeoPoint ( maxLat , maxLong ));
		return mPoints;
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
			Toast.makeText(MapFindsActivity.this, items.get(pIndex).getSnippet(),
					Toast.LENGTH_LONG).show();

			return true;
		}

		@Override
		public int size() {
			return items.size();
		}

	}
	
}
