/*******************************************************************************
 * Copyright (c) 2009 .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Prasanna Gautam - initial API and implementation
 *     Ralph Morelli - Supervisor
 *     Trishan deLanerolle - Director
 *     Antonio Alcorn - Summer 2009 Intern
 *     Gong Chen - Summer 2009 Intern
 *     Chris Fei - Summer 2009 Intern
 *     Qianqian Lin - Summer 2009 Intern 
 ******************************************************************************/
package org.hfoss.posit;

import android.app.Activity;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Bundle;
import android.widget.TextView;

/**
 * @author rmorelli
 *
 */
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
		if (newLocation.getLatitude() == 0.0
				&& newLocation.getLongitude() == 0.0) {
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

