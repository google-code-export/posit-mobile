/**
 * GPS Utils
 * @author Phil Fritzsche
 * 
 * This is a class designed to initiate and make calls to the GPS.
 */

package com.hfoss.summer09.cc;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;

public class GpsUtils
{
	//creation of gps variables
	private LocationManager manager;
	private LocationListener listener;
	private GeoPoint currentGp;
	private String provider;

	//timer variables
	private int time;
	public static final int SLOW_UPDATE = 30000;
	public static final int QUICK_UPDATE = 3000;
	public static final int CONSTANT_UPDATE = 0;

	public GpsUtils() { }

	/**
	 * Initialize the required GPS-related variables.
	 * @param
	 * @return
	 */
	public void initGps(Context context, int duration)
	{
		time = duration;
		manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE); 
		listener = new MyLocationListener(); 
		provider = LocationManager.GPS_PROVIDER;
		manager.requestLocationUpdates(provider, time, 0, listener);
	}

	/**
	 * Enables the location listener.
	 * @param
	 * @return
	 */
	public void enableListener()
	{
		manager.requestLocationUpdates(provider, time, 0, listener);
	}

	/**
	 * Disables the location listener.
	 * @param
	 * @return
	 */
	public void disableListener()
	{
		manager.removeUpdates(listener);
	}

	/**
	 * @param currentGp the currentGp to set
	 */
	public void setCurrentGp(GeoPoint currentGp)
	{
		this.currentGp = currentGp;
	}

	/**
	 * @return the currentGp
	 */
	public GeoPoint getCurrentGp()
	{
		return currentGp;
	}

	/**
	 * 
	 * Class to monitor for GPS location updates; implements default in LocationListener class.
	 * @author Phil Fritzsche
	 *
	 */
	class MyLocationListener implements LocationListener
	{
		public void onLocationChanged(Location location) //if location is changed
		{
			setCurrentGp(new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6)));
		}

		public void onProviderDisabled(String provider) { }

		public void onProviderEnabled(String provider) { }

		public void onStatusChanged(String provider, int status, Bundle extras) { }
	}
}
