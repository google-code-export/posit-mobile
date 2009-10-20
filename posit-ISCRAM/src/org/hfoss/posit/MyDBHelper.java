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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;

/**
 * The class is the interface with the Database. It controls all DB access 
 *  and directly handles all DB queries.
 * @author pgautam
 * @author rmorelli
 *
 */
public class MyDBHelper extends SQLiteOpenHelper {
	/*
	 * Add new tables here.
	 */
    private static final String DBName ="posit";
	public static final int DBVersion = 1;
	private static final int[] TABLES = { R.array.TABLE_FINDS }; // Only 1 table for now
    private static final String TAG = "DBHelper";

	/* the core items that we would be using in POSIT for sure. */
	public static final String KEY_ID = "_id";
	public static final String KEY_NAME = "name";

	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_TIME = "time";
	
	private Context mContext;   // The Activity
	private SQLiteDatabase db;  // Pointer to the DB
	private String findsTable;
	
	public MyDBHelper(Context context) {
		super(context, DBName, null, DBVersion);
		 mContext= context;
		 findsTable = mContext.getResources().getResourceEntryName(R.array.TABLE_FINDS);
	}
	
	@Override
	public void onCreate(SQLiteDatabase arg0) throws SQLException {
		String query = "CREATE TABLE " + findsTable;
		db.execSQL(query);
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
	
    /**
     * This method is called from a Find object to add its data to DB.
     * @param args  contains the <attr,value> pairs for each DB field.
     * @return
     */
    public long addNewFind(ContentValues args) {
		db = getWritableDatabase();
		long result = -1;
    	if (args!=null) {
    		result =  db.insert(findsTable, null, args);
    		db.close();
    	}
    	return result;
    }

    public ContentValues getValuesFromRow(Cursor cursor) {
    	ContentValues values = new ContentValues();
    	cursor.moveToFirst();
    	for (String column : cursor.getColumnNames()) {
    		
    		values.put(column, cursor.getString(cursor.getColumnIndexOrThrow(column)));
    	}
    	/*String value = "testvalue";
 //   	value = cursor.getString(cursor.getColumnIndexOrThrow("name"));
    	
    	value = cursor.getString(cursor.getColumnIndexOrThrow("description"));
    	values.put("description", value);*/
    	return values;
    }
   

    public boolean updateFind(long rowId, ContentValues args) {
		db = getWritableDatabase();
    	if (args!=null) {
    		return db.update(findsTable, args, KEY_ID+"="+rowId, null)>0;
    	}else {
    		return false;
    	}
    }
    
     
    /**
     * This method is called from a Find object.  It queries the DB with the 
     *   Find's ID and returns a Cursor to the find.  This method
     *   should return null if the query fails. 
     * @param id
     * @return
     */
    public Cursor fetchFind(long id) {
		db = getReadableDatabase();
    	//String[] columns = mContext.getResources().getStringArray(R.array.TABLE_FINDS_core_fields);
		String[] columns = null;
    	String[] selectionArgs = null;
		String groupBy = null, having = null, orderBy = null;
    	Cursor cursor = db.query(findsTable, columns, KEY_ID+"="+id, selectionArgs, groupBy, having, orderBy);
    	cursor.moveToFirst();
    	db.close();
    	return cursor;	
    }
    
    /**
     * This method is called from a Find object, passing its ID. It delete's the object's
     *   data from the FindsTable DB.
     * @param mRowId
     * @return
     */
    public boolean deleteFind(long mRowId) {
    	//deleteImages(mRowId);
		db = getWritableDatabase();
		boolean result = db.delete(findsTable, KEY_ID+"="+mRowId, null)>0;
		db.close();
    	return result;
    }
    
    /**
     * This method deletes all the images associated witha a find or pretty much anything else.
     * @param imagesQuery
     * @return
     */
    public boolean deleteImages (Cursor imagesQuery) {
    	boolean result =true;
		while (imagesQuery.moveToNext()) {
			long id =imagesQuery.getLong(imagesQuery.getColumnIndexOrThrow(BaseColumns._ID));
			Log.i(TAG, ""+id);
			Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id+"");
			int r = mContext.getContentResolver().delete(uri, null, null);
			result = result && (r>0); 
		}
		return result; //returns true if all the images are deleted
    }
    
    
    
}
