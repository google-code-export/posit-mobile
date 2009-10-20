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
import java.util.HashMap;
import java.util.Iterator;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
/**
 * The class to interface with the Database.
 * @author pgautam
 *
 */
public class DBHelper {
	/*
	 * Add new tables here.
	 */
	private static final int[] TABLES = { R.array.TABLE_FINDS };
    private static final String TAG = "DBHelper";
    private static final String DBName ="posit";
	public static final int DBVersion = 1;
	/* the core items that we would be using in POSIT for sure. */
	public static final String KEY_ID = "_id";
	public static final String KEY_NAME = "name";

	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_TIME = "time";
	public static final String KEY_SYNCED = "synced";
	public static final String KEY_SID = "sid";
	public static final String KEY_REVISION = "revision";
	private Context mContext;
    
	 
	/**
	 * This class helps to open, create and upgrade the database file
	 * @author pgautam
	 *
	 */
	class DatabaseHelper extends SQLiteOpenHelper  {
		
		public DatabaseHelper(Context context) {
			super(context, DBName,null, DBVersion);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) 
			throws SQLException {
			for (int table:TABLES)
				try {
					db.execSQL(createTableQueryFromResource(table));
				} catch (ResourceIDNotStringArrayException e) {
					Log.e(TAG, e.getMessage());
				}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
			for (int table:TABLES)
				db.execSQL("DROP TABLE IF EXISTS "+mContext.getResources()
						.getResourceEntryName(table));
			onCreate(db);
		}
		
	}
	private DatabaseHelper mOpenHelper;
	private SQLiteDatabase db=null;
	
	public DBHelper(Context context) {
		 mContext= context;
	}
	
	public void open() throws SQLException{
		
		mOpenHelper = new DatabaseHelper(mContext);
		if (db== null)
			db = mOpenHelper.getWritableDatabase();
		else if (!db.isOpen()) {
			db = mOpenHelper.getWritableDatabase();
		}
	}
	
	public void openReadOnly() throws SQLException{
		mOpenHelper = new DatabaseHelper(mContext);
		db = mOpenHelper.getReadableDatabase();
	}
	
	 /**
     * Close the db, save some memory.
     */
    public void close() {
        db.close();
    }
    
    /**
     * Generic method to add a new find to the database
     * @param tableId
     * @param args
     * @return
     */
    public long addNewFind( int tableId, ContentValues args) {
    	if (args!=null) {
    		args.put(KEY_SYNCED, 0);
    		return db.insert(getTableName(tableId), null, args);
    	}else 
    		return -1;
    }
    /**
     * updates an entry given the table id
     * @param tableId
     * @param rowId
     * @param args
     * @return
     */
    public boolean updateFind(int tableId, long rowId, ContentValues args) {
    	if (args!=null) {
    		return db.update(getTableName(tableId), args, KEY_ID+"="+rowId, null)>0;
    	}else {
    		return false;
    	}
    }
    
    /** 
     * You pass the projection and it gives you the required Cursor with rows or null if none.
     * @param projection
     * @return
     */
    public Cursor fetchSelectedColumns(int tableId ,String[] projection){
    	return db.query(getTableName(tableId), projection,null,null,null, null, null);
    }
    /**
     * Gives a cursor that the calling activity can use to display finds.
     * @param tableId
     * @param projection
     * @param rowId
     * @return
     */
    public Cursor fetchFind(int tableId, String[]projection, long rowId) {
    	return db.query(getTableName(tableId), projection,KEY_ID+"="+rowId,null,null, null, null);
    }
    /**
     * Puts the values in the relevant mapped views. The projection should match the HashMap
     * of views though.
     * @param tableId
     * @param projection
     * @param rowId
     * @param displayViews
     * @return
     */
    public boolean fetchFind(int tableId, String[]projection, long rowId, HashMap<String,View> displayViews) {
    	Cursor c = db.query(getTableName(tableId), projection,KEY_ID+"="+rowId,null,null, null, null);
    	c.moveToFirst();
    	if (c.getCount()>0) {
    		Utils.putCursorItemsInViews(c, projection, displayViews);
    	}else {
    		return false;
    	}
    	return true;
    }
    
    
    /** Puts the values in the relevant mapped views. The projection should match the HashMap
    * of views though.
    * @param tableId
    * @param projection
    * @param rowId
    * @param viewObjects
    * @return
    */
   public boolean fetchFind(int tableId, String[]projection, long rowId, ArrayList<ViewObject>viewObjects) {
   	Cursor c = db.query(getTableName(tableId), projection,KEY_ID+"="+rowId,null,null, null, null);
   	c.moveToFirst();
   	if (c.getCount()>0) {
   		Utils.putCursorItemsInViews(mContext, c, projection, viewObjects);
   	}else {
   		return false;
   	}
   	return true;
   }
    
    public boolean deleteFind(int tableId, long mRowId) {
    	//deleteImages(mRowId);
    	return db.delete(getTableName(tableId), KEY_ID+"="+mRowId, null)>0;
    }
    
    public void deleteImages (Cursor imagesQuery) {
		while (imagesQuery.moveToNext()) {
			long id =imagesQuery.getLong(imagesQuery.getColumnIndexOrThrow(BaseColumns._ID));
			Log.i(TAG, ""+id);
			Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id+"");
			mContext.getContentResolver().delete(uri, null, null);
		}
    }
	/**
	 * Cleans up the database, removes the tables and creates them again.
	 * @throws SQLException
	 * @throws ResourceIDNotStringArrayException
	 */
	public void cleanDB() 
	throws SQLException, ResourceIDNotStringArrayException {
		for (int table:TABLES){
			db.execSQL("DROP TABLE IF EXISTS "+getTableName(table)+";");
			db.execSQL(createTableQueryFromResource(table));
			
		}
	}
	/**
	 * gets the table name from the array 
	 * @param tableId
	 * @return
	 * @throws NotFoundException
	 */
	private String getTableName(int tableId) throws NotFoundException{
		return mContext.getResources().getResourceEntryName(tableId);
	}
    
	/**
	 * generates
	 * create Table <resourceID's Entry Name> ( < items separated by commas> );
	 * This is useful for generating creating tables initially
	 * @param ctx
	 * @param id
	 * @return
	 * @throws ResourceIDNotStringArrayException
	 */
	public  String createTableQueryFromResource( int id) 
	throws ResourceIDNotStringArrayException
			 {
		Resources res = mContext.getResources();
		String[] items;
		String tableName;
			items = res.getStringArray(id);
			tableName = res.getResourceEntryName(id);
			
		/* if the entry is declared, it will have item 0 */	
		if (! (items[0] instanceof String) ){
			throw new ResourceIDNotStringArrayException(
					"Cannot create the table, ");
		}
		String query = "CREATE TABLE " + tableName + "(";
		for (int i = 0; i < items.length - 1; i++)
			query += items[i] + ",";
		query += items[items.length - 1] + ");";
		return query;
	}

	  /**
	   * Gets all the new finds;
	   * @param tableId
	   * @return
	   */
	    public Cursor getAllUnsynced(int tableId){
	    	String[] projection = new String[]{
	    			DBHelper.KEY_ID,"name","identifier","latitude","longitude","description","time"
	    		};
	    	return db.query(getTableName(tableId), projection,KEY_SYNCED+"=0",null,null, null, null);
	    }
	    
	    
	    /** Gets all the new finds;
		   * @param tableId
		   * @return
		   */
		    public Cursor getAllUnsyncedIds(int tableId){
		    	String[] projection = new String[]{
		    			KEY_ID,KEY_REVISION
		    		};
		    	return db.query(getTableName(tableId), projection,KEY_SYNCED+"=0",null,null, null, null);
		    }
	    public void setServerId(int tableId,long rowId, int server_id) {
	    	ContentValues args = new ContentValues();
	    	args.put(KEY_SID, server_id);
	    	//if new items are being added from the phone, assume it as synced for obvious reasons
	    	args.put(KEY_SYNCED, true); 
	    	updateFind(tableId, rowId, args);
	    }
	    /**
	     * get all the server ids in the finds table
	     * @return
	     */
	    public int[] fetchAllServerFindIds() {
	    	Cursor c = db.query(getTableName(R.array.TABLE_FINDS), new String[] {KEY_SID}, null, null, null, null, null);
	    	int[] sids = new int[c.getCount()];
	    	for (int i = 0; i < c.getCount(); i++) {
	    		sids[i] = c.getInt(c.getColumnIndexOrThrow(KEY_SID));
	    	}
	    	return sids;
	    }
	    /**
	     * check if the remote find i
	     * @param l
	     */
	    private long findIdofRemoteFind(int l) {
	    	Cursor c = db.query(getTableName(R.array.TABLE_FINDS), new String[] {KEY_ID}, KEY_SID+"="+l, null, null, null, null);
	    	c.moveToFirst();
	    	if ( c.getCount()== 0)
	    		return  0;
	    	else 
	    		return (c.getLong(c.getColumnIndexOrThrow(KEY_ID)));
	    }
	    
	    /**
	     * 
	     * @return
	     */
	    public long addRemoteFind(HashMap<String,Object> findsMap) {
	    	Iterator iter = findsMap.keySet().iterator();
	    	ContentValues args = new ContentValues();
	    	while (iter.hasNext()) {
	    		String key = (String) iter.next();
	    		Object value = findsMap.get(key);
	    		/* recognize server find */
	    		if (value instanceof Integer) {
	    			args.put(key, Integer.parseInt(value.toString()));
	    		}else if (value instanceof Double) {
	    			args.put(key, Double.parseDouble(value.toString()));
	    		}else {
	    			args.put(key, value.toString());
	    		}
	    		
	    	}
	    	int sid = (Integer)findsMap.get(KEY_SID);
	    	long rowId = findIdofRemoteFind(sid);
	    	int tableId=R.array.TABLE_FINDS;
			if (rowId ==0) { /* if there's no find with such server id */
				args.put(KEY_SYNCED, true);
    			rowId = addNewFind(tableId, args);
    		}else {
    			updateFind(tableId, rowId, args);
    		}
			return rowId;
	    }
}
