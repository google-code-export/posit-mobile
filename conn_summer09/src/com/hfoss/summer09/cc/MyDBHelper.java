/**
 * @author Phil Fritzsche
 * Helper for accessing the program's SQLite database.
 * 
 * Much of this code follows the structure of POSIT's database structure, as written by pgautam.
 */

package com.hfoss.summer09.cc;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDBHelper extends SQLiteOpenHelper
{
	private static final String DBName = "alfred";
	public static final int DBVersion = 2;
	private static final String TAG = "MyDBHelper";

	public static final String FIND_TABLE_NAME = "finds";
	public static final String DEFAULT_FILE_PATH = "/sdcard/finds/";
	public static final String DEFAULT_IMG_EXT = "img/";
	public static final String DEFAULT_VID_EXT = "video/";
	public static final String DEFAULT_AUD_EXT = "audio/";

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

	public static final String[] LIST_PATH_EXT =
	{
		DEFAULT_IMG_EXT,
		DEFAULT_VID_EXT,
		DEFAULT_AUD_EXT
	};

	public static final String[] LIST_ROW_DATA =
	{ 
		KEY_ID,
		KEY_PIC_NAME,
		KEY_DESCRIPTION,
		KEY_CONTACT_NAME,
		KEY_EMAIL,
		KEY_PHONE,
		KEY_IMAGE,
		KEY_VIDEO,
		KEY_AUDIO,
		KEY_LATITUDE,
		KEY_LONGITUDE,
		KEY_CREATE_TIME,
		KEY_MOD_TIME,
		KEY_SENT_TIME
	};

	private static final String[] LIST_MEDIA_DATA =
	{ 
		KEY_IMAGE,
		KEY_VIDEO,
		KEY_AUDIO
	};

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
	private SQLiteDatabase mDb;

	public MyDBHelper(Context context)
	{
		super(context, DBName, null, DBVersion);
		this.context = context;
		Log.e(TAG, "constructor");
	}

	/**
	 * Called when the DB is initially created.
	 * @return
	 */
	@Override
	public void onCreate(SQLiteDatabase db) throws SQLException
	{
		Log.e(TAG, "onCreate()");
		db.execSQL(CREATE_FINDS_TABLE);
		Log.e(TAG, "created table");
	}

	/**
	 * Called when the DB is upgraded.
	 * @return
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		Log.w(TAG, "Warning: upgrading from version " + oldVersion + " to "
				+ newVersion + "." + " This will remove all old data.");
		db.execSQL("DROP TABLE IF EXISTS " + FIND_TABLE_NAME);
		onCreate(db);
	}

	/**
	 * Called from Find.java. Used to insert a new Find object into the
	 * database.
	 * 
	 * @param values the <column, value> pairs to insert into the database
	 * @return the row ID of the new row; returns -1 if an error occurred
	 */
	public long addNewFind(ContentValues values)
	{
		mDb = getWritableDatabase();
		long result = mDb.insert(FIND_TABLE_NAME, null, values);
		mDb.close();
		Log.e(TAG, "add new find result: " + result);
		return result;
	}

	/**
	 * Deletes on specific find from the database.
	 * @param rowId the id of the find to be deleted
	 * @return true if deleted, false if not
	 */
	public boolean deleteFind(long rowId)
	{
		mDb = getWritableDatabase();
		deleteFindMedia(rowId);
		boolean result = mDb.delete(FIND_TABLE_NAME, KEY_ID + "=" + rowId, null) > 0;
		mDb.close();
		return result;
	}

	/**
	 * Deletes all finds in the database. 
	 * @return true if deleted, false if not
	 */
	public boolean deleteAllFinds()
	{
		mDb = getWritableDatabase();
		deleteAllMedia();
		boolean result = mDb.delete(FIND_TABLE_NAME, null, null) == 0;
		mDb.close();
		return result;
	}

	/**
	 * Deletes all media related to a specific find.
	 * @param rowId the row whose images are to be deleted
	 */
	public void deleteFindMedia(long rowId)
	{
		File file;
		ContentValues result = fetchFindColumns(rowId, LIST_MEDIA_DATA);

		for (String media : LIST_MEDIA_DATA)
		{
			if (result.get(media) != null)
			{
				file = new File(result.getAsString(media));
				file.delete();
			}
		}
	}

	/**
	 * Deletes all media relating to this program.
	 * @return
	 */
	public void deleteAllMedia()
	{
		for (String media : LIST_PATH_EXT)
		{
			File dir = new File(DEFAULT_FILE_PATH + media);
			File[] files = dir.listFiles();
			for (int i = Array.getLength(files); i > 0; i--)
			{
				files[i].delete();
			}
		}
	}

	/**
	 * Called from Find.java. Used to update a find's database data.
	 * @param rowId the row id of the find in the database
	 * @param values the values to update the database with
	 * @return true if updated, false if not
	 */
	public boolean updateFind(long rowId, ContentValues values)
	{
		mDb = getWritableDatabase();
		boolean result = false;
		if (values != null)
		{
			result = mDb.update(FIND_TABLE_NAME, values, KEY_ID + "=" + rowId, null) > 0;
		}
		mDb.close();
		return result;
	}

	/**
	 * Called from Find.java. Queries the database with the row id of the find,
	 * creating a ContentValues object of the find's information.
	 * @param id the id of the find
	 * @return ContentValues object containing the database information on the find
	 */
	public ContentValues fetchFindData(long id)
	{
		String[] columns = null;
		ContentValues values = fetchFindColumns(id, columns);
		return values;
	}

	/**
	 * Gets the columns of information related to a specified find.
	 * @param id the id of the find
	 * @param columns the columns whose values are to be returned
	 * @return the values from the columns specified by the columns parameter
	 */
	public ContentValues fetchFindColumns(long id, String[] columns)
	{
		mDb = getReadableDatabase();

		String[] selectionArgs = null;
		String groupBy = null;
		String having = null;
		String orderBy = null;
		Cursor cursor = mDb.query(FIND_TABLE_NAME, columns, KEY_ID + "=" + id,
				selectionArgs, groupBy, having, orderBy);
		cursor.moveToFirst();
		ContentValues values = null;
		if (cursor.getCount() != 0)
		{
			values = getValuesFromRow(cursor);
		}
		cursor.close();
		mDb.close();
		return values;
	}

	/**
	 * Fetches a cursor to rows of data for all finds.
	 * @return the cursor
	 */
	public Cursor fetchAllFinds()
	{
		mDb = getReadableDatabase();
		return mDb.query(FIND_TABLE_NAME, LIST_ROW_DATA, null, null, null, null, null);
	}

	/**
	 * Called from Find.java. Queries the database for a ContentValues hash
	 * table and returns it. Useful for sending information via HTTP.
	 * @param id the id of the find to query
	 * @return hashmap containing string-type values of the find's information
	 */
	public HashMap<String, String> fetchFindMap(long id)
	{
		String[] columns = null;
		String[] selectionArgs = null;
		String groupBy = null;
		String having = null;
		String orderBy = null;

		mDb = getReadableDatabase();
		Cursor cursor = mDb.query(FIND_TABLE_NAME, columns, KEY_ID + "=" + id,
				selectionArgs, groupBy, having, orderBy);
		cursor.moveToFirst();

		HashMap<String, String> findsMap = new HashMap<String, String>();
		if (cursor.getCount() != 0)
		{
			findsMap = Utils.getMapFromCursor(cursor);
		}

		cursor.close();
		mDb.close();
		return findsMap;
	}

	/**
	 * Fetches entire columns specified [all rows].
	 * 
	 * @param columns the columns to return
	 * @return a cursor to the columns requested
	 */
	public Cursor fetchSelectedColumns(String[] columns)
	{
		mDb = getReadableDatabase();
		return mDb.query(FIND_TABLE_NAME, columns, null, null, null, null, null);
	}

	/**
	 * Helper method; extracts the names and values of the columns in the row
	 * pointed to by the passed in cursor. It puts the data into a ContentValues
	 * object and returns it.
	 * @param c cursor specifying which information to work with
	 * @return the values from the row given by the cursor
	 */
	private ContentValues getValuesFromRow(Cursor c)
	{
		ContentValues values = new ContentValues();
		c.moveToFirst();

		for (String column : c.getColumnNames())
		{
			values.put(column, c.getString(c.getColumnIndexOrThrow(column)));
		}
		return values;
	}

	/**
	 * Creates a ContentValues object from a given hash map.
	 * @param map the map to retrieve values from
	 * @return a ContentValues object containing the values
	 */
	public static ContentValues getContentValuesFromMap(HashMap<String, Object> map)
	{
		Iterator iter = map.keySet().iterator();
		ContentValues cv = new ContentValues();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			Object value = map.get(key);

			if (value instanceof Integer)
			{
				cv.put(key, Integer.parseInt(value.toString()));
			} 
			else if (value instanceof Double)
			{
				cv.put(key, Double.parseDouble(value.toString()));
			}
			else
			{
				cv.put(key, value.toString());
			}
		}
		return cv;
	}

	/**
	 * Returns a list containing the row id's of any finds that have been updated
	 * since they were last sent to the server. Can return null.
	 * @return an integer list of the row id's of the finds
	 */
	public List<Integer> getUpdatedFinds()
	{
		List<Integer> idFinds = new ArrayList<Integer>();
		String selection = null;
		String[] selectionArgs = null;
		String groupBy = null;
		String having = null;
		String orderBy = null;
		String[] modTimeA;
		String[] sentTimeA;
		String modTimeS;
		String sentTimeS;
		String delims;
		double modTime;
		double sentTime;

		mDb = getWritableDatabase();

		String[] columns = { KEY_ID, KEY_MOD_TIME, KEY_SENT_TIME };

		Cursor c = mDb.query(FIND_TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy);
		c.moveToFirst();
		
		do {
			ContentValues result = getValuesFromRow(c);
			
			modTimeS = result.getAsString(columns[1]);
			sentTimeS = result.getAsString(columns[2]);
			
			delims = "[/.]+";
			
			modTimeA = modTimeS.split(delims);
			sentTimeA = sentTimeS.split(delims);
			
			modTime = Double.parseDouble(modTimeA[3]);
			sentTime = Double.parseDouble(sentTimeA[3]);
			
			if (modTime > sentTime)
			{
				idFinds.add(result.getAsInteger(columns[0]));
			}
		} while (c.moveToNext());

		return idFinds;
	}
}