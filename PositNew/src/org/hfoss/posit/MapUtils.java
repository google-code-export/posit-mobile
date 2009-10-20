package org.hfoss.posit;
import java.util.ArrayList;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;


public class MapUtils {
	private static final String TAG = "MapUtils";
	/**
	 * A static method to return a set of OverlayItems to overlay overlay over the mapView 
	 * @param c
	 * @param mapView
	 * @param mapController
	 * @return
	 */
	public static ArrayList<OverlayItem> getScaledOverlayItemsFromCursor(Cursor c, MapView mapView, MapController mapController) {
		if (c.getCount() <= 0)
			return null;
		mapController = mapView.getController();
		ArrayList<OverlayItem> mPoints = new ArrayList<OverlayItem>();
		int maxLong = (int)(-81*1E6);
		int maxLat = (int)(-81*1E6);
		int minLat = (int)(81*1E6);
		int minLong = (int)(81*1E6);
		while (c.moveToNext()) {
			int latitude = (int) (c.getDouble(c
				.getColumnIndex(MyDBHelper.KEY_LATITUDE))*1E6);
			int longitude = (int) (c.getDouble(c
				.getColumnIndex(MyDBHelper.KEY_LONGITUDE))*1E6);
			if (longitude>maxLong) maxLong = longitude;
			if (latitude>maxLat ) maxLat = latitude;
			if (latitude<minLat) minLat = latitude;
			if (longitude<minLong) minLong = longitude;
			String description = c.getString(c
				.getColumnIndex(MyDBHelper.KEY_DESCRIPTION));
			Log.i(TAG, latitude+" "+longitude+" "+description);
			mPoints.add(new OverlayItem(new GeoPoint(latitude,longitude),"",description));
		}
		mapController.zoomToSpan(maxLat-minLat, maxLong-minLong);
		return mPoints;
	}
}
	

