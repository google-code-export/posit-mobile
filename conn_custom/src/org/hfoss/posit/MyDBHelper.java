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
	public static final String KEY_ID = "_id";
	public static final String PROJECT_ID = "projectId";
	public static final String KEY_NAME = "name";
	public static final String KEY_IDENTIFIER = "identifier";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_TIME = "time";
	public static final String KEY_SYNCED = "synced";
	public static final String KEY_SID = "sid";
	public static final String KEY_REVISION = "revision";
	public static final String MODIFY_TIME = "modify_time";
	public static final String KEY_FIND_ID = "findId";
	
	/**
	 * Table and Fields for the photos table
	 */
	public static final String PHOTO_TABLE_NAME = "photos";
	public static final String KEY_PHOTO_ID = "_id";
	public static final String KEY_IMAGE_URI = "imageUri";
	public static final String KEY_PHOTO_THUMBNAIL_URI = "imageThumbnailUri";
	public static final String PHOTO_IDENTIFIER = "photo_identifier";

	/**
	 * Table and Fields for the video table
	 */
	public static final String VIDEO_TABLE_NAME = "videos";
	public static final String KEY_VIDEO_ID = "_id";
	public static final String KEY_VIDEO_URI = "videoUri";
	public static final String KEY_VIDEO_THUMBNAIL_URI = "videoThumbnailUri";
	public static final String VIDEO_IDENTIFIER = "video_identifier";

	/**
	 * Table and Fields for the audio table
	 */
	public static final String AUDIO_TABLE_NAME = "audios";
	public static final String KEY_AUDIO_ID = "_id";
	public static final String KEY_AUDIO_URI = "audioUri";
	public static final String AUDIO_IDENTIFIER = "audio_identifier";

	public static final String[] list_row_data = { 
		KEY_ID,
		KEY_NAME,
		KEY_DESCRIPTION,
		KEY_LATITUDE,
		KEY_LONGITUDE,
		KEY_SYNCED
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
	 */
	private static final String CREATE_FINDS_TABLE = "CREATE TABLE "
		+ FIND_TABLE_NAME  
		+ " (" + KEY_ID + " integer primary key autoincrement, "
		+ KEY_NAME + " text, "
		+ KEY_DESCRIPTION + " text, "
		+ KEY_LATITUDE + " double, "
		+ KEY_LONGITUDE + " double, "
		+ KEY_IDENTIFIER + " text, " /* for barcodes*/
		+ KEY_TIME + " text, "
		+ KEY_SID + " integer, "
		+ KEY_SYNCED + " integer default 0, "
		+ KEY_REVISION + " integer default 1, "
		+ MODIFY_TIME + " text, "
		+ PROJECT_ID + " integer "
		+ ");";

	private static final String CREATE_IMAGES_TABLE = "CREATE TABLE "
		+ PHOTO_TABLE_NAME  
		+ " (" + KEY_ID + " integer primary key autoincrement, "  // User Key
		+ PHOTO_IDENTIFIER + " integer, "
		+ KEY_FIND_ID + " integer, "      // User Key
		+ KEY_IMAGE_URI + " text, "      // The image's URI
		+ KEY_PHOTO_THUMBNAIL_URI + " text "      // The thumbnail's URI
		+ ");";

	private static final String CREATE_VIDEO_TABLE = "CREATE TABLE "
		+ VIDEO_TABLE_NAME  
		+ " (" + KEY_ID + " integer primary key autoincrement, "  // User Key
		+ VIDEO_IDENTIFIER + " integer, "
		+ KEY_FIND_ID + " integer, "      // User Key
		+ KEY_VIDEO_URI + " text, "      // The video's URI
		+ KEY_VIDEO_THUMBNAIL_URI + " text "      // The thumbnail's URI
		+ ");";

	private static final String CREATE_AUDIO_TABLE = "CREATE TABLE "
		+ AUDIO_TABLE_NAME  
		+ " (" + KEY_ID + " integer primary key autoincrement, "  // User Key
		+ AUDIO_IDENTIFIER + " integer, "
		+ KEY_FIND_ID + " integer, "      // User Key
		+ KEY_AUDIO_URI + " text "      // The audio's URI
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
		db.execSQL(CREATE_VIDEO_TABLE);
		db.execSQL(CREATE_AUDIO_TABLE);
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
		db.execSQL("DROP TABLE IF EXISTS " + VIDEO_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + AUDIO_TABLE_NAME);
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
		Log.i(TAG, "tblName = " + FIND_TABLE_NAME);
		mDb.close();
		return result;
	}

	/**
	 * This method is called from a Find object to add a photo to DB.
	 * @param values  contains the <column_name,value> pairs for each DB field.
	 * @return
	 */  
	public long addNewPhoto(ContentValues values, long id) {
		mDb = getWritableDatabase(); // Either create or open DB
		values.put(KEY_FIND_ID,id);
		if (!values.containsKey(PHOTO_IDENTIFIER))
			values.put(PHOTO_IDENTIFIER, new Random().nextInt(999999999));
		long result = mDb.insert(PHOTO_TABLE_NAME, null, values);
		mDb.close();
		return result;
	}

	/**
	 * This method is called from a Find object to add a video to DB.
	 * @param values  contains the <column_name,value> pairs for each DB field.
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long addNewVideo(ContentValues values, long id) {
		mDb = getWritableDatabase(); //Either create or open DB
		values.put(KEY_FIND_ID, id);
		if (!values.containsKey(VIDEO_IDENTIFIER))
			values.put(VIDEO_IDENTIFIER, new Random().nextInt(999999999));
		long result = mDb.insert(VIDEO_TABLE_NAME, null, values);
		mDb.close();
		return result;
	}

	/**
	 * This method is called from a Find object to add an audio clip to DB.
	 * @param values contains the <column_name,value>  pairs for each DB field.
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long addNewAudio(ContentValues values, long id) {
		mDb = getWritableDatabase(); //Either create or open DB
		values.put(KEY_FIND_ID, id);
		if (!values.containsKey(AUDIO_IDENTIFIER))
			values.put(AUDIO_IDENTIFIER, new Random().nextInt(999999999));
		long result = mDb.insert(AUDIO_TABLE_NAME, null, values);
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

		Cursor images = mDb.query(PHOTO_TABLE_NAME, new String[]{ KEY_IMAGE_URI, KEY_PHOTO_THUMBNAIL_URI }, KEY_PHOTO_ID+"="+photoId, null,null,null,null);
		images.moveToFirst();
		Uri image = Uri.parse(images.getString(images.getColumnIndexOrThrow(KEY_IMAGE_URI)));
		Uri thumbnail = Uri.parse(images.getString(images.getColumnIndexOrThrow(KEY_PHOTO_THUMBNAIL_URI)));
		boolean result = mDb.delete(PHOTO_TABLE_NAME, KEY_ID+"="+photoId, null)>0;
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

	/**
	 * This method is called from a Find object, passing a video ID and deletes that video.
	 * @param videoId is the ID of the video to be deleted
	 * @return
	 */
	public boolean deleteVideo(long videoId) {
		mDb = getWritableDatabase();
		Cursor cursor = mDb.query(VIDEO_TABLE_NAME, new String[]{KEY_VIDEO_URI}, KEY_ID+"="+videoId, null, null, null, null);
		cursor.moveToFirst();
		String uriString = cursor.getString(0);
		Uri uri = Uri.parse(uriString);
		try {
			mContext.getContentResolver().delete(uri, null, null);
		}
		catch(UnsupportedOperationException e) { 
			Log.d(TAG, "Could not delete db entry: " + cursor.toString(), e);
		}
		boolean result = mDb.delete(VIDEO_TABLE_NAME, KEY_ID + "=" + videoId, null) > 0;
		mDb.close();
		cursor.close();
		return result;
	}

	/**
	 * This method is called from a Find object, passing an audio clip ID and deletes that audio clip.
	 * @param audioId is the ID of the audio clip to be deleted
	 * @return
	 */
	public boolean deleteAudio(long audioId) {
		mDb = getWritableDatabase();
		Cursor cursor = mDb.query(AUDIO_TABLE_NAME, new String[]{ KEY_AUDIO_URI }, KEY_ID + "=" + audioId, null, null, null, null);
		cursor.moveToFirst();
		String uriString = cursor.getString(0);
		Uri uri = Uri.parse(uriString);
		
		/*
		 * This try / catch is apparently not currently working with audio filesInstead, it is
		 * being handled by manual file deletion, below.
		 */
//		try {
//			mContext.getContentResolver().delete(uri, null, null);
//		}
//		catch(UnsupportedOperationException e) { 
//			Log.d(TAG, "Could not delete db entry: " + cursor.toString(), e);
//		}
		
		String filename = Utils.getFilenameFromMediaUri(mContext, uri);
		File file = new File(filename);
		file.delete();
		boolean result = mDb.delete(AUDIO_TABLE_NAME, KEY_ID + "=" + audioId, null) > 0;
		mDb.close();
		cursor.close();
		return result;
	}	
	
	public void deleteAllPhotos() {
		mDb = getWritableDatabase();
		Cursor images = mDb.query(PHOTO_TABLE_NAME, null, null, null,null,null,null);
		images.moveToFirst();
		while(images.moveToNext()) {
			Uri image = Uri.parse(images.getString(images.getColumnIndexOrThrow(KEY_IMAGE_URI)));
			mContext.getContentResolver().delete(image,null,null);
		}
		images.close();
		mDb.delete(PHOTO_TABLE_NAME, null, null);
		mDb.close();
	}
	
	public void deleteAllVideos() {
		mDb = getWritableDatabase();
		Cursor videos = mDb.query(VIDEO_TABLE_NAME, null, null, null, null, null, null);
		videos.moveToFirst();
		while(videos.moveToNext()) {
			Uri video = Uri.parse(videos.getString(videos.getColumnIndexOrThrow(KEY_VIDEO_URI)));
			mContext.getContentResolver().delete(video,null,null);
		}
		videos.close();
		mDb.delete(VIDEO_TABLE_NAME, null, null);
		mDb.close();
	}
	
	public void deleteAllAudioClips() {
		mDb = getWritableDatabase();
		Cursor audios = mDb.query(AUDIO_TABLE_NAME, null, null, null,null,null,null);
		audios.moveToFirst();
		while(audios.moveToNext()) {
			Uri audio = Uri.parse(audios.getString(audios.getColumnIndexOrThrow(KEY_AUDIO_URI)));
			mContext.getContentResolver().delete(audio,null,null);
		}
		audios.close();
		mDb.delete(AUDIO_TABLE_NAME, null, null);
		mDb.close();
	}

	/**
	 * Deletes a photo from the database, based on a specific find and position.
	 * @param findId the find whose photo is to be deleted
	 * @param position the specific position [row id, list position, etc] the photo is located at
	 * @return true if deleted, false if not
	 */
	public boolean deletePhotoByPosition(long findId, int position) {
		mDb = getWritableDatabase();
		Cursor images = mDb.query("photos", new String[]{ KEY_ID, KEY_IMAGE_URI, KEY_PHOTO_THUMBNAIL_URI },
				KEY_FIND_ID + "=" + findId, null, null, null, null);
		images.moveToPosition(position);
		long photoId = images.getLong(images.getColumnIndex(KEY_ID));
		images.close();
		mDb.close();
		return deletePhoto(photoId);
	}

	/**
	 * Deletes a video from the database, based on a specific find and position.
	 * @param findId the find whose video is to be deleted
	 * @param position the specific position [row id, list position, etc] the video is located at
	 * @return true if deleted, false if not
	 */
	public boolean deleteVideoByPosition(long findId, int position) {
		mDb = getWritableDatabase();
		Cursor videos = mDb.query(VIDEO_TABLE_NAME, new String[]{ KEY_ID, KEY_VIDEO_URI },
				KEY_FIND_ID + "=" + findId, null, null, null, null);
		videos.moveToPosition(position);
		long videoId = videos.getLong(videos.getColumnIndex(KEY_ID));
		videos.close();
		mDb.close();
		return deleteVideo(videoId);
	}

	/**
	 * Deletes an audio clip from the database, based on a specific find and position.
	 * @param findId the find whose audio clip is to be deleted
	 * @param position the specific position [row id, list position, etc] the audio clip is located at
	 * @return true if deleted, false if not
	 */
	public boolean deleteAudioByPosition(long findId, int position) {
		mDb = getWritableDatabase();
		Cursor audios = mDb.query(AUDIO_TABLE_NAME, new String[]{ KEY_ID, KEY_AUDIO_URI },
				KEY_FIND_ID + "=" + findId, null, null, null, null);
		audios.moveToPosition(position);
		long audioId = audios.getLong(audios.getColumnIndex(KEY_ID));
		audios.close();
		mDb.close();
		return deleteAudio(audioId);
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
		Log.i(TAG, "column count = " + cursor.getColumnCount());
		Log.i(TAG, "count = " + cursor.getCount());

		if (cursor.moveToPosition(position)) {
			String s = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URI));
			Log.i(TAG, "uri = " + s);
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
	 * Retrieves a video from the database, based on a specific find and position.
	 * @param findId the find whose video is to be retrieved
	 * @param position the specific position [row id, list position, etc] the video is located at
	 * @return Uri to the video
	 */
	public Uri getVideoUriByPosition(long findId, int position) {
		mDb = getWritableDatabase();
		Cursor cursor = getVideosCursor(findId);

		if (cursor.moveToPosition(position)) {
			String s = cursor.getString(cursor.getColumnIndex(KEY_VIDEO_URI));
			Log.i(TAG, "uri = " + s);
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
	 * Retrieves an audio clip from the database, based on a specific find and position.
	 * @param findId the find whose id is to be retrieved
	 * @param position the specific position [row id, list position, etc] the audio clip is located at
	 * @return Uri to the audio clip
	 */
	public Uri getAudioUriByPosition(long findId, int position) {
		mDb = getWritableDatabase();
		Cursor cursor = getAudiosCursor(findId);

		if (cursor.moveToPosition(position)) {
			String s = cursor.getString(cursor.getColumnIndex(KEY_AUDIO_URI));
			Log.i(TAG, "uri = " + s);
			mDb.close();
			cursor.close();
			if (s != null) {
				return Uri.parse(s);
			}
		}

		return null;
	}

	/**
	 * This method will update a Find's data in the DB.
	 * @param rowId  The Find's row identifier in the DB table.
	 * @param args   The values for each column of the table.
	 * @return
	 */
	public boolean updateFind(long rowId, ContentValues args) {
		mDb = getWritableDatabase();  // Either create or open the DB.
		boolean result = false;
		if (args != null) {
			result = mDb.update(FIND_TABLE_NAME, args, KEY_ID + "=" + rowId, null) > 0;
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
	public boolean deleteFind(long mRowId) {
		//deleteImages(mRowId);
		mDb = getWritableDatabase();
		boolean result = mDb.delete(FIND_TABLE_NAME, KEY_ID + "=" + mRowId, null)>0;
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
		deleteAllVideos();
		deleteAllAudioClips();
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
		String[] columns = {KEY_IMAGE_URI, PHOTO_IDENTIFIER};
		String[] selectionArgs = null;
		String groupBy = null, having = null, orderBy = null;
		Cursor cursor = mDb.query(PHOTO_TABLE_NAME, columns, KEY_FIND_ID + "=" + id, selectionArgs, groupBy, having, orderBy);
		return cursor;
	}

	public Cursor getVideosCursor(long id) {
		mDb = getReadableDatabase(); 
		String[] columns = { KEY_FIND_ID, KEY_VIDEO_ID, KEY_VIDEO_URI, VIDEO_IDENTIFIER };
		String[] selectionArgs = null;
		String groupBy = null, having = null, orderBy = null;
		Cursor cursor = mDb.query(VIDEO_TABLE_NAME, columns, KEY_FIND_ID + "=" + id, selectionArgs, groupBy, having, orderBy);
		return cursor;
	}

	public Cursor getAudiosCursor(long id) {
		mDb = getReadableDatabase(); 
		String[] columns = { KEY_FIND_ID, KEY_AUDIO_ID, KEY_AUDIO_URI, AUDIO_IDENTIFIER };
		String[] selectionArgs = null;
		String groupBy = null, having = null, orderBy = null;
		Cursor cursor = mDb.query(AUDIO_TABLE_NAME, columns, KEY_FIND_ID + "=" + id, selectionArgs, groupBy, having, orderBy);
		return cursor;
	}

	public ContentValues getImages(long id) {
		mDb = getReadableDatabase();
		Cursor cursor = getImagesCursor(id);
		cursor.moveToFirst();
		ContentValues values = new ContentValues();
		Log.i(TAG, "Images count = " + cursor.getCount() + " for _id = " + id);
		if (cursor.getCount() != 0)
			values = getValuesFromRow(cursor);
		cursor.close();
		mDb.close();
		return values;
	}

	public ContentValues getVideos(long id) {
		mDb = getReadableDatabase();
		Cursor cursor = getVideosCursor(id);
		cursor.moveToFirst();
		ContentValues values = new ContentValues();
		Log.i(TAG, "Videos count = " + cursor.getCount() + " for _id = " + id);
		if (cursor.getCount() != 0) {
			values = getValuesFromRow(cursor);
		}
		cursor.close();
		mDb.close();
		return values;
	}

	public ContentValues getAudios(long id) {
		mDb = getReadableDatabase();
		Cursor cursor = getAudiosCursor(id);
		cursor.moveToFirst();
		ContentValues values = new ContentValues();
		Log.i(TAG, "Audio clips count = " + cursor.getCount() + " for _id = " + id);
		if (cursor.getCount() != 0) {
			values = getValuesFromRow(cursor);
		}
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
				Log.i(TAG, "Column " + column + " = " + image);
				if (image != null)
					values.put(column, cursor.getString(cursor.getColumnIndexOrThrow(column)));
			}

		}
		cursor.close();
		mDb.close();
	}

	/**
	 * Adds videos Uris to existing ContentValues
	 * @param id  is the Key of the Find whose videos are sought
	 * @param values is an existing ContentValues with Find's <key, value> pairs
	 */
	public void getVideos(long id, ContentValues values) {
		Cursor cursor = getVideosCursor(id);
		cursor.moveToFirst();
		Log.i(TAG, "Videos count = " + cursor.getCount());
		if (cursor.getCount() != 0) {
			cursor.moveToFirst();
			for (String column : cursor.getColumnNames()) {
				String video = cursor.getString(cursor.getColumnIndexOrThrow(column));
				Log.i(TAG, "Column " + column + " = " + video);
				if (video != null) {
					values.put(column, cursor.getString(cursor.getColumnIndexOrThrow(column)));
				}
			}
		}
		cursor.close();
	}

	/**
	 * Adds audios Uris to existing ContentValues
	 * @param id  is the Key of the Find whose videos are sought
	 * @param values is an existing ContentValues with Find's <key, value> pairs
	 */
	public void getAudios(long id, ContentValues values) {
		Cursor cursor = getAudiosCursor(id);
		cursor.moveToFirst();
		Log.i(TAG, "Audios count = " + cursor.getCount());
		if (cursor.getCount() != 0) {
			cursor.moveToFirst();
			for (String column : cursor.getColumnNames()) {
				String audioClip = cursor.getString(cursor.getColumnIndexOrThrow(column));
				Log.i(TAG, "Column " + column + " = " + audioClip);
				if (audioClip != null) {
					values.put(column, cursor.getString(cursor.getColumnIndexOrThrow(column)));
				}
			}
		}
		cursor.close();
	}

	public ContentValues fetchFindColumns(long id, String[] columns) {
		mDb = getReadableDatabase();  // Either open or create the DB    	
		//String[] columns = mContext.getResources().getStringArray(R.array.TABLE_FINDS_core_fields);

		String[] selectionArgs = null;
		String groupBy = null, having = null, orderBy = null;
		Cursor cursor = mDb.query(FIND_TABLE_NAME, columns, KEY_ID+"="+id, selectionArgs, groupBy, having, orderBy);
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
		Cursor cursor = mDb.query(FIND_TABLE_NAME, columns, KEY_ID + "=" + id, selectionArgs, groupBy, having, orderBy);
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
			Log.i(TAG, "Column " + column + " = " + 
					cursor.getString(cursor.getColumnIndexOrThrow(column)));
			values.put(column, cursor.getString(cursor.getColumnIndexOrThrow(column)));
		}
		cursor.close();
		return values;
	}
	
	public int getIdBySID(int sid) {
		mDb = getReadableDatabase();
		String[] cols = {KEY_ID};
		Cursor c = mDb.query(FIND_TABLE_NAME, cols, KEY_SID + "="+sid,
				null, null, null, null);
		c.moveToFirst();
		int toReturn = c.getInt(c.getColumnIndexOrThrow(KEY_ID));
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
	public void setServerId(int tableId,long rowId, int server_id) {
		mDb = getReadableDatabase(); // Either open or create the DB
		ContentValues args = new ContentValues();
		args.put(KEY_SID, server_id);
		//if new items are being added from the phone, assume it as synced for obvious reasons
		args.put(KEY_SYNCED, true); 
		updateFind(rowId, args);
		mDb.close();
	}    

	/**
	 * Adds remote find.
	 * @return 
	 */
	public long addRemoteFind(HashMap<String,Object> findsMap) {
		mDb = getReadableDatabase();
		ContentValues args = getContentValuesFromMap(findsMap);
		int sid = (Integer)findsMap.get(KEY_SID);
		long rowId = findIdofRemoteFind(sid);
		int tableId = R.array.TABLE_FINDS;
		if (rowId ==0) { /* if there's no find with such server id */
			args.put(KEY_SYNCED, true);
			rowId = addNewFind(args);
		} else {
			updateFind(rowId, args);
		}
		mDb.close();
		return rowId;
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
			queryCondition += "( " + KEY_SID + "=" + find.get("id") + " AND "
			+ KEY_REVISION + ">" + find.get("revision") + ")";
			if (i != remoteFindsList.size() - 1) {
				queryCondition += " OR ";
			}
		}
		Log.i(TAG, "Querying for updated SIDs: " + queryCondition);

		try {
			mDb = getReadableDatabase();

			Cursor c = mDb.query(FIND_TABLE_NAME, new String[] { KEY_SID },
					queryCondition, null, null, null, null);
			if (c.getCount() != 0) {
				c.moveToFirst();
				do {
					idsList.add(c.getInt(c.getColumnIndexOrThrow(KEY_SID)));

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
			
			queryCondition += "( " + KEY_SID + "=" + find.get("id") + " AND "
			+ KEY_REVISION + "<" + find.get("revision") + ")";
			if (i != remoteFindsList.size() - 1) {
				queryCondition += " OR ";
			}
		}
		Log.i(TAG, "Quering for SIDs needing updating: " + queryCondition);

		try {
			mDb = getReadableDatabase();

			Cursor c = mDb.query(FIND_TABLE_NAME, new String[] { KEY_SID },
					queryCondition, null, null, null, null);
			if (c.getCount() != 0) {
				c.moveToFirst();
				do {
					idsList.add(c.getInt(c.getColumnIndexOrThrow(KEY_SID)));

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
		Cursor c = mDb.query(FIND_TABLE_NAME, new String[] { KEY_ID }, KEY_SID + "=" + l, null, null, null, null);
		c.moveToFirst();
		if ( c.getCount()== 0)
			return  0;
		else 
			return (c.getLong(c.getColumnIndexOrThrow(KEY_ID)));
	}

	public List<Integer> getAllPhoneSIDs () {
		List<Integer> allSIDs = new ArrayList<Integer>();
		try {
			mDb = getReadableDatabase();
			/* Get all the SIDs in our database in a list */
			Cursor c = fetchSelectedColumns(new String[] { KEY_SID });
			if (c.getCount() != 0) {
				c.moveToFirst();
				do {
					allSIDs.add(c.getInt(c.getColumnIndexOrThrow(KEY_SID)));
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
	public boolean deleteImages (Cursor imagesQuery) {
		boolean result =true;
		while (imagesQuery.moveToNext()) {
			long id =imagesQuery.getLong(imagesQuery.getColumnIndexOrThrow(BaseColumns._ID));
			Log.i(TAG, ""+id);
			Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id+"");
			int r = mContext.getContentResolver().delete(uri, null, null);
			result = result && (r>0);
			imagesQuery.close();
		}
		return result; //returns true if all the images are deleted
	}

	/**
	 * This method deletes all the videos associated with a find.
	 * @param videosQuery
	 * @return
	 */
	public boolean deleteVideos(Cursor videosQuery) {
		mDb = getWritableDatabase();
		boolean result = true;
		if (videosQuery.getCount() == 0) //If there is no videos in the list then don't delete anything and return true
			return result;
		long id = videosQuery.getLong(videosQuery.getColumnIndexOrThrow(KEY_FIND_ID));
		videosQuery.moveToFirst();
		boolean isLast = videosQuery.isLast(); 
		while (!isLast) {
			String uriString = videosQuery.getString(videosQuery.getColumnIndexOrThrow(KEY_VIDEO_URI));
			Uri uri = Uri.parse(uriString);
			try {
				mContext.getContentResolver().delete(uri, null, null);
			}
			catch(UnsupportedOperationException e) { 
				Log.d(TAG, "Could not delete db entry: " + videosQuery.toString(), e);
			}
			
			isLast = videosQuery.isLast();
			if (!isLast)
				videosQuery.moveToNext();
		}
		int r = mDb.delete(VIDEO_TABLE_NAME, KEY_FIND_ID + " = " + id, null);
		mDb.close();
		videosQuery.close();
		result = result && (r > 0);
		return result; //returns true if all the videos are deleted
	}

	/**
	 * This method deletes all the audio clips associated with a find.
	 * @param audiosQuery
	 * @return
	 */
	public boolean deleteAudioClips(Cursor audiosQuery) {
		mDb = getWritableDatabase();
		boolean result = true;
		if (audiosQuery.getCount() == 0) //If there is no videos in the list then don't delete anything and return true
			return result;
		long id = audiosQuery.getLong(audiosQuery.getColumnIndexOrThrow(KEY_FIND_ID));
		audiosQuery.moveToFirst();
		boolean isLast = audiosQuery.isLast(); 
		while (!isLast) {
			String uriString = audiosQuery.getString(audiosQuery.getColumnIndexOrThrow(KEY_AUDIO_URI));
			Uri uri = Uri.parse(uriString);
			try {
				mContext.getContentResolver().delete(uri, null, null);
			}
			catch(UnsupportedOperationException e) { 
				Log.d(TAG, "Could not delete db entry: " + audiosQuery.toString(), e);
			}
			
			isLast = audiosQuery.isLast();
			if (!isLast)
				audiosQuery.moveToNext();
		}
		int r = mDb.delete(AUDIO_TABLE_NAME, KEY_FIND_ID + " = " + id, null);
		mDb.close();
		audiosQuery.close();
		result = result && (r > 0);
		return result; //returns true if all the videos are deleted
	}

	/**
	 * This method gets all new SIDs (server IDs).  These are the ones that
	 *  are set to 0 initially. 
	 * @return
	 */
	public List<Integer> getAllNewIds(){
		String[] projection = new String[] { KEY_ID };
		List<Integer> findsList = new ArrayList<Integer>();
		try {
			mDb = getReadableDatabase();
			Cursor c = mDb.query(FIND_TABLE_NAME, projection, KEY_SID + "=0",
					null, null, null, null);
			//			Cursor c = mDb.query(TABLE_NAME, projection, null,
			//					null, null, null, null);			
			if (c.getCount() > 0) {
				c.moveToFirst();
				do {
					findsList.add(c.getInt(c.getColumnIndexOrThrow(KEY_ID)));
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
		Cursor cursor = mDb.query("finds", null, KEY_IDENTIFIER + "=" + value, null, null, null, null);
		cursor.getCount();
		mDb.close();
		return cursor;
	}
	
	public boolean containsImage(int imageId) {
		mDb = getReadableDatabase();
		Cursor c = mDb.query(PHOTO_TABLE_NAME, null, PHOTO_IDENTIFIER +"="+imageId, null, null, null, null);
		int count = c.getCount();
		c.close();
		mDb.close();
		if (count != 0)
			return true;
		else return false;
	}
	public boolean containsVideo(int videoId) {
		mDb = getReadableDatabase();
		Cursor c = mDb.query(VIDEO_TABLE_NAME, null, VIDEO_IDENTIFIER + "=" + videoId, null, null, null, null);
		int count = c.getCount();
		mDb.close();
		if (count != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean containsAudio(int audioId) {
		mDb = getReadableDatabase();
		Cursor c = mDb.query(AUDIO_TABLE_NAME, null, AUDIO_IDENTIFIER + "=" + audioId, null, null, null, null);
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
		Cursor c = mDb.query(FIND_TABLE_NAME, null, KEY_ID + "=" + rowId, null, null, null, null);
		if ( c.getCount()== 0)
			return  0;
		else {
			c.moveToFirst();
			return (c.getLong(c.getColumnIndexOrThrow(KEY_IDENTIFIER)));
		}
	}
	
}