package org.hfoss.posit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class NewFind extends Activity {
	private static final String APP = "NewFind";
	private static final int ACTIVITY_CREATE = 0;

	private EditText mDescription;
	private EditText mLatitude;
	private EditText mLongitude;
	private DBHelper mDbHelper;
	private Location location;
	private Long mRowId = 1L;
	private String latitude = null;
	private String longitude = null;

	public NewFind() {
	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		mDbHelper = new DBHelper(this);
		mDbHelper.open();
		setContentView(R.layout.new_find);

		mDescription = (EditText) findViewById(R.id.description);
		mLatitude = (EditText) findViewById(R.id.latitude);
		mLongitude = (EditText) findViewById(R.id.longitude);
		// Set current location
		try {
			location = ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
					.getCurrentLocation("gps");
		} catch (NullPointerException e) {/*if there's no device, we get an error, so gracefully,showing an error and giving option*/
			Log.e(APP, "Error getting GPS data");
			new AlertDialog.Builder(NewFind.this)
					.setTitle("Device Missing")
					.setMessage(
							"I can't find a GPS device.\n Do you still want to continue?")
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									((EditText) findViewById(R.id.latitude))
											.setText("null");
									((EditText) findViewById(R.id.longitude))
											.setText("null");
								}
							}).setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									finish();
								}
							}).show();
		}

		if (location != null) { /*if we have a location, put it in*/
			((EditText) findViewById(R.id.latitude)).setText(""
					+ location.getLatitude());
			((EditText) findViewById(R.id.longitude)).setText(""
					+ location.getLongitude());
		}

		// Check whether this is a new or existing record.
		mRowId = icicle != null ? icicle.getLong(DBHelper.KEY_ROWID) : null;
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(DBHelper.KEY_ROWID) : null;
		}

		populateFields();

		Button save = (Button) findViewById(R.id.confirm);
		save.setOnClickListener(saveItListener);

	/*	Button cancel = (Button) findViewById(R.id.dismiss);
		cancel.setOnClickListener(cancelListener);
*/
		Button mapIt = (Button) findViewById(R.id.mapit);
		mapIt.setOnClickListener(mapItListener);

		Button takeAPic = (Button) findViewById(R.id.takepic);
		takeAPic.setOnClickListener(cameraListener);
	}

	private void populateFields() {
		if (mRowId != null) {
			Cursor record = mDbHelper.fetchRow(mRowId);
			startManagingCursor(record);
			mDescription.setText(record.getString(record
					.getColumnIndex(DBHelper.KEY_DESCRIPTION)));
			mLongitude.setText(record.getString(record
					.getColumnIndex(DBHelper.KEY_LONGITUDE)));
			mLatitude.setText(record.getString(record
					.getColumnIndex(DBHelper.KEY_LATITUDE)));
		}
	}

	@Override
	protected void onFreeze(Bundle outState) {
		super.onFreeze(outState);
		outState.putLong(DBHelper.KEY_ROWID, mRowId);
	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
	}

	private void savetoDB() {
		String description = mDescription.getText().toString();
		longitude = mLongitude.getText().toString();
		latitude = mLatitude.getText().toString();

		if (mRowId == null) {
			long id = mDbHelper.createRow(description, longitude, latitude);
			if (id > 0) {
				mRowId = id;
			}
		} else {
			mDbHelper.updateRow(mRowId, description, longitude, latitude);
		}
	}

	/**
	 * Listener for the mapIt button
	 * Calls the map activity and records the point
	 */
	private OnClickListener mapItListener = new OnClickListener() {
		public void onClick(View v) {
			recordPoint();
		}
	};
	private OnClickListener saveItListener = new OnClickListener() {
		public void onClick(View view) {
			setResult(RESULT_OK);
			savetoDB();
			finish();
		}
	};
	private OnClickListener cameraListener = new OnClickListener() {
		public void onClick(View v) {
			takePicture();
		}
	};
	private OnClickListener cancelListener = new OnClickListener() {
		public void onClick(View v) {
			setResult(RESULT_CANCELED);
			finish();
		}
	};

	private void takePicture() {
		Intent i = new Intent(this, Camera.class);
		startSubActivity(i, ACTIVITY_CREATE);
	}

	private void recordPoint() {
		// TODO: Open the mapper and get the values.
		//Essentially to let the user double check the value.
		savetoDB();
		Intent i = new Intent(this, Mapper.class);
		i.putExtra(DBHelper.KEY_LATITUDE, latitude);
		i.putExtra(DBHelper.KEY_LONGITUDE, longitude);

		startSubActivity(i, 1);

		// I want to take the value of latitude and longitude...
		// maybe let the user view the scenario before committing to the db.

	}

}
