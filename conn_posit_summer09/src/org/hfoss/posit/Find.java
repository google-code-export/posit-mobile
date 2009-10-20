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
 *     Qianqian Lin - Summer 2009 Intern 
 ******************************************************************************/
package org.hfoss.posit;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * Represents a specific find for a project, with a unique identifier
 * 
 * @author Prasanna Gautam
 * @author Ralph Morelli
 * @author Qianqian Lin
 */
public class Find {
	private static final String TAG = "Find";

	private MyDBHelper mDbHelper;  // Handles all the DB actions
	private long mId;  	         // The Find's rowID (should be changed to DB ID)
	private Cursor images = null;
	private Cursor videos = null;
	private Cursor audioClips = null;
	private Uri mImageUri;
	private Uri mImageThumbnailUri;
	private Uri mVideoUri;
	private Uri mVideoThumbnailUri;
	private Uri mAudioUri;
	private Context mContext;


	/**
	 * This constructor is used for a new Find
	 * @param context is the Activity
	 */
	public Find (Context context) {
		mDbHelper = new MyDBHelper(context);
		mContext = context;
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
		videos = getVideos();
		audioClips = getAudioClips();
		mContext = context;
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
	 * @return the mVideoUri
	 */
	public Uri getVideoUri() {
		return mVideoUri;
	}

	/**
	 * @param videoUri the mVideoUri to set
	 */
	public void setVideoUri(Uri videoUri) {
		mVideoUri = videoUri;
	}
	
	/**
	 * @return the mAudioUri
	 */
	public Uri getAudioUri() {
		return mAudioUri;
	}
	
	/**
	 * @param audioUri the mAudioUri to set
	 */
	public void setAudioUri(Uri audioUri) {
		mAudioUri = audioUri;
	}

	/**
	 * @return the mBitmapUri
	 */
	public Uri getImageThumbnailUri() {
		return mImageThumbnailUri;
	}

	/**
	 * @param bitmapUri the mBitmapUri to set
	 */
	public void setImageThumbnailUri(Uri bitmapUri) {
		mImageThumbnailUri = bitmapUri;
	}
	
	/**
	 * @return the mBitmapUri
	 */
	public Uri getVideoThumbnailUri() {
		return mImageThumbnailUri;
	}
	
	/**
	 * @param bitmapUri the mBitmapUri to set
	 */
	public void setVideoThumbnailUri(Uri bitmapUri) {
		mVideoThumbnailUri = bitmapUri;
	}
	
	/**
	 * getContent() returns the Find's <attr:value> pairs in a ContentValues array. 
	 *   This method assumes the Find object has been instantiated with a context
	 *   and an id.
	 * @return A ContentValues with <key, value> pairs
	 */
	public ContentValues getContent() {
		ContentValues values = mDbHelper.fetchFindData(mId);
		getMediaUrisFromDb(values);
		return values;
	}
	
	private void getMediaUrisFromDb(ContentValues values) {
		mDbHelper.getImages(mId, values);
		mDbHelper.getVideos(mId, values);
		mDbHelper.getAudios(mId, values);
	}
	
	public Uri getImageUriByPosition(long findId, int position) {
		return mDbHelper.getPhotoUriByPosition(findId, position);
	}
	
	public Uri getVideoUriByPosition(long findId, int position) {
		return mDbHelper.getVideoUriByPosition(findId, position);
	}
	
	public Uri getAudioUriByPosition(long findId, int position) {
		return mDbHelper.getAudioUriByPosition(findId, position);
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
	public boolean insertToDB(ContentValues content, List<ContentValues> images, List<ContentValues> videos, List<ContentValues> audioClips) {
		if (content != null) {
			if (!content.containsKey(MyDBHelper.KEY_SID)) {
				content.put(MyDBHelper.KEY_SID, 0);
				Log.i(TAG, "Set KEY_SID to 0");
			}
			if (!content.containsKey(MyDBHelper.KEY_REVISION))
				content.put(MyDBHelper.KEY_REVISION, 1);
		
			mId = mDbHelper.addNewFind(content); // returns rowId
		}
		if (images != null && images.size() > 0) {
			ListIterator<ContentValues> it = images.listIterator();
			while (it.hasNext()) {
				ContentValues imageValues = it.next();
				mDbHelper.addNewPhoto(imageValues, mId);
			}
		}
		if (videos != null && videos.size() > 0) {
			ListIterator<ContentValues> it = videos.listIterator();
			while (it.hasNext()) {
				ContentValues videoValues = it.next();
				mDbHelper.addNewVideo(videoValues, mId);
			}
		}
		if (audioClips != null && audioClips.size() > 0) {
			ListIterator<ContentValues> it = audioClips.listIterator();
			while (it.hasNext()) {
				ContentValues audioValues = it.next();
				mDbHelper.addNewAudio(audioValues, mId);
			}
		}
		return mId != -1;
	}
	
	/** 
	 * updateDB() assumes that the context and rowID has be passed through a constructor.
	 * @param content contains the Find's attributes and values.  
	 * @return whether the DB operation succeeds
	 */
	public boolean updateToDB(ContentValues content) {//, ContentValues images) {
		if (isSynced()) { //TODO think of a better way to do this
			//set it as unsynced
			content.put(MyDBHelper.KEY_SYNCED, false);
			//add revision only once. Don't count revisions for in-phone updates.
			content.put(MyDBHelper.KEY_REVISION, getRevision()+1);
		}
		return mDbHelper.updateFind(mId, content);
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
	
	/**
	 * Get all images attached to this find
	 * @return the cursor that points to the images
	 */
	public Cursor getImages() {
		return mDbHelper.getImagesCursor(this.mId);
	}
	
	/**
	 * Get all videos attached to this find
	 * @return the cursor that points to the videos
	 */
	public Cursor getVideos() {
		return mDbHelper.getVideosCursor(this.mId);
	}
	
	/**
	 * Get all audio clips attached to this find
	 * @return the cursor that points to the audio clips
	 */
	public Cursor getAudioClips() {
		return mDbHelper.getVideosCursor(this.mId);
	}
	
	/**
	 * Delete all images from this find
	 * @return whether or not the images have been deleted
	 */
	private boolean deleteImages() {
		if (images!=null) {
			return mDbHelper.deleteImages(images);
		}else {
			return true;
		}
	}
	
	/**
	 * Delete all videos from this find
	 * @return whether or not the images have been deleted
	 */
	private boolean deleteVideos() {
		if (videos != null) {
			return mDbHelper.deleteVideos(videos);
		} else {
			return true;
		}
	}

	/**
	 * Delete all audio clips from this find
	 * @return whether or not the images have been deleted
	 */
	private boolean deleteAudioClips() {
		if (audioClips != null) {
			return mDbHelper.deleteAudioClips(audioClips);
		} else {
			return true;
		}
	}
	
	/**
	 * @return whether or not there are images attached to this find
	 */
	public boolean hasImages(){
		return getImages().getCount()>0;
	}
	
	/**
	 * @return whether or not there are videos attached to this find
	 */
	public boolean hasVideos() {
		return getVideos().getCount() > 0;
	}
	
	/**
	 * @return whether or not there are audio clips attached to this find
	 */
	public boolean hasAudioClips() {
		return getAudioClips().getCount() > 0;
	}
	
	/**
	 * Delete a particular image from this find
	 * @param id the id of the photo to be deleted
	 * @return whether the photo has been successfully deleted or not
	 */
	public boolean deleteImage(long id) {
		return mDbHelper.deletePhoto(id);
	}
	
	/**
	 * Delete a particular video from this find
	 * @param id the id of the video to be deleted
	 * @return whether or not the video has been successfully deleted
	 */
	public boolean deleteVideo(long id) {
		return mDbHelper.deleteVideo(id);
	}
	
	/**
	 * Delete a particular audio clip from this find
	 * @param id the id of the audio clip to be deleted
	 * @return whether or not the audio clip has been successfully deleted
	 */
	public boolean deleteAudioClip(long id) {
		return mDbHelper.deleteAudio(id);
	}
	
	public boolean deleteImageByPosition(int position) {
		return mDbHelper.deletePhotoByPosition(mId, position);
	}
	
	public boolean deleteVideoByPosition(int position) {
		return mDbHelper.deleteVideoByPosition(mId, position);
	}
	
	public boolean deleteAudioClipByPosition(int position) {
		return mDbHelper.deleteAudioByPosition(mId, position);
	}
	
	public void setServerId(int serverId){
		ContentValues content = new ContentValues();
		content.put(MyDBHelper.KEY_SID, serverId);
		updateToDB(content);
	}
	
	public void setRevision(int revision){
		ContentValues content = new ContentValues();
		content.put(MyDBHelper.KEY_REVISION, revision);
		updateToDB(content);
	}
	
	public void setSyncStatus(boolean status){
		ContentValues content = new ContentValues();
		content.put(MyDBHelper.KEY_SYNCED, status);
		updateToDB(content);
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