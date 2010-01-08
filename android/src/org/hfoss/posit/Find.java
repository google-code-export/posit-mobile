/*
 * File: Find.java
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

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.hfoss.posit.provider.MyDBHelper;
import org.hfoss.posit.provider.POSITProvider;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Represents a specific find for a project, with a unique identifier
 * 
 */
public class Find {
	private static final String TAG = "Find";

	private MyDBHelper mDbHelper;  // Handles all the DB actions
	private long mId;  	         // The Find's rowID (should be changed to DB ID)
	private String mGuid;       // BARCODE -- globally unique ID
	public Cursor images = null;
	private Context mContext;

	/**
	 * This constructor is used for a new Find
	 * @param context is the Activity
	 */
	public Find (Context context) {
		mDbHelper = new MyDBHelper(context);
		mContext = context;
	}
	
	/**
	 * This constructor is used for an existing Find.
	 * @param context is the Activity
	 * @param id is the Find's rowID (should be changed to its DB ID)
	 */
	public Find (Context context, long id) {
		this(context);
		mId = id;
		mGuid = "";
		images = getImages();
		mContext = context;
	}
	
	/**
	 * This constructor is used for an existing Find.
	 * @param context is the Activity
	 * @param guid is a globally unique identifier, used by the server
	 *   and other devices
	 */
	public Find (Context context, String guid) {
		this(context);
		mId = -1;
		mGuid = guid;
//		images = getImages();
		mContext = context;
	}

	/**
	 * 	getContent() returns the Find's <attr:value> pairs in a ContentValues array. 
	 *  This method assumes the Find object has been instantiated with a context
	 *  and an id.
	 *  @return A ContentValues with <key, value> pairs
	 */
	public ContentValues getContent() {
		ContentValues values = mDbHelper.fetchFindData(mId);
		getMediaUrisFromDb(values);
		return values;
	}
	
	private void getMediaUrisFromDb(ContentValues values) {
		mDbHelper.getImages(mId, values);
	}
	
	public Uri getImageUriByPosition(long findId, int position) {
		return mDbHelper.getPhotoUriByPosition(findId, position);
	}
	
	
	/**
	 * Deprecated
	 * Returns a hashmap of string key and value
	 * this is used primarily for sending and receiving over Http, which is why it 
	 * somewhat works as everything is sent as string eventually. 
	 * @return
	 */
	public HashMap<String,String> getContentMap(){
		Log.i(TAG, "getContentMap " + mId);
		return mDbHelper.fetchFindMap(mId);
	}
	
	/**
	 * Returns a hashmap of key/value pairs represented as Strings.
	 * This is used primarily for sending and receiving over Http.
	 * @return
	 */
	public HashMap<String,String> getContentMapGuid(){
		Log.i(TAG, "getContentMapGuid " + mGuid);
		return mDbHelper.fetchFindMap(mGuid);
	}
	
	/**
	 * Deprecated
	 * @return
	 */
	public HashMap<String,String> getContentMapSID(){
		Log.i(TAG, "getContentMapSID " + mId);
		return mDbHelper.fetchFindMapSID(mId);
	}

	/** 
	 * Creates an entry for a Find in the DB. 
	 * Assumes that the context has been passed through a constructor.
	 * @param content contains the Find's attributes and values.  
	 * @param images list of contentvalues containing the image references to add
	 * @return whether the DB operation succeeds
	 */
	public boolean insertToDB(ContentValues content, List<ContentValues> images) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		int projId = sp.getInt("PROJECT_ID", 0);
		if (content != null) {
			//		content.put(MyDBHelper.PROJECT_ID, projId);
			content.put(POSITProvider.COLUMN_REVISION, 1);
			mDbHelper.addNewFind(content, null);
		}

		if (images != null && images.size() > 0) {
			ListIterator<ContentValues> it = images.listIterator();
			while (it.hasNext()) {
				ContentValues imageValues = it.next();
				if (!imageValues.containsKey(POSITProvider.COLUMN_FIND_ID))
					imageValues.put(POSITProvider.COLUMN_FIND_ID,mId);
				if (!imageValues.containsKey(POSITProvider.COLUMN_PHOTO_IDENTIFIER))
					imageValues.put(POSITProvider.COLUMN_PHOTO_IDENTIFIER, new Random().nextInt(999999999));
				if (!imageValues.containsKey(POSITProvider.COLUMN_PROJECT_ID))
					imageValues.put(POSITProvider.COLUMN_PROJECT_ID, projId);
				mContext.getContentResolver().insert(POSITProvider.PHOTOS_CONTENT_URI, imageValues);
				//mDbHelper.addNewPhoto(imageValues, mId);
			}
		}
		return mId != -1;
	}

	/** 
	 * updateDB() assumes that the context and rowID has be passed through 
	 * a constructor.
	 * @param content contains the Find's attributes and values.  
	 * @return whether the DB operation succeeds
	 */
	// TODO: Confirm that this works with GUIDs
	public boolean updateToDB(ContentValues content) {//, ContentValues images) {
		if (isSynced()) { //TODO think of a better way to do this
			//set it as unsynced
			content.put(MyDBHelper.COLUMN_SYNCED, false);
			//add revision only once. Don't count revisions for in-phone updates.
			content.put(MyDBHelper.COLUMN_REVISION, getRevision()+1);
		}
		return mDbHelper.updateFind(mId, content);
	}
	
	/**
	 * deletes the Find object form the DB
	 * @return whether the DB operation was successful
	 */
	public boolean delete() {
		Log.i("FIND","deleteing find #"+mId);
		return mDbHelper.deleteFind(mId);
		
		//mContext.getContentResolver().delete(Uri.parse("content://org.hfoss.provider.POSIT/finds_id/"+mId), null, null);
		//return true;
	}

	/**
	 * @return the mId
	 */
	public long getId() {
		return mId;
	}
	
	/**
	 * @return the guId
	 */
	public String getguId() {
		return mGuid;
	}
	
	
	/**
	 * Get all images attached to this find
	 * @return the cursor that points to the images
	 */
	public Cursor getImages() {
		Log.i("","find id = "+mId);
		return 	mContext.getContentResolver().query(Uri.parse("content://org.hfoss.provider.POSIT/photo_findid/"+mId), null, null,null,null);

	}
	
	/**
	 * Delete all images from this find
	 * @return whether or not the images have been deleted
	 */
	private boolean deleteImages(long mId) {
		if ((images=getImages())!=null) {
			return mDbHelper.deleteImages(images,mId);
		}else {
			return true;
		}
	}
	
	/**
	 * @return whether or not there are images attached to this find
	 */
	public boolean hasImages(){
		return getImages().getCount() > 0;
	}
	
	/**
	 * Delete a particular image from this find
	 * @param id the id of the photo to be deleted
	 * @return whether the photo has been successfully deleted or not
	 */
	public boolean deleteImage(long id) {
		return mDbHelper.deletePhoto(id);
	}
	
	public boolean deleteImageByPosition(int position) {
		return mDbHelper.deletePhotoByPosition(mId, position);
	}
	
	
	public void setServerId(int serverId){
		ContentValues content = new ContentValues();
		content.put(MyDBHelper.COLUMN_SID, serverId);
		updateToDB(content);
	}
	
	public void setRevision(int revision){
		ContentValues content = new ContentValues();
		content.put(MyDBHelper.COLUMN_REVISION, revision);
		updateToDB(content);
	}
	
	/**
	 * Directly sets the Find as either Synced or not.
	 * @param status
	 */
	public void setSyncStatus(boolean status){
		ContentValues content = new ContentValues();
		content.put(MyDBHelper.COLUMN_SYNCED, status);
		if (mId != -1)
			mDbHelper.updateFind(mId, content);
		else 
			mDbHelper.updateFind(mGuid, content);
//		updateToDB(content);
	}
	
	// TODO: Test that this method works with GUIDs
	public int getRevision() {
		ContentValues value = mDbHelper.fetchFindColumns(mId, new String[] { MyDBHelper.COLUMN_REVISION});
		return value.getAsInteger(MyDBHelper.COLUMN_REVISION);
	}
	
	/**
	 * Tests whether the Find is synced.  This method should work with
	 * either GUIDs or row iDs. 
	 * @return
	 */
	public boolean isSynced() {
		ContentValues value=null;
		if (mId != -1) {
			value = mDbHelper.fetchFindColumns(mId, new String[] { MyDBHelper.COLUMN_SYNCED});
		} else if (!mGuid.equals("")){
			value = mDbHelper.fetchFindColumnsByGuId(mGuid, new String[] { MyDBHelper.COLUMN_SYNCED});
		}
		return value.getAsInteger(MyDBHelper.COLUMN_SYNCED)==1;
	}	
}