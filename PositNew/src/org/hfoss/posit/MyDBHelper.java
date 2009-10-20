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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
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
	public static final String KEY_SYNCED = "synced";
	public static final String KEY_SID = "sid";
	public static final String KEY_REVISION = "revision";

	
	private Context mContext;   // The Activity
	private SQLiteDatabase db;  // Pointer to the DB
	private String findsTable;
	
	public MyDBHelper(Context context) {
		super(context, DBName, null, DBVersion);
		 mContext= context;
		 findsTable = mContext.getResources().getResourceEntryName(R.array.TABLE_FINDS);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) throws SQLException {
		
		try {
			db.execSQL(createTableQueryFromResource(R.array.TABLE_FINDS));
		} catch (ResourceIDNotStringArrayException e) {
			Log.i(TAG, "Array not passed");
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
    	return cursor;	
    }
    
   /** This method is called from a Find object.  It queries the DB with the 
    *   Find's ID. It constructs a ContentsValue hash table and returns it.
    *   Note that it closes the DB and the Cursor -- to prevent leaks.
    *   TODO:  It should handle a failed query with an Exception 
    * @param id
    * @return
    */
   public ContentValues fetchFindData(long id) {
		db = getReadableDatabase();  // Either open or create the DB    	
   	//String[] columns = mContext.getResources().getStringArray(R.array.TABLE_FINDS_core_fields);
		String[] columns = null;
   	String[] selectionArgs = null;
		String groupBy = null, having = null, orderBy = null;
   	Cursor cursor = db.query(findsTable, columns, KEY_ID+"="+id, selectionArgs, groupBy, having, orderBy);
   	cursor.moveToFirst();
   	ContentValues values = null;
   	if (cursor.getCount() != 0)
   		values = getValuesFromRow(cursor);
   	cursor.close();
   	db.close();
   	return values;
   }
   
   
    /** 
     * You pass the projection and it gives you the required Cursor with rows or null if none.
     * @param projection
     * @return
     */
    public Cursor fetchSelectedColumns(String[] projection){
    	db = getReadableDatabase();
    	return db.query(findsTable, projection,null,null,null, null, null);
    }
    
	  /**
	   * Gets all the new finds;
	   * @param tableId
	   * @return
	   */
	    public Cursor getAllNewFinds(int tableId){
	    	db = getReadableDatabase();
	    	String[] projection = new String[]{
	    			KEY_ID,"name","identifier","latitude","longitude","description","time"
	    		};
	    	return db.query(findsTable, projection,KEY_SYNCED+"=0",null,null, null, null);
	    }
	    /**
	     * Gets new finds on the server
	     * The two methods getNewFindIdsOnServer are almost identical, they're separate
	     * just to keep the logic simple.
	     * @param remoteFindsList
	     * @return list of all ids in the server that have to be updated i.e, the sids we have to request
	     */
	    public List<Integer> getNewFindIdsOnServer(
			List<HashMap<String, Object>> remoteFindsList) {
		String qExisting = "";
		String qNew = "";
		List<Integer> allIds = new ArrayList<Integer>();
		Collection<Integer> idsSet = new HashSet<Integer>();
		try {
			db = getReadableDatabase();
			/* Get all the SIDs in our database in a list */
			Cursor c = fetchSelectedColumns(new String[] { KEY_SID });
			if (c.getCount() != 0) {
				c.moveToFirst();
				do {
					allIds.add(c.getInt(c.getColumnIndexOrThrow(KEY_SID)));
				} while (c.moveToNext());
			}
			c.close();
			/*
			 * For each of the remote finds, create the query If the remote find
			 * isn't in our database, put it in our set
			 */
			for (int i = 0; i < remoteFindsList.size(); i++) {
				HashMap<String, Object> find = remoteFindsList.get(i);
				Integer Id = Integer.parseInt(find.get("id").toString());
				qExisting += "( " + KEY_SID + "=" + Id + " AND " + KEY_REVISION
						+ "<" + find.get("revision") + ")";
				if (!allIds.contains(Id)) {
					// idsList.add(Id);
					idsSet.add(Id);
				}
				if (i != remoteFindsList.size() - 1) {
					qExisting += " OR ";
				}
			}
			Log.i(TAG, qExisting);

			c = db.query(findsTable, new String[] { KEY_SID }, qExisting, null,
					null, null, null);
			if (c.getCount() != 0) {
				c.moveToFirst();
				do {
					// add the unsynced ones here.
					idsSet.add(c.getInt(c.getColumnIndexOrThrow(KEY_SID)));
				} while (c.moveToNext());
			}
			c.close();
			db.close(); // close db
			// returning as List for consistency
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		return Utils.getListFromCollection(idsSet);
	}
	    
	    /**
	     * Gets new finds on the server
	     * The two methods getNewFindIdsOnServer are almost identical, they're separate
	     * just to keep the logic simple.
	     * @param remoteFindsList
	     * @return list of all the ids in the phone that the phone has to send now
	     */
	    public List<Integer> getUpdatedFindIdsOnPhone(
			List<HashMap<String, Object>> remoteFindsList) {
		String queryCondition = "";
		List<Integer> idsList = new ArrayList<Integer>();

		for (int i = 0; i < remoteFindsList.size(); i++) {
			HashMap<String, Object> find = remoteFindsList.get(i);
			queryCondition += "( " + KEY_SID + "=" + find.get("id") + " AND "
					+ KEY_REVISION + ">" + find.get("revision") + ")";
			if (i != remoteFindsList.size() - 1) {
				queryCondition += " OR ";
			}
		}
		try {
			db = getReadableDatabase();

			Cursor c = db.query(findsTable, new String[] { KEY_ID },
					queryCondition, null, null, null, null);
			if (c.getCount() != 0) {
				c.moveToFirst();
				do {
					idsList.add(c.getInt(c.getColumnIndexOrThrow(KEY_ID)));

				} while (c.moveToNext());
			}
			c.close();
			db.close(); // close db
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		return idsList;
	}
	    
	    
	    
	    public void setServerId(int tableId,long rowId, int server_id) {
	    	db = getReadableDatabase();
	    	ContentValues args = new ContentValues();
	    	args.put(KEY_SID, server_id);
	    	//if new items are being added from the phone, assume it as synced for obvious reasons
	    	args.put(KEY_SYNCED, true); 
	    	updateFind(rowId, args);
	    	db.close();
	    }    
	    
	    /**
	     * 
	     * @return
	     */
	    public long addRemoteFind(HashMap<String,Object> findsMap) {
	    	db = getReadableDatabase();
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
    			rowId = addNewFind(args);
    		}else {
    			updateFind(rowId, args);
    		}
			return rowId;
	    }    
    
	    /**
	     * check if the remote find i
	     * @param l
	     */
	    private long findIdofRemoteFind(int l) {
	    	Cursor c = db.query(findsTable, new String[] {KEY_ID}, KEY_SID+"="+l, null, null, null, null);
	    	c.moveToFirst();
	    	if ( c.getCount()== 0)
	    		return  0;
	    	else 
	    		return (c.getLong(c.getColumnIndexOrThrow(KEY_ID)));
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
    
    public List<Integer> getAllUnsyncedIds(){
    	String[] projection = new String[] { KEY_ID };
		List<Integer> findsList = new ArrayList<Integer>();
		try {
			db = getReadableDatabase();
			Cursor c = db.query(findsTable, projection, KEY_SYNCED + "=0",
					null, null, null, null);
			if (c.getCount() > 0) {
				c.moveToFirst();
				do {
					findsList.add(c.getInt(c.getColumnIndexOrThrow(KEY_ID)));
				} while (c.moveToNext());
			}
			c.close();
			db.close();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		return findsList;
    }
}
