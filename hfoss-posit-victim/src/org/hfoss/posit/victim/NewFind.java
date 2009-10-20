package org.hfoss.posit.victim;

import java.util.Calendar;

import org.hfoss.posit.util.SpinnerBindings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class NewFind extends Activity {
	/* Declarations here */

	private TextView latitudeText, longitudeText, numberText, dateText;
	private EditText descriptionText,identifierText,ageText,nameText;
	private CheckBox taggedCheckBox;
	private RadioGroup sexRadioGroup;
	private RadioButton maleRadioButton, femaleRadioButton;
//	private Spinner nameSpinner;
	private Button addNameButton;
	private ImageButton mapButton, cameraButton, saveButton, viewGalleryButton;
	private DBHelper mDbHelper;
	private Location location;
	private Long mRowId = 1L;
	private String latitude = null, longitude = null, description = null;
	private final String APP = "NewFind";
	private ArrayAdapter<String> adapter;
	private static final int ACTIVITY_CREATE = 0;
	private String newItemName=null; /*The name of the current item set by the add button later*/
	/*Done */

	public NewFind() {
	}

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		mDbHelper = new DBHelper(this);
		mDbHelper.open();
		setContentView(R.layout.new_find_victims);
		mapViewItems();
		//fillTypes();
		

		// Check whether this is a new or existing record.
		mRowId = icicle != null ? icicle.getLong(DBHelper.KEY_ROWID) : null;
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(DBHelper.KEY_ROWID) : null;

		}
		if (mRowId != null) {
			populateFields();
		} else {
			setNumberText();
			setDateText();
			getLocation();
			
		}
		
		setListeners();
	}
	
	private void setNumberText() {
		String[] projection = { DBHelper.KEY_ROWID };
		Cursor myCursor = mDbHelper.fetchSelectedColumns(projection);
		int RowCount = myCursor.count() + 1;
		numberText.setText("" + RowCount);
	}
	
	private void setDateText(String dateString){
		dateText.setText(dateString);
	}
	
	private void setDateText() {
		Calendar c = Calendar.getInstance();
		String dateString = c.get(Calendar.YEAR) + "/" + c.get(Calendar.MONTH)
				+ "/" + c.get(Calendar.DAY_OF_MONTH) + " "
				+ c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);
		dateText.setText(dateString);
	}

	private void setListeners() {
		saveButton.setOnClickListener(saveButtonListener);
		mapButton.setOnClickListener(mapButtonListener);
		cameraButton.setOnClickListener(cameraButtonListener);
		viewGalleryButton.setOnClickListener(viewGalleryButtonListener);
		/*nameSpinner
				.setOnItemSelectedListener(nameSpinnerOnItemSelectedListener);*/
	}
	private String name;
	/*private OnItemSelectedListener nameSpinnerOnItemSelectedListener = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView parent, View v, int position,
				long id) {

			Log.i(APP, "Spinner id =" + id);
			Log.i(APP, "Spinner position =" + id);
			Log.i(APP, "" + nameSpinner.getAdapter().getItem(position));
			name = (String) nameSpinner.getAdapter().getItem(position);
			
			
				mRowId = mDbHelper.getIdForAdapterPosition(position);
				NewFindSci.this.populateFields();
			

		}

		public void onNothingSelected(AdapterView arg0) {
			name = (String) nameSpinner.getAdapter().getItem(0);
		}

	};*/
	
	/**
	 * Returns the id depending on which of the buttons are selected.
	 * @return
	 */
	private int findSex(){
		int sex=0;
		if (sexRadioGroup.getCheckedRadioButtonId()==R.id.maleRadioButton) sex = 0;
		else if (sexRadioGroup.getCheckedRadioButtonId()==R.id.maleRadioButton) sex =1;
		return sex;
	}
	private void savetoDB() {
		description = descriptionText.getText().toString();
		String time = dateText.getText().toString();
		name = nameText.getText().toString();
		int sex = findSex();
		int age = Integer.parseInt(ageText.getText().toString());
		String identifier = identifierText.getText().toString();
		//String name = (newItemName!=null)?newItemName:"Untitled";
		int tagged = taggedCheckBox.isChecked()?1:0;
			if (mRowId == null) {
			long id = mDbHelper.createRow(description, longitude, latitude,
					time,sex, age,identifier,name, tagged);
			if (id > 0) {
				mRowId = id;
			}
		} else {
			mDbHelper.updateRow(mRowId, description, longitude, latitude, 
					time,sex,age,identifier,name, tagged);
		}
	}
	private void clearFields() {
		identifierText.setText("");
		ageText.setText("");
		taggedCheckBox.setSelected(false);
		maleRadioButton.setSelected(false);
		femaleRadioButton.setSelected(false);
		descriptionText.setText("");
	}

	/**
	 * Opens a dialog box to select a new Name
	 */
	private void addNewName(){
		final Dialog addNewNameDialog = new Dialog(this,android.R.style.Theme_Dialog);
		addNewNameDialog.setContentView(R.layout.add_new_name);
		final EditText newName = (EditText)addNewNameDialog.findViewById(R.id.addNewNameDialogText);
		final Button ok = (Button)addNewNameDialog.findViewById(R.id.addNewNameDialogOkButton);
		final Button cancel = (Button)addNewNameDialog.findViewById(R.id.addNewNameDialogCancelButton);
		newName.setOnKeyListener(new OnKeyListener(){
			public boolean onKey(View v, int keyCode, KeyEvent event){
				switch(keyCode){
					case KeyEvent.KEYCODE_DPAD_CENTER:
						setItemName(newName.getText().toString());
						clearFields();
						setNumberText();
						setDateText();
						getLocation();
						addNewNameDialog.dismiss();
						
						break;
				}
				return false;
			}
		});
		ok.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				setItemName(newName.getText().toString());
				addNewNameDialog.dismiss();
			}
		});
		cancel.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				addNewNameDialog.dismiss();
			}
		});
		//addNewNameDialog.setCancelable(true);
		addNewNameDialog.setTitle(R.string.addNewNameLabel);
		addNewNameDialog.show();
	
	}
	/**
	 * Sets a name for the item
	 * @param x
	 */
	private void setItemName(String x){
		newItemName = x;
		//fillTypes();
	}
	
	private OnClickListener addNameButtonListener = new OnClickListener(){
		public void onClick(View v){
			addNewName();
		}
	};

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

	private OnClickListener viewGalleryButtonListener = new OnClickListener() {
		public void onClick(View v) {
			openGallery();
		}
	};

	private void openGallery() {
		Intent i = new Intent(this, PictureViewer.class);
		i.putExtra(DBHelper.KEY_ROWID, mRowId);
		startSubActivity(i, ACTIVITY_CREATE);
	}

	private void takePicture() {
		savetoDB();
		Intent i = new Intent(this, Camera.class);
		i.putExtra(DBHelper.KEY_ROWID, mRowId);
		Log.i(APP, mRowId + "");
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
	/**
	 * Gets the latitude and longitude from the current record
	 * @param record
	 */
	private void getLatLongFromDb(Cursor record){
		latitude = ""+record.getInt(record
				.getColumnIndex(DBHelper.KEY_LATITUDE));
		longitude = ""+record.getInt(record
				.getColumnIndex(DBHelper.KEY_LONGITUDE));
	}
	
	private void populateFields() {
		Cursor record = mDbHelper.fetchRow(mRowId);
		startManagingCursor(record);
		
		numberText.setText(record.getString(record
				.getColumnIndex(DBHelper.KEY_ROWID)));
		descriptionText.setText(record.getString(record
				.getColumnIndex(DBHelper.KEY_DESCRIPTION)));
		identifierText.setText(record.getString(record.getColumnIndex(DBHelper.KEY_IDENTIFIER)));
		ageText.setText(record.getString(record.getColumnIndex(DBHelper.KEY_AGE)));
		setTagged(record.getInt(record.getColumnIndex(DBHelper.KEY_TAGGED)));
		setSex(record.getInt(record.getColumnIndex(DBHelper.KEY_SEX)));
		getLatLongFromDb(record);
		setLatLongText();
		setDateText(record.getString(record.getColumnIndex(DBHelper.KEY_TIME)));
		//nameSpinner.setSelection(0); //take the default at 0, safe bet :)
		//Since we are using all the names, we need to switch the spinner to current name
		name= record.getString(record.getColumnIndex(DBHelper.KEY_NAME));
		nameText.setText(name);
		/*HAWK
		 * nameSpinner.setSelection(adapter.getPosition(name));*/
		
		viewGalleryButton.setVisibility(View.VISIBLE);
	}
	/**
	 * Sets sex based on the value
	 * @param value
	 */
	private void setSex(int value){
		print("sex="+value);
		if (value == 0 ){
			maleRadioButton.setSelected(true);
		}else if(value == 1){
			femaleRadioButton.setSelected(true);
		}	
	}
	private void print(String value){
		Log.i(APP,value);
	}
	/**
	 * Sets if the entry is tagged or not
	 * @param value
	 */
	private void setTagged(int value){
		print(""+"Tagged "+value);
		taggedCheckBox.setChecked((value==1)?true:false);
	
	}
	/**
	 * Maps individual items to the ids in xml file new_find_sci_2
	 */
	private void mapViewItems() {
		latitudeText = (TextView) findViewById(R.id.latitudeText);
		longitudeText = (TextView) findViewById(R.id.longitudeText);
		numberText = (TextView) findViewById(R.id.numberText);
		descriptionText = (EditText) findViewById(R.id.descriptionText);
		dateText = (TextView) findViewById(R.id.dateText);
		/*HAWK
		 * nameSpinner = (Spinner) findViewById(R.id.nameSpinner);*/
		identifierText = (EditText)findViewById(R.id.identifierText);
		ageText = (EditText)findViewById(R.id.ageText);
		
		nameText = (EditText)findViewById(R.id.nameText);
		sexRadioGroup = (RadioGroup)findViewById(R.id.sexRadioGroup);
		maleRadioButton = (RadioButton)findViewById(R.id.maleRadioButton);
		femaleRadioButton = (RadioButton)findViewById(R.id.femaleRadioButton);
		taggedCheckBox = (CheckBox)findViewById(R.id.taggedCheckBox);
		addNameButton = (Button) findViewById(R.id.addNameButton);
		mapButton = (ImageButton) findViewById(R.id.mapButton);
		cameraButton = (ImageButton) findViewById(R.id.cameraButton);
		
		saveButton = (ImageButton) findViewById(R.id.saveButton);
		viewGalleryButton = (ImageButton) findViewById(R.id.viewGalleryButton);
		//set the Gallery Button to be Invisible by default
		viewGalleryButton.setVisibility(View.INVISIBLE);
		
	}


//	private void fillTypes() {
//		Cursor c = mDbHelper.allNames();
//		SpinnerBindings.bindCursor(this, c, nameSpinner, newItemName);
//			}

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
			new AlertDialog.Builder(NewFind.this)
					.setTitle("Device Missing")
					.setMessage(
							"I can't find a GPS device.\n Do you still want to continue?")
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									latitude="null"; longitude="null";
									setLatLongText();
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
			setLatLong(location);
			setLatLongText();
		}
	}
	
	private void setLatLong(Location location){
		latitude = ""+location.getLatitude();
		longitude = ""+location.getLongitude();
	}
	
	private void setLatLongText(){
		if (latitude!="null" && longitude!="null"){
			if (latitude.length() > 10)
			latitudeText.setText(latitude.substring(0, 9));
			else 
				latitudeText.setText(latitude);
			
			if (longitude.length()>10)
				longitudeText.setText(longitude.substring(0, 9));
			else 
				longitudeText.setText(longitude);
		}else {
			latitudeText.setText(latitude);
			longitudeText.setText(longitude);	
		}
	}
}
