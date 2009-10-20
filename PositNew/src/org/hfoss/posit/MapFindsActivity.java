package org.hfoss.posit;
import java.util.ArrayList;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
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
	private MyDBHelper mDBHelper;
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
		
		mDBHelper = new MyDBHelper(this);
//		mDBHelper.open();
		String[] projection = getResources().getStringArray(R.array.TABLE_FINDS_core_fields);
		Cursor c = mDBHelper.fetchSelectedColumns(projection);
		if (c.getCount()==0) {
			Utils.showToast(this, "No Finds to display");
			finish();
			return;
		}
		setContentView(R.layout.map_finds);
		mMapView = (MapView) findViewById(R.id.mapFinds);
		mMapView.setClickable(true);
		registerForContextMenu(mMapView);
		final GestureDetector gd = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {

			/* (non-Javadoc)
			 * @see android.view.GestureDetector.SimpleOnGestureListener#onLongPress(android.view.MotionEvent)
			 */
			@Override
			public void onLongPress(MotionEvent e) {
				Log.i(TAG, "Long click");
				super.onLongPress(e);
				
			}
			
		});
				
		 LinearLayout zoomLayout = (LinearLayout) findViewById(R.id.map_zoom);
		 zoomLayout.addView(mMapView.getZoomControls(),
		            new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
		                LayoutParams.WRAP_CONTENT));
		 mMapView.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent ev) {
				return gd.onTouchEvent(ev);
			}
			 
			 
		 });
		 // add the zoom buttons + and -
	     
		ArrayList<OverlayItem> mPoints = MapUtils.getScaledOverlayItemsFromCursor(c, mMapView, mapController);
		Drawable marker = getResources().getDrawable(android.R.drawable.btn_star_big_on);
		mMapView.getOverlays().add(new SitesOverlay(marker ,mPoints));
		mDBHelper.close();
		
	}
	
	
	class MapOverlay extends Overlay{
		
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
	private  class SitesOverlay extends ItemizedOverlay<OverlayItem> {
		private static final String TAG = "SitesOverlay";
		private ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
		private Drawable marker = null;
		private int lastIndex=-1;
		private long lastClickTime=-1;
		
		public SitesOverlay(Drawable _marker, ArrayList<OverlayItem> _items) {
			super(boundCenterBottom(_marker));
			items = _items;
			marker = _marker;
			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return (items.get(i));

		}

		@Override
		protected boolean onTap(int pIndex) {
			// show the description
			/*	Toast.makeText(MapFindsActivity.this, items.get(pIndex).getSnippet(),
					Toast.LENGTH_LONG).show();*/
			if (pIndex == lastIndex) {
				Log.i(TAG, "double click");
				
				lastIndex = -1;
			}else {
				lastIndex = pIndex;
			}
			
			Log.i(TAG, "pIndex"+System.currentTimeMillis());
			return true;
		}

		@Override
		public int size() {
			return items.size();
		}
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		return super.onContextItemSelected(item);
		
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onContextMenuClosed(android.view.Menu)
	 */
	@Override
	public void onContextMenuClosed(Menu menu) {
		super.onContextMenuClosed(menu);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_finds_menu, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
		
	}
}
