package org.hfoss.posit.web;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.hfoss.posit.Find;
import org.hfoss.posit.MyDBHelper;
import org.hfoss.third.Base64Coder;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Images.Media;
import android.util.Log;

public class SyncThread extends Thread {
	
	private static final String TAG = "SyncThread";
	public static final int DONE = 0;
	private static final int THUMBNAIL_TARGET_SIZE = 320;
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
		
		Log.i(TAG,"# to be updated = "+phoneUpdateSIDs.size());
		Log.i(TAG, "Updated Phone SIDs: " + phoneUpdateSIDs.toString());
		
		// Retrieve each new find from the server
		for (int i = 0; i < remoteFindsList.size(); i++) {
			HashMap<String, Object> find = remoteFindsList.get(i);
			//Integer id = new ResponseParser(find.get(i));
			Integer Id = Integer.parseInt(find.get("id").toString());
			Log.i("getNewFindsFromServer()",Id+"");
			if (!phoneSIDs.contains(Id)) {
				int id = Id.intValue();
				Log.i("getNewFindsFromServer()","GOT HERE!!!");
				Communicator.cleanupOnReceive(find);
				ContentValues args = MyDBHelper.getContentValuesFromMap(find);
				args.remove("imageUri");
				
				ArrayList<HashMap<String, Object>> images = 
					(ArrayList<HashMap<String, Object>>) find.get("imageUri");
				List<ContentValues> allImageValues = new LinkedList<ContentValues>();
				if (images != null) {
					List<Bitmap> bitmaps = new LinkedList<Bitmap>();
					
					for (int j = 0; j < images.size(); j++) {
						HashMap<String, Object> image = images.get(j);
						String fullData = (String) image.get("data_full");
						byte[] data = Base64Coder.decode(fullData);
						Bitmap imageBM = BitmapFactory.decodeByteArray(data, 0, data.length);
						bitmaps.add(imageBM);
					}
					saveImagesAndUris(bitmaps);
					allImageValues= retrieveImagesFromUris();
					mNewImageUris = new LinkedList<Uri>();
					mNewImageThumbnailUris = new LinkedList<Uri>();
				}
				if (!args.equals(null)) {
					Find newFind = new Find(mContext);
					newFind.insertToDB(args, allImageValues, null, null);
					allImageValues = new LinkedList<ContentValues>();
					Log.i(TAG, "Inserting new find from server into DB: " + id);
				}				
			}
			else if(phoneSIDs.contains(Id)&&phoneUpdateSIDs.contains(Id)) {
				int id = Id.intValue();
				Communicator.cleanupOnReceive(find);
				ContentValues args = MyDBHelper.getContentValuesFromMap(find);
				if(!args.equals(null)) {
					Log.i("getNewFindsFromServer()","id = "+id);
					Log.i("getNewFindsFromServer()","GOT HERE!!!");
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
		/*if (newFindsOnPhone==null || newFindsOnPhone.size()==0) {
			Log.i(TAG, "No NEW finds to send to server.");
			return;
		}*/

		Log.i(TAG, "Sending finds to server: " + newFindsOnPhone.toString());

		if (newFindsOnPhone!=null) {
			for (int id: newFindsOnPhone) {
				Find find = new Find(mContext, id);
				comm.sendFind(find);
			}
		}
		if (phoneSIDs!=null) {
			for(int id: phoneSIDs) {
				Find find = new Find(mContext, mDBHelper.getIdBySID(id));
				comm.updateFind(find);
			}
		}
	}
	
	/**
	 * This method retrieves finds that have been updated from the server
	 *  and puts them in the phone's DB.
	 * @param comm
	 * @param remoteFindsList
	 */
	/*private void getUpdatedFindsFromServer(Communicator comm, List<HashMap<String, Object>> remoteFindsList) {
		Log.i(TAG, "Remote Finds (SIDs) From Server: " + remoteFindsList.toString());
		
		MyDBHelper mDBHelper = new MyDBHelper(mContext);  // Get matching SIDs from phone
		List<Long> phoneSIDs = mDBHelper.getFindsNeedingUpdate(remoteFindsList);
		Log.i("getUpdatedFindsFromServer()","# to be updated = "+phoneSIDs.size());
		Log.i(TAG, "Updated Phone SIDs: " + phoneSIDs.toString());
		
		for (long id: phoneSIDs) {
			Find updatedFind = new Find(mContext,id);
			//comm.updateRemoteFind(updatedFind);
			Log.i(TAG, "Sent updated find to server: " + id);
		}
	}*/
	
	/**
	 * This method retrieves finds that have been updated on the phone
	 *  and sends them to the server.
	 * @param comm
	 * @param remoteFindsList
	 */
	/*private void putUpdatedFindsToServer(Communicator comm, List<HashMap<String, Object>> remoteFindsList) {
		Log.i(TAG, "Remote Finds (SIDs) From Server: " + remoteFindsList.toString());
		
		MyDBHelper mDBHelper = new MyDBHelper(mContext);  // Get matching SIDs from phone
		List<Integer> phoneSIDs = mDBHelper.getUpdatedSIDsFromPhone(remoteFindsList);
		Log.i(TAG, "Updated Phone SIDs: " + phoneSIDs.toString());
		
		for (int id: phoneSIDs) {
			Find updatedFind = new Find(mContext,id);
			comm.updateRemoteFind(updatedFind);
			Log.i(TAG, "Sent updated find to server: " + id);
		}
	}*/
	
	/**
	 * Saves the camera images and associated bitmaps to Media storage and
	 *  save's their respective Uri's in aFind, which will save them to Db.
	 * @param aFind  the current Find we are creating or editing
	 * @param bm the bitmap from the camera
	 */
	private void saveImagesAndUris(List<Bitmap> bitmaps) {
		if (bitmaps.size() == 0) {
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
				thumbnailImage.compress(Bitmap.CompressFormat.JPEG, 70, outstream);
				outstream.close();
			} catch (Exception e) {
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
				result.put(MyDBHelper.KEY_IMAGE_URI, value);
				value = thumbnailUri.toString();
				result.put(MyDBHelper.KEY_PHOTO_THUMBNAIL_URI, value);
			}
			values.add(result);
		}
		return values;
	}
}

