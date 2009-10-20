package org.hfoss.posit.db;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Resources;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class HPhotoProvider extends ContentProvider {

	private SQLiteDatabase mDB;

	private static final String DEBUG_TAG = "HContentProvider";

	private static final String DB_NAME = "posit.db";

	private static final int DB_VERSION = 2;

	private static HashMap<String, String> PHOTOS_LIST_PROJECTION_MAP;

	private static final int PHOTOS = 1;

	private static final int PHOTO_ID = 2;

	private static final UriMatcher URL_MATCHER;

	private static class DatabaseHelper extends SQLiteOpenHelper {
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE photos ("
					+ "_id INTEGER PRIMARY KEY AUTOINCREMENT," + "name TEXT,"
					+ "description TEXT," + "_data TEXT,"
					+ "latitude INTEGER," + "longitude INTEGER,"
					+ "created INTEGER,"
					+ "modified INTEGER" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(DEBUG_TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS notes");
			onCreate(db);
		}

	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int count;
		long rowID=0;
		switch (URL_MATCHER.match(uri)){
		case PHOTOS:
			count = mDB.delete("photos", where, whereArgs);
			break;
		case PHOTO_ID:
			String segment = uri.getPathSegments().get(1);
			rowID = Long.parseLong(segment);
			count = mDB.delete("photos", "_id="+segment+ (!TextUtils.isEmpty(where) ? " AND (" + where
                    + ')' : ""), whereArgs);
			break;
		 default:
	            throw new IllegalArgumentException("Unknown URL " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (URL_MATCHER.match(uri)){
			case PHOTOS:
				return "vnd.org.hfoss.posit.cursor.dir/vnd.org.hfoss.posit";
			case PHOTO_ID:
				return "vnd.org.hfoss.posit.cursor.item/vnd.org.hfoss.posit";
			default:
				throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		long rowID;
		ContentValues values;

		if (initialValues != null){
			values = new ContentValues(initialValues);
		}else {
			values = new ContentValues();
		}

		if (URL_MATCHER.match(uri) != PHOTOS) {
			throw new IllegalArgumentException ("Unknown URL"+uri);
		}

		Long now = Long.valueOf(System.currentTimeMillis());
		Resources r = Resources.getSystem();

		//Make sure all fields are set

		if (values.containsKey(PositData.Photos.CREATED_DATE)==false){
			values.put(PositData.Photos.CREATED_DATE, now);
		}
		if (values.containsKey(PositData.Photos.MODIFIED_DATE)==false){
			values.put(PositData.Photos.MODIFIED_DATE, now);
		}
		if (values.containsKey(PositData.Photos.NAME)==false){
			values.put(PositData.Photos.NAME, r.getString(android.R.string.untitled));
		}
		if (values.containsKey(PositData.Photos.DESCRIPTION)== false){
			values.put(PositData.Photos.DESCRIPTION,"");
		}
		rowID = mDB.insert("photos", "photo", values);

		if (rowID >0){
			String path = null;

			try {
				getContext().openFileOutput(
						new Long(rowID).toString(), Context.MODE_PRIVATE).close();
				path = getContext().getFileStreamPath(
						new Long(rowID).toString()).getAbsolutePath();
				values.put(PositData.Photos.BITMAP, path);
			}catch (Exception e)
			{
				Log.e(DEBUG_TAG,"Impossible to create file"+ path);
				Log.e(DEBUG_TAG, e.toString());
			}

			//mDB.update("photos", values, "_id="+rowID, null);

			return uri;
		}
		throw new SQLException("Failed to insert row into "+uri);
	}

	@Override
	public boolean onCreate() {
		DatabaseHelper dbHelper = new DatabaseHelper();
		mDB = dbHelper.openDatabase(getContext(), DB_NAME, null, DB_VERSION);
		return (mDB == null)?false:true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch(URL_MATCHER.match(uri)){
		case PHOTOS:
			qb.setTables("photos");
			qb.setProjectionMap(PHOTOS_LIST_PROJECTION_MAP);
			break;

		case PHOTO_ID:
			qb.setTables("photos");
			qb.appendWhere("_id="+uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URL"+uri);
		}

		String orderBy;
		if (TextUtils.isEmpty(sortOrder)){
			orderBy = PositData.Photos.DEFAULT_SORT_ORDER;
		}else {
			orderBy = sortOrder;
		}

		Cursor c = qb.query(mDB, projection, selection, selectionArgs, null, null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        int count;
        switch (URL_MATCHER.match(uri)) {
        case PHOTOS:
            count = mDB.update("photos", values, where, whereArgs);
            break;

        case PHOTO_ID:
            String segment = uri.getPathSegments().get(1);
            count = mDB
                    .update("photos", values, "_id="
                            + segment
                            + (!TextUtils.isEmpty(where) ? " AND (" + where
                                    + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URL " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        URL_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URL_MATCHER.addURI("org.hfoss.posit.db.Posit", "photos", PHOTOS);
        URL_MATCHER.addURI("org.hfoss.posit.db.Posit", "photos/#", PHOTO_ID);

        PHOTOS_LIST_PROJECTION_MAP = new HashMap<String, String>();
        PHOTOS_LIST_PROJECTION_MAP.put(PositData.Photos._ID, "_id");
        PHOTOS_LIST_PROJECTION_MAP.put(PositData.Photos.NAME, "name");
        PHOTOS_LIST_PROJECTION_MAP.put(PositData.Photos.DESCRIPTION, "description");
        PHOTOS_LIST_PROJECTION_MAP.put(PositData.Photos.BITMAP, "_data");
        PHOTOS_LIST_PROJECTION_MAP.put(PositData.Photos.LATITUDE, "latitude");
        PHOTOS_LIST_PROJECTION_MAP.put(PositData.Photos.LONGITUDE, "longitude");

        PHOTOS_LIST_PROJECTION_MAP.put(PositData.Photos.CREATED_DATE, "created");
        PHOTOS_LIST_PROJECTION_MAP.put(PositData.Photos.MODIFIED_DATE, "modified");
	}



}
