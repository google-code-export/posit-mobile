/*
 * File: PositProvider.java
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

package org.hfoss.posit.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class POSITProvider extends ContentProvider{

	public static final String PROVIDER_NAME = "org.hfoss.provider.POSIT";
	public static final Uri FINDS_CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME + "/finds");
	public static final Uri PHOTOS_CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME + "/photos");
	
	private static final int FINDS = 1;
    private static final int FIND_ID = 2; 
    private static final int FINDS_BY_PROJECT = 3;
    private static final int PHOTO_FINDID = 4;
    private static final int PHOTOS_BY_PROJECT = 5;
    
	private static final UriMatcher uriMatcher;
    static{
       uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
       uriMatcher.addURI(PROVIDER_NAME, "finds", FINDS);
       uriMatcher.addURI(PROVIDER_NAME, "finds_id/#", FIND_ID);
       uriMatcher.addURI(PROVIDER_NAME, "finds_project/#", FINDS_BY_PROJECT);
       uriMatcher.addURI(PROVIDER_NAME, "photo_findid/#", PHOTO_FINDID);
       uriMatcher.addURI(PROVIDER_NAME, "photos_project/#", PHOTOS_BY_PROJECT);
    }
    
 
    private SQLiteDatabase db;
    private static final String DATABASE_NAME ="posit";
	public static final int DATABASE_VERSION = 2;
	private static final String TAG = "MyDBHelper";
	
	/**
	 * Finds table and field
	 */
    public static final String FIND_TABLE_NAME = "finds";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_PROJECT_ID = "projectId";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_IDENTIFIER = "identifier";
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_LATITUDE = "latitude";
	public static final String COLUMN_LONGITUDE = "longitude";
	public static final String COLUMN_TIME = "time";
	public static final String COLUMN_SYNCED = "synced";
	public static final String COLUMN_SID = "sid";
	public static final String COLUMN_REVISION = "revision";
	public static final String MODIFY_TIME = "modify_time";
	public static final String IS_AD_HOC = "is_adhoc";
	
	/**
	 * Table and Fields for the photos table
	 */
	public static final String PHOTO_TABLE_NAME = "photos";
	public static final String COLUMN_PHOTO_ID = "_id";
	public static final String COLUMN_IMAGE_URI = "imageUri";
	public static final String COLUMN_PHOTO_THUMBNAIL_URI = "imageThumbnailUri";
	public static final String COLUMN_PHOTO_IDENTIFIER = "photo_identifier";
	public static final String COLUMN_FIND_ID = "findId";

	
	private static final String CREATE_FINDS_TABLE = "CREATE TABLE "
		+ FIND_TABLE_NAME  
		+ " (" + COLUMN_ID + " integer primary key autoincrement, "
		+ COLUMN_NAME + " text, "
		+ COLUMN_DESCRIPTION + " text, "
		+ COLUMN_LATITUDE + " double, "
		+ COLUMN_LONGITUDE + " double, "
		+ COLUMN_IDENTIFIER + " text, " /* for barcodes*/
		+ COLUMN_TIME + " text, "
		+ COLUMN_SID + " text, "
		+ COLUMN_SYNCED + " integer default 0, "
		+ COLUMN_REVISION + " integer default 1, "
		+ "audioClips integer, "	// needed for compatibility
		+ "videos integer, "		// needed for compatibility
		+ MODIFY_TIME + " text, "
		+ COLUMN_PROJECT_ID + " integer, "
		+ IS_AD_HOC + " integer default 0"
		+ ");";

	private static final String CREATE_PHOTOS_TABLE = "CREATE TABLE "
		+ PHOTO_TABLE_NAME  
		+ " (" + COLUMN_ID + " integer primary key autoincrement, "  // User Key
		+ COLUMN_PHOTO_IDENTIFIER + " integer, "
		+ COLUMN_FIND_ID + " integer, "      // User Key
		+ COLUMN_IMAGE_URI + " text, "      // The image's URI
		+ COLUMN_PHOTO_THUMBNAIL_URI + " text, "      // The thumbnail's URI
		+ COLUMN_PROJECT_ID + " integer "
		+ ");";
	
	private static class DatabaseHelper extends SQLiteOpenHelper 
    {
       DatabaseHelper(Context context) {
          super(context, DATABASE_NAME, null, DATABASE_VERSION);
       }

       @Override
       public void onCreate(SQLiteDatabase db) {
          db.execSQL(CREATE_FINDS_TABLE);
          db.execSQL(CREATE_PHOTOS_TABLE);
       }

       @Override
       public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	   Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
   				+ newVersion + ", which will destroy all old data");
   			db.execSQL("DROP TABLE IF EXISTS " + FIND_TABLE_NAME);
   			db.execSQL("DROP TABLE IF EXISTS " + PHOTO_TABLE_NAME);
   			onCreate(db);
       }
    }

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		String findId = "";
		String projId = "";
		Cursor c = null;
		
		switch(uriMatcher.match(uri)) {
		case FINDS_BY_PROJECT:
			projId = uri.getPathSegments().get(1);
			Log.i("PROVIDER", "delete finds from project #"+projId);
			count = db.delete(FIND_TABLE_NAME, COLUMN_PROJECT_ID + " = " + projId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
			delete(Uri.parse("content://org.hfoss.provider.POSIT/photos_project/"+projId),null,null);
			break;
		case PHOTOS_BY_PROJECT:
			projId = uri.getPathSegments().get(1);
			c = query(Uri.parse("content://org.hfoss.provider.POSIT/photos_project/"+projId),null,null,null,null);
			//Delete photos from gallery
			while (c.moveToNext()) {
				String uriString = c.getString(c.getColumnIndexOrThrow(COLUMN_IMAGE_URI));
				Uri _uri = Uri.parse(uriString);
				getContext().getContentResolver().delete(_uri, null, null);
			}
			count = db.delete(PHOTO_TABLE_NAME, COLUMN_PROJECT_ID + " = " + projId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
			break;
		case FIND_ID:
			findId = uri.getPathSegments().get(1);
			Log.i("PROVIDER", "delete find #"+findId);
			count = db.delete(FIND_TABLE_NAME, COLUMN_IDENTIFIER + " = " + findId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
			delete(Uri.parse("content://org.hfoss.provider.POSIT/photo_findid/"+findId),null,null);
			break;
		case PHOTO_FINDID:
			findId = uri.getPathSegments().get(1);
			Log.i("PROVIDER", "delete photos with find #"+findId);
			c = query(Uri.parse("content://org.hfoss.provider.POSIT/photo_findid/"+findId),null,null,null,null);
			//Delete photos from gallery
			while (c.moveToNext()) {
				String uriString = c.getString(c.getColumnIndexOrThrow(COLUMN_IMAGE_URI));
				Uri _uri = Uri.parse(uriString);
				getContext().getContentResolver().delete(_uri, null, null);
			}
			count = db.delete(PHOTO_TABLE_NAME, COLUMN_FIND_ID + " = " + findId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Wrong arg for provider!");
		}
			
			
		getContext().getContentResolver().notifyChange(uri, null);
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		String table = "";
		if(uri.equals(FINDS_CONTENT_URI))
			table = FIND_TABLE_NAME;
		else if (uri.equals(PHOTOS_CONTENT_URI))
			table = PHOTO_TABLE_NAME;
		long rowId = db.insert(table, "", values);
		Uri _uri = null;
		if(rowId>0) {
			if(uri.equals(FINDS_CONTENT_URI)) {
				Cursor c = query(Uri.parse("content://org.hfoss.provider.POSIT/finds_id/"+rowId),null,null,null,null);
				c.moveToFirst();
				String identifier = c.getString(c.getColumnIndexOrThrow(COLUMN_IDENTIFIER));
				_uri = ContentUris.withAppendedId(uri, Long.parseLong(identifier));
		        getContext().getContentResolver().notifyChange(_uri, null);
		        c.close();
			}
			else {
				_uri = ContentUris.withAppendedId(uri, rowId);
			}
			getContext().getContentResolver().notifyChange(_uri, null); 
	        return _uri;
		}
		throw new SQLException("Failed to insert row into "+uri);
	}

	@Override
	public boolean onCreate() {
		Context context = getContext();
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
		return (db == null)? false:true;
	}

	@Override
	public Cursor query(Uri uri, String[] projections, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		switch(uriMatcher.match(uri)) {
		case FINDS_BY_PROJECT:
			sqlBuilder.setTables(FIND_TABLE_NAME);
			//Log.i("POSITProvider", "project id = "+uri.getPathSegments().get(1));
			sqlBuilder.appendWhere(COLUMN_PROJECT_ID + " = " + uri.getPathSegments().get(1));
			break;
		case FIND_ID:
			sqlBuilder.setTables(FIND_TABLE_NAME);
			sqlBuilder.appendWhere(COLUMN_ID + " = " + uri.getPathSegments().get(1));
			break;
		case PHOTO_FINDID:
			sqlBuilder.setTables(PHOTO_TABLE_NAME);
			//Log.i("POSITProvider", "query photos with find id = "+uri.getPathSegments().get(1));
			sqlBuilder.appendWhere(COLUMN_FIND_ID + " = " + uri.getPathSegments().get(1));
			break;
		case PHOTOS_BY_PROJECT:
			sqlBuilder.setTables(PHOTO_TABLE_NAME);
			sqlBuilder.appendWhere(COLUMN_PROJECT_ID + " = " + uri.getPathSegments().get(1));
			break;
		}
		
		Cursor c = sqlBuilder.query(db, projections, selection, selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
}
