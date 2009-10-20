package org.hfoss.posit;

import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.hfoss.posit.util.SpinnerBindings;
import org.hfoss.posit.util.location.LocationNameHandler;
import org.hfoss.posit.util.weather.GoogleWeatherHandler;
import org.hfoss.posit.util.weather.WeatherForecastCondition;
import org.hfoss.posit.util.weather.WeatherSet;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

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
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * NewSighting is an activity for recording instances of the record that we are
 * recording. Originally designed for tracking Hawks, it includes the following
 * features: - GPS co-ordinates are added to the database[Sightings/Instance
 * table] using the location service - The type of finding is detected and
 * saved. - Weather for the city is detected and saved along with the data.
 * 
 * What is a sighting? A sighting, in general is referred to all the instances
 * of the finds that the device gathers. So, sighting is a more particular term
 * to represent the more general term "find." A sighting is bound to a find by
 * the recordId of the "find" which is unique to the finds.
 * 
 * @author pgautam
 * 
 */
public class NewSighting extends Activity {
	private TextView latitudeText, longitudeText, numberText, dateText,
			weatherText;
	private Location location;
	private Spinner observationTypeSpinner, nameSpinner;
	private ImageButton mapButton, cameraButton, saveButton, viewGalleryButton;
	private ImageView weatherImage;
	private final String APP = "NewSighting";
	private String latitude, longitude;
	private Long mRowId = 1L,recordId;
	private static final int ACTIVITY_CREATE = 1;
	public NewSighting() {
	}

	private DBHelper dbHelper = new DBHelper(this);

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		dbHelper.open();
		setContentView(R.layout.new_sighting);
		mapViewItems();
		
		fillNames();
		fillObjectTypes();
		setListeners();
		getLocation(); 
		mRowId = icicle != null ? icicle.getLong(DBHelper.KEY_ROWID) : null;
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(DBHelper.KEY_ROWID) : null;

		}
		if (mRowId != null) {
			populateFields();
		} else {
			getLocation(); //gets the location, needs to be here because it puts the lat and long in the boxes too.
			/**These need to be filled anyways, so no checking required*/
			
			
			//TODO  Seriously, implement this in a thread. Make it faster
			findWeather();
			
				
			setDateText();	
		}
		
		
		
	}

	
	private void populateFields() {
		Cursor record = dbHelper.fetchRow(mRowId);
		startManagingCursor(record);
		
	}
	/**
	 * Set some listeners here
	 */
	private void setListeners() {
		saveButton.setOnClickListener(saveButtonListener);
		mapButton.setOnClickListener(mapButtonListener);
		cameraButton.setOnClickListener(cameraButtonListener);
		viewGalleryButton.setOnClickListener(viewGalleryButtonListener);
		
		nameSpinner
				.setOnItemSelectedListener(nameSpinnerOnItemSelectedListener);
	}
	
	/**
	 * Get the current city and displays on the textView. The output from this
	 * is fed into the weather package to get the current weather
	 * 
	 * @param location
	 * @return String with the name of the city in the following
	 *         <city-name>,<state-name>,<country-name>
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
	 * Gets the weather and puts on the screen space with picture pulled from google.
	 */
	//TODO move to a different package/implement this as runnable/thread.
	private void findWeather() {
		try {
			/* Get the current city's name */
			String cityName = findCity(location);
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
			weatherText.setText(condition.getCondition() + " at " + cityName+"\n"+
					"Max "+condition.getTempMaxCelsius()+" Celcius" + "\n"+ 
					 "	Min "+condition.getTempMinCelsius()+ " Celcius");
			//weatherImage = (ImageView) NewSighting.this.findViewById(R.id.weatherImage);
			/* Get the image that google presents and draw it */
			/*DrawImage.fromURL(this, weatherImage, "http://www.google.com/"
					+ condition.getIconURL());*/
		} catch (Exception e) {
			Log.i(APP, "We have exception" + e.getMessage());
		}

	}
	/**
	 * Gets the location, and if the device isn't available, gives the user
	 * option to continue although it puts latitude and longitude as "null"
	 * String.
	 */
	private void getLocation() {
		try {
			location = ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
					.getCurrentLocation("gps");
		} catch (NullPointerException e) {/*
										 * if there's no device, we get an
										 * error, so gracefully,showing an error
										 * and giving option
										 */
			Log.e(APP, "Error getting GPS data");
			new AlertDialog.Builder(this)
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
	 * Sets the current date and puts on the dateText TextView
	 */
	private void setDateText() {
		Calendar c = Calendar.getInstance();
		String dateString = c.get(Calendar.YEAR) + "/" + c.get(Calendar.MONTH)
				+ "/" + c.get(Calendar.DAY_OF_MONTH) + " "
				+ c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);
		dateText.setText(dateString);
	}
	
	/**
	 * Gets the latitude, longitude.
	 * @param location
	 */
	private void setLatLong(Location location){
		latitude = ""+location.getLatitude();
		longitude = ""+location.getLongitude();
	}
	
	/**
	 * Makes the latitude and longitude values small so that they fit on the space
	 */
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
	
	/**
	 * Pulls all the names of finds from the database and shows in the Spinner
	 */
	private void fillNames() {
		Cursor c = dbHelper.allNames();
		SpinnerBindings.bindCursor(this, c, nameSpinner, null);
	}

	/**
	 * Gets all the predefined find types from array observation_types in
	 * src/res/arrays.xml and puts them into the Spinner
	 */
	private void fillObjectTypes() {
		SpinnerBindings.bindArrayFromResource(this, R.array.observation_types,
				observationTypeSpinner);
	}

	/**
	 * Maps the variables with the Views in the screen
	 */
	private void mapViewItems() {
		latitudeText = (TextView) findViewById(R.id.latitudeTextSighting);
		longitudeText = (TextView) findViewById(R.id.longitudeTextSighting);
		numberText = (TextView) findViewById(R.id.numberTextSighting);
		dateText = (TextView) findViewById(R.id.dateTextSighting);
		nameSpinner = (Spinner) findViewById(R.id.nameSpinnerSighting);
		observationTypeSpinner = (Spinner) findViewById(R.id.observationTypeSpinner);
		weatherText = (TextView) findViewById(R.id.weatherText);
		mapButton = (ImageButton) findViewById(R.id.mapSightingButton);
		cameraButton = (ImageButton) findViewById(R.id.cameraSightingButton);
		saveButton = (ImageButton) findViewById(R.id.saveSightingButton);
		viewGalleryButton = (ImageButton) findViewById(R.id.viewGallerySightingButton);
		weatherImage = (ImageView ) findViewById(R.id.weatherImage);
		viewGalleryButton.setVisibility(View.INVISIBLE);
	}
	
	private void savetoDB() {
		HashMap<String,String> values = new HashMap<String,String>();
		values.put(DBHelper.KEY_LATITUDE,latitude);
		values.put(DBHelper.KEY_LONGITUDE, longitude);
		values.put(DBHelper.KEY_RECORD_ID, ""+recordId);
		values.put(DBHelper.KEY_TIME, dateText.getText().toString());
		values.put(DBHelper.KEY_OBSERVED,observationTypeSpinner.getSelectedItem().toString());
		values.put(DBHelper.KEY_WEATHER, weatherText.getText().toString());
		dbHelper.addSighting(values);
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
		latitude = ""+record.getDouble(record
				.getColumnIndex(DBHelper.KEY_LATITUDE));
		longitude = ""+record.getDouble(record
				.getColumnIndex(DBHelper.KEY_LONGITUDE));
	}

	private OnItemSelectedListener nameSpinnerOnItemSelectedListener = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView parent, View v, int position,
				long id) {

			/*Log.i(APP, "Spinner id =" + id);
			Log.i(APP, "Spinner position =" + id);
			Log.i(APP, "" + nameSpinner.getAdapter().getItem(position));*/
			
			
			recordId = dbHelper.getIdForAdapterPosition(position);
			Log.i(APP, "Record Id = "+recordId);
		}

		public void onNothingSelected(AdapterView arg0) {
			//on nothing selected, do nothing :)
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
		i.putExtra("Activity","NewSightings");
		Log.i(APP, mRowId + "");
		startSubActivity(i, ACTIVITY_CREATE);
	}
}
