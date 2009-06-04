

package org.hfoss.posit;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * @author rmorelli
 *
 */
public class Find {
	private static final String TAG = "Find";

	private MyDBHelper mDbHelper;  // Handles all the DB actions
	private Context mContext;    // The Activity, needed for DB access
	private long mId;  	         // The Find's rowID (should be changed to DB ID)
	private Cursor images=null;
	private Uri mImageUri;
	private Uri mThumbnailUri;
	


	/**
	 * This constructor is used for a new Find
	 * @param context is the Activity
	 */
	public Find (Context context) {
		mContext = context;
		mDbHelper = new MyDBHelper(context);
//		mDbHelper = new NotesDbAdapter(context);

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
	 * @return the mImageUri
	 */
	public Uri getImageUri() {
		return mImageUri;
	}

	/**
	 * @param imageUri the mImageUri to set
	 */
	public void setImageUri(Uri imageUri) {
		mImageUri = imageUri;
	}

	/**
	 * @return the mBitmapUri
	 */
	public Uri getThumbnailUri() {
		return mThumbnailUri;
	}

	/**
	 * @param bitmapUri the mBitmapUri to set
	 */
	public void setThumbnailUri(Uri bitmapUri) {
		mThumbnailUri = bitmapUri;
	}
	
	/**
	 * getContent() returns the Find's <attr:value> pairs in a ContentValues array. 
	 *   This method assumes the Find object has been instantiated with a context
	 *   and an id.
	 * @return A ContentValues with <key, value> pairs
	 */
	public ContentValues getContent() {
		ContentValues values = mDbHelper.fetchFindData(mId);
		getImageUrisFromDb(values);
		return values;
	}
	
	private void getImageUrisFromDb(ContentValues values) {
		mDbHelper.getImages(mId, values);
	}
	
	/**
	 * Returns a hashmap of string key and value
	 * this is used primarily for sending and receiving over Http, which is why it 
	 * somewhat works as everything is sent as string eventually. 
	 * @return
	 */
	public HashMap<String,String> getContentMap(){
		return mDbHelper.fetchFindMap(mId);
	}
	
	/** 
	 * insertToDB() assumes that the context has be passed through a constructor.
	 * @param content contains the Find's attributes and values.  
	 * @return whether the DB operation succeeds
	 */
	public boolean insertToDB(ContentValues content, ContentValues images) {
		if (!content.containsKey(MyDBHelper.KEY_SID)) {
			content.put(MyDBHelper.KEY_SID, 0);
			Log.i(TAG, "Set KEY_SID to 0");

		}
		if (!content.containsKey(MyDBHelper.KEY_REVISION))
			content.put(MyDBHelper.KEY_REVISION, 1);
	
		mId = mDbHelper.addNewFind(content); // returns rowId
		if (images != null)
			mDbHelper.addNewPhoto(images, mId);
		return mId != -1;
	}
	
	/** 
	 * updateDB() assumes that the context and rowID has be passed through a constructor.
	 * @param content contains the Find's attributes and values.  
	 * @return whether the DB operation succeeds
	 */
	public boolean updateToDB(ContentValues content, ContentValues images) {
		if (isSynced()) { //TODO think of a better way to do this
			//set it as unsynced
			content.put(MyDBHelper.KEY_SYNCED, false);
			//add revision only once. Don't count revisions for in-phone updates.
			content.put(MyDBHelper.KEY_REVISION, getRevision()+1);
		}
		boolean result = mDbHelper.updateFind(mId, content);
		mDbHelper.addNewPhoto(images, mId);
		return result;
//		return false;
	}	
	
	/**
	 * deletes the Find object form the DB
	 * @return whether the DB operation was successful
	 */
	public boolean delete() {
//		if (true) {
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
		return mDbHelper.getImagesCursor(this.mId);
	}
	
	private boolean deleteImages() {
		if (images!=null) {
//			return false;
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
		content.put(MyDBHelper.KEY_SID, serverId);
		updateToDB(content, null);
	}
	
	public void setRevision(int revision){
		ContentValues content = new ContentValues();
		content.put(MyDBHelper.KEY_REVISION, revision);
		updateToDB(content, null);
	}
	
	public void setSyncStatus(boolean status){
		ContentValues content = new ContentValues();
		content.put(MyDBHelper.KEY_SYNCED, status);
		updateToDB(content, null);
	}
	
	public int getRevision() {
		ContentValues value = mDbHelper.fetchFindColumns(mId, new String[] { MyDBHelper.KEY_REVISION});
		return value.getAsInteger(MyDBHelper.KEY_REVISION);
	}
	
	public boolean isSynced() {
		ContentValues value = mDbHelper.fetchFindColumns(mId, new String[] { MyDBHelper.KEY_SYNCED});
		return value.getAsInteger(MyDBHelper.KEY_SYNCED)==1;
	}
}
