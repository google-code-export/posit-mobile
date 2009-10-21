/*******************************************************************************
 * Copyright (c) 2009 .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Prasanna Gautam - initial API and implementation
 *     Ralph Morelli - Supervisor
 *     Trishan deLanerolle - Director
 *     Antonio Alcorn - Summer 2009 Intern
 *     Gong Chen - Summer 2009 Intern
 *     Chris Fei - Summer 2009 Intern
 *     Phil Fritzsche - Summer 2009 Intern
 *     James Jackson - Summer 2009 Intern
 *     Qianqian Lin - Summer 2009 Intern 
 *     Khanh Pham - Summer 2009 Intern
 ******************************************************************************/

package org.hfoss.posit;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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
 * @author Prasanna Gautam
 * @author Ralph Morelli
 * @author Phil Fritzsche
 * @author Chris Fei
 * @author Qianqian Lin
 */
public class MyDBHelper extends SQLiteOpenHelper {
	/*
	 * Add new tables here.
	 */
	private static final String DBName ="posit";
	public static final int DBVersion = 2;
	private static final String TAG = "MyDBHelper";

	/**
	 *  The primary table
	 */
	public static final String FIND_TABLE_NAME = "finds";
	public static final String COLUMN_ID = "_id";
	public static final String PROJECT_ID = "projectId";
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


	public static final String[] list_row_data = { 
		COLUMN_IDENTIFIER,
		COLUMN_NAME,
		COLUMN_DESCRIPTION,
		COLUMN_LATITUDE,
		COLUMN_LONGITUDE,
		COLUMN_SYNCED
	};

	public static final int[] list_row_views = {
		R.id.find_image, 
		R.id.name_id, 
		R.id.description_id,
		R.id.latitude_id,
		R.id.longitude_id,
		R.id.status
	};

	/**
	 * Finds table creation sql statement. 
	 *  To clean up: get those string literals out of there
	 *  
	 *  Most of the field-name identifiers start with "COLUMN_". This is a naming convention.
	 */
	private static final String CREATE_FINDS_TABLE = "CREATE TABLE "
		+ FIND_TABLE_NAME  
		+ " (" + COLUMN_ID + " integer primary key autoincrement, "
		+ COLUMN_NAME + " text, "
		+ COLUMN_DESCRIPTION + " text, "
		+ COLUMN_LATITUDE + " double, "
		+ COLUMN_LONGITUDE + " double, "
		+ COLUMN_IDENTIFIER + " text, " /* for barcodes*/
		+ COLUMN_TIME + " text, "
		+ COLUMN_SID + " integer, "
		+ COLUMN_SYNCED + " integer default 0, "
		+ COLUMN_REVISION + " integer default 1, "
		+ "audioClips integer, "	// needed for compatibility
		+ "videos integer, "		// needed for compatibility
		+ MODIFY_TIME + " text, "
		+ PROJECT_ID + " integer, "
		+ IS_AD_HOC + " integer default 0"
		+ ");";

	private static final String CREATE_IMAGES_TABLE = "CREATE TABLE "
		+ PHOTO_TABLE_NAME  
		+ " (" + COLUMN_ID + " integer primary key autoincrement, "  // User Key
		+ COLUMN_PHOTO_IDENTIFIER + " integer, "
		+ COLUMN_FIND_ID + " integer, "      // User Key
		+ COLUMN_IMAGE_URI + " text, "      // The image's URI
		+ COLUMN_PHOTO_THUMBNAIL_URI + " text "      // The thumbnail's URI
		+ ");";

	private Context mContext;   // The Activity
	private SQLiteDatabase mDb;  // Pointer to the DB	

	public MyDBHelper(Context context) {
		super(context, DBName, null, DBVersion);
		this.mContext= context;
	}

	/**
	 * This method is called only when the DB is first created.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) throws SQLException {
		db.execSQL(CREATE_FINDS_TABLE);
		db.execSQL(CREATE_IMAGES_TABLE);
	}

	/**
	 * This method is called when the DB needs to be upgraded -- not
	 *   sure when that is??
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + FIND_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + PHOTO_TABLE_NAME);
		onCreate(db);		
	}

	/**
	 * This method is called from a Find object to add its data to DB.
	 * @param values  contains the <column_name,value> pairs for each DB field.
	 * @return the rowId of the new insert or -1 in case of error
	 */
	public long addNewFind(ContentValues values) {
		mDb = getWritableDatabase();  // Either create the DB or open it.
		long result = mDb.insert(FIND_TABLE_NAME, null, values);
		long findId = -1;
		if (result != -1) {
			findId = getIdentifierFromRowId(result);
		}
		mDb.close();
		return findId;
	}

	/**
	 * This method is called from a Find object to add a photo to DB.
	 * @param values  contains the <column_name,value> pairs for each DB field.
	 * @return
	 */  
	public long addNewPhoto(ContentValues values, long id) {
		mDb = getWritableDatabase(); // Either create or open DB
		if (!values.containsKey(COLUMN_FIND_ID))
			values.put(COLUMN_FIND_ID,id);
		if (!values.containsKey(COLUMN_PHOTO_IDENTIFIER))
			values.put(COLUMN_PHOTO_IDENTIFIER, new Random().nextInt(999999999));
		long result = mDb.insert(PHOTO_TABLE_NAME, null, values);
		mDb.close();
		return result;
	}

	/**
	 * This method is called from a Find object, passing a photo ID and deletes that photo.
	 * @param photoId is the Id of the image to be deleted
	 * @return
	 */
	public boolean deletePhoto(long photoId) {
		mDb = getWritableDatabase();

		Cursor images = mDb.query(PHOTO_TABLE_NAME, new String[]{ COLUMN_IMAGE_URI, COLUMN_PHOTO_THUMBNAIL_URI }, COLUMN_PHOTO_ID+"="+photoId, null,null,null,null);
		images.moveToFirst();
		Uri image = Uri.parse(images.getString(images.getColumnIndexOrThrow(COLUMN_IMAGE_URI)));
		Uri thumbnail = Uri.parse(images.getString(images.getColumnIndexOrThrow(COLUMN_PHOTO_THUMBNAIL_URI)));
		boolean result = mDb.delete(PHOTO_TABLE_NAME, COLUMN_PHOTO_ID+"="+photoId, null)>0;
		int rImage = 0;

		/**
		 * We would like to delete the thumbnails too, but it's a problem with android
		 * @see http://code.google.com/p/android/issues/detail?id=2724
		 */

		//int rThumb = 0;
		try {
			rImage = mContext.getContentResolver().delete(image, null, null);
			//rThumb = mContext.getContentResolver().delete(thumbnail, null, null);
		}
		catch(UnsupportedOperationException e) { 
			Log.d(TAG, "Could not delete db entry: " + image.toString(), e);
		}
		images.close();
		mDb.close();

		return result && rImage>0; //&& rThumb>0;
	}

	public boolean deleteAllPhotos() {
		mDb = getWritableDatabase();
		Cursor images = mDb.query(PHOTO_TABLE_NAME, null, null, null,null,null,null);
		//images.moveToFirst();
		while(images.moveToNext()) {
			Uri image = Uri.parse(images.getString(images.getColumnIndexOrThrow(COLUMN_IMAGE_URI)));
			try {
				mContext.getContentResolver().delete(image,null,null);
			}
			catch(Exception e) {
				if(Utils.debug)
					Log.i(TAG,"Could not delete all photos");
			}
		}
		images.close();
		boolean result = mDb.delete(PHOTO_TABLE_NAME,null,null) == 0; //deletes all rows
		mDb.close();
		return result;
	}
	
	/**
	 * Deletes a photo from the database, based on a specific find and position.
	 * @param findId the find whose photo is to be deleted
	 * @param position the specific position [row id, list position, etc] the photo is located at
	 * @return true if deleted, false if not
	 */
	public boolean deletePhotoByPosition(long findId, int position) {
		mDb = getWritableDatabase();
		Cursor images = mDb.query("photos", new String[]{ COLUMN_PHOTO_ID, COLUMN_IMAGE_URI, COLUMN_PHOTO_THUMBNAIL_URI },
				COLUMN_FIND_ID + "=" + findId, null, null, null, null);
		images.moveToPosition(position);
		long photoId = images.getLong(images.getColumnIndex(COLUMN_ID));
		images.close();
		mDb.close();
		return deletePhoto(photoId);
	}

	/**
	 * Retrieves a photo from the database, based on a specific find and position.
	 * @param findId the find whose photo is to be retrieved
	 * @param position the specific position [row id, list position, etc] the photo is located at
	 * @return Uri to the photo
	 */
	public Uri getPhotoUriByPosition(long findId, int position) {
		mDb = getWritableDatabase();
		Cursor cursor = getImagesCursor(findId);

		if (cursor.moveToPosition(position)) {
			String s = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URI));
			mDb.close();
			cursor.close();
			if (s != null) {
				return Uri.parse(s);
			}
		} else {
			Log.e(TAG, "cursor could not move to position: " + position);
		}

		return null;
	}

	/**
	 * This method will update a Find's data in the DB.
	 * @param rowId  The Find's row identifier in the DB table.
	 * @param args   The values for each column of the table.
	 * @return
	 */
	public boolean updateFind(long id, ContentValues args) {
		mDb = getWritableDatabase();  // Either create or open the DB.
		boolean result = false;
		if (args != null) {
			Log.i(TAG, "id = "+id);
			result = mDb.update(FIND_TABLE_NAME, args, COLUMN_IDENTIFIER + "=" + id, null) > 0;
			Log.i(TAG,"result = "+result);
		}
		mDb.close();
		return result;
	}

	/**
	 * This method is called from a Find object, passing its ID. It delete's the object's
	 *   data from the FindsTable DB.
	 * @param mRowId
	 * @return
	 */
	public boolean deleteFind(long id) {
		//deleteImages(mRowId);
		mDb = getWritableDatabase();
		boolean result = mDb.delete(FIND_TABLE_NAME, COLUMN_IDENTIFIER + "=" + id, null)>0;
		mDb.close();
		return result;
	}

	/**
	 * This method is called from ListActivity to delete all the finds currently
	 *  in the DB.
	 * @return
	 */
	public boolean deleteAllFinds() {
		//deleteImages(mRowId);
		mDb = getWritableDatabase();
		boolean result = mDb.delete(FIND_TABLE_NAME,null,null) == 0;  // deletes all rows
		deleteAllPhotos();
		mDb.close();
		return result;
	}

	/**
	 * This method is called from a Find object.  It queries the DB with the 
	 *   Find's ID. It constructs a ContentsValue hash table and returns it.
	 *   Note that it closes the DB and the Cursor -- to prevent leaks.
	 *   TODO:  It should handle a failed query with an Exception 
	 * @param id the find's id
	 * @return
	 */
	public ContentValues fetchFindData(long id) {
		String[] columns = null;
		ContentValues values = fetchFindColumns(id, columns);
		return values;
	}

	public Cursor getImagesCursor(long id) {
		mDb = getReadableDatabase(); 
		String[] columns = {COLUMN_IMAGE_URI, COLUMN_FIND_ID};
		String[] selectionArgs = null;
		String groupBy = null, having = null, orderBy = null;
		Cursor cursor = mDb.query(PHOTO_TABLE_NAME, null, COLUMN_FIND_ID + "=" + id, selectionArgs, groupBy, having, orderBy);
		return cursor;
	}

	public ContentValues getImages(long id) {
		mDb = getReadableDatabase();
		Cursor cursor = getImagesCursor(id);
		cursor.moveToFirst();
		ContentValues values = new ContentValues();
		if(Utils.debug)
			Log.i(TAG, "Images count = " + cursor.getCount() + " for _id = " + id);
		if (cursor.getCount() != 0)
			values = getValuesFromRow(cursor);
		cursor.close();
		mDb.close();
		return values;
	}

	/**
	 * Adds images Uris to existing ContentValues
	 * @param id  is the Key of the Find whose images are sought
	 * @param values is an existing ContentValues with Find's <key, value> pairs
	 */
	public void getImages(long id, ContentValues values) {
		Cursor cursor = getImagesCursor(id);
		cursor.moveToFirst();
		if (cursor.getCount() != 0) {
			for (String column : cursor.getColumnNames()) {
				String image = cursor.getString(cursor.getColumnIndexOrThrow(column));
				if (image != null)
					values.put(column, cursor.getString(cursor.getColumnIndexOrThrow(column)));
			}

		}
		cursor.close();
		mDb.close();
	}

	public ContentValues fetchFindColumns(long id, String[] columns) {
		mDb = getReadableDatabase();  // Either open or create the DB    	
		//String[] columns = mContext.getResources().getStringArray(R.array.TABLE_FINDS_core_fields);

		String[] selectionArgs = null;
		String groupBy = null, having = null, orderBy = null;
		Cursor cursor = mDb.query(FIND_TABLE_NAME, columns, COLUMN_IDENTIFIER+"="+id, selectionArgs, groupBy, having, orderBy);
		cursor.moveToFirst();
		ContentValues values = null;
		if (cursor.getCount() != 0)
			values = getValuesFromRow(cursor);
		cursor.close();
		mDb.close();
		return values;
	}

	/**
	 * This method is called from a Find object.  It queries the DB with the 
	 *   Find's ID. It constructs a ContentsValue hash table and returns it.
	 *   Note that it closes the DB and the Cursor -- to prevent leaks.
	 *   TODO:  It should handle a failed query with an Exception 
	 * @param id
	 * @return
	 */
	public HashMap<String,String> fetchFindMap(long id) {
		mDb = getReadableDatabase();  // Either open or create the DB    	
		//String[] columns = mContext.getResources().getStringArray(R.array.TABLE_FINDS_core_fields);
		String[] columns = null;
		String[] selectionArgs = null;
		String groupBy = null, having = null, orderBy = null;
		Cursor cursor = mDb.query(FIND_TABLE_NAME, columns, COLUMN_IDENTIFIER + "=" + id, selectionArgs, groupBy, having, orderBy);
		cursor.moveToFirst();
		HashMap<String,String> findsMap = new HashMap<String, String>();
		if (cursor.getCount() != 0) {
			findsMap = Utils.getMapFromCursor(cursor);
		}
		cursor.close();
		mDb.close();
		return findsMap;
	}

	/**
	 * This helper method is passed a cursor, which points to a row of the DB.
	 *  It extracts the names of the columns and the values in the columns,
	 *  puts them into a ContentValues hash table, and returns the table.
	 * @param cursor is an object that manipulates DB tables. 
	 * @return
	 */
	private ContentValues getValuesFromRow(Cursor cursor) {
		ContentValues values = new ContentValues();
		cursor.moveToFirst();
		for (String column : cursor.getColumnNames()) {
			
			if(Utils.debug)
			Log.i(TAG, "Column " + column + " = " + 
					cursor.getString(cursor.getColumnIndexOrThrow(column)));
			values.put(column, cursor.getString(cursor.getColumnIndexOrThrow(column)));
		}
		cursor.close();
		return values;
	}

	public int getIdBySID(int sid) {
		mDb = getReadableDatabase();
		String[] cols = {COLUMN_ID};
		Cursor c = mDb.query(FIND_TABLE_NAME, cols, COLUMN_SID + "="+sid,
				null, null, null, null);
		c.moveToFirst();
		int toReturn = c.getInt(c.getColumnIndexOrThrow(COLUMN_ID));
		c.close();
		mDb.close();
		return toReturn;
	}

	/** 
	 * This method will return a Cursor with rows of data for all Finds.
	 * @return
	 */
	public Cursor fetchAllFinds(int project_id) {
		mDb = getReadableDatabase(); // Either open or create the DB.
		Cursor c = mDb.query(FIND_TABLE_NAME,null, PROJECT_ID +"="+project_id, null, null, null, null);
		Log.i("CURSOR","count = "+c.getCount());
		mDb.close();
		return c;
	}

	public Cursor fetchSelectedColumns(String[] columns){
		mDb = getReadableDatabase(); // Either open or create the DB.
		Cursor c = null;
		c = mDb.query(FIND_TABLE_NAME, columns, null, null, null, null, null); //  NOTE WELL: Closing causes an error in cursor mgmt
		return c;
	}

	/**
	 * Sets server ID.
	 * @param tableId
	 * @param rowId
	 * @param server_id
	 */ 
	public void setServerId(int tableId,long id, int server_id) {
		mDb = getReadableDatabase(); // Either open or create the DB
		ContentValues args = new ContentValues();
		args.put(COLUMN_SID, server_id);
		//if new items are being added from the phone, assume it as synced for obvious reasons
		args.put(COLUMN_SYNCED, true); 
		updateFind(id, args);
		mDb.close();
	}    

	/**
	 * Adds remote find.
	 * @return 
	 */
	public long addRemoteFind(HashMap<String,Object> findsMap) {
		mDb = getReadableDatabase();
		ContentValues args = getContentValuesFromMap(findsMap);
		int sid = (Integer)findsMap.get(COLUMN_SID);
		long id = findIdofRemoteFind(sid);
		int tableId = R.array.TABLE_FINDS;
		if (id ==0) { /* if there's no find with such server id */
			args.put(COLUMN_SYNCED, true);
			id = addNewFind(args);
		} else {
			updateFind(id, args);
		}
		mDb.close();
		return id;
	}

	public static ContentValues getContentValuesFromMap(HashMap<String, Object> findsMap) {
		Iterator<String> iter = findsMap.keySet().iterator();
		ContentValues args = new ContentValues();

		while (iter.hasNext()) {
			String key = iter.next();
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
		return args;
	}    

	/**
	 * This method returns a list of those Finds that have been updated
	 *  since last synced with the server.
	 * @param remoteFindsList -- the list of all finds on the server.
	 * @return list of all the SIDs in the phone that the phone has to send now
	 */
	public List<Integer> getUpdatedSIDsFromPhone(List<HashMap<String, Object>> remoteFindsList) {
		String queryCondition = "";
		List<Integer> idsList = new ArrayList<Integer>();
		for (int i = 0; i < remoteFindsList.size(); i++) {
			HashMap<String, Object> find = remoteFindsList.get(i);
			if(find.containsKey("id")&&find.containsKey("revision")) {
				queryCondition += "( " + COLUMN_SID + "=" + find.get("id") + " AND "
				+ COLUMN_REVISION + ">" + find.get("revision") + ")";
				if (i != remoteFindsList.size() - 1) {
					queryCondition += " OR ";
				}
			}
		}
			Log.i(TAG, "Querying for updated SIDs: " + queryCondition);

		try {
			mDb = getReadableDatabase();

			Cursor c = mDb.query(FIND_TABLE_NAME, new String[] { COLUMN_SID },
					queryCondition, null, null, null, null);
			if (c.getCount() != 0) {
				c.moveToFirst();
				do {
					idsList.add(c.getInt(c.getColumnIndexOrThrow(COLUMN_SID)));

				} while (c.moveToNext());
			}
			c.close();
			mDb.close(); // close mDb
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
			Log.i(TAG, "Finds that need synching to server (Phone keys): " + idsList.toString());

		return idsList;
	}

	/**
	 * This method returns a list of those Finds that need updating 
	 *  since last synched with the server.
	 * @param remoteFindsList -- the list of all finds on the server.
	 * @return list of all the SIDs in the phone that need updating
	 */
	public List<Integer> getFindsNeedingUpdate(List<HashMap<String, Object>> remoteFindsList) {
		String queryCondition = "";
		List<Integer> idsList = new ArrayList<Integer>();

		for (int i = 0; i < remoteFindsList.size(); i++) {
			HashMap<String, Object> find = remoteFindsList.get(i);

			queryCondition += "( " + COLUMN_SID + "=" + find.get("id") + " AND "
			+ COLUMN_REVISION + "<" + find.get("revision") + ")";
			if (i != remoteFindsList.size() - 1) {
				queryCondition += " OR ";
			}
		}
			Log.i(TAG, "Querying for SIDs needing updating: " + queryCondition);

		try {
			mDb = getReadableDatabase();

			Cursor c = mDb.query(FIND_TABLE_NAME, new String[] { COLUMN_SID },
					queryCondition, null, null, null, null);
			if (c.getCount() != 0) {
				c.moveToFirst();
				do {
					idsList.add(c.getInt(c.getColumnIndexOrThrow(COLUMN_SID)));

				} while (c.moveToNext());
			}
			c.close();
			mDb.close(); // close mDb
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
			Log.i(TAG, "Finds that need synching from server (Phone keys): " + idsList.toString());
		return idsList;
	}	    

	/**
	 * ?
	 * @param l
	 */
	private long findIdofRemoteFind(int l) {
		Cursor c = mDb.query(FIND_TABLE_NAME, new String[] { COLUMN_IDENTIFIER }, COLUMN_SID + "=" + l, null, null, null, null);
		c.moveToFirst();
		if ( c.getCount()== 0)
			return  0;
		else 
			return (c.getLong(c.getColumnIndexOrThrow(COLUMN_IDENTIFIER)));
	}

	public List<Integer> getAllPhoneSIDs () {
		List<Integer> allSIDs = new ArrayList<Integer>();
		try {
			mDb = getReadableDatabase();
			/* Get all the SIDs in our database in a list */
			Cursor c = fetchSelectedColumns(new String[] { COLUMN_SID });
			if (c.getCount() != 0) {
				c.moveToFirst();
				do {
					allSIDs.add(c.getInt(c.getColumnIndexOrThrow(COLUMN_SID)));
				} while (c.moveToNext());
			}
			c.close();
			close(); // Close the DB
				Log.i(TAG, "All SIDs on phone: " + allSIDs.toString());
		} catch (Exception e) {
			Log.e(TAG,  e.getStackTrace() + "blah blah");
		}
		return allSIDs;
	}


	/**
	 * This method deletes all the images associated with a find or pretty much anything else.
	 * @param imagesQuery
	 * @return
	 */
	public boolean deleteImages (Cursor imagesQuery, long findId) {
		boolean result =true;
		while (imagesQuery.moveToNext()) {
			String uriString = imagesQuery.getString(imagesQuery.getColumnIndexOrThrow(COLUMN_IMAGE_URI));
			Uri uri = Uri.parse(uriString);
			int r = mContext.getContentResolver().delete(uri, null, null);
			result = result && (r>0);
		}
		int r = mDb.delete(PHOTO_TABLE_NAME, COLUMN_FIND_ID + " = " + findId, null);
		mDb.close();
		imagesQuery.close();
		return result; //returns true if all the images are deleted
	}

	/**
	 * This method gets all new SIDs (server IDs).  These are the ones that
	 *  are set to 0 initially. 
	 * @return
	 */
	public List<Integer> getAllNewIds(){
		String[] projection = new String[] { COLUMN_IDENTIFIER };
		List<Integer> findsList = new ArrayList<Integer>();
		try {
			mDb = getReadableDatabase();
			Cursor c = mDb.query(FIND_TABLE_NAME, projection, COLUMN_SID + "=0 and "+ IS_AD_HOC +"=0",
					null, null, null, null);
			//			Cursor c = mDb.query(TABLE_NAME, projection, null,
			//					null, null, null, null);			
			if (c.getCount() > 0) {
				c.moveToFirst();
				do {
					findsList.add(c.getInt(c.getColumnIndexOrThrow(COLUMN_IDENTIFIER)));
				} while (c.moveToNext());
			}
			c.close();
			mDb.close();
		} catch (Exception e) {
			Log.e(TAG, e.getStackTrace()+"");
		}
		return findsList;
	}

	public Cursor getFindsWithIdentifier(long value) {
		mDb = getWritableDatabase();
		Cursor cursor = mDb.query("finds", null, COLUMN_IDENTIFIER + "=" + value, null, null, null, null);
		mDb.close();
		return cursor;
	}

	/**
	 * Checks if the database already contains the image specified.  Used by the sync thread as a quick way to
	 * check if an image needs to be saved or not.  Also prevents having too many copies of the same photos in 
	 * the MediaStore.
	 * @param imageId
	 * @return
	 */
	public boolean containsImage(int imageId) {
		mDb = getReadableDatabase();
		Cursor c = mDb.query(PHOTO_TABLE_NAME, null, COLUMN_PHOTO_IDENTIFIER +"="+imageId, null, null, null, null);
		int count = c.getCount();
		mDb.close();
		if (count != 0) {
			return true;
		} else {
			return false;
		}
	}

	public long getIdentifierFromRowId(long rowId) {
		mDb = getReadableDatabase();
		Cursor c = mDb.query(FIND_TABLE_NAME, null, COLUMN_ID + "=" + rowId, null, null, null, null);
		if ( c.getCount()== 0)
			return  0;
		else {
			c.moveToFirst();
			return (c.getLong(c.getColumnIndexOrThrow(COLUMN_IDENTIFIER)));
		}
	}
}