package org.hfoss.posit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;

public class Find {
	private MyDBHelper mDbHelper;  // Handles all the DB actions
	private Context mContext;    // The Activity, needed for DB access
	private long mId;  	         // The Find's rowID (should be changed to DB ID)
	private Cursor images=null;
	
	/**
	 * This constructor is used for a new Find
	 * @param context is the Activity
	 */
	public Find (Context context) {
		mContext = context;
		mDbHelper = new MyDBHelper(context);
	}
	
	/**
	 * This constructor is used for an existing Find.
	 * @param context is the Activity
	 * @param id is the Find's rowID (should be changed to its DB ID)
	 */
	public Find (Context context, long id) {
		this(context);
		mId = id;
		images = getImages();
	}
	
	/**
	 * getContent() returns the Find's <attr:value> pairs in a ContentValues array. 
	 *   This method assumes the Find object has been instantiated with a context
	 *   and an id.
	 * @return
	 */
	public ContentValues getContent() {
		Cursor cursor = mDbHelper.fetchFind(mId);
		ContentValues values = new ContentValues();
		values = mDbHelper.getValuesFromRow(cursor);
		return values;
	}
	
	
	
	/** 
	 * insertToDB() assumes that the context has be passed through a constructor.
	 * @param content contains the Find's attributes and values.  
	 * @return whether the DB operation succeeds
	 */
	public boolean insertToDB(ContentValues content) {
		mId = mDbHelper.addNewFind(content);
		return mId != -1;
	}
	
	/** 
	 * updateDB() assumes that the context and rowID has be passed through a constructor.
	 * @param content contains the Find's attributes and values.  
	 * @return whether the DB operation succeeds
	 */
	public boolean updateDB(ContentValues content) {
		long result = mDbHelper.addNewFind(content);
		return result != -1;
	}	
	
	/**
	 * deletes the Find object form the DB
	 * @return whether the DB operation was successful
	 */
	public boolean delete() {
		if (mDbHelper.deleteFind(mId)) {
			return deleteImages();
		}else 
			return false;
		
	}

	/**
	 * @return the mId
	 */
	public long getId() {
		return mId;
	}
	
	public Cursor getImages() {
		Cursor imageQuery = mContext.getContentResolver().query (MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        		new String[]{ BaseColumns._ID, ImageColumns.BUCKET_ID},
        		ImageColumns.BUCKET_ID+"=\"posit\" AND "
        		+ImageColumns.BUCKET_DISPLAY_NAME+"=\"posit|"+mId+"\"", null,null);
		return imageQuery;
	}
	
	private boolean deleteImages() {
		if (images!=null) {
			return mDbHelper.deleteImages(images);
		}else {
			return true;
		}
	}
	
	public boolean hasImages(){
		return getImages().getCount()>0;
	}
}
