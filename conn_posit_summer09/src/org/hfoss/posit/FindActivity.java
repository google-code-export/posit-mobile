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

import java.io.OutputStream;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Handles both adding new finds as well as editing existing finds.  
 * Includes adding and editing name, description, and barcode.  Also allows user
 * to attach photos to the find, as well as delete everything.
 * 
 * @author Prasanna Gautam
 * @author Ralph Morelli
 * @author Qianqian Lin
 * @author Chris Fei
 */
public class FindActivity extends Activity 
implements OnClickListener, OnItemClickListener, LocationListener {
	private Find mFind;
	private long mRowId;
	private int mState;
	private Cursor mCursor = null;
	private Gallery mGallery;

	//list of temporary files representing pictures, videos, and audio cilps taken for a find
	//that has not been added to the database
	private List<Bitmap> mTempBitmaps = new LinkedList<Bitmap>();
	private List<Uri> mTempVideos = new LinkedList<Uri>();
	private List<Uri> mTempAudios = new LinkedList<Uri>();
	//list of uris of new media and thumbnails being attached to the find
	private List<Uri> mNewImageUris = new LinkedList<Uri>();
	private List<Uri> mNewImageThumbnailUris = new LinkedList<Uri>();
	private List<Uri> mNewVideoUris = new LinkedList<Uri>();
	private List<Uri> mNewVideoThumbnailUris = new LinkedList<Uri>();
	private List<Uri> mNewAudioUris = new LinkedList<Uri>();

	private static final int BUFSIZE = 4096;

	private double mLongitude = 0;
	private double mLatitude = 0;

	private TextView mLatitudeTextView;
	private TextView mLongitudeTextView;

	private Thread mThread;
	private LocationManager mLocationManager;

	public static final int STATE_EDIT = 1;
	public static final int STATE_INSERT= 2;
	public static final int BARCODE_READER= 3;
	public static final int CAMERA_ACTIVITY= 4;
	public static final int VIDEO_ACTIVITY = 5;
	public static final int AUDIO_ACTIVITY = 6;
	private static final int NEW_FIND_CAMERA_ACTIVITY = 7;
	private static final int NEW_FIND_VIDEO_ACTIVITY = 8;
	private static final int NEW_FIND_AUDIO_ACTIVITY = 9;
	public static final int SYNC_ACTIVITY= 10;
	public static final int IMAGE_VIEW = 11;
	public static final int VIDEO_VIEW = 12;
	public static final int AUDIO_VIEW = 13;
	private static final String TAG = "FindActivity";
	private static final int CONFIRM_DELETE_DIALOG = 0;
	private static final int NON_UNIQUE_ID = 1;
	private static final int UPDATE_LOCATION = 2;
	private static final boolean ENABLED_ONLY = true;
	private static final int THUMBNAIL_TARGET_SIZE = 320;

	/**
	 * Handles GPS updates.  
	 * Source: Android tutorials
	 * @see http://www.androidph.com/2009/02/app-10-beer-radar.html
	 */
	Handler updateHandler = new Handler() {
		/** Gets called on every message that is received */
		// @Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_LOCATION:
				mLatitudeTextView.setText(" " + mLatitude);
				mLongitudeTextView.setText(" " + mLongitude);
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Sets up the various actions for the FindActivity, which are 
	 * to insert new finds in the DB, edit existing finds, and attaching images
	 * to the finds
	 * @param savedInstanceState (not currently used) is to restore state.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		finishActivity(ListFindsActivity.FIND_FROM_LIST);
		final Intent intent = getIntent();
		String action = intent.getAction();

		setContentView(R.layout.add_find);
		mLatitudeTextView = (TextView)findViewById(R.id.latitudeText);
		mLongitudeTextView = (TextView)findViewById(R.id.longitudeText);

		mGallery = (Gallery)findViewById(R.id.picturesTaken);
		Button scanButton = (Button) findViewById(R.id.idBarcodeButton);
		scanButton.setOnClickListener(this);
		Button viewVidButton = (Button) findViewById(R.id.view_video);
		viewVidButton.setOnClickListener(this);
		Button viewAudButton = (Button) findViewById(R.id.view_audio);
		viewAudButton.setOnClickListener(this);
		TextView barcodeError = (TextView)findViewById(R.id.barcodeReaderError);
		barcodeError.setVisibility(TextView.GONE);
		if(!isIntentAvailable(this,"com.google.zxing.client.android.SCAN"))
		{
			scanButton.setClickable(false);
			barcodeError.setVisibility(TextView.VISIBLE);
		}
		if (action.equals(Intent.ACTION_EDIT)) {
			doEditAction();
		} 
		else if (action.equals(Intent.ACTION_INSERT)) {
			doInsertAction();
		}
	} // onCreate()

	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list =
			packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	/**
	 * Inserts a new Find. A TextView handles all the data entry. For new
	 * Finds, both a time stamp and GPS location are fixed.  
	 */
	private void doInsertAction() {
		mState = STATE_INSERT;
		TextView tView = (TextView) findViewById(R.id.timeText);
		tView.setText(getDateText());
		initializeLocationAndStartGpsThread();
	}

	/**
	 * Sets the Find's location to the last known location and starts 
	 *  a separate thread to update GPS location.
	 */
	private void initializeLocationAndStartGpsThread() {
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		List<String> providers = mLocationManager.getProviders(ENABLED_ONLY);
		Log.i(TAG, "Enabled providers = " + providers.toString());
		String provider = mLocationManager.getBestProvider(new Criteria(),ENABLED_ONLY);
		Log.i(TAG, "Best provider = " + provider);

		setCurrentGpsLocation(null);   
		mThread = new Thread(new MyThreadRunner());
		mThread.start();
	}

	/**
	 * Repeatedly attempts to update the Find's location.
	 */
	class MyThreadRunner implements Runnable {
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				Message m = Message.obtain();
				m.what = 0;
				FindActivity.this.updateHandler.sendMessage(m);
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	/**
	 * Allows editing of editable data for existing finds.  For existing finds, 
	 * we retrieve the Find's data from the DB and display it in a TextView. The
	 * Find's location and time stamp are not updated.
	 */
	private void doEditAction() {
		mState = STATE_EDIT;
		mRowId = getIntent().getLongExtra(MyDBHelper.KEY_ID, 0); 
		Log.i(TAG, "rowID = " + mRowId);

		// Instantiate a find object and retrieve its data from the DB
		mFind = new Find(this, mRowId);        
		ContentValues values = mFind.getContent();
		if (values == null) {
			Utils.showToast(this, "No values found for Find " + mRowId);
			mState = STATE_INSERT;
		} else {
			displayContentInView(values);  
		}
		displayGallery(mRowId);
	}


	@Override
	protected void onResume() {
		super.onResume();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onPause(){
		super.onPause();
		finishActivity(ListFindsActivity.FIND_FROM_LIST);
	}	


	/**
	 * This method is invoked by showDialog() when a dialog window is created. It displays
	 *  the appropriate dialog box, currently a dialog to confirm that the user wants to 
	 *  delete this find and a dialog to warn user that a barcode has already been entered into the system
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case CONFIRM_DELETE_DIALOG:
			return new AlertDialog.Builder(this)
			.setIcon(R.drawable.alert_dialog_icon)
			.setTitle(R.string.alert_dialog_2)
			.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// User clicked OK so do some stuff 
					if (mFind.delete()) // Assumes find was instantiated in onCreate
					{
						Utils.showToast(FindActivity.this, R.string.deleted_from_database);
					}

					else 
						Utils.showToast(FindActivity.this, R.string.delete_failed);
					finish();
					Intent intent = new Intent(FindActivity.this, ListFindsActivity.class);
					startActivity(intent);
				}
			}
			)
			.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					/* User clicked Cancel so do nothing */
				}
			})
			.create();
		case NON_UNIQUE_ID:
			return new AlertDialog.Builder(this)
			.setIcon(R.drawable.alert_dialog_icon)
			.setTitle(R.string.alert_dialog_non_unique)
			.setPositiveButton(R.string.alert_dialog_ok, 
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					//do nothing
				}
			})
			.create();

		default:
			return null;
		} // switch
	}


	@Override
	protected void onStop() {
		super.onStop();
	}

	/** 
	 * Creates the menu for this activity by inflating a menu resource file.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_finds_menu, menu);
		if(mState == STATE_INSERT)
			menu.removeItem(R.id.delete_find_menu_item);
		return true;
	} // onCreateOptionsMenu()

	public void save() {

	}

	/** 
	 * Handles the various menu item actions.
	 * @param featureId is unused
	 * @param item is the MenuItem selected by the user
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.save_find_menu_item:
			ContentValues contentValues = retrieveContentFromView();

			// Don't save to database if ID is zero.
			// This indicates a non-numeric or invalid ID				
			if(contentValues.getAsInteger(getString(R.string.idDB)) == 0)
				return false;

			if (mState == STATE_INSERT) { //if this is a new find
				mFind = new Find(this);
				saveCameraImageAndUri(mFind, mTempBitmaps); //save all temporary images
				saveVideoAndUri(mFind, mTempVideos);
				saveAudioAndUri(mFind, mTempAudios);
				List<ContentValues> imageValues = retrieveImagesFromUris(); //get uris for all new images
				List<ContentValues> videoValues = retrieveVideosFromUris(); //get uris for all new videos
				List<ContentValues> audioValues = retrieveAudioClipsFromUris(); //get uris for all new audio clips 

				if (mFind.insertToDB(contentValues, imageValues, videoValues, audioValues)) //insert find into database
					Utils.showToast(this, R.string.saved_to_database);
				else 
					Utils.showToast(this, R.string.save_failed);
			} else { 
				if (mFind.updateToDB(contentValues))
					Utils.showToast(this, R.string.saved_to_database);
				else 
					Utils.showToast(this, R.string.save_failed);
			}
			finish();
			Intent in = new Intent(this, ListFindsActivity.class); //redirect to list finds
			startActivity(in);
			break;
		case R.id.discard_changes_menu_item:
			if (mState == STATE_EDIT) {
				displayContentInView(mFind.getContent());
			} else {
				onCreate(null);
			}
			break;			
		case R.id.camera_menu_item:
			intent = new Intent("android.media.action.IMAGE_CAPTURE");
			intent.putExtra("rowId",mRowId);
			if (mFind == null)
				startActivityForResult(intent, NEW_FIND_CAMERA_ACTIVITY); //camera for new find
			else
				startActivityForResult(intent, CAMERA_ACTIVITY); //camera for existing find
			break;
		case R.id.video_menu_item:
			intent = new Intent("android.media.action.VIDEO_CAPTURE");
			intent.putExtra("rowId", mRowId);
			if (mFind == null)
				startActivityForResult(intent, NEW_FIND_VIDEO_ACTIVITY); //camera for new find
			else
				startActivityForResult(intent, VIDEO_ACTIVITY); //camera for existing find
			break;
		case R.id.audio_menu_item:
			intent = new Intent("android.provider.MediaStore.RECORD_SOUND");
			intent.putExtra("rowId", mRowId);
			if (mFind == null)
				startActivityForResult(intent, NEW_FIND_AUDIO_ACTIVITY); //camera for new find
			else
				startActivityForResult(intent, AUDIO_ACTIVITY); //camera for existing find
			break;
		case R.id.delete_find_menu_item:
			showDialog(CONFIRM_DELETE_DIALOG);
			break;

		default:
			return false;
		}
		return true;
	} // onMenuItemSelected

	/**
	 * Retrieves values from the View fields and stores them as <key,value> pairs in a ContentValues.
	 * This method is invoked from the Save menu item.  It also marks the find 'unsynced'
	 * so it will be updated to the server.
	 * @return The ContentValues hash table.
	 */
	private ContentValues retrieveContentFromView() {
		ContentValues result = new ContentValues();

		EditText eText = (EditText) findViewById(R.id.nameText);
		String value = eText.getText().toString();
		result.put(getString(R.string.nameDB), value);
		eText = (EditText) findViewById(R.id.descriptionText);
		value = eText.getText().toString();
		result.put(getString(R.string.descriptionDB), value);
		eText = (EditText) findViewById(R.id.idText);
		value = eText.getText().toString();
		try {
			result.put(getString(R.string.idDB), Long.parseLong(value));
		} catch (NumberFormatException e) {
			// If user entered non-numeric ID, show an error
			Utils.showToast(this, "Error: ID must be numeric");
			// and set id in result object to 0
			result.put(getString(R.string.idDB), 0);
		}
		TextView tView = (TextView) findViewById(R.id.longitudeText);
		value = tView.getText().toString();
		result.put(getString(R.string.longitudeDB), value);
		tView = (TextView) findViewById(R.id.latitudeText);
		value = tView.getText().toString();
		result.put(getString(R.string.latitudeDB), value);
		tView = (TextView) findViewById(R.id.timeText);
		value = tView.getText().toString();
		result.put(getString(R.string.timeDB), value);

		// Mark the find unsynched
		result.put(getString(R.string.syncedDB),"0");

		return result;
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

	/**
	 * Retrieves videos and thumbnails from their uris stores them as <key,value> pairs in a ContentValues,
	 * one for each video.  Each ContentValues is then stored in a list to carry all the video
	 * @return the list of video stored as ContentValues
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
				result.put(getString(R.string.videoUriDB), value);
			}
			values.add(result);
		}
		return values;
	}

	/**
	 * Retrieves audio clips and thumbnails from their uris stores them as <key,value> pairs in a ContentValues,
	 * one for each clip.  Each ContentValues is then stored in a list to carry all the audio clips
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
				result.put(getString(R.string.audioUriDB), value);
			}
			values.add(result);
		}
		return values;
	}

	/**
	 * Retrieves values from a ContentValues has table and puts them in the View.
	 * @param contentValues stores <key, value> pairs
	 */
	private void displayContentInView(ContentValues contentValues) {
		EditText eText = (EditText) findViewById(R.id.nameText);
		eText.setText(contentValues.getAsString(getString(R.string.nameDB)));
		eText = (EditText) findViewById(R.id.descriptionText);
		eText.setText(contentValues.getAsString(getString(R.string.descriptionDB)));
		eText = (EditText) findViewById(R.id.idText);
		eText.setText(contentValues.getAsString(getString(R.string.idDB)));
		TextView tView = (TextView) findViewById(R.id.timeText);

		if (mState == STATE_EDIT) {
			tView.setText(contentValues.getAsString(getString(R.string.timeDB)));
		}
		tView = (TextView) findViewById(R.id.longitudeText);
		tView.setText(contentValues.getAsString(getString(R.string.longitudeDB)));
		tView = (TextView) findViewById(R.id.latitudeText);
		tView.setText(contentValues.getAsString(getString(R.string.latitudeDB)));
	}

	/**
	 * Handles the barcode reader button click. 
	 * @param v is the View where the click occured.
	 */
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.idBarcodeButton:
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			try{
				startActivityForResult(intent, BARCODE_READER);
			} catch(ActivityNotFoundException e) {
				Log.e(TAG, e.toString());
			}
			break;
		case R.id.view_video:
			intent = new Intent(this, ListVideosActivity.class);
			intent.putExtra("rowId", mRowId);
			try {
				startActivity(intent);
			} catch(ActivityNotFoundException e) {
				Log.e(TAG, e.toString());
			}
			break;
		case R.id.view_audio:
			intent = new Intent(this, ListAudioClipsActivity.class);
			intent.putExtra("rowId", mRowId);
			try {
				startActivity(intent);
			} catch(ActivityNotFoundException e) {
				Log.e(TAG, e.toString());
			}
			break;
		}
	}

	/**
	 * Invoked when one of the Activities started
	 *  from FindActivity menu, such as the BARCODE_READER or the CAMERA, finishes.
	 *  It handles the results of the Activities. RESULT_OK == -1, RESULT_CANCELED = 0
	 *  @param requestCode is the code that launched the sub-activity
	 *  @param resultCode specifies whether the sub-activity was successful or not
	 *  @param data is an Intent storing whatever data is passed back by the sub-activity
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		int rowId;
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(TAG, "Result code = " + resultCode);
		Log.i(TAG, "Request code = " + requestCode);

		if (resultCode == RESULT_CANCELED)
			return;
		switch (requestCode) {

		case BARCODE_READER:
			String value = data.getStringExtra("SCAN_RESULT");
			MyDBHelper dBHelper = new MyDBHelper(this);
			Cursor cursor = dBHelper.getFindsWithIdentifier(Long.parseLong(value));
			if (cursor.getCount() == 0) {
				EditText eText = (EditText) findViewById(R.id.idText);
				eText.setText(value);
			} else showDialog(NON_UNIQUE_ID);
			break;

		case CAMERA_ACTIVITY: //for existing find: saves image to db when user clicks "attach"
			rowId = data.getIntExtra("rowId", -1);
			Log.i(TAG, "RowId from camera = " + rowId);
			Bitmap tempImage = (Bitmap) data.getExtras().get("data");
			Log.i(TAG, "bitmap = " + tempImage.toString());
			mTempBitmaps.add(tempImage);
			saveCameraImageAndUri(mFind, mTempBitmaps);
			List<ContentValues> imageValues = retrieveImagesFromUris();

			if (mFind.insertToDB(null, imageValues, null, null))
				Utils.showToast(this, R.string.saved_image_to_db);
			else 
				Utils.showToast(this, R.string.save_failed);
			displayGallery(mRowId);
			mTempBitmaps.clear();
			break;

		case VIDEO_ACTIVITY: //for existing find: saves video to db when user clicks "attach"
			rowId = data.getIntExtra("rowId", -1);
			Log.i(TAG, "Access intent data: " + data.toString());
			Uri tempVideo = data.getData();
			Log.i(TAG, "video = " + tempVideo.getPath());
			mTempVideos.add(tempVideo);
			
			if (saveVideoAndUri(mFind, mTempVideos)) {
				Utils.showToast(this, R.string.saved_video_to_db);
			} else {
				Utils.showToast(this, R.string.save_failed);
			}
			displayGallery(mRowId);
			mTempVideos.clear();
			break;

		case AUDIO_ACTIVITY:
			rowId = data.getIntExtra("rowId", -1);
			Log.i(TAG, "Access intent data: " + data.toString());
			Uri tempAudio = data.getData();
			Log.i(TAG, "video = " + tempAudio.getPath());
			mTempVideos.add(tempAudio);
			
			if (saveAudioAndUri(mFind, mTempVideos)) {
				Utils.showToast(this, R.string.saved_video_to_db);
			} else {
				Utils.showToast(this, R.string.save_failed);
			}
			displayGallery(mRowId);
			mTempVideos.clear();
			break;

		case NEW_FIND_CAMERA_ACTIVITY: //for new finds: stores temporary images in a list
			rowId = data.getIntExtra("rowId", -1);
			Log.i(TAG, "RowId from camera = " + rowId);
			tempImage = (Bitmap) data.getExtras().get("data");
			Log.i(TAG, "bitmap = " + tempImage.toString());
			mTempBitmaps.add(tempImage);
			displayGallery(mRowId);
			break;

		case NEW_FIND_VIDEO_ACTIVITY://for new finds: stores temporary videos in a list
			rowId = data.getIntExtra("rowId", -1);
			Log.i(TAG, "RowId from camera = " + rowId);
			tempVideo = data.getData();
			Log.i(TAG, "VidUri = " + tempVideo.toString());
			mTempVideos.add(tempVideo);
			break;

		case NEW_FIND_AUDIO_ACTIVITY://for new finds: stores temporary videos in a list
			rowId = data.getIntExtra("rowId", -1);
			Log.i(TAG, "RowId from audio recorder = " + rowId);
			tempAudio = data.getData();
			Log.i(TAG, "VidUri = " + tempAudio.toString());
			mTempAudios.add(tempAudio);
			break;

		case IMAGE_VIEW:
			Log.i(TAG,"finish old activity");
			finish();
			break;
		}
	}

	/**
	 * Saves the camera images and associated bitmaps to Media storage and
	 *  save's their respective Uri's in aFind, which will save them to Db.
	 * @param aFind  the current Find we are creating or editing
	 * @param bm the bitmap from the camera
	 */
	private void saveCameraImageAndUri(Find aFind, List<Bitmap> bitmaps) {
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
			Uri imageUri = getContentResolver()
			.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			Log.i(TAG, "Saved image uri = " + imageUri.toString());
			OutputStream outstream;
			try {
				outstream = getContentResolver().openOutputStream(imageUri);
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
			Uri thumbnailUri = getContentResolver()
			.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values);
			Log.i(TAG, "Saved thumbnail uri = " + thumbnailUri.toString());

			try {
				outstream = getContentResolver().openOutputStream(thumbnailUri);
				thumbnailImage.compress(Bitmap.CompressFormat.JPEG, 70, outstream);
				outstream.close();
			} catch (Exception e) {
				Log.i(TAG, "Exception during thumbnail save " + e.getMessage());
			}

			// Save the Uri's
			aFind.setImageUri(imageUri);
			aFind.setImageThumbnailUri(thumbnailUri);

			mNewImageUris.add(imageUri);
			mNewImageThumbnailUris.add(thumbnailUri);
		}
	}

	/**
	 * Saves the videos to Media storage and save's their respective Uri's in aFind, which
	 * will save them to Db.
	 * @param aFind  the current Find we are creating or editing
	 * @param tempVideos the videos from the camera
	 */
	private boolean saveVideoAndUri(Find aFind, List<Uri> tempVideos) {
		if (tempVideos.size() == 0) {
			Log.i(TAG, "No videos to save ...exiting ");
			return false;
		}

		ListIterator<Uri> it = tempVideos.listIterator();
		while (it.hasNext()) { 
			Uri videoUri = it.next();

			ContentValues values = new ContentValues();
			values.put(MyDBHelper.KEY_VIDEO_URI, videoUri.toString());
			
			List<ContentValues> cvList = new LinkedList<ContentValues>();
			cvList.add(values);

			// Save the Uri's
			mNewVideoUris.add(videoUri);
			return aFind.insertToDB(null, null, cvList, null);
		}
		return false;
	}

	/**
	 * Saves the audio clips to Media storage and save's their respective Uri's in aFind, which
	 * will save them to Db.
	 * @param aFind  the current Find we are creating or editing
	 * @param audios the audio clips from the microphone
	 * @return 
	 */
	private boolean saveAudioAndUri(Find aFind, List<Uri> tempAudios) {
		if (tempAudios.size() == 0) {
			Log.i(TAG, "No audio clips to save ...exiting ");
			return false;
		}

		ListIterator<Uri> it = tempAudios.listIterator();
		while (it.hasNext()) { 
			Uri audioUri = it.next();

			ContentValues values = new ContentValues();
			values.put(MyDBHelper.KEY_AUDIO_URI, audioUri.toString());
			
			List<ContentValues> cvList = new LinkedList<ContentValues>();
			cvList.add(values);

			// Save the Uri's
			mNewAudioUris.add(audioUri);
			return aFind.insertToDB(null, null, null, cvList);
		}
		return false;
	}

	/**
	 * Queries for images for this Find and shows them in a Gallery at the bottom of the View.
	 *  @param id is the rowId of the find
	 */
	private void displayGallery(long id) {
		if (id != 0) { 		//for existing finds
			// Select just those images associated with this find.
			if (mFind.hasImages()) {
				finishActivity(FindActivity.IMAGE_VIEW);
				mCursor = mFind.getImages();  // Returns the Uris of the images from the Posit Db
				mCursor.moveToFirst();
				ImageAdapter adapter = new ImageAdapter(mCursor, this);
				Log.i(TAG, "displayGallery(), adapter = " + adapter.getCount());

				mGallery.setAdapter(adapter);
				mGallery.setOnItemClickListener(this);
			} else 
				Utils.showToast(this, "No images to display.");
		} else {	//for new finds
			if (mTempBitmaps.size() > 0) {
				finishActivity(FindActivity.IMAGE_VIEW);
				ImageAdapter adapter = new ImageAdapter(this, mTempBitmaps);
				Log.i(TAG, "displayGallery(), adapter = " + adapter.getCount());

				mGallery.setAdapter(adapter);
				mGallery.setOnItemClickListener(this);
			}
		}
	}

	/**
	 * To detect the user clicking on the displayed images. Displays all pictures attached to this
	 * find by creating a new activity that shows
	 */
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		if (mFind != null) {
			try {
				//
				mCursor.moveToPosition(position);
				String s = mCursor.getString(mCursor.getColumnIndexOrThrow(getString(R.string.imageUriDB)));
				if (s != null) {
					Uri uri = Uri.parse(s);
					Intent intent = new Intent(Intent.ACTION_VIEW, uri, this, ImageViewActivity.class);
					intent.putExtra("position",position);
					intent.putExtra("findId", mRowId);
					setResult(RESULT_OK,intent);
					startActivityForResult(intent, IMAGE_VIEW);
				}
			} catch (Exception e) {
				//do nothing, 
				// hack to work around this exception
				Utils.showToast(this,e.toString());
			}
		} else {
			Bitmap bm = mTempBitmaps.get(position);
			Intent intent = new Intent(this, ImageViewActivity.class);
			intent.putExtra("position",position);
			intent.putExtra("findId", mRowId);
			intent.putExtra("bitmap", bm);
			startActivity(intent);

		}
	}

	@Override
	public void finishActivityFromChild(Activity child, int requestCode) {
		super.finishActivityFromChild(child, requestCode);
		Utils.showToast(this,"YOOOOOOOOO");
		if(requestCode==IMAGE_VIEW)
			finish();
	}

	/**
	 * Returns the current date and time from the Calendar Instance
	 * @return a string representing the current time stamp.
	 */
	private String getDateText() {
		Calendar c = Calendar.getInstance();
		int minute=c.get(Calendar.MINUTE);
		String minStr = minute+"";
		if(minute<10)
			minStr="0"+minute;
		String dateString = c.get(Calendar.YEAR) + "/" + c.get(Calendar.MONTH)
		+ "/" + c.get(Calendar.DAY_OF_MONTH) + " "
		+ c.get(Calendar.HOUR_OF_DAY) + ":" + 
		minStr;
		//if the minute field is only one digit, add a 0 in front of it
		return dateString;
	}

	/* ***************   Location Listener Interface Methods **********     */

	/**
	 * Invoked by the location service when phone's location changes.
	 */
	public void onLocationChanged(Location newLocation) {
		setCurrentGpsLocation(newLocation);  	
	}
	/**
	 * Resets the GPS location whenever the provider is enabled.
	 */
	public void onProviderEnabled(String provider) {
		setCurrentGpsLocation(null);  	
	}
	/**
	 * Resets the GPS location whenever the provider is disabled.
	 */
	public void onProviderDisabled(String provider) {
		//		Log.i(TAG, provider + " disabled");
		setCurrentGpsLocation(null);  	
	}
	/**
	 * Resets the GPS location whenever the provider status changes. We
	 * don't care about the details.
	 */
	public void onStatusChanged(String provider, int status, Bundle extras) {
		setCurrentGpsLocation(null);  	
	}

	/**
	 * Sends a message to the update handler with either the current location or 
	 *  the last known location. 
	 * @param location is either null or the current location
	 */
	private void setCurrentGpsLocation(Location location) {
		String bestProvider = "";
		if (location == null) {
			mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			List<String> providers = mLocationManager.getProviders(ENABLED_ONLY);
			Log.i(TAG, "Enabled providers = " + providers.toString());
			bestProvider = mLocationManager.getBestProvider(new Criteria(),ENABLED_ONLY);
			if (bestProvider != null && bestProvider.length() != 0) {
				mLocationManager.requestLocationUpdates(
						bestProvider, 30000, 0, this);	 // Every 30000 millisecs	
				location = mLocationManager.getLastKnownLocation(bestProvider);				
			}	
		}
		Log.i(TAG, "Best provider = |" + bestProvider + "|");

		try {
			mLongitude = location.getLongitude();
			mLatitude = location.getLatitude();
			Message msg = Message.obtain();
			msg.what = UPDATE_LOCATION;
			FindActivity.this.updateHandler.sendMessage(msg);
		} catch (NullPointerException e) {
			//	Avoid crashing Phone when no service is available.
		}
	}

	/**
	 * Create a video thumbnail for a video. May return null if the video is corrupt.
	 * @param filePath
	 * @return
	 */
	public static Bitmap createVideoThumbnail(String filePath) {
		Bitmap bitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try {
			retriever.setMode(MediaMetadataRetriever.MODE_CAPTURE_FRAME_ONLY);
			retriever.setDataSource(filePath);
			bitmap = retriever.captureFrame();
		} catch(IllegalArgumentException ex) {
			// Assume this is a corrupt video file
		} catch (RuntimeException ex) {
			// Assume this is a corrupt video file.
		} finally {
			try {
				retriever.release();
			} catch (RuntimeException ex) {
				// Ignore failures while cleaning up.
			}
		}
		return bitmap;
	}
}