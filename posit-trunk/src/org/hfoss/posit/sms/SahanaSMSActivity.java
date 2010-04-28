package org.hfoss.posit.sms;

import java.util.List;

import org.hfoss.posit.R;
import org.hfoss.posit.R.id;
import org.hfoss.posit.R.layout;
import org.hfoss.posit.R.menu;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SahanaSMSActivity extends Activity implements OnClickListener, LocationListener, GpsStatus.Listener{
	private String phoneNumber;
	private String message;
	private String username;
	private String title;
	
	String TAG = "TEST";
	
	private LocationManager mLocationManager;
	private Location mLocation;
	private double mLongitude = 0;
	private double mLatitude = 0;
	
	private TextView mLocationTextView;

	private Thread mThread;
	
	
	
	
	Handler updateHandler = new Handler() {

		/** Gets called on every message that is received */
		public void handleMessage(Message msg) {
			String s = mLatitude+","+mLongitude;
			mLocationTextView.setText(s);
			super.handleMessage(msg);
		}
	};
	protected Object gpsStatus;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sahanasms);
        ((Button)findViewById(R.id.send)).setOnClickListener(this);
        mLocationTextView = (TextView)findViewById(R.id.locationText);
        initializeLocationAndStartGpsThread();
    }

    

	private boolean checkNumber(String number) {
		for(int i = 0; i < number.length(); i++) {
			if(number.charAt(i)<'0'|| number.charAt(i)>'9')
				if(!(i==0&&number.charAt(i)=='+'))
					return false;
		}
		return true;
	}
	
	/**
	 * Sets the Find's location to the last known location and starts 
	 *  a separate thread to update GPS location.
	 */
	private void initializeLocationAndStartGpsThread() {
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		List<String> providers = mLocationManager.getProviders(true);
		Log.i(TAG, "Enabled providers = " + providers.toString());
		String provider = mLocationManager.getBestProvider(new Criteria(),true);
		Log.i(TAG, "Best provider = " + provider);

		setCurrentGpsLocation(null);   
		mThread = new Thread(new MyThreadRunner());
		mThread.start();
	}

	/**
	 * Repeatedly attempts to update the Find's location.
	 */
	class MyThreadRunner implements Runnable {
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				Message m = Message.obtain();
				m.what = 0;
				SahanaSMSActivity.this.updateHandler.sendMessage(m);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
    
	private void setCurrentGpsLocation(Location location) {
		String bestProvider = "";
		if (location == null) {
			mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			List<String> providers = mLocationManager.getProviders(true);
			Log.i(TAG, "Enabled providers = " + providers.toString());
			bestProvider = "gps";//mLocationManager.getBestProvider(new Criteria(),true);
			if (bestProvider != null && bestProvider.length() != 0) {
				//mLocationManager.requestLocationUpdates(bestProvider, 15000, 0, this);	 // Every 30000 millisecs	
				location = mLocationManager.getLastKnownLocation(bestProvider);				
			}	
		}
			Log.i(TAG, "Best provider = |" + bestProvider + "|");

		try {
			mLongitude = location.getLongitude();
			mLatitude = location.getLatitude();
			Log.i("TEST", "lat = "+mLatitude+"\tlong = "+mLongitude);
			Message msg = Message.obtain();
			this.updateHandler.sendMessage(msg);
		} catch (NullPointerException e) {
			Log.e(TAG, e.toString());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.sahanasmsmenu, menu);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if(item.getItemId()==R.id.refresh) {
			((EditText)findViewById(R.id.phoneNumber)).setText("4073715284");
			((EditText)findViewById(R.id.message)).setText("");
		}
		return true;
	}

	
	@Override
	protected void onPause() {
		mLocationManager.removeGpsStatusListener(this);
		mLocationManager.removeUpdates(this);
		super.onPause();
	}
	
	public void onGpsStatusChanged(int event) {
		gpsStatus = mLocationManager.getGpsStatus(null);
				
	}
	
	@Override
	protected void onResume() {
		mLocationManager.addGpsStatusListener(this);
		mLocationManager.requestLocationUpdates
		(LocationManager.GPS_PROVIDER, 5000, 0.0f, this);
		super.onResume();
	}


	public void onLocationChanged(Location newLocation) {
		setCurrentGpsLocation(newLocation);  
	}


	public void onProviderDisabled(String arg0) {
		setCurrentGpsLocation(null); 
	}


	public void onProviderEnabled(String arg0) {
		setCurrentGpsLocation(null); 	
	}


	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		setCurrentGpsLocation(null); 
	}



	public void onClick(View v) {
		if(v.getId()==R.id.send) { 

			phoneNumber = ((EditText)findViewById(R.id.phoneNumber)).getText().toString();
	        message = ((EditText)findViewById(R.id.message)).getText().toString();
	        username = ((EditText)findViewById(R.id.sahanaName)).getText().toString();
	        title = ((EditText)findViewById(R.id.title)).getText().toString();
	        setCurrentGpsLocation(null);
			Log.i("TEST", "Send to "+phoneNumber);
			Log.i("TEST", "Sending "+message);
			Log.i("TEST", "lat = "+mLatitude+"\tlong = "+mLongitude);
			
			message = "sm add ("+title+":"+username+") ("+message+") ("+mLatitude+", "+mLongitude+")";
			String SENT = "SMS_SENT";
	        String DELIVERED = "SMS_DELIVERED";
	        
	        List<String> providers = mLocationManager.getProviders(true);
			Log.i(TAG, "Enabled providers = " + providers.toString());
			String bestProvider = mLocationManager.getBestProvider(new Criteria(),true);
			/*if (bestProvider != null && bestProvider.length() != 0) {
				//mLocationManager.requestLocationUpdates(bestProvider, 5000, 0, this);	 // Every 30000 millisecs	
				mLocation = mLocationManager.getLastKnownLocation("gps");				
			}*/
			try{
				mLatitude = mLocation.getLatitude();
				mLongitude = mLocation.getLongitude();
			}catch(NullPointerException e){Log.i(TAG,e.toString());}
	        
	        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0,
	            new Intent(SENT), 0);
	 
	        PendingIntent deliveryIntent = PendingIntent.getBroadcast(this, 0,
	            new Intent(DELIVERED), 0);
	        
			if(phoneNumber.length()>0 && message.length()>0 && message.length()<=160 && checkNumber(phoneNumber)) {
				try {
				SmsManager sms = SmsManager.getDefault();
		        sms.sendTextMessage(phoneNumber, null, message, sentIntent, deliveryIntent);    
		        Toast.makeText(this, "SMS Sent!\n"+message, Toast.LENGTH_LONG).show();
		        Log.i("SMS",message);
		        ((EditText)findViewById(R.id.message)).setText("");
		        ((EditText)findViewById(R.id.title)).setText("");
				}catch(Exception e){Log.i("TEST",e.toString());}
			}
			else
				Toast.makeText(this, "SMS Failed\nCheck phone number or length of message", Toast.LENGTH_LONG).show();
		}
		
	}
    
}