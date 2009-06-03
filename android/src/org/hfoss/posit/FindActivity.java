/**
 * 
 */
package org.hfoss.posit;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Config;
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
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author rmorelli
 *
 */
public class FindActivity extends Activity implements OnClickListener, OnItemClickListener {

	private Find mFind;
	private long mRowId;
	private int mState;
	private Cursor mCursor = null;
	private Gallery mGallery;
	private LocationManager mLocationManager;
	private Location mLocation;
	private MyLocationListener[] mLocationListeners;
	
	public static final int STATE_EDIT = 1;
	public static final int STATE_INSERT= 2;
	public static final int BARCODE_READER= 3;
	public static final int CAMERA_ACTIVITY= 4;
	public static final int SYNC_ACTIVITY= 5;
	private static final String TAG = "FindActivity";
	private static final int CONFIRM_DELETE_DIALOG = 0;

	/**
	 * 
	 */
	public FindActivity() {
		// TODO Auto-generated constructor stub
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
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
			mGallery = (Gallery)findViewById(R.id.picturesTaken);
			((Button)findViewById(R.id.idBarcodeButton)).setOnClickListener(this);
	
			if (action.equals(Intent.ACTION_EDIT)) {
				doEditAction();
			} else if (action.equals(Intent.ACTION_INSERT)) {
				doInsertAction();
			}
		}
	} // onCreate()
	
	private void doInsertAction() {
		mState = STATE_INSERT;
		TextView tView = (TextView) findViewById(R.id.timeText);
		tView.setText(getDateText());
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mLocationListeners = new MyLocationListener[2];
		mLocationListeners[0] = new MyLocationListener(this, LocationManager.GPS_PROVIDER);
		mLocationListeners[1] = new MyLocationListener(this, LocationManager.NETWORK_PROVIDER);
		mLocation = getCurrentLocation();
		startReceivingLocationUpdates(); // Throws exception if no GPS??		
	}
	
	private void doEditAction() {
		mState = STATE_EDIT;
		mRowId = getIntent().getLongExtra(MyDBHelper.KEY_ID, 0); 
		Log.i(TAG, "rowID = " + mRowId);

		// Create a find object and get its data
		mFind = new Find(this, mRowId);        
		ContentValues values = mFind.getContent();
		if (values == null) {
			Utils.showToast(this, "No values found for item " + mRowId);
			mState = STATE_INSERT;
		} else {
			putContentToView(values);
		}
		showImages();
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
 //       				fillData(); // Doesn't refresh the view.
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
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_finds_menu, menu);
		return true;
	} // onCreateOptionsMenu()
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.save_find_menu_item:
			ContentValues contentValues = getContentFromView();
			if (mState == STATE_INSERT) {
				mFind = new Find(this);
				if (mFind.insertToDB(contentValues))
					Utils.showToast(this, R.string.saved_to_database);
				else 
					Utils.showToast(this, R.string.save_failed);

			} else {  // STATE_EDIT
				if (mFind.updateToDB(contentValues))
					Utils.showToast(this, R.string.saved_to_database);
				else 
					Utils.showToast(this, R.string.save_failed);
			}
			finish();
			break;
		case R.id.discard_changes_menu_item:
			if (mState == STATE_EDIT) {
				putContentToView(mFind.getContent());
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
			startActivityForResult(intent, CAMERA_ACTIVITY);
			break;
		default:
			return false;
		}
		return true;
	} // onMenuItemSelected

	/**
	 * Takes values from View fields and puts them in a ContentValues hash table.
	 * @return The hash table.
	 */
	private ContentValues getContentFromView() {
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
	
	/**
	 * Takes values from View fields and puts them in a ContentValues hash table.
	 * @return The hash table.
	 */
	private void putContentToView(ContentValues contentValues) {
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
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.idBarcodeButton:
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			startActivityForResult(intent, BARCODE_READER);
			break;
		}		
	}


	/**
	 * This call-back method is invoked when one of the Activities started
	 *  from FindActivity, such as the BARCODE_READER or the CAMERA, finishes.
	 *  It handles the results of the Activities.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK)
			return;
		switch (requestCode) {
		case BARCODE_READER:
			String value = data.getStringExtra("SCAN_RESULT");
			EditText eText = (EditText) findViewById(R.id.idText);
			eText.setText(value);
			break;
		case CAMERA_ACTIVITY:
			if (mState == STATE_INSERT) { // this saves to the database so that we have the row Id
				ContentValues content = getContentFromView();
				mFind = new Find(this);
				mFind.insertToDB(content);
				mState = STATE_EDIT;
			}
			ImageUtils.saveImageFromCamera(this,mFind.getId(),data); //save Image to MediaStore
			showImages();
			break;
		}
	}
	
	/**
	 * Queries for images for this Find and shows at the bottom.
	 *  The images are identified by BUCKET_ID='posit' AND 
	 *  BUCKET_DISPLAY_NAME='posit##'
	 */
	private void showImages() {
		// Select just those images associated with this find.
		 String imageId = ImageColumns.BUCKET_ID + "=\"posit\" " 
		 	+ " AND "
			+ ImageColumns.BUCKET_DISPLAY_NAME+"=\"posit|"
			+ mRowId + "\""
			;
		 
//		 Utils.showToast(this, "This image = " + imageId);

		 if (mCursor==null) {
			 Log.i(TAG, "Building new imagesQuery");

			 mCursor = managedQuery (MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        		new String[]{ BaseColumns._ID, ImageColumns.BUCKET_ID},
        		ImageColumns.BUCKET_ID+"=\"posit\" AND "
        		+ImageColumns.BUCKET_DISPLAY_NAME+"=\"posit|"+mRowId+"\"", null,null);


		 }else {
			 mCursor.requery();
		 }
		 
		 if (mCursor != null) { 
			 mGallery.setAdapter(new ImageAdapter(mCursor,this));
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
			long id = mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID));
			//create the Uri for the Image 
			Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id+"");
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(uri);
			startActivity(intent);
		} catch (CursorIndexOutOfBoundsException e) {
			//do nothing, 
			// hack to work around this exception
		}
	}
	
	
	/**
	 * Gets the current date and time from the Calendar Instance
	 */
	private String getDateText() {
		Calendar c = Calendar.getInstance();
		String dateString = c.get(Calendar.YEAR) + "/" + c.get(Calendar.MONTH)
				+ "/" + c.get(Calendar.DAY_OF_MONTH) + " "
				+ c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);
		return dateString;
	}
	
	private void startReceivingLocationUpdates() {
		if (mLocationManager != null) {
			try {
				mLocationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 1000, 0F,
						mLocationListeners[1]);
			} catch (java.lang.SecurityException ex) {
				// ok
			} catch (IllegalArgumentException ex) {
				if (Config.LOGD) {
					Log.d(TAG, "provider does not exist " + ex.getMessage());
				}
			}
			try {
				mLocationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 1000, 0F,
						mLocationListeners[0]);
			} catch (java.lang.SecurityException ex) {
				// ok
			} catch (IllegalArgumentException ex) {
				if (Config.LOGD) {
					Log.d(TAG, "provider does not exist " + ex.getMessage());
				}
			}
		}
	}

	private void stopReceivingLocationUpdates() {
		if (mLocationManager != null) {
			for (int i = 0; i < mLocationListeners.length; i++) {
				try {
					mLocationManager.removeUpdates(mLocationListeners[i]);
				} catch (Exception ex) {
					// ok
				}
			}
		}
	}

	/**
	 * This method scans the LocationListeners trying to determine the 
	 *  phone's location. This can be simulated on the emulator in DDMS 
	 *  perspective (in Eclipse) by setting a <long,lat> in the Emulator's
	 *  Location Control.
	 * @return
	 */	
	private Location getCurrentLocation() {
		Location l = null;

		// go in best to worst order
		for (int i = 0; i < mLocationListeners.length; i++) {
			l = mLocationListeners[i].current();
			if (l != null)
				break;
		}

		return l;
	}


	
}