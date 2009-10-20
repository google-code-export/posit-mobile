package org.hfoss.posit.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.hfoss.posit.Find;
import org.hfoss.posit.MyDBHelper;
import org.hfoss.third.Base64Coder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Images.Media;
import android.util.Log;

/**
 * Handles Syncing
 * 
 * @author Prasanna Gautam
 * @author Chris Fei
 * @author Qianqian Lin
 *
 */
public class SyncThread extends Thread {

	private static final String TAG = "SyncThread";
	public static final int DONE = 0;
	private static final int THUMBNAIL_TARGET_SIZE = 320;
	private Handler mHandler;
	private Context mContext;
	private List<HashMap<String, Object>> mRemoteFindsList;
	private List<Uri> mNewImageUris = new LinkedList<Uri>();
	private List<Uri> mNewImageThumbnailUris = new LinkedList<Uri>();
	private List<Uri> mNewVideoUris = new LinkedList<Uri>();
	private List<Uri> mNewAudioUris = new LinkedList<Uri>();

	public SyncThread(Context context, Handler handler) {
		mHandler = handler;
		mContext = context;
	}

	public void run() {
		try {
			Communicator comm = new Communicator(mContext);
			mRemoteFindsList =  comm.getAllRemoteFinds();  // Get all server SIDs

			getNewFindsFromServer(comm, mRemoteFindsList);
			putNewFindsToServer(comm, mRemoteFindsList);

			//getUpdatedFindsFromServer(comm, mRemoteFindsList);
			//putUpdatedFindsToServer(comm, mRemoteFindsList);
		}catch (NullPointerException e) {
			Log.e(TAG, e.getMessage()+"");
		}
		mHandler.sendEmptyMessage(DONE);
	}

	/**
	 * This method retrieves all finds from the server that aren't already on the phone.
	 * @param comm
	 */
	private void getNewFindsFromServer(Communicator comm, List<HashMap<String, Object>> remoteFindsList) {
		Log.i(TAG, "Remote Finds (SIDs) From Server: " + remoteFindsList.toString());
		Log.i(TAG, "Remote Finds (SIDs) From Server Size: " + remoteFindsList.size());
		MyDBHelper mDBHelper = new MyDBHelper(mContext);  		// Get all phone SIDs
		List<Integer> phoneSIDs = mDBHelper.getAllPhoneSIDs();
		List<Integer> phoneUpdateSIDs = mDBHelper.getFindsNeedingUpdate(remoteFindsList);
		Log.i(TAG, "Phone SIDs: " + phoneSIDs.toString());
		Log.i(TAG, "# to be updated = " + phoneUpdateSIDs.size());
		Log.i(TAG, "Updated Phone SIDs: " + phoneUpdateSIDs.toString());

		// Retrieve each new find from the server
		for (int i = 0; i < remoteFindsList.size(); i++) {
			HashMap<String, Object> find = remoteFindsList.get(i);
			//Integer id = new ResponseParser(find.get(i));
			Integer Id = Integer.parseInt(find.get("id").toString());
			Log.i(TAG, "getNewFindsFromServer()" + Id);
			if (!phoneSIDs.contains(Id)) {
				int id = Id.intValue();
				Log.i(TAG, "GOT HERE!!! getNewFindsFromServer()");
				Communicator.cleanupOnReceive(find);
				ContentValues args = MyDBHelper.getContentValuesFromMap(find);
				args.remove(MyDBHelper.COLUMN_IMAGE_URI);
				args.remove(MyDBHelper.COLUMN_VIDEO_URI);
				args.remove(MyDBHelper.COLUMN_AUDIO_URI);
				
				Log.d(TAG, "Find: " + find.toString());

				//get all images associated to the find
				ArrayList<HashMap<String, Object>> images = null;
				try{
				images = (ArrayList<HashMap<String, Object>>) find.get(MyDBHelper.COLUMN_IMAGE_URI);
				}
				catch(Exception e) {Log.e(TAG,"caught exception?");}
				List<ContentValues> allImageValues = new LinkedList<ContentValues>();
				List<Integer> imageIdentifiers = new LinkedList<Integer>();

				if (images != null) {
					Log.i(TAG, "images not null!!");
					List<Bitmap> bitmaps = new LinkedList<Bitmap>();
					int imageId;
					for (int j = 0; j < images.size(); j++) {
						HashMap<String, Object> image = images.get(j);
						try {
							imageId = Integer.parseInt((String) image.get("id"));
							if (!mDBHelper.containsImage(imageId)) {
								imageIdentifiers.add(imageId);
								String fullData = (String) image.get("data_full");
								Log.i("The IMAGE DATA", fullData);
								byte[] data = Base64Coder.decode(fullData);
								Bitmap imageBM = BitmapFactory.decodeByteArray(data, 0, data.length);
								Log.i("The Bitmap To Save", imageBM.toString());
								bitmaps.add(imageBM);
								Log.i(TAG, "bitmap saved!");	
							}
						}
						catch (Exception e){
							Log.d(TAG, ""+e);
						}
					}
					saveImagesAndUris(bitmaps);
					allImageValues= retrieveImagesFromUris();

					Iterator<Integer> it = imageIdentifiers.iterator();
					for (ContentValues value: allImageValues) {
						value.put(MyDBHelper.COLUMN_PHOTO_IDENTIFIER, it.next());
						value.put(MyDBHelper.COLUMN_FIND_ID, id);
					}

					mNewImageUris = new LinkedList<Uri>();
					mNewImageThumbnailUris = new LinkedList<Uri>();
				}

				ArrayList<HashMap<String, Object>> videos = 
					(ArrayList<HashMap<String, Object>>) find.get(MyDBHelper.COLUMN_VIDEO_URI);
				List<ContentValues> allVideoValues = new LinkedList<ContentValues>();
				List<Integer> videoIdentifiers = new LinkedList<Integer>();
				
				if (videos != null) {
					Log.i(TAG, "videos not null");
					List<byte[]> files = new LinkedList<byte[]>();

					for (int j = 0; j < videos.size(); j++) {
						HashMap<String, Object> video = videos.get(j);
						int videoId;
						try {
							videoId = Integer.parseInt((String) video.get("id"));
							if (!mDBHelper.containsVideo(videoId)) {
								videoIdentifiers.add(videoId);
								String fullData = (String) video.get("data_full");
								byte[] data = Base64Coder.decode(fullData);
								files.add(data);
								Log.i(TAG, "video base 64 code received!");
								Log.d(TAG, "video base64: " + fullData);
								Log.d(TAG, "video byte data: " + data);
							}
						}
						catch (Exception e){
							Log.d(TAG, ""+e);
						}
					}
					saveVideosAndUris(files);
					allVideoValues = retrieveVideosFromUris();

					Iterator<Integer> it = videoIdentifiers.iterator();
					for (ContentValues value : allVideoValues) {
						value.put(MyDBHelper.COLUMN_VIDEO_IDENTIFIER, it.next());
						value.put(MyDBHelper.COLUMN_FIND_ID, id);
					}

					mNewVideoUris = new LinkedList<Uri>();
				}
				

				ArrayList<HashMap<String, Object>> audioClips = 
					(ArrayList<HashMap<String, Object>>) find.get(MyDBHelper.COLUMN_AUDIO_URI);
				List<ContentValues> allAudioValues = new LinkedList<ContentValues>();
				List<Integer> audioIdentifiers = new LinkedList<Integer>();

				if (audioClips.size() > 0) {
					Log.i(TAG, "audios not null");
					List<byte[]> files = new LinkedList<byte[]>();

					for (int j = 0; j < videos.size(); j++) {
						HashMap<String, Object> audio = audioClips.get(j);
						int audioId;
						try{
							audioId = Integer.parseInt((String) audio.get("id"));

							if (!mDBHelper.containsAudio(audioId)) {
								audioIdentifiers.add(audioId);
								String fullData = (String) audio.get("data_full");
								byte[] data = Base64Coder.decode(fullData);
								files.add(data);
								Log.i(TAG, "audio base 64 code received!");
								Log.d(TAG, "audio base64: " + fullData);
								Log.d(TAG, "audio byte data: " + data);
							}
						}
						catch (Exception e){
							Log.d(TAG, ""+e);
						}
					}
					saveAudioClipsAndUris(files);
					allAudioValues = retrieveAudioClipsFromUris();

					Iterator<Integer> it = audioIdentifiers.iterator();
					for (ContentValues value : allAudioValues) {
						value.put(MyDBHelper.COLUMN_AUDIO_IDENTIFIER, it.next());
						value.put(MyDBHelper.COLUMN_FIND_ID, id);

					}

					mNewAudioUris = new LinkedList<Uri>();
				}
				
				if (!args.equals(null)) {
					Find newFind = new Find(mContext);
					newFind.insertToDB(args, allImageValues, allVideoValues, allAudioValues);
					allImageValues = new LinkedList<ContentValues>();
					allVideoValues = new LinkedList<ContentValues>();
					allAudioValues = new LinkedList<ContentValues>();
					Log.i(TAG, "Inserting new find from server into DB: " + id);
				}				
			}
			else if(phoneSIDs.contains(Id)&&phoneUpdateSIDs.contains(Id)) {
				int id = Id.intValue();
				Communicator.cleanupOnReceive(find);
				ContentValues args = MyDBHelper.getContentValuesFromMap(find);
				if(!args.equals(null)) {
					Log.i(TAG, "getNewFindsFromServer(); id = "+id);
					Log.i(TAG,"getNewFindsFromServer(); GOT HERE!!!");
					args.remove("imageUri");
					args.remove("videoUri");
					args.remove("audioUri");
					mDBHelper.updateFind(mDBHelper.getIdBySID(id),args);
					Log.i(TAG, "Updating find from server into DB : "+ id);
				}
			}
		}
	}

	/**
	 * This method sends the phone's new finds to the server.
	 * @param comm
	 */
	private void putNewFindsToServer(Communicator comm, List<HashMap<String, Object>> remoteFindsList) {
		MyDBHelper mDBHelper = new MyDBHelper(mContext); 
		List<Integer> newFindsOnPhone = mDBHelper.getAllNewIds();
		List<Integer> phoneSIDs = mDBHelper.getUpdatedSIDsFromPhone(remoteFindsList);
		if(phoneSIDs!=null) {
			for(int id : phoneSIDs) {
				Log.i("toUpdate", "updateId = "+id);
			}
		}
		Log.i(TAG, "Sending finds to server: " + newFindsOnPhone.toString());

		if (newFindsOnPhone!=null) {
			for (int id: newFindsOnPhone) {
				Find find = new Find(mContext, id);
				comm.sendFind(find);
				Cursor cursor = find.getImages();
				putNewImagesToServer(comm, cursor, id);
				cursor = find.getVideos();
				putNewMediaToServer(comm, cursor, id, "video/3gpp");
				cursor = find.getAudioClips();
				putNewMediaToServer(comm, cursor, id, "audio/3gpp");
			}
		}

		if (phoneSIDs!=null) {
			for(int id: phoneSIDs) {
				Log.i("Updated Find", "I guess it recognizes that it's been updated");
				Find find = new Find(mContext, id);
				comm.updateFind(find);
				Cursor cursor = find.getImages();
				putNewImagesToServer(comm, cursor, id);
				cursor = find.getVideos();
				putNewMediaToServer(comm, cursor, id, "video/3gpp");
				cursor = find.getAudioClips();
				putNewMediaToServer(comm, cursor, id, "audio/3gpp");
			}
		}
	}

	/**
	 * Sends images from a certain find to the server.  The Cursor contains all the information of
	 * the images.  If an image does not already exist on the server, the bitmap for the image is gotten 
	 * from the MediaStore and converted (code from external source) into base64 code and sent to the server.
	 * 
	 * @param comm the Communicator Object to send the image
	 * @param cursor the Cursor that contains all the information
	 * @param findId the Find that this image is associated with
	 */
	private void putNewImagesToServer(Communicator comm, Cursor cursor, int findId) {
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {			
			Uri imageUri = Uri.parse(cursor.getString(cursor.getColumnIndex(MyDBHelper.COLUMN_IMAGE_URI)));
			int identifier = cursor.getInt(cursor.getColumnIndex(MyDBHelper.COLUMN_PHOTO_IDENTIFIER));

			if (!comm.imageExistsOnServer(identifier)) {
				ByteArrayOutputStream imageByteStream= new ByteArrayOutputStream();
				byte[] imageByteArray = null;
				Bitmap bitmap = null;
	
				try {
					bitmap = android.provider.MediaStore.Images.Media.getBitmap
					(mContext.getContentResolver(), imageUri);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}			
	
				if (bitmap == null) {
					Log.d(TAG, "No bitmap");
				}
				// Compress bmp to jpg, write to the byte output stream
				bitmap.compress(Bitmap.CompressFormat.JPEG, 80, imageByteStream);
				// Turn the byte stream into a byte array
				imageByteArray = imageByteStream.toByteArray();
				char[] base64 = Base64Coder.encode(imageByteArray);
				String base64String = new String(base64);
	
				Log.i("Image", "got the base64 code!");
				comm.sendMedia(identifier, findId, base64String, "image/jpeg");
			}
			cursor.moveToNext();
		}
	}

	private void putNewMediaToServer(Communicator comm, Cursor cursor, int findId, String type) {
		Uri uri;
		int identifier;
		cursor.moveToFirst();
		Log.d(TAG, "putNewMediaToServer 345" + "About the loop through the video cursor");
		while (!cursor.isAfterLast()) {	

			if (type == "video/3gpp") {
				uri = Uri.parse(cursor.getString(cursor.getColumnIndex(MyDBHelper.COLUMN_VIDEO_URI)));
				identifier = cursor.getInt(cursor.getColumnIndex(MyDBHelper.COLUMN_VIDEO_IDENTIFIER));
				comm.sendMediaFileToServer(identifier, findId, uri, type);
				Log.d(TAG, "putNewMediaToServer 352" + "send video info to Communicator");

			} else if (type == "audio/3gpp") {
				uri = Uri.parse(cursor.getString(cursor.getColumnIndex(MyDBHelper.COLUMN_AUDIO_URI)));
				identifier = cursor.getInt(cursor.getColumnIndex(MyDBHelper.COLUMN_AUDIO_IDENTIFIER));
				comm.sendMediaFileToServer(identifier, findId, uri, type);
				Log.d(TAG, "putNewMediaToServer 358" + "send audio info to Communicator");

			} else {
				return;
			}
			cursor.moveToNext();
		}
	}
	/**
	 * Saves the camera images and associated bitmaps to Media storage.  Code borrowed
	 * from FindActivity.
	 */
	private void saveImagesAndUris(List<Bitmap> bitmaps) {
		if (bitmaps.size() == 0) {
			Log.i(TAG, "No camera images to save ...exiting ");
			return;
		}
		Log.i("Saving These Bitmaps", bitmaps.toString());
		ListIterator<Bitmap> it = bitmaps.listIterator();
		while (it.hasNext()) { 
			Bitmap bm = it.next();
			Log.i("Bitmap", bm.toString());
			ContentValues values = new ContentValues();
			values.put(MediaColumns.TITLE, "posit image");
			values.put(ImageColumns.BUCKET_DISPLAY_NAME,"posit");
			values.put(ImageColumns.IS_PRIVATE, 0);
			values.put(MediaColumns.MIME_TYPE, "image/jpeg");
			Uri imageUri = mContext.getContentResolver()
			.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			Log.i(TAG, "Saved image uri = " + imageUri.toString());
			OutputStream outstream;
			try {
				outstream = mContext.getContentResolver().openOutputStream(imageUri);
				bm.compress(Bitmap.CompressFormat.JPEG, 70, outstream);
				outstream.close();
			} catch (Exception e) {
				Log.i(TAG, "Exception during image save " + e.getMessage());
			}

			// Now create a thumbnail and save it
			int width = bm.getWidth();
			int height = bm.getHeight();
			int newWidth = THUMBNAIL_TARGET_SIZE;
			int newHeight = THUMBNAIL_TARGET_SIZE;

			float scaleWidth = ((float)newWidth)/width;
			float scaleHeight = ((float)newHeight)/height;

			Matrix matrix = new Matrix();
			matrix.setScale(scaleWidth, scaleHeight);
			Bitmap thumbnailImage = Bitmap.createBitmap(bm, 0, 0,width,height,matrix,true);

			int imageId = Integer.parseInt(imageUri.toString()
					.substring(Media.EXTERNAL_CONTENT_URI.toString().length()+1));	

			Log.i(TAG, "imageId from camera = " + imageId);

			values = new ContentValues(4);
			values.put(Images.Thumbnails.KIND, Images.Thumbnails.MINI_KIND);
			values.put(Images.Thumbnails.IMAGE_ID, imageId);
			values.put(Images.Thumbnails.HEIGHT, height);
			values.put(Images.Thumbnails.WIDTH, width);
			Uri thumbnailUri = mContext.getContentResolver()
			.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values);
			Log.i(TAG, "Saved thumbnail uri = " + thumbnailUri.toString());

			try {
				outstream = mContext.getContentResolver().openOutputStream(thumbnailUri);
				thumbnailImage.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
				outstream.close();
			} catch (Exception e) {
				Log.i(TAG, "Exception during thumbnail save " + e.getMessage());
			}

			mNewImageUris.add(imageUri);
			mNewImageThumbnailUris.add(thumbnailUri);
		}
	}

	private void saveVideosAndUris(List<byte[]> files) {
		if (files.size() == 0) {
			Log.i(TAG, "No video clips to save... exiting");
			return;
		}

		ListIterator<byte[]> it = files.listIterator();
		while (it.hasNext()) { 
			byte[] data = it.next();

			ContentValues values = new ContentValues();
			values.put(MediaColumns.TITLE, "posit video");
			values.put(MediaColumns.MIME_TYPE, "video/3gpp");
			Uri videoUri = mContext.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

			try {
				OutputStream outStream = mContext.getContentResolver().openOutputStream(videoUri);
				outStream.write(data);
				outStream.close();
				Log.i(TAG, "Saved video uri = " + videoUri.toString());
			} catch (Exception e) {
				Log.i(TAG, "Exception during video save: " + e.getMessage());
			}

			mNewVideoUris.add(videoUri);
		}
	}

	private void saveAudioClipsAndUris(List<byte[]> files) {
		if (files.size() == 0) {
			Log.i(TAG, "No audio clips to save... exiting");
			return;
		}
		ListIterator<byte[]> it = files.listIterator();
		while (it.hasNext()) { 
			byte[] data = it.next();
			try {
				File file = new File(Environment.getExternalStorageDirectory(), "recording"+System.currentTimeMillis()+".3gpp");

				FileOutputStream fos = new FileOutputStream(file);
				ByteArrayInputStream bais = new ByteArrayInputStream(data);
				int bytesAvailable = bais.available();
				int maxBufferSize = 1024;
				int bufferSize = Math.min(bytesAvailable, maxBufferSize);
				byte[] buffer = new byte[bufferSize];

				int bytesRead = bais.read(buffer, 0, bufferSize);

				while (bytesRead > 0) {
					fos.write(buffer, 0, bufferSize);
					bytesAvailable = bais.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = bais.read(buffer, 0, bufferSize);
				}

				bais.close();
				fos.flush();
				ContentValues values = new ContentValues();
				values.put(MediaColumns.TITLE, "posit audio clip");
				values.put(MediaColumns.MIME_TYPE, "audio/3gpp");
				values.put(MediaColumns.DATA, file.getAbsolutePath());
				Uri audioUri = mContext.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
				Log.i(TAG, "Saved audio clip uri = " + audioUri.toString());
				mNewAudioUris.add(audioUri);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			} catch(Exception e) {
				Log.e(TAG, e.toString());
				return;
			}
			

		}
	}

	/**
	 * Retrieves images and thumbnails from their uris stores them as <key,value> pairs in a ContentValues,
	 * one for each image.  Each ContentValues is then stored in a list to carry all the images
	 * @return the list of images stored as ContentValues
	 */
	private List<ContentValues> retrieveImagesFromUris() {
		List<ContentValues> values = new LinkedList<ContentValues>();
		ListIterator<Uri> imageIt = mNewImageUris.listIterator();
		ListIterator<Uri> thumbnailIt = mNewImageThumbnailUris.listIterator();

		while (imageIt.hasNext() && thumbnailIt.hasNext()) {
			Uri imageUri = imageIt.next();
			Uri thumbnailUri = thumbnailIt.next();

			ContentValues result = new ContentValues();
			String value = "";
			if (imageUri != null) {
				value = imageUri.toString();
				result.put(MyDBHelper.COLUMN_IMAGE_URI, value);
				value = thumbnailUri.toString();
				result.put(MyDBHelper.COLUMN_PHOTO_THUMBNAIL_URI, value);
			}
			values.add(result);
		}
		return values;
	}

	/**
	 * Retrieves videos from their uris, stores them as <key,value> pairs in a ContentValues,
	 * one for each video. Each ContentValues is then stored in a list to carry all the videos
	 * @return the list of videos stored as ContentValues
	 */
	private List<ContentValues> retrieveVideosFromUris() {
		List<ContentValues> values = new LinkedList<ContentValues>();
		ListIterator<Uri> videoIt = mNewVideoUris.listIterator();

		while (videoIt.hasNext()) {
			Uri videoUri = videoIt.next();

			ContentValues result = new ContentValues();
			String value = "";
			if (videoUri != null) {
				value = videoUri.toString();
				result.put(MyDBHelper.COLUMN_VIDEO_URI, value);
			}
			values.add(result);
		}
		return values;
	}

	/**
	 * Retrieves audio clips from their uris, stores them as <key,value> pairs in a ContentValues,
	 * one for each audio clip. Each ContentValues is then stored in a list to carry all the audio clips
	 * @return the list of audio clips stored as ContentValues
	 */
	private List<ContentValues> retrieveAudioClipsFromUris() {
		List<ContentValues> values = new LinkedList<ContentValues>();
		ListIterator<Uri> audioIt = mNewAudioUris.listIterator();

		while (audioIt.hasNext()) {
			Uri audioUri = audioIt.next();

			ContentValues result = new ContentValues();
			String value = "";
			if (audioUri != null) {
				value = audioUri.toString();
				result.put(MyDBHelper.COLUMN_AUDIO_URI, value);
			}
			values.add(result);
		}
		return values;
	}
}