/**
 * @author Phil Fritzsche
 * 
 * Similar to MyDBHelper.java, this was written with much help from POSIT's DBHelper class, as written by pgautam.
 */

package com.hfoss.summer09.cc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*
 * Work in progress.
 */
public class DBHelper {

	private static final String DBName ="alfred";
	public static final int DBVersion = 2;
	private static final String TAG = "DBHelper";

	public static final String FIND_TABLE_NAME = "finds";
	public static final String KEY_ID = "_id";
	public static final String KEY_PIC_NAME = "pic_name";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_CONTACT_NAME = "contact_name";
	public static final String KEY_EMAIL = "email";
	public static final String KEY_PHONE = "phone";
	public static final String KEY_IMAGE = "image";
	public static final String KEY_VIDEO = "video";
	public static final String KEY_AUDIO = "audio";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_CREATE_TIME = "create_time";
	public static final String KEY_MOD_TIME = "mod_time";
	public static final String KEY_SENT_TIME = "sent_time";

	static final String CREATE_FINDS_TABLE = "CREATE TABLE "
		+ FIND_TABLE_NAME + " ("
		+ KEY_ID + " integer primary key autoincrement, "
		+ KEY_PIC_NAME + " text, "
		+ KEY_DESCRIPTION + " text, "
		+ KEY_CONTACT_NAME + " text, "
		+ KEY_EMAIL + " text, "
		+ KEY_PHONE + " text, "
		+ KEY_IMAGE + " text, "
		+ KEY_VIDEO + " text, "
		+ KEY_AUDIO + " text, "
		+ KEY_LATITUDE + " double, "
		+ KEY_LONGITUDE + " double, " 
		+ KEY_CREATE_TIME + " double, "
		+ KEY_MOD_TIME + " double, "
		+ KEY_SENT_TIME + " double "
		+ ");";

	private Context context;

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase db;

	public DBHelper(Context context) {
		this.context = context;
		db = null;
	}

	public void open() throws SQLException {
		mDbHelper = new DatabaseHelper(context);
		db = mDbHelper.getWritableDatabase();
	}

	public void openReadOnly() throws SQLException{
		mDbHelper = new DatabaseHelper(context);
		db = mDbHelper.getReadableDatabase();
	}

	public void close() {
		db.close();
	}


	public void formatDb() throws SQLException {
		db.execSQL("DROP TABLE IF EXISTS "+ FIND_TABLE_NAME +";");
		db.execSQL(CREATE_FINDS_TABLE);
	}

	public long addNewFind(ContentValues values) {
		if (values!=null) {
			values.put(KEY_MOD_TIME, System.currentTimeMillis());
			return db.insert(FIND_TABLE_NAME, null, values);
		} else {
			return -1;
		}
	}

	public boolean updateFind(long rowId, ContentValues values) {
		if (values != null) {
			return db.update(FIND_TABLE_NAME, values, KEY_ID + "=" + rowId, null) > 0;
		} else {
			return false;
		}
	}

	public boolean deleteFind(long mRowId) {
		return db.delete(FIND_TABLE_NAME, KEY_ID + "=" + mRowId, null)>0;
	}

	public Cursor fetchFind(String[]projection, long rowId) {
		return db.query(FIND_TABLE_NAME, projection, KEY_ID + "=" + rowId, null, null, null, null);
	}

	public Cursor fetchSelectedColumns(int tableId ,String[] projection) {
		return db.query(FIND_TABLE_NAME, projection, null, null, null, null, null);
	}

	class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DBName, null, DBVersion);
		}

		@Override
		public void onCreate(SQLiteDatabase db)	throws SQLException {
			db.execSQL(CREATE_FINDS_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			Log.w(TAG, "Warning: upgrading from version " + oldVersion + " to "
					+ newVersion + "." + " This will remove all old data.");
			db.execSQL("DROP TABLE IF EXISTS " + FIND_TABLE_NAME);
			onCreate(db);
		}
	}
}