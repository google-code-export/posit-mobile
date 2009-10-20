/**
 * Extension of MyLocationOverlay
 * @author Phil Fritzsche
 * 
 * This is an extension of the built in MyLocationOverlay class. Currently, its
 * sole purpose is to display the latitude and longitude of a tap on the screen
 * whenever someone taps the map.
 */

package com.hfoss.summer09.cc;

import android.content.Context;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class MMyLocationOverlay extends MyLocationOverlay
{	
	public MMyLocationOverlay(Context context, MapView mapView)
	{
		super(context, mapView);
	}

	/**
	 * @param gp GeoPoint of the location of the person's tap
	 * @param mapView The mapView currently in use by the system
	 */
	@Override
	public boolean onTap(GeoPoint gp, MapView mapView)
	{
		double lat = gp.getLatitudeE6() / 1E6;
		double lon = gp.getLongitudeE6() / 1E6;
		Utils.shortToast(mapView.getContext(), "Latitude: " + lat + "\n" + "Longitude: " + lon);
		return false;
	}

}