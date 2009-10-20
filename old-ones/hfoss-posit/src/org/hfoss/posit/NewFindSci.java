package org.hfoss.posit;

import java.util.Calendar;
import java.util.UUID;

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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

public class NewFindSci extends Activity{
	/* Declarations here */
	
	private TextView latitudeText,
					 longitudeText,
					 numberText,
					 dateText;
	private EditText  descriptionText;
	
	private Spinner typeSpinner;
	private ImageButton mapButton,
				   cameraButton,
				   saveButton,
				   viewGalleryButton;
	private DBHelper mDbHelper;
	private Location location;
	private Long mRowId = 1L;
	private String latitude = null,
				   longitude = null,
				   description = null,
	               APP = "NewFindSci";
	private static final int ACTIVITY_CREATE = 1;
	/*Done */
	
	public NewFindSci()
	{
	}
	
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		
		mDbHelper = new DBHelper(this);
		mDbHelper.open();
		setContentView(R.layout.new_find_sci_2);
		mapViewItems();
		fillTypes();
		getLocation();
		
		// Check whether this is a new or existing record.
		mRowId = icicle != null ? icicle.getLong(DBHelper.KEY_ROWID) : null;
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(DBHelper.KEY_ROWID) : null;
			
		}
		if (mRowId != null){
			populateFields();
		}else {
			setNumberText();
		}
		setDateText();
		setListeners();
	}
	
	private void setNumberText(){
		String[] projection = { DBHelper.KEY_ROWID};
		Cursor myCursor = mDbHelper.fetchSelectedColumns(projection);
		int RowCount = myCursor.count()+1;
		numberText.setText(""+RowCount);
	}
	
	private void setDateText()
	{
		Calendar c = Calendar.getInstance();
		String dateString=c.get(Calendar.YEAR)+"/"+
		c.get(Calendar.MONTH)+"/"+
		c.get(Calendar.DAY_OF_MONTH)+" "+
		c.get(Calendar.HOUR_OF_DAY)+":"+
		c.get(Calendar.MINUTE);
		dateText.setText(dateString);
	}
	
	private void setListeners(){
		saveButton.setOnClickListener(saveButtonListener);
		mapButton.setOnClickListener(mapButtonListener);
		cameraButton.setOnClickListener(cameraButtonListener);
	}
	

	
	private void savetoDB() {
		description = descriptionText.getText().toString();
		longitude = longitudeText.getText().toString();
		latitude = latitudeText.getText().toString();

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
	private OnClickListener mapButtonListener = new OnClickListener() {
		public void onClick(View v) {
			recordPoint();
		}
	};
	private OnClickListener saveButtonListener = new OnClickListener() {
		public void onClick(View view) {
			setResult(RESULT_OK);
			savetoDB();
			finish();
		}
	};
	private OnClickListener cameraButtonListener = new OnClickListener() {
		public void onClick(View v) {
			takePicture();
		}
	};
	
	
	private void takePicture() {
		Intent i = new Intent(this, CameraNew.class);
		i.putExtra(DBHelper.KEY_ROWID, mRowId);
		savetoDB();
		Log.i(APP,mRowId+"");
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
	
	private void populateFields()
	{
		Cursor record = mDbHelper.fetchRow(mRowId);
		startManagingCursor(record);
		numberText.setText(record.getString(record.getColumnIndex(
				DBHelper.KEY_ROWID)));
		descriptionText.setText(record.getString(record
				.getColumnIndex(DBHelper.KEY_DESCRIPTION)));
		latitudeText.setText(record.getString(record
				.getColumnIndex(DBHelper.KEY_LATITUDE)));
		longitudeText.setText(record.getString(record
				.getColumnIndex(DBHelper.KEY_LONGITUDE)));
	}
	/**
	 * Maps individual items to the ids in xml file new_find_sci_2
	 */
	private void mapViewItems()
	{
		latitudeText = (TextView)findViewById(R.id.latitudeText);
		longitudeText = (TextView)findViewById(R.id.longitudeText);
		numberText = (TextView)findViewById(R.id.numberText);
		descriptionText = (EditText)findViewById(R.id.descriptionText);
		dateText =(TextView)findViewById(R.id.dateText);
		typeSpinner = (Spinner)findViewById(R.id.typeSpinner);
		mapButton = (ImageButton)findViewById(R.id.mapButton);
		cameraButton = (ImageButton) findViewById(R.id.cameraButton);
		saveButton = (ImageButton)findViewById(R.id.saveButton);
		viewGalleryButton = (ImageButton) findViewById(R.id.viewGalleryButton);
		viewGalleryButton.setVisibility(View.INVISIBLE);
		
	}
	
	
	private void fillTypes(){
		 ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	                this, R.array.bird_types , android.R.layout.simple_spinner_item);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        typeSpinner.setAdapter(adapter);
	}
	
	private void getLocation() {
		try {
			location = ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
					.getCurrentLocation("gps");
		} catch (NullPointerException e) {/*
											 * if there's no device, we get an
											 * error, so gracefully,showing an
											 * error and giving option
											 */
			Log.e(APP, "Error getting GPS data");
			new AlertDialog.Builder(NewFindSci.this)
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

		if (location != null) { /* if we have a location, put it in */
			latitudeText.setText(""
					+ location.getLatitude());
			longitudeText.setText(""
					+ location.getLongitude());
		}
	}
}
