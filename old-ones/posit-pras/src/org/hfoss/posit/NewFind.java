 package org.hfoss.posit;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class NewFind extends Activity{

	private static final int ACTIVITY_CREATE = 0;
	/* (non-Javadoc)
	 * @see android.app.ListActivity#onCreate(android.os.Bundle)
	 */

	public NewFind(){

	}
//	@Override
//	protected void onCreate(Bundle main) {
//		super.onCreate(main);
//		getLocation();
//	}
///**
// * gets the location from the
// *
// */
//	private void getLocation()
//	{
//		Location loc;
//		LocationManager locMan;
//		LocationProvider locPro;
//		List<LocationProvider> proList;
//		Double lat, lon;
//		//Show "Loading" on the screen.
//
//		setContentView(R.layout.loading);
//
//		//Get the location manager from the server
//		locMan = (LocationManager) getSystemService(LOCATION_SERVICE);
//
//	 	proList = locMan.getProviders();
//
//		//Just grab the first member of the list. It's name will be "gps"
//		//locPro = proList.get(0);
//		loc = locMan.getCurrentLocation("gps");
//
//		lat =  loc.getLatitude();
//		lon =  loc.getLongitude();
//		setContentView(R.layout.new_find);
//		EditText latitude = (EditText)findViewById(R.id.latitude);
//		EditText longitude = (EditText)findViewById(R.id.longitude);
//		CharSequence latitudeSq= lat.toString();
//		CharSequence longitudeSq= lon.toString();
//		latitude.setText(latitudeSq);
//		longitude.setText(longitudeSq);
//
//
//
//		Button mapIt = (Button)findViewById(R.id.mapit);
//		mapIt.setOnClickListener(mapItListener);
//
//		Button takeAPic = (Button)findViewById(R.id.takepic);
//		takeAPic.setOnClickListener(new OnClickListener(){
//				public void onClick(View v){
//					OpenCamera();
//				}
//		});
//
//
//
//	}
//	/**
//	 * Listener for the mapIt button
//	 * Calls the map activity and records the point
//	 */
//	private OnClickListener mapItListener = new OnClickListener(){
//		public void onClick(View v){
//			recordPoint();
//		}
//	};
//	private void OpenCamera(){
//		Intent i = new Intent(this, Camera.class);
//		startSubActivity(i,ACTIVITY_CREATE);
//	}
//	private void recordPoint(){
//		// TODO: Open the mapper and get the values.
//		//Essentially to let the user double check the value.
//		Intent openMapper = new Intent(this,Mapper.class);
//		startSubActivity(openMapper,1);
//
//		// I want to take the value of latitude and longitude...
//		// maybe let the user view the scenario before committing to the db.
//
//	}
//	@Override
//
//	protected void onActivityResult(int requestCode, int resultCode, String data, Bundle extras) {
//		super.onActivityResult(requestCode, resultCode, data, extras);
//		switch (resultCode){
//		case  RESULT_OK:
//			//Log.i("MyLog",data);
//			ImageView showPicture = (ImageView)findViewById(R.id.takenpic);
//			//Used to draw stuff.. needs full path... geez
//			Drawable x = Drawable.createFromPath("/data/data/org.hfoss.posit/files/pic.png");
//
//			showPicture.setImageDrawable(x);
//
//		}
//	}

	}

