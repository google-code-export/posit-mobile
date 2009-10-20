package org.hfoss.posit;

import java.net.URL;
import java.util.Calendar;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.hfoss.posit.util.DataSet;
import org.hfoss.posit.util.SpinnerBindings;
import org.hfoss.posit.util.location.LocationNameHandler;
import org.hfoss.posit.util.weather.GoogleWeatherHandler;
import org.hfoss.posit.util.weather.WeatherForecastCondition;
import org.hfoss.posit.util.weather.WeatherSet;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

/**
 * The class used for New Finds. It displays the fields that the user is required to fill from the layout XML files and saves the information
 * using the DBHelper class to the SQLite database underneath.
 * <u>Features:</u>
 * <ul>
 * <li> Supports single and multiple instances both </li>
 * </ul>
 * @author pgautam
 *
 */
public class NewFind extends Activity {
	/* Declarations here */
	private static final String REFRESH_INSTANCE = "org.hfoss.posit.find.refreshInstance";
	private TextView latitudeText, longitudeText, numberText, dateText,
			weatherText;
	private EditText descriptionText, identifierText, ageText, nameText,
			instanceDescriptionText;
	private CheckBox taggedCheckBox;
	private RadioGroup sexRadioGroup;
	private RadioButton maleRadioButton, femaleRadioButton;
	private ImageView weatherImage;
	private Spinner observationTypeSpinner, nameSpinner;
	private Button addInstanceButton;
	private ImageButton mapButton, cameraButton, saveButton, viewGalleryButton;
	private DBHelper mDbHelper;
	private Location location;
	private ProgressDialog mProgressDialog;
	protected IntentFilter myIntentFilter = new IntentFilter(REFRESH_INSTANCE);
	protected MyIntentReceiver myIntentReceiver;
	// this integer will determine how the application behaves according to the
	// action required.
	private int applicationMode;
	/**
	 * The modes the Activity will run depending on the course the user takes
	 */
	private static final int NEW_RECORD = 0;
	private static final int EDIT_RECORD = 1;
	private static final int NEW_INSTANCE = 2;
	private Long mRowId = 1L, recordId = mRowId; /*
												 * sets recordId to mRowId for
												 * default to keep things from
												 * breaking apart
												 */
	private String latitude = null, longitude = null, description = null;
	private final String APP = "NewFind",
			GETTING_WEATHER = "Getting weather Data";
	private static final int ACTIVITY_CREATE = 0;
	private String newItemName = null; /*
										 * The name of the current item set by
										 * the add button later
										 */

	/**
	 * Intent Receiver that repopulates the entries depending on what the user selects.
	 * Currently in use when the user selects an entry in the Spinner in the instance box. 
	 */
	class MyIntentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String data = intent.getAction();
			if (data.equals(NewFind.REFRESH_INSTANCE)) {
				Log.i(APP, "intent received");
				populateInstanceFields();

			}
		}
	}
	/**
	 * if the program is paused, we want to unregister our intent.
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		/*if (DataSet.isMultipleInstances())//if the program has multiple instances only we need to refresh
			this.unregisterReceiver(this.myIntentReceiver);*/
	}
	

	@Override
	protected void onStop() {
		super.onStop();
		try {
		if (DataSet.isMultipleInstances())
			this.unregisterReceiver(this.myIntentReceiver);
		} catch (Exception e ) {
			
		}

	}

	public NewFind() {
		//left blank on purpose, nothing to instantiate
	}

	/**
	 * Re register our instance 
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (DataSet.isMultipleInstances()) {
			this.myIntentReceiver = new MyIntentReceiver();
			this.registerReceiver(this.myIntentReceiver, myIntentFilter);

		}

	}

	private Bundle extras;

	/**
	 * when the application is loaded, it reads the preferences from the xml file and loads settings accordingly
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		DataSet.getPOSITPreferences(this);
		mDbHelper = new DBHelper(this);
		mDbHelper.open();
		//if a clean run is set, we just want to clear the database of all stuff
		if (DataSet.isCleanRun())
			mDbHelper.cleanDB();
		setContentView(R.layout.new_find_sci_2);
		mapViewItems();
		// fillTypes();

		if (DataSet.isMultipleInstances()) {
			this.myIntentReceiver = new MyIntentReceiver();
			this.registerReceiver(this.myIntentReceiver, myIntentFilter);
			fillStaticSpinners();
		}
		// Check whether this is a new or existing record.
		mRowId = icicle != null ? icicle.getLong(DBHelper.KEY_ROWID) : null;
		if (mRowId == null) {
			extras = getIntent().getExtras();
			boolean test = extras.containsKey(DBHelper.KEY_ROWID);
			Log.i(APP, test + "");
			mRowId = test ? extras.getLong(DBHelper.KEY_ROWID) : null;

		}
		// applicationMode = getMode(icicle.getString("action"));
		applicationMode = 1;
		if (DataSet.isMultipleInstances()) {
			((TextView) findViewById(R.id.instancesName)).setText(DataSet
					.getInstanceName());
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage(GETTING_WEATHER);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.show();
			/**
			 * Need to run this one on a thread, otherwise it's slow and
			 * obvious.
			 */
			new Thread() {
				public void run() {
					try {
						weather = findWeather(findCity(location));
						weatherText.setText(weather);

						/*
						 * This one, finds out which city you are in using the
						 * GPS coordinates, and passes into the findWeather
						 * function to find the weather there and then sets the
						 * output from that to the weatherText field.
						 */
					} catch (Exception e) {
						e.printStackTrace();
					}
					mProgressDialog.dismiss();
				}
			}.start();
		}

		if (mRowId != null ) {
			
			try {
				populateFields();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
//			setNumberText();
			setDateText();
			getLocation();
		}

		setListeners();
	}

	/**
	 * This maps the core Views, that is common to everything the application
	 * needs to deal with.
	 * <b> Future Implementation</b>
	 * @param actionInput
	 * @return
	 */

	private int getMode(String actionInput) {
		if (actionInput.equals("Edit Record"))
			return EDIT_RECORD;
		else if (actionInput.equals("New Record"))
			return NEW_RECORD;
		else if (actionInput.equals("New Instance"))
			return NEW_INSTANCE;
		else
			return -1;
	}
	/**
	 * Current Row count
	 */
	private void setNumberText() {
		String[] projection = { DBHelper.KEY_ROWID };
		Cursor myCursor = mDbHelper.fetchSelectedColumns(projection);
		int RowCount = myCursor.getCount() + 1;
		numberText.setText("" + RowCount);
	}

	/**
	 * Sets the date from the given String, used for populating the field from
	 * the database.
	 * 
	 * @param dateString
	 */
	private void setDateText(String dateString) {
		dateText.setText(dateString);
	}

	/**
	 * Gets the current date and time from the Calendar Instance
	 */
	private void setDateText() {
		Calendar c = Calendar.getInstance();
		String dateString = c.get(Calendar.YEAR) + "/" + c.get(Calendar.MONTH)
				+ "/" + c.get(Calendar.DAY_OF_MONTH) + " "
				+ c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);
		dateText.setText(dateString);
	}
	/**
	 * Sets all the listeners here.
	 */
	private void setListeners() {
		saveButton.setOnClickListener(saveButtonListener);
		// mapButton.setOnClickListener(mapButtonListener);
		cameraButton.setOnClickListener(cameraButtonListener);
		viewGalleryButton.setOnClickListener(viewGalleryButtonListener);
		sexRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.maleRadioButton)
					Sex = 0;
				else
					Sex = 1;
			}
		});

		if (DataSet.isMultipleInstances())
			setInstanceListeners();
	}
	/**
	 * Separate for Instances,since we don't need these objects for single instances.
	 */
	private void setInstanceListeners() {
		try {
			addInstanceButton.setOnClickListener(newInstanceListener);
			nameSpinner
					.setOnItemSelectedListener(nameSpinnerOnItemSelectedListener);
			/*
			 * observationTypeSpinner.setOnItemSelectedListener(obsTypeItemSelectedListener
			 * );
			 */
		} catch (NullPointerException e) {
			print("Check the mapping for the view Items message = "
					+ e.getMessage() + "cause =" + e.getCause());
		}
	}

	private String name;

	private int Sex;

	/**
	 * Returns the id depending on which of the buttons are selected.
	 * 
	 * @return
	 */
	private int findSex() {

		/*
		 * if (sexRadioGroup.getCheckedRadioButtonId() == R.id.maleRadioButton)
		 * sex = 0; else if
		 * (sexRadioGroup.getCheckedRadioButtonId()==R.id.femaleRadioButton) sex
		 * = 1; Log.i(APP,sex+"");
		 */
		return Sex;
	}

	/**
	 * Saves to the database
	 */
	private void savetoDB() {
		int age=0;

		description = descriptionText.getText().toString();
		String time = dateText.getText().toString();
		name = nameText.getText().toString();
		int sex = findSex();
		if (!ageText.getText().toString().equals("")){
		 age = Integer.parseInt(ageText.getText().toString());
		}
		
		String identifier = identifierText.getText().toString();
		// String name = (newItemName!=null)?newItemName:"Untitled";
		int tagged = taggedCheckBox.isChecked() ? 1 : 0;
		if (mRowId == null) {
			long id = mDbHelper.createRow(description, longitude, latitude,
					time, sex, age, identifier, name, tagged);
			long newInstanceId = saveToInstanceDB(id);

			if (!DataSet.isMultipleInstances())
				mDbHelper.setDefaultInstance(id, newInstanceId);

			if (id > 0) {
				mRowId = id;

			}

		} else {
			mDbHelper.updateRow(mRowId, description, sex, age, identifier,
					name, tagged);
		}
		
		// if the application has multiple Instances enabled, then save to the
		// instance DB

	}

	/**
	 * Saving to instance database. Saves the current rowId of the entry being edited as it's record id for keying.
	 * @param recordId
	 * @return row Id of instance database.
	 */
	private long saveToInstanceDB(long recordId) {

		// TODO move everything to contentvalues instead of HashMap and then to
		// contentValues anyways
		ContentValues values = new ContentValues();
		values.put(DBHelper.KEY_LATITUDE, latitude!=null?Double.parseDouble(latitude):0);
		values.put(DBHelper.KEY_LONGITUDE, longitude!=null?Double.parseDouble(longitude):0);
		values.put(DBHelper.KEY_RECORD_ID, "" + recordId);
		values.put(DBHelper.KEY_NAME, name);
		values.put(DBHelper.KEY_DESCRIPTION, descriptionText.getText()
				.toString());
		values.put(DBHelper.KEY_TIME, dateText.getText().toString());
		if (DataSet.isMultipleInstances()) {

			values.put(DBHelper.KEY_OBSERVED, observationTypeSpinner
					.getSelectedItem().toString());
			values.put(DBHelper.KEY_WEATHER, weatherText.getText().toString());
		}
		return mDbHelper.addSighting(values);
	}

	/**
	 * clears all the info on the screen for new ones mostly
	 */
	private void clearFields() {
		identifierText.setText("");
		ageText.setText("");
		taggedCheckBox.setSelected(false);
		maleRadioButton.setSelected(false);
		femaleRadioButton.setSelected(false);
		descriptionText.setText("");
	}

	private void addNewName() {
		NewInstanceDialog nInt = new NewInstanceDialog(this);
		nInt.setValues(mDbHelper, latitude, longitude, mRowId, weather);
		nInt.show();

	}



	/**
	 * Pulls all the names of finds from the database and shows in the Spinner
	 */
	private void fillNames() {
		Cursor c = mDbHelper.getSightings(mRowId,
				new String[] { DBHelper.KEY_TIME });
		SpinnerBindings.bindCursor(this, c, nameSpinner, null,
				DBHelper.KEY_TIME);
	}

	private OnClickListener newInstanceListener = new OnClickListener() {
		public void onClick(View v) {
			getLocation();
			addNewName();
		}
	};

	/**
	 * Listener for the mapIt button Calls the map activity and records the
	 * point
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

	private OnItemSelectedListener nameSpinnerOnItemSelectedListener = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView parent, View v, int position,
				long id) {

			long rowId = mDbHelper.getIdForAdapterPosition(position);
			//
			Log.i(APP, "Record Id = " + rowId);
			if (mRowId != null)
				populateInstanceFields(mDbHelper.getInstance(rowId));
		}

		public void onNothingSelected(AdapterView arg0) {
			// on nothing selected, do nothing :)
		}

	};

	private void openGallery() {
		Intent i = new Intent(this, PictureViewer.class);
		i.putExtra(DBHelper.KEY_ROWID, mRowId);
		startActivity(i);
	}

	private void takePicture() {
		savetoDB();
		Intent i = new Intent(this, CameraPreview2.class);
		i.putExtra(DBHelper.KEY_ROWID, mRowId);
		Log.i(APP, mRowId + "");
		startActivity(i);
	}

	private void recordPoint() {
		// TODO: Open the mapper and get the values.
		// Essentially to let the user double check the value.
		savetoDB();
		Intent i = new Intent(this, PositMain.class) ;
		i.putExtra(DBHelper.KEY_LATITUDE, latitude);
		i.putExtra(DBHelper.KEY_LONGITUDE, longitude);

		startActivity(i);

		// I want to take the value of latitude and longitude...
		// maybe let the user view the scenario before committing to the db.

	}

	/**
	 * Gets the latitude and longitude from the current record
	 * 
	 * @param record
	 */
	private void getLatLongFromDb(Cursor record) {
		try {
			latitude = ""
					+ record.getDouble(record
							.getColumnIndex(DBHelper.KEY_LATITUDE));
			Log.i(APP, latitude);
			longitude = ""
					+ record.getDouble(record
							.getColumnIndex(DBHelper.KEY_LONGITUDE));
		} catch (Exception e) {
			Log.e(APP, e.getMessage() + "\n" + e.getStackTrace());
		}
	}

	private void populateFields() {
		Cursor record = mDbHelper.fetchRow(mRowId);
		try {
		startManagingCursor(record);
		populateCoreFields(record);
		if (DataSet.isMultipleInstances()) {
			populateInstanceFields();
		} else {
			populateDefaultInstanceField();

		}
		} catch (Exception n) {
			
		}
		viewGalleryButton.setVisibility(View.VISIBLE);
	}
	/**
	 * Gets the default instance for the given rowId and shows it.
	 */
	private void populateDefaultInstanceField() {
		Cursor record = mDbHelper.getInstances(mRowId);
		record.moveToFirst();
		getLatLongFromDb(record);
		setLatLongText();
		setDateText(record.getString(record.getColumnIndex(DBHelper.KEY_TIME)));
	}
	/**
	 * Populates the core fields.
	 * @param record
	 */
	private void populateCoreFields(Cursor record) {

		nameText.setText(record.getString(record
				.getColumnIndex(DBHelper.KEY_NAME)));
		numberText.setText(record.getString(record
				.getColumnIndex(DBHelper.KEY_ROWID)));
		descriptionText.setText(record.getString(record
				.getColumnIndex(DBHelper.KEY_DESCRIPTION)));
		identifierText.setText(record.getString(record
				.getColumnIndex(DBHelper.KEY_IDENTIFIER)));
		ageText.setText(record.getString(record
				.getColumnIndex(DBHelper.KEY_AGE)));
		setTagged(record.getInt(record.getColumnIndex(DBHelper.KEY_TAGGED)));
		setSex(record.getInt(record.getColumnIndex(DBHelper.KEY_SEX)));
		
	}

	private Cursor instanceRecord;
	/**
	 * Populates the instance fields, called by populateCoreFields.
	 * Creates a cursor and calls populateInstanceFields(Cursor)
	 */
	private void populateInstanceFields() {
		
		try {
			instanceRecord = mDbHelper.getInstances(mRowId);
			populateInstanceFields(instanceRecord);

			fillNames();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Populates InstanceFields by the cursor. Useful for updating .
	 * @param instanceRecord
	 */
	private void populateInstanceFields(Cursor instanceRecord) {
		try {
		instanceRecord.moveToFirst();
		getLatLongFromDb(instanceRecord);
		setLatLongText();
		weatherText.setText(instanceRecord.getString(instanceRecord
				.getColumnIndex(DBHelper.KEY_WEATHER)));
		// fillNames();

		/*
		 * observationTypeSpinner.setSelection(
		 * ((ArrayAdapter<String>)observationTypeSpinner.getAdapter())
		 * .getPosition
		 * (instanceRecord.getString(instanceRecord.getColumnIndex(DBHelper
		 * .KEY_OBSERVED))));
		 */
		}catch (NullPointerException n) {
			
		}
		((LinearLayout) findViewById(R.id.observationTypeLayout))
				.removeAllViews();
		instanceDescriptionText.setText(getMetaData(instanceRecord));

	}
	/**
	 * Get the metadata that we want to display
	 * @param record
	 * @return
	 */
	private String getMetaData(Cursor record) {

		return DBHelper.KEY_OBSERVED
				+ ": "
				+ record
						.getString(record.getColumnIndex(DBHelper.KEY_OBSERVED))
				+ "\n"
				+ DBHelper.KEY_DESCRIPTION
				+ ": "
				+ record.getString(record
						.getColumnIndex(DBHelper.KEY_DESCRIPTION));
	}

	/*
	 * private void populateInstanceFields(Cursor record) { long id =
	 * record.getLong(record.getColumnIndex(DBHelper.KEY_ROWID)); Log.i(APP,
	 * id+""); instanceRecord = mDbHelper.getInstances(id);
	 * 
	 * startManagingCursor(instanceRecord); instanceRecord.next(); Log.i(APP,
	 * instanceRecord.count()+""); getLatLongFromDb(instanceRecord);
	 * setLatLongText(); setDateText(instanceRecord.getString(instanceRecord
	 * .getColumnIndex(DBHelper.KEY_TIME)));
	 * weatherText.setText(instanceRecord.getString
	 * (instanceRecord.getColumnIndex(DBHelper.KEY_WEATHER))); fillNames(); }
	 */

	/**
	 * Sets sex based on the value
	 * 
	 * @param value
	 */
	private void setSex(int value) {
		print("sex=" + value);
		if (value == 0) {
			maleRadioButton.setChecked(true);
		} else if (value == 1) {
			femaleRadioButton.setChecked(true);
		}
	}

	private void print(String value) {
		Log.i(APP, value);
	}

	/**
	 * Sets if the entry is tagged or not
	 * 
	 * @param value
	 */
	private void setTagged(int value) {
		print("" + "Tagged " + value);
		taggedCheckBox.setChecked((value == 1) ? true : false);

	}

	/**
	 * Maps individual items to the ids in xml file new_find_sci_2
	 */
	private void mapViewItems() {
		mapCoreViews(); // needed anyways
		LinearLayout multipleInstancesLayout = (LinearLayout) findViewById(R.id.multipleInstancefillLayout);

		if (!DataSet.isMultipleInstances()) {// i.e, if we don't have multiple
			// instances, don't show anything
			multipleInstancesLayout.removeAllViews(); /*
													 * this removes all the
													 * children of this
													 * LinearLayout
													 */
		} else {
			mapInstanceViews();
		}

	}

	/**
	 * This selects and maps all the views associated with the core record
	 * entities that this application deals with namely, name, latitude,
	 * longitude -> although they are saved in the Instance views and saved as
	 * default(if needed) description and other metadata.
	 */
	private void mapCoreViews() {
		try {
			latitudeText = (TextView) findViewById(R.id.latitudeText);
			longitudeText = (TextView) findViewById(R.id.longitudeText);
			numberText = (TextView) findViewById(R.id.numberText);
			descriptionText = (EditText) findViewById(R.id.descriptionText);
			dateText = (TextView) findViewById(R.id.dateText);

			identifierText = (EditText) findViewById(R.id.identifierText);
			ageText = (EditText) findViewById(R.id.ageText);

			nameText = (EditText) findViewById(R.id.nameText);
			sexRadioGroup = (RadioGroup) findViewById(R.id.sexRadioGroup);
			maleRadioButton = (RadioButton) findViewById(R.id.maleRadioButton);
			femaleRadioButton = (RadioButton) findViewById(R.id.femaleRadioButton);
			taggedCheckBox = (CheckBox) findViewById(R.id.taggedCheckBox);
			// TODO Reenable mapButton
			// mapButton = (ImageButton) findViewById(R.id.mapButton);
			cameraButton = (ImageButton) findViewById(R.id.cameraButton);
			saveButton = (ImageButton) findViewById(R.id.saveButton);
			viewGalleryButton = (ImageButton) findViewById(R.id.viewGalleryButton);

			// set the Gallery Button to be Invisible by default
			viewGalleryButton.setVisibility(View.INVISIBLE);
		} catch (NullPointerException e) {
			// ignore null pointers for now
		}
	}

	/**
	 * This selects and maps all the views associated with the instances
	 */
	private void mapInstanceViews() {
		try {
			nameSpinner = (Spinner) findViewById(R.id.nameSpinner);
			observationTypeSpinner = (Spinner) findViewById(R.id.observationTypeSpinner);
			weatherText = (TextView) findViewById(R.id.weatherText);
			weatherImage = (ImageView) findViewById(R.id.weatherImage);
			addInstanceButton = (Button) findViewById(R.id.addInstanceButton);
			instanceDescriptionText = (EditText) findViewById(R.id.instanceDescriptionText);
		} catch (NullPointerException e) {
			// ignore null pointers for now
		}
	}
	/**
	 * Finds the city based on the coordinates
	 * uses http://ws.geonames.org/findNearestAddress to ge the nearest city
	 * @param location
	 * @return
	 */
	private String findCity(Location location) {
		String cityName = null;
		try {
			String latitude = String.valueOf(location.getLatitude());
			String longitude = String.valueOf(location.getLongitude());
			URL url = new URL("http://ws.geonames.org/findNearestAddress?lat="
					+ latitude + "&lng=" + longitude);
			Log.i(APP, url.toString());
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xr = sp.getXMLReader();
			/* Create a new ContentHandler and apply it to the XML-Reader */
			LocationNameHandler locationHandler = new LocationNameHandler();
			xr.setContentHandler(locationHandler);

			/* Parse the xml-data from our URL. */
			xr.parse(new InputSource(url.openStream()));
			/* Parsing has finished. */
			cityName = locationHandler.getParsedData();

		} catch (Exception e) {
			// TODO err... add something useful here
		}
		return cityName;
	}

	/**
	 * Gets the weather and puts on the screen space with picture pulled from
	 * google.
	 */
	// TODO move to a different package/implement this as runnable/thread.
	private String findWeather(String cityName) {
		String output = "N/A";
		try {
			/* Get the current city's name */
			String queryString = "http://www.google.com/ig/api?weather="
					+ cityName;
			/* need to change the query so that google processes it properly */
			URL url = new URL(queryString.replace(" ", "%20"));

			/* Get a SAXParser from the SAXPArserFactory. */
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xr = sp.getXMLReader();

			/*
			 * Create a new ContentHandler and apply it to the XML-Reader
			 */
			GoogleWeatherHandler gwh = new GoogleWeatherHandler();
			xr.setContentHandler(gwh);

			/* Parse the xml-data our URL-call returned. */
			xr.parse(new InputSource(url.openStream()));

			/* Our Handler now provides the parsed weather-data to us. */
			WeatherSet ws = gwh.getWeatherSet();
			WeatherForecastCondition condition = ws
					.getLastWeatherForecastCondition();

			output = condition.getCondition() + " at " + cityName + " Max "
					+ condition.getTempMaxCelsius() + " Celcius" + " Min "
					+ condition.getTempMinCelsius() + " Celcius";
			// weatherImage = (ImageView)
			// NewSighting.this.findViewById(R.id.weatherImage);
			/* Get the image that google presents and draw it */
			/*
			 * DrawImage.fromURL(this, weatherImage, "http://www.google.com/" +
			 * condition.getIconURL());
			 */

		} catch (Exception e) {
			Log.i(APP, "We have weather exception" + e.getMessage());
		}
		return output;
	}

	private String weather;
	/**
	 * Statically binds the Spinners to data Items.
	 */
	private void fillStaticSpinners() {
		SpinnerBindings.bindArrayFromResource(this, R.array.observation_types,
				observationTypeSpinner);
	}

	private void getLocation() {
		try {
			location = ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
			.getLastKnownLocation("gps");
					//.getCurrentLocation("gps");
		} catch (NullPointerException e) {/*
										 * if there's no device, we get an
										 * error, so gracefully,showing an error
										 * and giving option
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
									latitude = "null";
									longitude = "null";
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
	/**
	 * Sets the latitude, longitude from the gps location coordinates
	 * @param location
	 */
	private void setLatLong(Location location) {
		latitude = "" + location.getLatitude();
		longitude = "" + location.getLongitude();
	}
	/**
	 * Fits the latitude, longitude text on the screen
	 * default... 10 characters.
	 */
	private void setLatLongText() {
		if (latitude != "null" && longitude != "null") {
			if (latitude.length() > 10)
				latitudeText.setText(latitude.substring(0, 9));
			else
				latitudeText.setText(latitude);

			if (longitude.length() > 10)
				longitudeText.setText(longitude.substring(0, 9));
			else
				longitudeText.setText(longitude);
		} else {
			latitudeText.setText(latitude);
			longitudeText.setText(longitude);
		}
	}
}
