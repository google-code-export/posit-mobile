package org.hfoss.posit;

//TODO use XML (if necessary to load the content
//TODO have return mechanisms so that files can check with appropriate flags
//TODO Use sqllite db and query mechanisms

import java.util.List;

import org.hfoss.posit.db.PositData;
import org.hfoss.posit.util.Date;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentReceiver;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayController;
import com.google.android.maps.Point;

public class Mapper extends MapActivity {
	private static final String MY_LOCATION_CHANGED_ACTION =
		"android.intent.action.LOCATION_CHANGED";
	private MapView mMapView;
	//some important constants here
	private String LOG_TAG = "Mapper";  //to simplify the logging process!
	protected final IntentFilter myIntentFilter =  new IntentFilter(MY_LOCATION_CHANGED_ACTION);
	protected boolean doUpdates = true;
	protected MyIntentReceiver myIntentReceiver = null;
	protected NotificationManager mNotificationManager;

	protected MapController mapController;
	protected OverlayController overlayController;
	private Location location ;
	private LocationManager locationManager ;
	private LocationProvider locationProvider ;
	private ActionDialog actionDialog;

	List<LocationProvider> providerList; //Simple List of providers.. nothing fancy

	private ContentValues values = new ContentValues();

	Double latitude, longitude;          //Declaration for the latitude and longitude
	//Value is in xE-6 format that needs to be
	//multiplied by 1E6 to make sense.



	//some constants for later use!
	protected final long MINIMUM_DISTANCECHANGE_FOR_UPDATE = 25; // in Meters

	protected final long MINIMUM_TIME_BETWEEN_UPDATE = 2500; // in Milliseconds
	// ....



	/**
	 * Database stuff
	 */
	private Cursor mCursor;
	private Uri mURI;
	private static final String[] PROJECTION = new String[]{
		PositData.Photos._ID,
		PositData.Photos.NAME,
		PositData.Photos.DESCRIPTION,
		PositData.Photos.BITMAP,
		PositData.Photos.LATITUDE,
		PositData.Photos.LONGITUDE,
		PositData.Photos.CREATED_DATE,
		PositData.Photos.MODIFIED_DATE

	};
	
	private static int NAME_INDEX =1;
	private static int DESCRIPTION_INDEX = 2;
	private static int BITMAP_INDEX = 3;
	private static int LATITUDE_INDEX =4;
	private static int LONGITUDE_INDEX =5;
	private static int ALTITUDE_INDEX = 6;
	private static int CREATED_INDEX = 7;
	private static int MODIFIED_INDEX = 8;
	
	
	
	
	/**
	 * This tiny IntentReceiver updates
	 * our stuff as we receive the intents
	 * (LOCATION_CHANGED_ACTION) we told the
	 * myLocationManager to send to us.
	 */
	class MyIntentReceiver extends IntentReceiver {
		@Override
		public void onReceiveIntent(Context context, Intent intent) {
			String data = intent.getAction();
			if ( data.equals(Mapper.MY_LOCATION_CHANGED_ACTION)){
				if(Mapper.this.doUpdates){
					// Will simply update our list, when receiving an intent
					Mapper.this.updateView();
				}
			}
		}
	}
	/**
	 * The overlay for the points that we need to display on the
	 * screen.
	 * TODO Make it fancy :)
	 */
	protected class MyLocationOverlay extends Overlay{
		public void draw (Canvas canvas, PixelCalculator calculator, boolean shadow){
			super.draw(canvas, calculator, shadow);
			//	setContentView(R.layout.test);
			Paint paint = new Paint();
			paint.setTextSize(12);

			//Create a Point that represents our GPS location
			latitude = Mapper.this.location.getLatitude()*1E6;
			longitude = Mapper.this.location.getLongitude()*1E6;
			Point point = new Point (latitude.intValue(), longitude.intValue());

			int[] myScreenCoords = new int[2];
			calculator.getPointXY(point, myScreenCoords);

			//Draw a circle for our location
			RectF oval = new RectF(myScreenCoords[0]-7, myScreenCoords[1]+7,
					myScreenCoords[0]+7, myScreenCoords[1]-7);

			// Setup a color for our location
			paint.setStyle(Style.FILL);
			canvas.drawText(getString(R.string.map_overlay_own_name),
					myScreenCoords[0] + 9, myScreenCoords[1], paint);

			// Change the paint to a 'Lookthrough' Android-Green
			paint.setARGB(90, 156, 192, 36);

			paint.setStrokeWidth(1);
			// draw an oval around our location
			canvas.drawOval(oval, paint);
			mapController.centerMapTo(point, true);
			Paint textPaint = new Paint();
			textPaint.setARGB(100,0, 0, 0);
			textPaint.setStyle(Style.FILL);
			textPaint.setStrokeWidth(1);
			//// SOme casting history.. leave it here
			canvas.drawText("latitude:"+((Double)(latitude)).toString(),
					0,10, textPaint);
			canvas.drawText("longitude:"+((Double)(longitude)).toString(),
					0,20, textPaint);
//			
			
		}


	}

	

	@Override
	public void onCreate(Bundle icicle) {
		//just the basic onCreate stuff here

		super.onCreate(icicle);
		final Intent intent = getIntent();

		// Is any data thing supplied? if not, then declare one for yourself
		if (intent.getData() == null )
		{
			intent.setData(PositData.Photos.CONTENT_URI);
			intent.setAction(Intent.INSERT_ACTION);

		}

		mURI = intent.getData();

		mCursor = managedQuery(mURI, PROJECTION, null, null);

		
		
		mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);



		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.newfindmap);
		mMapView = (MapView)findViewById(R.id.mapper);
		mapController = mMapView.getController();
		Log.i(LOG_TAG,mapController.toString());
		/*
		 * Now we are capable of drawing graphics stuff on top of
		 * the map
		 */
		this.overlayController = mMapView.createOverlayController();
		MyLocationOverlay locationOverlay = new MyLocationOverlay();
		this.overlayController.add(locationOverlay, true);
		this.mapController.zoomTo(19);
		//Get the location manager from the server
		//recommended way to do it!!
		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		this.updateView();
		//get a list of LocationProviders

		this.setupForGPSAutoRefreshing();



//		if (mURI == null){
//		Log.e(LOG_TAG, "Failed to insert photos into"+ getIntent().getData());
//		finish();
//		return;
//		}
		//mCursor = managedQuery(getIntent().getData(),PROJECTION,null,null);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		//TODO Load the dialog AFTER the View is loaded, not before.
		showDialog();
		notify("It's working");
	}

	public void showDialog(){
		actionDialog = new ActionDialog(this);

		
		actionDialog.setContentView(R.layout.loading);
		ImageView Cross = (ImageView)actionDialog.findViewById(R.id.dialog_close_cross);
		Cross.setFocusable(true);
		Cross.setClickable(true);

		Cross.setOnClickListener(new OnClickListener(){
			public void onClick(View v){	
				Mapper.this.actionDialog.dismiss();
				Mapper.this.doUpdates = true;
			}
		});
		
		
		ImageView NewRecord = (ImageView)actionDialog.findViewById(R.id.new_entry);

		NewRecord.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				Mapper.this.notify("Latitude"+latitude.toString());
				newEntryDialog();
				//newEntryDialog(latitude, longitude);
			}
		});
		
		ImageView NewPic = (ImageView)actionDialog.findViewById(R.id.camera);
		NewPic.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				Mapper.this.notify("Take snaps");
				CameraActivity();
			}
		});
		ImageView Save = (ImageView)actionDialog.findViewById(R.id.save);
		Save.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				mURI = getContentResolver().insert(PositData.Photos.CONTENT_URI, values);
				// resume the stopped updating screen.
				Mapper.this.notify("saved");
				Mapper.this.doUpdates = true;
			}
		});

		actionDialog.show();
	}
	
	private void CameraActivity(){
		Intent i = new Intent(this, Camera.class);
		startSubActivity(i,0);
	}
	
	private EditText Name_Value, Latitude_Value, Longitude_Value, Description_Value;
	private Dialog newEntry;
	private void newEntryDialog(){
		newEntry = new Dialog(this);
		newEntry.setContentView(R.layout.new_find);
		
		
		Name_Value = (EditText)newEntry.findViewById(R.id.nameText);	
		Latitude_Value = (EditText)newEntry.findViewById(R.id.latitudeText);
		Longitude_Value = (EditText) newEntry.findViewById (R.id.longitudeText);
		Description_Value = (EditText) newEntry.findViewById(R.id.DescriptionText);
		Button SaveButton = (Button) newEntry.findViewById(R.id.SaveButton);
		
		CharSequence lat = latitude.toString();
		CharSequence lon =longitude.toString();
		Log.i("newentry", (String)lat);
		Latitude_Value.setText(lat);
		Longitude_Value.setText(lon);
		
		SaveButton.setOnClickListener(
				new OnClickListener(){
					public void onClick(View view){
						values.put(PositData.Photos.LATITUDE, Latitude_Value.getText().toString() );
						values.put(PositData.Photos.LONGITUDE, Longitude_Value.getText().toString());
						values.put(PositData.Photos.NAME, Name_Value.getText().toString());
						values.put(PositData.Photos.DESCRIPTION, Description_Value.getText().toString());
						values.put(PositData.Photos.CREATED_DATE, Date.now());
						Mapper.this.newEntry.dismiss();
						Mapper.this.doUpdates = true;
					}
				});
		newEntry.show();
		this.doUpdates = false;
	}

	/**
	 * Make sure to stop the animation when we're no longer on screen,
	 * failing to do so will cause a lot of unnecessary cpu-usage!
	 */
	@Override
	public void onFreeze(Bundle icicle) {
		this.doUpdates = false;
		this.unregisterReceiver(this.myIntentReceiver);
		super.onFreeze(icicle);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_I) {
			// Zoom In
			int level = mMapView.getZoomLevel();
			mapController.zoomTo(level + 1);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_O) {
			// Zoom Out
			int level = mMapView.getZoomLevel();
			mapController.zoomTo(level - 1);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_S) {
			// Switch on the satellite images
			mMapView.toggleSatellite();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_T) {
			// Switch on traffic overlays
			mMapView.toggleTraffic();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK){
			finish();
		}else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER){
			showDialog();
		}
		return false;
	}



	public void onResume() {
		super.onResume();
		this.doUpdates = true;

		/* As we only want to react on the LOCATION_CHANGED
		 * intents we made the OS send out, we have to
		 * register it along with a filter, that will only
		 * "pass through" on LOCATION_CHANGED-Intents.
		 */
		this.registerReceiver(this.myIntentReceiver, this.myIntentFilter);
	}

	private void setupForGPSAutoRefreshing(){
		providerList = this.locationManager.getProviders();
		Log.i(LOG_TAG, providerList.get(0).getName());
		//get the GPS device
		locationProvider = providerList.get(0);

		this.locationManager.requestUpdates(locationProvider, MINIMUM_TIME_BETWEEN_UPDATE,
				MINIMUM_DISTANCECHANGE_FOR_UPDATE,
				new Intent(MY_LOCATION_CHANGED_ACTION));
		//So, we need to listen for the intent that we asked for our locationManager to
		// send us
		this.myIntentReceiver = new MyIntentReceiver();

	}
	//....

	/**
	 * Refreshes the screen and moves the cursor for us!
	 *
	 */
	private void updateView() {
		// Refresh our gps-location
		this.location = locationManager.getCurrentLocation("gps");

		/* Redraws the mapView, which also makes our
		 * OverlayController redraw our Circles and Lines */
		this.mMapView.invalidate();
	}

	private void notify(String text){
		CharSequence tickerText = (CharSequence)text;
		Intent contentIntent = new Intent(this, Mapper.class);
		Intent appIntent = new Intent(this, org.hfoss.posit.Mapper.class);
		mNotificationManager.notify(1, new Notification(
				this, // this context
				R.drawable.update, //application icon
				tickerText, // the tickertext
				System.currentTimeMillis(), // get current timestamp
				getText(R.string.app_name), // expanded title
				getText(R.string.app_name), // expanded text
				contentIntent, // look above 
				R.drawable.icon, // app icon
				getText(R.string.app_name), // application name
				appIntent));

	}

}
