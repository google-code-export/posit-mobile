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
import org.hfoss.posit.PositMain;
import org.hfoss.posit.Utils;
import org.hfoss.third.Base64Coder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
	public static final int NONETWORK = 2;
	public volatile boolean shutdownRequested = false;
	private static final int THUMBNAIL_TARGET_SIZE = 320;
	public static final int SYNCERROR = 1;
	private Handler mHandler;
	private Context mContext;
	private List<HashMap<String, Object>> mRemoteFindsList;
	private List<Uri> mNewImageUris = new LinkedList<Uri>();
	private List<Uri> mNewImageThumbnailUris = new LinkedList<Uri>();
	public SyncThread(Context context, Handler handler) {
		mHandler = handler;
		mContext = context;
	}

	public void run() {
		while(!shutdownRequested){
			try{
			Communicator comm = new Communicator(mContext);
			Log.i(TAG, "Getting remote finds...");

			mRemoteFindsList =  comm.getAllRemoteFinds();  // Get all server SIDs
			
			Log.i(TAG, "Sending new finds to server...");
			putNewFindsToServer(comm, mRemoteFindsList);
			
			Log.i(TAG, "Getting new finds from server...");
			getNewFindsFromServer(comm, mRemoteFindsList);
			

			//getUpdatedFindsFromServer(comm, mRemoteFindsList);
			//putUpdatedFindsToServer(comm, mRemoteFindsList);

			mHandler.sendEmptyMessage(DONE);
			}
			catch(Exception e){
					Log.i("SyncThread","Sync Error"); 
					mHandler.sendEmptyMessage(SYNCERROR);
					shutdownRequested = true;	
				}
		}
	}

	/**
	 * This method retrieves all finds from the server that aren't already on the phone.
	 * @param comm
	 */
	private void getNewFindsFromServer(Communicator comm, List<HashMap<String, Object>> remoteFindsList) {
		if (!Utils.isConnected(mContext)){
			mHandler.sendEmptyMessage(NONETWORK);
			return;
		}
		if(Utils.debug) {
			Log.i(TAG, "Remote Finds (SIDs) From Server: " + remoteFindsList.toString());
			Log.i(TAG, "Remote Finds (SIDs) From Server Size: " + remoteFindsList.size());
		}
		MyDBHelper mDBHelper = new MyDBHelper(mContext);  		// Get all phone SIDs
		List<Integer> phoneSIDs = mDBHelper.getAllPhoneSIDs();
		List<Integer> phoneUpdateSIDs = mDBHelper.getFindsNeedingUpdate(remoteFindsList);
		if(Utils.debug) {
			Log.i(TAG, "Phone SIDs: " + phoneSIDs.toString());
			Log.i(TAG, "# to be updated = " + phoneUpdateSIDs.size());
			Log.i(TAG, "Updated Phone SIDs: " + phoneUpdateSIDs.toString());
		}

		// Retrieve each new find from the server
		for (int i = 0; i < remoteFindsList.size(); i++) {
			HashMap<String, Object> find = remoteFindsList.get(i);
			//Integer id = new ResponseParser(find.get(i));
			Integer Id = Integer.parseInt(find.get("id").toString());
			
				int id = Id.intValue();
				Communicator.cleanupOnReceive(find);
				ContentValues args = MyDBHelper.getContentValuesFromMap(find);
				args.remove(MyDBHelper.COLUMN_IMAGE_URI);
				if(Utils.debug)
					Log.d(TAG, "Find: " + find.toString());

				//get all images associated to the find
				ArrayList<HashMap<String, Object>> images = null;
				try{
				images = (ArrayList<HashMap<String, Object>>) find.get(MyDBHelper.COLUMN_IMAGE_URI);
				}
				catch(Exception e) {
					if(Utils.debug)
						Log.e(TAG,"caught exception?");
				}
				List<ContentValues> allImageValues = new LinkedList<ContentValues>();
				List<Integer> imageIdentifiers = new LinkedList<Integer>();

				if (images != null) {
					List<Bitmap> bitmaps = new LinkedList<Bitmap>();
					int imageId;
					for (int j = 0; j < images.size(); j++) {
						HashMap<String, Object> image = images.get(j);
						try {
							imageId = Integer.parseInt((String) image.get("id"));
							if (!mDBHelper.containsImage(imageId)) {
								imageIdentifiers.add(imageId);
								String fullData = (String) image.get("data_full");
								byte[] data = Base64Coder.decode(fullData);
								Bitmap imageBM = BitmapFactory.decodeByteArray(data, 0, data.length);
								bitmaps.add(imageBM);	
							}
						}
						catch (Exception e){
							if(Utils.debug)
								Log.d(TAG, ""+e+":142");
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

			if (!phoneSIDs.contains(Id)) {	
				if (!args.equals(null)) {
					Find newFind = new Find(mContext);
					newFind.insertToDB(args, allImageValues);
					allImageValues = new LinkedList<ContentValues>();
					if(Utils.debug)
						Log.i(TAG, "Inserting new find from server into DB: " + id);
				}				
			}
			else if(phoneSIDs.contains(Id)&&phoneUpdateSIDs.contains(Id)) {
				if(!args.equals(null)) {
					args.remove("imageUri");
					mDBHelper.updateFind(id,args);
					if(Utils.debug)
						Log.i(TAG, "Updating find from server into DB : "+ id);
				}
				for(ContentValues values : allImageValues) {
					mDBHelper.addNewPhoto(values, id);
				}
			}
		}
	}

	/**
	 * This method sends the phone's new finds to the server.
	 * @param comm
	 */
	private void putNewFindsToServer(Communicator comm, List<HashMap<String, Object>> remoteFindsList) {
		if (!Utils.isConnected(mContext)){
			mHandler.sendEmptyMessage(NONETWORK);
			return;
		}
		MyDBHelper mDBHelper = new MyDBHelper(mContext); 
		List<Integer> newFindsOnPhone = mDBHelper.getAllNewIds();
		List<Integer> phoneSIDs = mDBHelper.getUpdatedSIDsFromPhone(remoteFindsList);
		if(Utils.debug)
			Log.i(TAG, "Sending finds to server: " + newFindsOnPhone.toString());

		if (newFindsOnPhone!=null) {
			for (int id: newFindsOnPhone) {
				Find find = new Find(mContext, id);
				comm.sendFind(find);
				Cursor cursor = find.getImages();
				putNewImagesToServer(comm, cursor, id);
			}
		}

		if (phoneSIDs!=null) {
			for(int id: phoneSIDs) {
				Find find = new Find(mContext, id);
				comm.updateFind(find);
				Cursor cursor = find.getImages();
				putNewImagesToServer(comm, cursor, id);
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
				comm.sendMedia(identifier, findId, base64String, "image/jpeg");
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
			if(Utils.debug)
				Log.i(TAG, "No camera images to save ...exiting ");
			return;
		}
		ListIterator<Bitmap> it = bitmaps.listIterator();
		while (it.hasNext()) { 
			Bitmap bm = it.next();
			ContentValues values = new ContentValues();
			values.put(MediaColumns.TITLE, "posit image");
			values.put(ImageColumns.BUCKET_DISPLAY_NAME,"posit");
			values.put(ImageColumns.IS_PRIVATE, 0);
			values.put(MediaColumns.MIME_TYPE, "image/jpeg");
			Uri imageUri = mContext.getContentResolver()
			.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			OutputStream outstream;
			try {
				outstream = mContext.getContentResolver().openOutputStream(imageUri);
				bm.compress(Bitmap.CompressFormat.JPEG, 70, outstream);
				outstream.close();
			} catch (Exception e) {
				if(Utils.debug)
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

			values = new ContentValues(4);
			values.put(Images.Thumbnails.KIND, Images.Thumbnails.MINI_KIND);
			values.put(Images.Thumbnails.IMAGE_ID, imageId);
			values.put(Images.Thumbnails.HEIGHT, height);
			values.put(Images.Thumbnails.WIDTH, width);
			Uri thumbnailUri = mContext.getContentResolver()
			.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values);

			try {
				outstream = mContext.getContentResolver().openOutputStream(thumbnailUri);
				thumbnailImage.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
				outstream.close();
			} catch (Exception e) {
				if(Utils.debug)
					Log.i(TAG, "Exception during thumbnail save " + e.getMessage());
			}

			mNewImageUris.add(imageUri);
			mNewImageThumbnailUris.add(thumbnailUri);
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
}