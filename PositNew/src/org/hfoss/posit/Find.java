package org.hfoss.posit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;

public class Find {
	private static final String TAG = "Find";
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
	 * @param id is the Find's rowID (should be changed to its DB ID) ??
	 */
	public Find (Context context, long id) {
		this(context);
		mId = id;
	}
	
	/**
	 * getContent() returns the Find's <attr:value> pairs in a ContentValues array. 
	 *   This method assumes the Find object has been instantiated with a context
	 *   and an id.
	 * @return
	 */
	public ContentValues getContent() {
		return mDbHelper.fetchFindData(mId);
	}
	/**
	 * Returns a hashmap of string key and value
	 * this is used primarily for sending and receiving over Http, which is why it 
	 * somewhat works as everything is sent as string eventually. 
	 * @return
	 */
	public HashMap<String,String> getContentMap(){
		HashMap<String, String> findMap = new HashMap<String, String>();
		try {
			 ContentValues cv = getContent();
			 
			 Iterator<Entry<String,Object>> iter = cv.valueSet().iterator();
			 while (iter.hasNext()) {
				 Entry<String,Object> entry = iter.next();
				 String key = entry.getKey();
				 String value = entry.getValue().toString().equals(null)?"":entry.getValue().toString();		 
				 findMap.put(entry.getKey(), value);
				 Log.i(TAG, entry.toString());
			 }
		} catch (Exception e) {
			Log.e(TAG, e.getMessage()+"");
		}
		return findMap;
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
	public boolean updateToDB(ContentValues content) {
		boolean result = mDbHelper.updateFind(mId, content);
		return result;
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
	
	public void setServerId(int serverId){
		ContentValues content = new ContentValues();
		content.put(DBHelper.KEY_SID, serverId);
		updateToDB(content);
	}
	
	public void setRevision(int revision){
		ContentValues content = new ContentValues();
		content.put(DBHelper.KEY_REVISION, revision);
		updateToDB(content);
	}
	
	public void setSyncStatus(boolean status){
		ContentValues content = new ContentValues();
		content.put(DBHelper.KEY_SYNCED, status);
		updateToDB(content);
	}
}
