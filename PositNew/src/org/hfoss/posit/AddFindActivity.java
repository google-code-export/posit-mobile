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
package org.hfoss.posit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Config;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class AddFindActivity extends Activity implements OnClickListener, OnItemClickListener {

	private DBHelper mDbHelper;
	LocationListener[] mLocationListeners = new LocationListener[] {
			new LocationListener(LocationManager.GPS_PROVIDER),
			new LocationListener(LocationManager.NETWORK_PROVIDER) };
	private LocationManager mLocationManager;
	private Location location;
	private Find find; // the find object
	private static final String TAG = "AddFindActivity";
	private HashMap<String, View> coreViewObjects;
	private ArrayList<String> coreViewItems = new ArrayList<String>();

	private static final int STATE_INSERT = 0;
	private static final int STATE_EDIT = 1;
	private static final int CAMERA_ACTIVITY = 12;
    private int mState;
	private long mRowId=0;
	private ArrayList<ViewObject> extraViewObjects;
	private HashMap<Integer,OnClickListener> extraViewListeners = new HashMap<Integer,OnClickListener>();
	private Gallery picturesTaken;
	private Cursor imagesQuery=null;
	private class LocationListener implements android.location.LocationListener {
		Location mLastLocation;
		boolean mValid = false;
		String mProvider;

		public LocationListener(String provider) {
			mProvider = provider;
			mLastLocation = new Location(mProvider);
		}

		public void onLocationChanged(Location newLocation) {
			if (newLocation.getLatitude() == 0.0
					&& newLocation.getLongitude() == 0.0) {
				// filter out 0.0,0.0 locations
				return;
			}
			mLastLocation.set(newLocation);
			((TextView) coreViewObjects.get("latitude")).setText(mLastLocation
					.getLatitude()
					+ "");
			((TextView) coreViewObjects.get("longitude")).setText(mLastLocation
					.getLongitude()
					+ "");
			mValid = true;
		}

		public void onProviderEnabled(String provider) {
		}

		public void onProviderDisabled(String provider) {
			mValid = false;
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (status == LocationProvider.OUT_OF_SERVICE) {
				mValid = false;
			}
		}

		public Location current() {
			return mValid ? mLastLocation : null;
		}
	}

	private void startReceivingLocationUpdates() {
		if (mLocationManager != null) {
			try {
				mLocationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 1000, 0F,
						mLocationListeners[1]);
			} catch (java.lang.SecurityException ex) {
				// ok
			} catch (IllegalArgumentException ex) {
				if (Config.LOGD) {
					Log.d(TAG, "provider does not exist " + ex.getMessage());
				}
			}
			try {
				mLocationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 1000, 0F,
						mLocationListeners[0]);
			} catch (java.lang.SecurityException ex) {
				// ok
			} catch (IllegalArgumentException ex) {
				if (Config.LOGD) {
					Log.d(TAG, "provider does not exist " + ex.getMessage());
				}
			}
		}
	}

	private void stopReceivingLocationUpdates() {
		if (mLocationManager != null) {
			for (int i = 0; i < mLocationListeners.length; i++) {
				try {
					mLocationManager.removeUpdates(mLocationListeners[i]);
				} catch (Exception ex) {
					// ok
				}
			}
		}
	}

	public AddFindActivity() {
		coreViewObjects = new HashMap<String, View>();
		extraViewObjects = new ArrayList<ViewObject>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		String action = intent.getAction();

		if (Intent.ACTION_EDIT.equals(action)) {
			mState = STATE_EDIT;
			mRowId = getIntent().getExtras().getLong(DBHelper.KEY_ID);
			// if the user wants to edit, set the state as in editing
			// get the data and display
		} else if (Intent.ACTION_INSERT.equals(action)) {
			mState = STATE_INSERT;
		} else {

		}
		initialize();
		
		
		setContentView(R.layout.add_find);
		LinearLayout l = (LinearLayout) findViewById(R.id.extra_items_layout);
		int count = 0;
		generateExtraViewItems(l, count);
		mapLayoutItems();
		populateFields();
		location = getCurrentLocation();
		startReceivingLocationUpdates();
	}

	/**
	 * 
	 */
	private void initialize() {
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		coreViewItems = Utils.getFieldsFromResource(this,
				R.array.TABLE_FINDS_core_fields);
		extraViewObjects = Utils.getViewsFromResource(this,
				R.array.TABLE_FINDS_extra_fields);
	}

	/**
	 * @param l
	 * @param count
	 */
	private void generateExtraViewItems(LinearLayout l, int count) {
		for (ViewObject item : extraViewObjects) {
			
			l.addView(item.generateEditableLayout(this));
			if (item.hasExtras()) {
				addOnClickListener(count);
				item.setOnButtonClickListener(extraViewListeners.get(count));
			}
			count++;
		}
	}
	 public void addOnClickListener (final int count) {
	    	OnClickListener l = new OnClickListener() {
	    		public void onClick(View v) {
	    			try {
	    				startActivityForResult(extraViewObjects.get(count).getIntent(), count);
	    			} catch (ActivityNotFoundException e) {
	    				new AlertDialog.Builder(AddFindActivity.this)
	    				.setTitle("No Activity Found")
	    				.setMessage("The activity couldn't be found. The application might not be installed on the phone")
	    				.setPositiveButton("OK", null)
	    				.show();
	    			}
	    		}
	    	};
	    	extraViewListeners.put(count, l);
	    }
	// Needs to be updated for other fields besides texts
	private void populateFields() {
		if (mState == STATE_INSERT) {
			setDateAndTime();
		} else if (mState == STATE_EDIT) {
			find = new Find(this, mRowId);
			ContentValues args = find.getContent();
			for (String item:coreViewItems) { //this makes a gross assumption that everything is text
				View v = coreViewObjects.get(item);
				if (v instanceof TextView)
					((TextView) v).setText(args.getAsString(item));
				else if (v instanceof EditText)
					((EditText) v).setText(args.getAsString(item));
			}
			for (ViewObject viewObject: extraViewObjects) { //this assumes that ViewObject is going to handle the values based on the types
				viewObject.setValue(args.getAsString(viewObject.getName()));
			}
			//if (find.hasImages())
			//showImages();
		}
	}

	private void setDateAndTime() {
		((TextView) coreViewObjects.get(DBHelper.KEY_TIME)).setText(getDateText());
	}

	// Needs to be updated for other fields besides texts
	private void clearFields() {
		for (String item : coreViewItems) {
			View v = coreViewObjects.get(item);
			if (v instanceof TextView)
				((TextView) v).setText("");
			else if (v instanceof EditText)
				((EditText) v).setText("");
		}
		for (ViewObject viewObject : extraViewObjects) {
			viewObject.reset();
		}
		setDateAndTime(); //reset the date to a new one
	}

	/**
	 * Adds the layout items to the addFindViews HashMap so that they're
	 * accessible the idea here is to use the same field names as the table to
	 * save and retrieve items. This assumes there are some naming conventions
	 * followed while naming the text fields the names of the fields are assumed
	 * to be <fieldName>Text. Have to add some conditions for checkboxes and
	 * stuff
	 */
	private void mapLayoutItems() {
		picturesTaken = (Gallery)findViewById(R.id.picturesTaken);
		for (String item : coreViewItems) {
			Integer resId = getResources().getIdentifier(
					"org.hfoss.posit:id/" + item + "Text", null, null);
			Log.w(TAG, "Id for org.hfoss.posit:id/" + item + "Text " + resId);
			if (resId != 0) {
				coreViewObjects.put(item, findViewById(resId));
			}
		}
	}

	/**
	 * saving to the database/table
	 */
	private void saveToDatabase() {
		ContentValues args = getContentFromView();
	
		if (mState == STATE_INSERT) {
			//mRowId = mDbHelper.addNewFind(R.array.TABLE_FINDS, args);
			find = new Find(this);
			
			
			if (find.insertToDB(args)) {
				Utils.showToast(this, R.string.saved_to_database);
			}
			mRowId = find.getId();
		} else if (mState == STATE_EDIT) {
			if (find.updateToDB(args)) {
				Utils.showToast(this, R.string.saved_to_database);
			}else {
				Utils.showToast(this, R.string.save_failed);
			}
		}
	}

	/**
	 * @return
	 */
	private ContentValues getContentFromView() {
		ContentValues args = new ContentValues();
		Location loc = getCurrentLocation();
		for (String item : coreViewItems) {
			View v = coreViewObjects.get(item);
			String text = null;
			if (v instanceof TextView)
				text = ((TextView) v).getText().toString();
			else if (v instanceof EditText)
				text = ((EditText) v).getText().toString();
			args.put(item, text);
		}
		args.putAll(ViewObject.getContentValuesFromViewObjects(extraViewObjects));
		return args;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_finds_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	/**
	 * Gets the current date and time from the Calendar Instance
	 */
	private String getDateText() {
		Calendar c = Calendar.getInstance();
		String dateString = c.get(Calendar.YEAR) + "/" + c.get(Calendar.MONTH)
				+ "/" + c.get(Calendar.DAY_OF_MONTH) + " "
				+ c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);
		return dateString;
	}

	private Location getCurrentLocation() {
		Location l = null;

		// go in best to worst order
		for (int i = 0; i < mLocationListeners.length; i++) {
			l = mLocationListeners[i].current();
			if (l != null)
				break;
		}

		return l;
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.save_find_menu_item:
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.save_find_menu_item:
			saveToDatabase();
			finish();
			break;
		case R.id.discard_changes_menu_item:
			if (mState == STATE_EDIT) {
				clearFields();
				populateFields();
			} else {
				clearFields();
			}
			break;
		case R.id.camera_menu_item:
			Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
			startActivityForResult(intent, CAMERA_ACTIVITY);
			break;
		case R.id.delete_find_menu_item:
			if (mState == STATE_EDIT) {

				if (find.delete()) {
					
					Utils.showToast(this, R.string.deleted_from_database);
				} else {
					Utils.showToast(this, R.string.delete_failed);
				}
			}
			finish();
			break;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopReceivingLocationUpdates();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode== RESULT_OK) {
			if (requestCode==CAMERA_ACTIVITY) {
				if (mState == STATE_INSERT) { // this saves to the database so that we have the row Id
					saveToDatabase();
					mState = STATE_EDIT;
				}
				ImageUtils.saveImageFromCamera(this,find.getId(),data); //save Image to MediaStore
				showImages();
			}
			else if (extraViewListeners.containsKey(requestCode)) {
				extraViewObjects.get(requestCode).setValue(this, data);
			}
		}
	}
	
	
	/**
	 * Queries for images for this Find and shows at the bottom
	 */
	private void showImages() {
		/* This is not exactly correct way to do it (since we are using find object)
		 * Something like getImages() method in find should return a cursor that we can bind to.
		 */
	 if (imagesQuery==null) {
		 imagesQuery = managedQuery (MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        		new String[]{ BaseColumns._ID, ImageColumns.BUCKET_ID, ImageColumns.MINI_THUMB_MAGIC},
        		ImageColumns.BUCKET_ID+"=\"posit\" AND "
        		+ImageColumns.BUCKET_DISPLAY_NAME+"=\"posit|"+mRowId+"\"", null,null);
		 }else {
			 imagesQuery.requery();
		 }
	   picturesTaken.setAdapter(new ImageAdapter(imagesQuery,this));
       picturesTaken.setOnItemClickListener(this);
		
		
	}

	/**
	 * To detect the user clicking on the displayed images
	 */
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		try {
			//
			imagesQuery.move(position);
			long id = imagesQuery.getLong(imagesQuery.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID));
			//create the Uri for the Image 
			Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id+"");
			uri.buildUpon().appendQueryParameter("bucketId", "posit");
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(uri);
			startActivity(intent);
		} catch (CursorIndexOutOfBoundsException e) {
			//do nothing, 
			// hack to work around this exception
		}
	}

	
}
