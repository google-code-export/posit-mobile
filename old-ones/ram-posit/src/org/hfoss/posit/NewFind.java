 package org.hfoss.posit;

import java.util.List;

import org.hfoss.posit.util.ImageAdapter;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;

public class NewFind extends Activity{

	private static final int ACTIVITY_CREATE = 0;
	
	private EditText mDescription;
	private EditText mLatitude;
	private EditText mLongitude;
	private EditText mImage;
    private DBHelper mDbHelper;
	private Double lat, lon;
	private Long mRowId=1L;
    private String imageList=null; 
	private Gallery Gallery;
    private PictureDB Pictures;
    private Cursor imageCursor;
    /* (non-Javadoc)
	 * @see android.app.ListActivity#onCreate(android.os.Bundle)
	 */

	public NewFind(){}
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
	    mDbHelper = new DBHelper(this);
	    mDbHelper.open();
	    setContentView(R.layout.new_find);
	    
	    mDescription = (EditText)findViewById(R.id.description);
	    mLatitude = (EditText)findViewById(R.id.latitude);
	    mLongitude = (EditText)findViewById(R.id.longitude);	 
	    mImage = null;
	
		Location loc = ((LocationManager)getSystemService(LOCATION_SERVICE)).getCurrentLocation("gps");
		((EditText)findViewById(R.id.latitude)).setText(""+loc.getLatitude());
		((EditText)findViewById(R.id.longitude)).setText(""+loc.getLongitude());
	
		// Check whether this is a new or existing record.
	    mRowId = icicle != null ? icicle.getLong(DBHelper.KEY_ROWID) : null;
	    if (mRowId == null) {
	         Bundle extras = getIntent().getExtras();
	         mRowId = extras != null ? extras.getLong(DBHelper.KEY_ROWID) : null;
	    }
	    
        populateFields();

		Button saveIt = (Button)findViewById(R.id.confirm);
	    saveIt.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View view) {
	                setResult(RESULT_OK);
	                finish();
	            }           
	        });
		
		Button mapIt = (Button)findViewById(R.id.mapit);
		mapIt.setOnClickListener(mapItListener);

		Button takeAPic = (Button)findViewById(R.id.takepic);
		takeAPic.setOnClickListener(new OnClickListener(){
				public void onClick(View v){
					takePicture();	
				}
		});
	}
	
	  private void populateFields() {
	    	if (mRowId != null) {
	            Cursor record = mDbHelper.fetchRow(mRowId);
	            startManagingCursor(record);
	            mDescription.setText(record.getString(
	    	            record.getColumnIndex(DBHelper.KEY_DESCRIPTION)));
	            mLongitude.setText(record.getString(
	    	            record.getColumnIndex(DBHelper.KEY_LONGITUDE)));
	            mLatitude.setText(record.getString(
	    	            record.getColumnIndex(DBHelper.KEY_LATITUDE)));
	            imageList = record.getString(
	            		record.getColumnIndex((DBHelper.KEY_IMAGE)));
	            updateImageAdapter(imageList);
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
	        saveState();
	    }
	    
	    @Override
	    protected void onResume() {
	        super.onResume();
	        populateFields();
	    }
	    
	    private void saveState() {
	        String description = mDescription.getText().toString();
	        String longitude = mLongitude.getText().toString();
	        String latitude = mLatitude.getText().toString();
	        String img=imageList;
//    public void createRow(String desc, String img, String longitude, String latitude) {

	        if (mRowId == null) {
	            long id = mDbHelper.createRow(description, img, longitude, latitude);
	            if (id > 0) {
	                mRowId = id;
	            }
	        } else {
	            mDbHelper.updateRow(mRowId, description, img, longitude, latitude);  //intent.setData(MediaStore.Images.Media.INTERNAL_CONTENT_URI);

	            
	    		//uri = intent.getData();
	    		
	        }
	    }
	

/**
 * gets the location from the
 *
 */
	private void getLocation()
	{
		Location loc;
		LocationManager locMan;
		LocationProvider locPro;
		List<LocationProvider> proList;

		//Show "Loading" on the screen.

		setContentView(R.layout.loading);

		//Get the location manager from the server
		locMan = (LocationManager) getSystemService(LOCATION_SERVICE);

	 	proList = locMan.getProviders();

		//Just grab the first member of the list. It's name will be "gps"
		//locPro = proList.get(0);
		loc = locMan.getCurrentLocation("gps");

		lat =  loc.getLatitude();
		lon =  loc.getLongitude();
		setContentView(R.layout.new_find);
		EditText latitude = (EditText)findViewById(R.id.latitude);
		EditText longitude = (EditText)findViewById(R.id.longitude);
		CharSequence latitudeSq= lat.toString();
		CharSequence longitudeSq= lon.toString();
		latitude.setText(latitudeSq);
		longitude.setText(longitudeSq);


/**
		Button mapIt = (Button)findViewById(R.id.mapit);
		mapIt.setOnClickListener(mapItListener);

		Button takeAPic = (Button)findViewById(R.id.takepic);
		takeAPic.setOnClickListener(new OnClickListener(){
				public void onClick(View v){
					OpenCamera();
				}
		});
**/
	}
	
	/**
	 * Listener for the mapIt button
	 * Calls the map activity and records the point
	 */
	private OnClickListener mapItListener = new OnClickListener(){
		public void onClick(View v){
			recordPoint();
		}
	};
	
	private void takePicture(){
		Intent i = new Intent(this, Camera.class);
		
		startSubActivity(i,ACTIVITY_CREATE);
	}
	private void recordPoint(){
		// TODO: Open the mapper and get the values.
		//Essentially to let the user double check the value.
		Intent openMapper = new Intent(this,Mapper.class);
		startSubActivity(openMapper,1);

		// I want to take the value of latitude and longitude...
		// maybe let the user view the scenario before committing to the db.

	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, String data, Bundle extras) {
        super.onActivityResult(requestCode, resultCode, data, extras);
        saveState();
        switch(resultCode){
        case RESULT_OK:
        	imageList = extras.getString("Pictures");
        	updateImageAdapter(imageList);
        }
    }
    private static final String PICTURES_ROW_ID = "_ID"; 
    /*
     * Parses a list of numbers in the form x, y, z into individual components
     */
    private void updateImageAdapter(String list)
    {
    	imageCursor =  Pictures.fetchRows(list);
        Gallery.setAdapter(new ImageAdapter(imageCursor,this));
    }
}

