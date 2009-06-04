/**
 * 
 */
package org.hfoss.posit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
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
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class FindActivity extends Activity 
	implements OnClickListener, OnItemClickListener, LocationListener {

	private Find mFind;
	private long mRowId;
	private Bitmap mTempImage;
	private Uri mImageUri;
	private Uri mThumbnailUri;
	private int mState;
	private Cursor mCursor = null;
	private Gallery mGallery;
	
	private double mLongitude = 0;
	private double mLatitude = 0;
	
	private TextView mLatitudeTextView;
	private TextView mLongitudeTextView;

	private Thread mThread;
	private Message mMessage;
	private LocationManager mLocationManager;
	
	public static final int STATE_EDIT = 1;
	public static final int STATE_INSERT= 2;
	public static final int BARCODE_READER= 3;
	public static final int CAMERA_ACTIVITY= 4;
	public static final int SYNC_ACTIVITY= 5;
	private static final String TAG = "FindActivity";
	private static final int CONFIRM_DELETE_DIALOG = 0;
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
			case UPDATE_LOCATION: {
				mLatitudeTextView.setText(" " + mLatitude);
				mLongitudeTextView.setText(" " + mLongitude);
				break;
			}
			}
			super.handleMessage(msg);
		}
	};

	
	/**
	 * Sets up the various actions for the FindActivity, which are 
	 * to insert new finds in the DB, edit existing finds, and delete 
	 * existing finds (one or all) from the phone's DB.
	 * @param savedInstanceState (not currently used) is to restore state.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final Intent intent = getIntent();
		String action = intent.getAction();
		
		if (action.equals(this.getString(R.string.delete_finds))) {
			showDialog(CONFIRM_DELETE_DIALOG);
		} else {		
			setContentView(R.layout.add_find);
			mLatitudeTextView = (TextView)findViewById(R.id.latitudeText);
			mLongitudeTextView = (TextView)findViewById(R.id.longitudeText);
			mGallery = (Gallery)findViewById(R.id.picturesTaken);
			((Button)findViewById(R.id.idBarcodeButton)).setOnClickListener(this);
	
			if (action.equals(Intent.ACTION_EDIT)) {
				doEditAction();
			} else if (action.equals(Intent.ACTION_INSERT)) {
				doInsertAction();
			}
		}
	} // onCreate()
	
	/**
	 * Inserts a new Find. A TextView handles all the data entry. For new
	 * Finds, both a time stamp and GPS location are fixed.  
	 */
	private void doInsertAction() {
		mState = STATE_INSERT;
		TextView tView = (TextView) findViewById(R.id.timeText);
		tView.setText(getDateText());
//		initializeLocationAndStartGpsThread();
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
		// @Override
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
	}	
	
	
	/**
	 * This method is invoked by showDialog() when a dialog window is created. It displays
	 *  the appropriate dialog box, currently a dialog to confirm that the user wants to 
	 *  delete all the finds.
	 */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case CONFIRM_DELETE_DIALOG:
        	return new AlertDialog.Builder(this)
            .setIcon(R.drawable.alert_dialog_icon)
            .setTitle(R.string.alert_dialog)
            .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // User clicked OK so do some stuff 
                	MyDBHelper mDbHelper = new MyDBHelper(FindActivity.this);
                	if (mDbHelper.deleteAllFinds()) {
        				Utils.showToast(FindActivity.this, R.string.deleted_from_database);
        				FindActivity.this.finish();
                	}
        			else {
        				Utils.showToast(FindActivity.this, R.string.delete_failed);
        				dialog.cancel();
        			}
                }
            })
            .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	
                    /* User clicked Cancel so do nothing */
                }
            })
            .create();
        default:
        	return null;
        } // switch
    }
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	/** 
	 * Creates the menu for this activity by inflating a menu resource file.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_finds_menu, menu);
		return true;
	} // onCreateOptionsMenu()
	
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

			if (mState == STATE_INSERT) {
				mFind = new Find(this);
				saveCameraImageAndUri(mFind, mTempImage);
				ContentValues imageValues = retrieveImagesFromUris();

				if (mFind.insertToDB(contentValues, imageValues))
					Utils.showToast(this, R.string.saved_to_database);
				else 
					Utils.showToast(this, R.string.save_failed);
			} else {  // STATE_EDIT
				saveCameraImageAndUri(mFind, mTempImage);
				ContentValues imageValues = retrieveImagesFromUris();
				if (mFind.updateToDB(contentValues, imageValues))
					Utils.showToast(this, R.string.saved_to_database);
				else 
					Utils.showToast(this, R.string.save_failed);
			}
			finish();
			break;
		case R.id.discard_changes_menu_item:
			if (mState == STATE_EDIT) {
				displayContentInView(mFind.getContent());
			} else {
				setContentView(R.layout.add_find);
			}
			break;			

		case R.id.delete_find_menu_item:
			if (mFind.delete()) // Assumes find was instantiated in onCreate
				Utils.showToast(this, R.string.deleted_from_database);
			else 
				Utils.showToast(this, R.string.delete_failed);
				     
			finish();
			break;
			
		case R.id.camera_menu_item:
			intent = new Intent("android.media.action.IMAGE_CAPTURE");
			mTempImage = null;
			ImageView iv = (ImageView) findViewById(R.id.photo); // Hide the previous image if any
			iv.setImageBitmap(null);
			intent.putExtra("rowId",mRowId);
			startActivityForResult(intent, CAMERA_ACTIVITY);
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
			Utils.showToast(this, "Error: ID must be numeric");
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
	
	private ContentValues retrieveImagesFromUris() {
		ContentValues result = new ContentValues();
		String value = "";
		if (mImageUri != null) {
			value = mImageUri.toString();
			result.put(getString(R.string.imageUriDB), value);
			value = mThumbnailUri.toString();
			result.put(getString(R.string.thumbnailUriDB),value);
		}
		return result;

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
		if (mState == STATE_EDIT)
			tView.setText(contentValues.getAsString(getString(R.string.timeDB)));
		tView = (TextView) findViewById(R.id.longitudeText);
		tView.setText(contentValues.getAsString(getString(R.string.longitudeDB)));
		tView = (TextView) findViewById(R.id.latitudeText);
		tView.setText(contentValues.getAsString(getString(R.string.latitudeDB)));
		
		ImageView iv = (ImageView) findViewById(R.id.photo);
		Uri iUri = Uri.parse(contentValues.getAsString(getString(R.string.imageUriDB)));
		iv.setImageURI(iUri);
	}

	/**
	 * Handles the barcode reader button click. 
	 * @param v is the View where the click occured.
	 */
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.idBarcodeButton:
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			startActivityForResult(intent, BARCODE_READER);
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
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(TAG, "Result code = " + resultCode);

		if (resultCode == RESULT_CANCELED)
			return;
		switch (requestCode) {
		case BARCODE_READER:
			String value = data.getStringExtra("SCAN_RESULT");
			EditText eText = (EditText) findViewById(R.id.idText);
			eText.setText(value);
			break;
		case CAMERA_ACTIVITY: 
			int rowId = data.getExtras().getInt("rowId");
			Log.i(TAG, "RowId from camera = " + rowId);
			mTempImage = (Bitmap) data.getExtras().get("data");
			Log.i(TAG, "bitmap = " + mTempImage.toString());
			displayImageInView(mTempImage);
			break;
		}
	}

	private void displayImageInView(Bitmap bm) {
		
		if (bm == null) {
			Utils.showToast(this,"Camera cancelled");
		}
		ImageView iv = (ImageView) findViewById(R.id.photo);
		iv.setImageBitmap(bm);
	}
	
	/**
	 * Saves the camera image and associated bitmap to Media storage and
	 *  save's their respective Uri's in aFind, which will save them to Db.
	 * @param aFind  the current Find we are creating or editing
	 * @param bm the bitmap from the camera
	 */
	private void saveCameraImageAndUri(Find aFind, Bitmap bm) {
		if (bm == null) {
			Log.i(TAG, "No camera images to save ...exiting ");
			return;
		}
		
		ContentValues values = new ContentValues();
		values.put(MediaColumns.TITLE, "posit image");
		values.put(ImageColumns.BUCKET_DISPLAY_NAME,"posit");
//		values.put(ImageColumns.BUCKET_ID, "posit|" + rowId);
		values.put(ImageColumns.IS_PRIVATE, 0);
		values.put(MediaColumns.MIME_TYPE, "image/jpeg");
		mImageUri = getContentResolver()
			.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		Log.i(TAG, "Saved image uri = " + mImageUri.toString());
		OutputStream outstream;
		try {
			outstream = getContentResolver().openOutputStream(mImageUri);
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
				
		int imageId = Integer.parseInt(mImageUri.toString()
				.substring(Media.EXTERNAL_CONTENT_URI.toString().length()+1));	

		Log.i(TAG, "imageId from camera = " + imageId);

		values = new ContentValues(4);
		values.put(Images.Thumbnails.KIND, Images.Thumbnails.MINI_KIND);
        values.put(Images.Thumbnails.IMAGE_ID, imageId);
        values.put(Images.Thumbnails.HEIGHT, height);
        values.put(Images.Thumbnails.WIDTH, width);
        mThumbnailUri = getContentResolver()
        	.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values);
		Log.i(TAG, "Saved thumbnail uri = " + mThumbnailUri.toString());

		try {
			outstream = getContentResolver().openOutputStream(mThumbnailUri);
			thumbnailImage.compress(Bitmap.CompressFormat.JPEG, 70, outstream);
			outstream.close();
		} catch (Exception e) {
			Log.i(TAG, "Exception during thumbnail save " + e.getMessage());
		}

		// Save the Uri's
		aFind.setImageUri(mImageUri);
		aFind.setThumbnailUri(mThumbnailUri);
	}
	
	/**
	 * Queries for images for this Find and shows them in a Gallery at the bottom of the View.
	 *  @param id is the rowId of the find
	 */
	private void displayGallery(long id) {

		// Select just those images associated with this find.
		mCursor = mFind.getImages();  // Returns the Uris of the images from the Posit Db
		Uri uri;
		if (mCursor != null) { 
			mCursor.moveToFirst();
 			ImageAdapter adapter = new ImageAdapter(mCursor, this);
			Log.i(TAG, "displayGallery(), adapter = " + adapter.getCount());

			mGallery.setAdapter(adapter);
			mGallery.setOnItemClickListener(this);
		} else 
			Utils.showToast(this, "No images to display.");
	}

	/**
	 * To detect the user clicking on the displayed images. We would like it to 
	 *  display just those images associated with that Find, but it currently 
	 *  displays all camera images.  We may have to write our own Activity to
	 *  display a single picture.
	 */
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		try {
			//
			mCursor.move(position);
      		String s = mCursor.getString(mCursor.getColumnIndexOrThrow(getString(R.string.imageUriDB)));
      		Uri uri = Uri.parse(s);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(uri);
			startActivity(intent);
		} catch (CursorIndexOutOfBoundsException e) {
			//do nothing, 
			// hack to work around this exception
		}
	}
	
	
	/**
	 * Returns the current date and time from the Calendar Instance
	 * @return a string representing the current time stamp.
	 */
	private String getDateText() {
		Calendar c = Calendar.getInstance();
		String dateString = c.get(Calendar.YEAR) + "/" + c.get(Calendar.MONTH)
				+ "/" + c.get(Calendar.DAY_OF_MONTH) + " "
				+ c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);
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
    
}