package org.hfoss.posit.util;

import java.util.HashMap;

import org.hfoss.posit.victim.DBHelper;
import org.hfoss.posit.victim.R;

import android.content.Context;
import android.util.Log;

import com.google.android.maps.Point;
/**
 * DataPoint is supposed to be a generic term for representing data items in posit for maps.
 * Since a variety of data items/types are dealt with in the form of records or different occurrences
 * of records (often referred to as "sightings"), it is necessary to have a data structure to represent
 * them automatically in the required map and to loosely couple them together. 
 * @author Prasanna Gautam
 *
 */
public class DataPoint {
	public Point mPoint;
	public String mName;
	public String mDescription;
	private Context context;
	private static String APP = "DataPoint";
	private HashMap<String,String> args= new HashMap<String,String>();
	public DataPoint(Point point, String name, String description){
		mPoint = point;
		Log.i(APP," "+mPoint.getLatitudeE6()+"-"+mPoint.getLongitudeE6());
		//mType = type;
		args.put(DBHelper.KEY_NAME, name);
		args.put(DBHelper.KEY_DESCRIPTION, description);
	}
	
	public DataPoint (int latitude, int longitude, String name, String description){
		mPoint = new Point(latitude, longitude);
		Log.i(APP," "+latitude+"-"+longitude);
		args.put(DBHelper.KEY_NAME, name);
		args.put(DBHelper.KEY_DESCRIPTION, description);
	}
	
	public Point getPoint(){
		return mPoint;
	}
	
	public DataPoint(Point point, HashMap<String,String> initValues) {
		mPoint = point;
		if (initValues.containsKey(DBHelper.KEY_NAME)) {
			mName = initValues.get(DBHelper.KEY_NAME);
			args.remove(DBHelper.KEY_NAME);
		}else { 
			mName = "entry";
		}
		/*mName = "entry";*/
		args = initValues;
	}
	
	public HashMap<String,String> getMaps(){
		return args;
	}
}
