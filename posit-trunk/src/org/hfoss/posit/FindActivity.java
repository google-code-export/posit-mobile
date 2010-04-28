/*
 * File: FindActivity.java
 * 
 * Copyright (C) 2009 The Humanitarian FOSS Project (http://www.hfoss.org)
 * 
 * This file is part of POSIT, Portable Open Search and Identification Tool.
 *
 * POSIT is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) as published 
 * by the Free Software Foundation; either version 3.0 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU LGPL along with this program; 
 * if not visit http://www.gnu.org/licenses/lgpl.html.
 * 
 */
package org.hfoss.posit;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.hfoss.posit.adhoc.RWGService;
import org.hfoss.posit.provider.PositDbHelper;
import org.hfoss.posit.utilities.ImageAdapter;
import org.hfoss.posit.utilities.Utils;
import org.hfoss.third.Base64Coder;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
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
 * Handles both adding new finds and editing existing finds.  
 * Includes adding and editing name, description, and barcode_id.  Also allows user
 * to attach photos to the find, as well as delete everything.
 * 
 */
public class FindActivity extends Activity 
implements OnClickListener, OnItemClickListener, LocationListener {

	private Find mFind;
	private long mFindId;
	private String mFindGuId = null;
	private int mState;
	private Gallery mGallery;
	private static boolean NEWFIND=true;
	
	
	private String imageBase64String = null;
	private boolean stopThread;
	
	//Temporary files representing pictures taken for a find
	//but not yet added to the database
	private ArrayList<Bitmap> mTempBitmaps = new ArrayList<Bitmap>();
	private ArrayList<ContentValues> mImagesData = new ArrayList<ContentValues>();

	//Uris of new images and thumbnails being attached to the find
	private List<Uri> mNewImageUris = new LinkedList<Uri>();
	private List<Uri> mNewImageThumbnailUris = new LinkedList<Uri>();

	private double mLongitude = 0;
	private double mLatitude = 0;

	private TextView mLatitudeTextView;
	private TextView mLongitudeTextView;

	private Thread mThread;
	private LocationManager mLocationManager;

	private String valueName;
	private String valueDescription;
	private String valueId;

	private boolean isClean = true;
	public static boolean SAVE_CHECK=false;
	public static int PROJECT_ID;
//	private static boolean IS_ADHOC = false;

	public int INTENT_CHECK=0;// anybody finds more suitable ways please change it 

	public static final int STATE_EDIT = 1;
	public static final int STATE_INSERT= 2;
	public static final int BARCODE_READER= 3;
	public static final int CAMERA_ACTIVITY= 4;
	public static final int NEW_FIND_CAMERA_ACTIVITY = 7;
	public static final int SYNC_ACTIVITY= 12;
	public static final int IMAGE_VIEW = 13;

	private static final String TAG = "FindActivity";
	private static final int CONFIRM_DELETE_DIALOG = 0;
	private static final int UPDATE_LOCATION = 2;
	private static final int CONFIRM_EXIT=3;
	private static final boolean ENABLED_ONLY = true;
	private static final int THUMBNAIL_TARGET_SIZE = 320;
	/* Listener for checking if the text has changed in any fields */
	private TextWatcher textChangedWatcher= new TextWatcher(){
		public void afterTextChanged(Editable s){
			SAVE_CHECK=true;
			Log.i(TAG, "Text Changed called");
			}

		public void beforeTextChanged(CharSequence arg0,
				int arg1, int arg2, int arg3) {
				//needed by the class
			
		}

		public void onTextChanged(CharSequence s, int start,
				int before, int count) {
			//needed by the class
		}};
	/**
	 * Handles GPS updates.  
	 * Source: Android tutorials
	 * @see http://www.androidph.com/2009/02/app-10-beer-radar.html
	 */
	Handler updateHandler = new Handler() {

		/** Gets called on every message that is received */
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
	 * inserting new finds in the DB, editing or deleting existing finds, 
	 * and attaching images to the finds
	 * @param savedInstanceState (not currently used) is to restore state.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//finishActivity(ListFindsActivity.FIND_FROM_LIST);

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		PROJECT_ID = sp.getInt("PROJECT_ID", 0);
//		IS_ADHOC = sp.getBoolean("IS_ADHOC", false);
		
		isClean = true;

		final Intent intent = getIntent();
		String action = intent.getAction();

		setContentView(R.layout.add_find);
		mLatitudeTextView = (TextView)findViewById(R.id.latitudeText);
		mLongitudeTextView = (TextView)findViewById(R.id.longitudeText);

		mGallery = (Gallery)findViewById(R.id.picturesTaken);

		Button scanButton = (Button)findViewById(R.id.idBarcodeButton);
		scanButton.setOnClickListener(this);
		TextView barcodeError = (TextView)findViewById(R.id.barcodeReaderError);
		Button barcodeDownload = (Button)findViewById(R.id.barcodeDownloadButton);
		TextView barcodeRestart = (TextView)findViewById(R.id.barcodeReaderRestart);
		barcodeDownload.setOnClickListener(this);
		barcodeError.setVisibility(TextView.GONE);
		barcodeDownload.setVisibility(Button.GONE);
		barcodeRestart.setVisibility(TextView.GONE);
		if(!isIntentAvailable(this,"com.google.zxing.client.android.SCAN")) {
			scanButton.setClickable(false);
			barcodeError.setVisibility(TextView.VISIBLE);
			barcodeDownload.setVisibility(Button.VISIBLE);
			barcodeRestart.setVisibility(TextView.VISIBLE);
		}
		if (action.equals(Intent.ACTION_EDIT)) {
			doEditAction();
			INTENT_CHECK=1;
		} else if (action.equals(Intent.ACTION_INSERT)) {
			doInsertAction();

		}
	} // onCreate()

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		mTempBitmaps = savedInstanceState.getParcelableArrayList("bitmaps");
		isClean = savedInstanceState.getBoolean("isclean");
		displayGallery(mFindId);
//		displayGallery(mFindGuId==null);  // New find
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("isclean", isClean);
		outState.putParcelableArrayList("bitmaps", mTempBitmaps);
		super.onSaveInstanceState(outState);
	}

	/**
	 * This method is used to check whether or not the user has an intent
	 * available before an activity is actually started.  This is only 
	 * invoked on the Find view to check whether or not the intent for
	 * the barcode scanner is available.  Since the barcode scanner requires
	 * a downloadable dependency, the user will not be allowed to click the 
	 * "Read Barcode" button unless the phone is able to do so.
	 * @param context
	 * @param action
	 * @return
	 */
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
	public void doInsertAction() {
		mState = STATE_INSERT;
		Log.i(TAG, "doInsertAction");
		TextView tView = (TextView) findViewById(R.id.timeText);
		tView.setText(getDateText());
		tView.addTextChangedListener(textChangedWatcher);
		TextView idView = (TextView) findViewById(R.id.idText);
		idView.setText("");
		idView.addTextChangedListener(textChangedWatcher);
		TextView nameView = (TextView) findViewById(R.id.nameText);
		nameView.setText("");
		nameView.addTextChangedListener(textChangedWatcher);
		TextView descView = (TextView) findViewById(R.id.descriptionText);
		descView.setText("");
		descView.addTextChangedListener(textChangedWatcher);
		initializeLocationAndStartGpsThread();
	}

	/**
	 * Sets the Find's location to the last known location and starts 
	 *  a separate thread to update GPS location.
	 */
	private void initializeLocationAndStartGpsThread() {
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		List<String> providers = mLocationManager.getProviders(ENABLED_ONLY);
		if(Utils.debug)
			Log.i(TAG, "Enabled providers = " + providers.toString());
		String provider = mLocationManager.getBestProvider(new Criteria(),ENABLED_ONLY);
		if(Utils.debug)
			Log.i(TAG, "Best provider = " + provider);

		setCurrentGpsLocation(null);   
		mThread = new Thread(new MyThreadRunner());
		stopThread = false;
		mThread.start();
	}

	/**
	 * Repeatedly attempts to update the Find's location.
	 */
	class MyThreadRunner implements Runnable {
		public void run() {
			while (!stopThread) {
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
		mFindId = getIntent().getLongExtra(PositDbHelper.FINDS_ID, 0); 
		Log.i(TAG,"Find id = " + mFindId);

		// Instantiate a find object and retrieve its data from the DB
		mFind = new Find(this, mFindId);   

		ContentValues values = mFind.getContent();
		if (values == null) {
			Utils.showToast(this, "No values found for Find " + mFindId);
			mState = STATE_INSERT;
		} else {
			mFind.setGuid(values.getAsString(PositDbHelper.FINDS_GUID));
			displayContentInView(values);  
		}
		
		
		displayGallery(mFindId);
		
	}


	@Override
	protected void onResume() {
		super.onResume();
	}

	
	@Override
	protected void  onPause(){
		super.onPause();
		//finishActivity(ListFindsActivity.FIND_FROM_LIST);
	}	

	@Override
	protected void onStop() {
		super.onStop();
		stopThread = true;
//		mDbHelper.close();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
//		mDbHelper.close();
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
						finish();
					}	else 
						Utils.showToast(FindActivity.this, R.string.delete_failed);
				}
			}
			)
			.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					/* User clicked Cancel so do nothing */
				}
			})
			.create();

		case CONFIRM_EXIT:
			Log.i(TAG, "CONFIRM_EXIT dialog");
			return new AlertDialog.Builder(this)
			.setIcon(R.drawable.alert_dialog_icon)
			.setTitle(R.string.check_saving)
			.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Log.i(TAG, "CONFIRM_EXIT setOK onClick");
					// User clicked OK so do some stuff 
					ContentValues contentValues = retrieveContentFromView();
					doSave(contentValues);
				}
			})
			.setNeutralButton(R.string.closing, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Log.i(TAG, "CONFIRM_EXIT setNeutral onClick");
					finish();
				}
			})
			.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Log.i(TAG, "CONFIRM_EXIT setCancel onClick");
					/* User clicked Cancel so do nothing */
				}
			})
			.create();
		default:
			return null;
		} // switch
	}

	/**
	 * Utility method to handle saving of the Find invoked from either 
	 *  a menu selection or a alert dialog.
	 */
	private void doSave(ContentValues contentValues) {
		String guid = contentValues.getAsString(PositDbHelper.FINDS_GUID);
		Log.i(TAG, "doSave, values= " + contentValues.toString());
		if(guid == null || guid.equals("")) {
			Log.i(TAG, "doSave, ERROR: null content values");
			return;
		}
		if (mState == STATE_INSERT) {            // if this is a new find
			mFind = new Find(FindActivity.this, guid);
			List<ContentValues> imageValues = Utils.saveImagesAndUris(this, mTempBitmaps);
			
			if (mFind.insertToDB(contentValues, imageValues)) {//insert find into database
				Utils.showToast(FindActivity.this, R.string.saved_to_database);
				// Is this correct, shouldn't we be setting the _id based on the result
				// of the insertion?
				mFind.setGuid(contentValues.getAsString(PositDbHelper.FINDS_GUID));
				Log.i(TAG, "doSave, id= " + mFind.getguId());
			} else {
				Utils.showToast(FindActivity.this, R.string.save_failed);
			}
		} else { 
			if (mFind.updateToDB(contentValues)) {
				Utils.showToast(FindActivity.this, R.string.saved_to_database);
			} else {
				Utils.showToast(FindActivity.this, R.string.save_failed);
			}
		}
		finish();
	}
	

	
	
	
	/**
	 * This method is used to close the current find activity
	 * when the back button is hit.  We ran into problems with
	 * activities running on top of each other and not finishing
	 * and this helps close old activities.
	 * @param keyCode is an integer representing which key is pressed
	 * @param event is a KeyEvent that is not used here
	 * @return a boolean telling whether or not the operation was successful
	 */
	
	
	/*
	 * TODO: This needs to be fixed. It doesn't work properly.
	 * 
	 * Here I use an Integer INTENT_CHECK to check whether the the action is insert or edit.
	 * if the action is edit, the OnKeyDown method will utilize the checkSave method to 
	 * check weather the database has been changed or not
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.i(TAG,"onKeyDown " + keyCode + " " + KeyEvent.KEYCODE_BACK);
//		if (INTENT_CHECK==1) {
//			checkSave();
//		} 

		if(keyCode == KeyEvent.KEYCODE_BACK && SAVE_CHECK == true) {
//		if(keyCode == KeyEvent.KEYCODE_BACK) {
			showDialog(CONFIRM_EXIT);
			return true;
		}
		return super.onKeyDown(keyCode, event);
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
			long start;
			Log.i("start",(start=System.currentTimeMillis())+"");
			ContentValues contentValues = retrieveContentFromView();
			Log.i("after retrive", (System.currentTimeMillis()-start)+"");
			//if (IS_ADHOC)
			if (RWGService.isRunning())
				sendAdhocFind(contentValues,null);//imageBase64String);
			Log.i("after adhoc check", (System.currentTimeMillis()-start)+"");
			
			doSave(contentValues);
			//Intent in = new Intent(this, ListFindsActivity.class); //redirect to list finds
			//startActivity(in);
			
			break;

		case R.id.discard_changes_menu_item:
			if (mState == STATE_EDIT) {
				displayContentInView(mFind.getContent());
			} else {
				mTempBitmaps.clear();
				onCreate(null);
			}
			break;	

		case R.id.camera_menu_item:
			intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra("rowId", mFindId);
			if (mFind == null) {
				Log.i(TAG,"New Find " + mFindId);
				startActivityForResult(intent, NEW_FIND_CAMERA_ACTIVITY); //camera for new find
			} else {
				Log.i(TAG, "Existing FInd " + mFindId);
				startActivityForResult(intent, CAMERA_ACTIVITY); //camera for existing find
			}
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
	 * Used with RWG algorithm to transmit finds peer-to-peer through an ad-hoc
	 * network.  
	 * @param contentValues
	 */
	private void sendAdhocFind(ContentValues contentValues, String image) {
		Utils.showToast(this, "sending ad hoc find");
		
		String longitude = contentValues.getAsString(getString(R.string.longitudeDB));
		String latitude = contentValues.getAsString(getString(R.string.latitudeDB));
//		long findId = contentValues.getAsLong(getString(R.string.idDB));
		String findId = contentValues.getAsString(getString(R.string.idDB));
		String name = contentValues.getAsString(getString(R.string.nameDB));
		String description = contentValues.getAsString(getString(R.string.descriptionDB));
		
//		Log.i("Adhoc", "Adhoc find: "+ new Long(findId).toString()+ ":"+ longitude+ ","+ latitude);
		Log.i("Adhoc", "Adhoc find: " + findId + ":"+ longitude+ ","+ latitude);
		
		JSONObject obj = new JSONObject();
		try {
			obj.put("findLong", longitude);
			obj.put("findLat", latitude);
			obj.put("findId", findId);
			obj.put("name", name);
			obj.put("description", description);
			obj.put("projectId", PROJECT_ID);
			if(image!=null)
				obj.put("image",image);
		} catch (JSONException e) {
			Log.e("JSONError", e.toString());
		}
		Log.i("Adhoc", "Sending:"+ obj.toString());
		
		/*if(AdhocClientActivity.adhocClient!=null)
			AdhocClientActivity.adhocClient.send(obj.toString());
		else if(PositMain.mAdhocClient!=null)
			PositMain.mAdhocClient.send(obj.toString());*/
		RWGService.send(obj.toString());
	}


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
		result.put(PositDbHelper.FINDS_NAME, value);
		eText = (EditText) findViewById(R.id.descriptionText);
		value = eText.getText().toString();
		result.put(PositDbHelper.FINDS_DESCRIPTION, value);
		eText = (EditText) findViewById(R.id.idText);
		value = eText.getText().toString();
		result.put(PositDbHelper.FINDS_GUID, value);         // guid 

		TextView tView = (TextView) findViewById(R.id.longitudeText);
		value = tView.getText().toString();
		result.put(PositDbHelper.FINDS_LONGITUDE, value);
		tView = (TextView) findViewById(R.id.latitudeText);
		value = tView.getText().toString();
		result.put(PositDbHelper.FINDS_LATITUDE, value);
		tView = (TextView) findViewById(R.id.timeText);
		value = tView.getText().toString();
//		result.put(PositDbHelper.FINDS_TIME, value);  // Timestamp added by Db
		
		// Mark the find unsynced
		result.put(PositDbHelper.FINDS_SYNCED,PositDbHelper.FIND_NOT_SYNCED);
		//Add project id and the revision number (revision is autoincrement)
		result.put(PositDbHelper.FINDS_PROJECT_ID, PROJECT_ID);
		return result;
	}


	/**
	 * Retrieves values from a ContentValues has table and puts them in the View.
	 * @param contentValues stores <key, value> pairs
	 */
	private void displayContentInView(ContentValues contentValues) {
		EditText eText = (EditText) findViewById(R.id.nameText);
		eText.setText(contentValues.getAsString(PositDbHelper.FINDS_NAME));
		valueName=eText.getText().toString();
		eText = (EditText) findViewById(R.id.descriptionText);
		eText.setText(contentValues.getAsString(PositDbHelper.FINDS_DESCRIPTION));
		valueDescription=eText.getText().toString();
		eText = (EditText) findViewById(R.id.idText);
		eText.setText(contentValues.getAsString(PositDbHelper.FINDS_GUID));
		eText.setFocusable(false);
		valueId=eText.getText().toString();
		TextView tView = (TextView) findViewById(R.id.timeText);

		if (mState == STATE_EDIT) {
			tView.setText(contentValues.getAsString(PositDbHelper.FINDS_TIME));
		}
		tView = (TextView) findViewById(R.id.longitudeText);
		tView.setText(contentValues.getAsString(PositDbHelper.FINDS_LONGITUDE));
		tView = (TextView) findViewById(R.id.latitudeText);
		tView.setText(contentValues.getAsString(PositDbHelper.FINDS_LATITUDE));
	}

	/**
	 * Handles the barcode reader button clicks. 
	 * @param v is the View where the click occurred.
	 */
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {

		case R.id.idBarcodeButton:
			intent = new Intent("com.google.zxing.client.android.SCAN");
			try {
				startActivityForResult(intent, BARCODE_READER);
			} catch(ActivityNotFoundException e) {
				Log.e(TAG, e.toString());
			}
			break;
		case R.id.barcodeDownloadButton:
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("market://search?q=pname:com.google.zxing.client.android"));
			startActivity(intent);
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
		int rowId;

		if (resultCode == RESULT_CANCELED) {
			return;
		}

		switch (requestCode) {

		case BARCODE_READER:
			String value = data.getStringExtra("SCAN_RESULT");
			EditText eText = (EditText) findViewById(R.id.idText);
			eText.setText(value);
			break;
		case CAMERA_ACTIVITY: //for existing find: saves image to db when user clicks "attach"
			rowId = data.getIntExtra("rowId", -1);
			Bitmap tempImage = (Bitmap) data.getExtras().get("data");
			
			mTempBitmaps.add(tempImage);
			
			List<ContentValues> imageValues = Utils.saveImagesAndUris(this, mTempBitmaps);
		
			if (mFind.insertImagesToDB(imageValues)) {
				Utils.showToast(this, R.string.saved_image_to_db);
			} else { 
				Utils.showToast(this, R.string.save_failed);
			}
			Log.i(TAG, "onActivityResult, inserted images to DB");
			displayGallery(mFindId);
			mTempBitmaps.clear();
			break;

		case NEW_FIND_CAMERA_ACTIVITY: //for new finds: stores temporary images in a list
			rowId = data.getIntExtra("rowId", -1);
			tempImage = (Bitmap) data.getExtras().get("data");
			
			//ByteArrayOutputStream baos = new ByteArrayOutputStream();  
			//tempImage.compress(Bitmap.CompressFormat.JPEG, 80, baos);  
			//imageBase64String = new String(baos.toByteArray()); 
			mTempBitmaps.add(tempImage);
			displayGallery(mFindId);
			break;

		case IMAGE_VIEW:
			Log.i(TAG, "onActivityResult mFindId = " + mFindId);
			finish();
			break;
		}
	}
	
	/**
	 * Queries for images for this Find and shows them in a Gallery at the bottom of the View.
	 *  @param id is the rowId of the find
	 */
	private void displayGallery(long id) {
		Log.i(TAG, "displayGallery mFindId=" + id);
		if (id != 0) { //for existing finds
			// Select just those images associated with this find.
			mImagesData = mFind.getImagesContentValuesList();
			if (mImagesData.size() > 0) {
				finishActivity(FindActivity.IMAGE_VIEW);
				ImageAdapter adapter = new ImageAdapter(mImagesData, this);
				mGallery.setAdapter(adapter);
				mGallery.setOnItemClickListener(this);
			} else {
				Utils.showToast(this, "No images to display.");
			}
			
		} else { //for new finds
			if (mTempBitmaps.size() > 0) {
				finishActivity(FindActivity.IMAGE_VIEW);
				ImageAdapter adapter = new ImageAdapter(this, mTempBitmaps);
				mGallery.setAdapter(adapter);
				mGallery.setOnItemClickListener(this);
			}
		}
	}

	/**
	 * To detect the user clicking on the displayed images. Displays all pictures 
	 * attached to this find by creating a new activity that shows
	 */
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		if (mFind != null) {
			Log.i(TAG, "onItemClick starting ImageViewActivity for existing find");
			try {
				String s = mImagesData.get(position).getAsString(PositDbHelper.PHOTOS_IMAGE_URI);
				if (s != null) {
					Uri uri = Uri.parse(s);
					Intent intent = new Intent(Intent.ACTION_VIEW, uri, this, ImageViewActivity.class);
					intent.putExtra("position",position);
					intent.putExtra("findId", mFindId);
					Log.i(TAG, "onItemClick mFindId = " + mFindId);
					setResult(RESULT_OK,intent);
					startActivityForResult(intent, IMAGE_VIEW);
				}
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		} else {
			Log.i(TAG, "onItemClick starting ImageViewActivity for new find");
			Bitmap bm = mTempBitmaps.get(position);
			Intent intent = new Intent(this, ImageViewActivity.class);
			intent.putExtra("position",position);
			intent.putExtra("findId", mFindId);
			intent.putExtra("bitmap", bm);
			startActivity(intent);
		}
	}

	/**
	 * Returns the current date and time from the Calendar Instance
	 * @return a string representing the current time stamp.
	 */
	private String getDateText() {
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		return ts.toString();
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
		if(Utils.debug)
			Log.i(TAG, provider + " disabled");
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
			if(Utils.debug)
				Log.i(TAG, "Enabled providers = " + providers.toString());
			bestProvider = mLocationManager.getBestProvider(new Criteria(),ENABLED_ONLY);
			if (bestProvider != null && bestProvider.length() != 0) {
				mLocationManager.requestLocationUpdates(bestProvider, 30000, 0, this);	 // Every 30000 millisecs	
				location = mLocationManager.getLastKnownLocation(bestProvider);				
			}	
		}
		if(Utils.debug)
			Log.i(TAG, "Best provider = |" + bestProvider + "|");

		try {
			mLongitude = location.getLongitude();
			mLatitude = location.getLatitude();
			Message msg = Message.obtain();
			msg.what = UPDATE_LOCATION;
			this.updateHandler.sendMessage(msg);
		} catch (NullPointerException e) {
				mLongitude = mLatitude = 0;   // In case no network and no GPS
			  	Log.e(TAG, e.toString());
				e.printStackTrace();
		}
	}
}