/*
 * File: MyLocationListener.java
 * 
 * Copyright (C) 2009 The Humanitarian FOSS Project (http://www.hfoss.org)
 * 
 * This file is part of POSIT, Portable Open Search and Identification Tool.
 *
 * POSIT is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) as published 
 * by the Free Software Foundation; either version 3.0 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU LGPL along with this program; 
 * if not visit http://www.gnu.org/licenses/lgpl.html.
 * 
 */
package org.hfoss.posit.utilities;

import org.hfoss.posit.R;
import org.hfoss.posit.R.id;

import android.app.Activity;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Bundle;
import android.widget.TextView;

class MyLocationListener implements android.location.LocationListener {
	Location mLastLocation;
	boolean mValid = false;
	Activity mActivity;
	String mProvider;

	public MyLocationListener(Activity activity, String provider) {
		mActivity = activity;
		mProvider = provider;
		mLastLocation = new Location(mProvider);
	}

	public void onLocationChanged(Location newLocation) {
		if (newLocation.getLatitude() == 0.0 && newLocation.getLongitude() == 0.0) {
			// filter out 0.0,0.0 locations
			return;
		}
		mLastLocation.set(newLocation);
		((TextView)mActivity.findViewById(R.id.latitudeText)).setText(" " + mLastLocation.getLatitude());
		((TextView)mActivity.findViewById(R.id.longitudeText)).setText(" " + mLastLocation.getLongitude());

		mValid = true;
	}

	public void onProviderEnabled(String provider) {
	}

	public void onProviderDisabled(String provider) {
		mValid = false;
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		if (status == LocationProvider.OUT_OF_SERVICE) {
			mValid = false;
		}
	}

	public Location current() {
		return mValid ? mLastLocation : null;
	}
}