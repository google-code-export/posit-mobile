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

public class PositContentProvider extends ContentProvider{

	public static final String PROVIDER_NAME = "org.hfoss.provider.POSIT";
	public static final Uri FINDS_CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME + "/finds");
	public static final Uri PHOTOS_CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME + "/photos");
	public static final String TAG = "PositContentProvier";
	
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


	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		String findId = "";
		String projId = "";
		Cursor c = null;
		
		switch(uriMatcher.match(uri)) {
//		case FINDS_BY_PROJECT:
//			projId = uri.getPathSegments().get(1);
//			Log.i(TAG, "delete finds from project #"+projId);
//			count = db.delete(FIND_TABLE_NAME, COLUMN_PROJECT_ID + " = " + projId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
//			delete(Uri.parse("content://org.hfoss.provider.POSIT/photos_project/"+projId),null,null);
//			break;
		case PHOTOS_BY_PROJECT:
			projId = uri.getPathSegments().get(1);
			c = query(Uri.parse("content://org.hfoss.provider.POSIT/photos_project/"+projId),null,null,null,null);
			//Delete photos from gallery
			while (c.moveToNext()) {
				String uriString = c.getString(c.getColumnIndexOrThrow(PositDbHelper.PHOTOS_IMAGE_URI));
				Uri _uri = Uri.parse(uriString);
				getContext().getContentResolver().delete(_uri, null, null);
			}
			count = db.delete(PositDbHelper.PHOTOS_TABLE, PositDbHelper.FINDS_PROJECT_ID
					+ " = " + projId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
			break;
//		case FIND_ID:
//			findId = uri.getPathSegments().get(1);
//			Log.i("PROVIDER", "delete find #"+findId);
//			count = db.delete(FIND_TABLE_NAME, COLUMN_IDENTIFIER + " = " + findId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
//			delete(Uri.parse("content://org.hfoss.provider.POSIT/photo_findid/"+findId),null,null);
//			break;
		case PHOTO_FINDID:
			findId = uri.getPathSegments().get(1);
			Log.i("PROVIDER", "delete photos with find #"+findId);
			c = query(Uri.parse("content://org.hfoss.provider.POSIT/photo_findid/"+findId),null,null,null,null);
			//Delete photos from gallery
			while (c.moveToNext()) {
				String uriString = c.getString(c.getColumnIndexOrThrow(PositDbHelper.PHOTOS_IMAGE_URI));
				Uri _uri = Uri.parse(uriString);
				getContext().getContentResolver().delete(_uri, null, null);
			}
			count = db.delete(PositDbHelper.PHOTOS_TABLE, PositDbHelper.FINDS_ID + " = " 
					+ findId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
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
			table = PositDbHelper.FINDS_TABLE;
		else if (uri.equals(PHOTOS_CONTENT_URI))
			table = PositDbHelper.PHOTOS_TABLE;
		long rowId = db.insert(table, "", values);
		Uri _uri = null;
		if(rowId>0) {
			if(uri.equals(FINDS_CONTENT_URI)) {
				Cursor c = query(Uri.parse("content://org.hfoss.provider.POSIT/finds_id/"+rowId),null,null,null,null);
				c.moveToFirst();
				String identifier = c.getString(c.getColumnIndexOrThrow(PositDbHelper.FINDS_GUID));
//				_uri = ContentUris.withAppendedId(uri, Long.parseLong(identifier));
 				Log.i(TAG,"rowID = " + rowId + " identifier = " + identifier);
				_uri = ContentUris.withAppendedId(uri, rowId);
				Log.i(TAG, "_uri= " + _uri.toString());
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

	/**
	 * Invoked automatically when Posit starts -- because of the Manifest??
	 */
	@Override
	public boolean onCreate() {
		Context context = getContext();
		PositDbHelper dbHelper = new PositDbHelper(context);
		//DatabaseHelper dbHelper = new DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
		return (db == null)? false:true;
	}

	@Override
	public Cursor query(Uri uri, String[] projections, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		switch(uriMatcher.match(uri)) {
		case FINDS_BY_PROJECT:
			sqlBuilder.setTables(PositDbHelper.FINDS_TABLE);
			//Log.i("POSITProvider", "project id = "+uri.getPathSegments().get(1));
			sqlBuilder.appendWhere(PositDbHelper.FINDS_PROJECT_ID + " = " + uri.getPathSegments().get(1));
			break;
		case FIND_ID:
			sqlBuilder.setTables(PositDbHelper.FINDS_TABLE);
			sqlBuilder.appendWhere(PositDbHelper.FINDS_ID + " = " + uri.getPathSegments().get(1));
			break;
		case PHOTO_FINDID:
			sqlBuilder.setTables(PositDbHelper.PHOTOS_TABLE);
			//Log.i("POSITProvider", "query photos with find id = "+uri.getPathSegments().get(1));
			sqlBuilder.appendWhere(PositDbHelper.FINDS_ID + " = " + uri.getPathSegments().get(1));
			break;
		case PHOTOS_BY_PROJECT:
			sqlBuilder.setTables(PositDbHelper.PHOTOS_TABLE);
			sqlBuilder.appendWhere(PositDbHelper.FINDS_PROJECT_ID + " = " + uri.getPathSegments().get(1));
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
