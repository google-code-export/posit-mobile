/**
 * Find
 * @author Phil Fritzsche
 * 
 * This is a node-like structure, designed to keep track of individual
 * submissions and their related information.
 */

package com.hfoss.summer09.cc;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class Find {
	public static final String TAG = "Find";

	//db variables
	private MyDBHelper mDbHelper;
	private long id;

	//gps variables
	private GeoPoint geoTag;
	private GpsUtils gpsUtils;

	//useful variables
	private double creationTime;

	/**
	 * Public constructor; creates a Find object.
	 */
	public Find(Context context) {
		Log.i(TAG, "Constructor");
		gpsUtils = new GpsUtils();
		gpsUtils.initGps(context, GpsUtils.SLOW_UPDATE);
		geoTag = gpsUtils.getCurrentGp();
		creationTime = System.currentTimeMillis();
		mDbHelper = new MyDBHelper(context);
	}

	/**
	 * Inserts the find to the database.
	 * @param values
	 * @return row id of the find in the database
	 */
	public long addToDb(ContentValues values) {
		values.put(MyDBHelper.KEY_CREATE_TIME, creationTime);
		values.put(MyDBHelper.KEY_MOD_TIME, System.currentTimeMillis());
		if (geoTag != null)
		{
			values.put(MyDBHelper.KEY_LATITUDE, geoTag.getLatitudeE6() / 1E6);
			values.put(MyDBHelper.KEY_LONGITUDE, geoTag.getLongitudeE6() / 1E6);

		}
		Log.e(TAG,"Values Added");
		id = mDbHelper.addNewFind(values);
		Log.e(TAG,">>Added");
		return id;
	}

	/** 
	 * Updates a find's entries in the database.
	 * @param content contains the Find's attributes and values.  
	 * @return true if updated, false if not
	 */
	public boolean updateToDB(ContentValues content)
	{
		boolean result = mDbHelper.updateFind(id, content);
		return result;
	}	

	/**
	 * Deletes the find from the database.
	 * @return true if deleted, false if not
	 */
	public boolean delete()
	{
		return mDbHelper.deleteFind(id);
	}

	/**
	 * Queries the database with the row id of the find, creating a ContentValues object
	 * of the find's information.
	 * @return ContentValues object containing the database information on the find
	 */
	public ContentValues getFindData() {
		ContentValues values = mDbHelper.fetchFindData(id);
		return values;
	}

	/**
	 * Queries the database for a ContentValues hash table and returns it. Useful for
	 * sending information via HTTP.
	 * @return hashmap containing string-type values of the find's information
	 */
	public HashMap<String,String> getContentMap()
	{
		return mDbHelper.fetchFindMap(id);
	}

	/**
	 * @return the id of the Find
	 */
	public long getId() {
		return id;
	}
}
