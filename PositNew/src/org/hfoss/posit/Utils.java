/*******************************************************************************
 * Copyright (c) 2009 .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     pgautam - initial API and implementation
 ******************************************************************************/
package org.hfoss.posit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.hfoss.posit.web.SyncService;

import android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
/**
 * A collection of utility functions that may not be worth moving to their own spaces
 * @author pgautam
 *
 */
public class Utils {
	/**
	 * This is for showing the Toast on screen for notifications
	 * 
	 * @param mContext
	 * @param text
	 */
	public static void showToast(Context mContext, String text) {
		Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
	}

	public static void showToast(Context mContext, int resId) {
		Toast.makeText(mContext, resId, Toast.LENGTH_SHORT).show();
	}
	/**
	 *  Helper method for creating notification 
	 *  You'll still need to get the NotificationManager using
	 *  mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)  
	 *
	 */
	public static void showDefaultNotification(Context cxt,NotificationManager mNotificationManager, int notification_id, String text){
		Notification notification = new Notification(R.drawable.btn_plus, "Posit", 
				System.currentTimeMillis());
		Intent intent = new Intent(cxt,SyncService.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(cxt, 0, intent, 0);
		notification.setLatestEventInfo(cxt, "Service started", "POSIT service started", pendingIntent);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		mNotificationManager.notify(2, notification); //magic number
	}
	/**
	 * This gives the list of strings in a string-array as an ArrayList
	 * 
	 * @param mContext
	 * @param resId
	 * @return
	 */
	public static ArrayList<String> getFieldsFromResource(Context mContext,
			int resId) {
		Resources res = mContext.getResources();
		ArrayList<String> items;
		items = new ArrayList<String>(Arrays.asList(res.getStringArray(resId)));
		return items;
	}
	/**
	 * Generates the ViewObjects from an Array resource given
	 * @param mContext
	 * @param resId Array declared in arrays.xml file
	 * @return
	 */
	public static ArrayList<ViewObject> getViewsFromResource(Context mContext,
			int resId) {
		Resources res = mContext.getResources();
		ArrayList<ViewObject> items = new ArrayList<ViewObject>();
		for (String field : res.getStringArray(resId)) {
			String[] str = field.split("\\s+");
			if (str.length==2)
				items.add(new ViewObject(str[0], str[1]));
			else if (str.length==3)
				items.add(new ViewObject(str[0], str[1],str[2]));
		}
		return items;
	}

	/**
	 * This is to find the name type of the columns from the table resource Id
	 * Since the tables are generated from the same resource Id, we are assured to have
	 * column_name type(as in integer,long,double,text etc. 
	 * This is very useful in pulling appropriate types from the database
	 * @param mContext
	 * @param resId
	 * @return
	 */
	public static HashMap<String,String> getColumnTypeMapFromResource(Context mContext, int resId){
		Resources res = mContext.getResources();
		HashMap<String,String> columnNameTypeMap = new HashMap<String, String>();
		
		for (String field : res.getStringArray(resId)) {
			String[] str = field.split("\\s+");
			columnNameTypeMap.put(str[0], str[1]);
		}
		return columnNameTypeMap;
	}
	
	/**
	 * Maps the items provided into the views
	 * 
	 * @param items
	 * @param views
	 */
	public static void putCursorItemsInViews(Cursor c, String[] items,
			HashMap<String, View> views) {
		for (String item : items) {
			View v = views.get(item);
			if (v instanceof TextView) {
				((TextView) v).setText(c.getString(c
						.getColumnIndexOrThrow(item)));
			} else if (v instanceof EditText) {
				((EditText) v).setText(c.getString(c
						.getColumnIndexOrThrow(item)));
			} else if (v instanceof CheckBox) {
				((CheckBox) v).setChecked(c.getInt(c
						.getColumnIndexOrThrow(item)) == 1 ? true : false);
			} else if (v instanceof RadioGroup) {
				((RadioGroup) v).check(c.getInt(c.getColumnIndexOrThrow(item)));
			}
		}
	}
	/**
	 * FIXME items isn't even used, at least need to throw some exception if there's no column 
	 * 
	 * @param c
	 * @param items
	 * @param viewObjects
	 */
	public static void putCursorItemsInViews(Context cxt,Cursor c, String[] items,
			ArrayList<ViewObject> viewObjects) {
		for (ViewObject viewObject : viewObjects) {
			viewObject.setValue(c.getString(c.getColumnIndexOrThrow(viewObject.getName())));
		}		

	}

	
	/**
	 * Finds the position of an object in the given resource array or -1 otherwise
	 * @param mContext
	 * @param object
	 * @param resId
	 * @return
	 */
	public static int getPositionInResourceArray(Context mContext, Object object, int resId) {
		Resources res = mContext.getResources();
		ArrayList<String> items;
		
		items = new ArrayList<String>(Arrays.asList(res.getStringArray(resId)));
		return items.indexOf(object);
	}

	/**
	 * @param mContext
	 * @param s
	 */
	public static void bindArrayListToSpinner(Context mContext, Spinner s, ArrayList<String> spinnerItems) {
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(mContext, 
				android.R.layout.simple_spinner_dropdown_item,  (String[])spinnerItems.toArray());
		s.setAdapter(adapter);
	}
	
	/**
	 * Gets the unique IMEI code for the phone used for identification
	 * The phone should have proper permissions (READ_PHONE_STATE) to be able to get this data.
	 */
	public static String getIMEI(Context mContext) {
		TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}
	
	
	public static ContentValues getContentValuesFromCursor(Cursor c) {
		ContentValues args = new ContentValues();
		for (int i = 0; i < c.getColumnCount(); i++) {
			args.put(c.getColumnName(i), c.getString(i));
		}
		return args;
	}
	
	public static List<Integer> getListFromCollection(Collection<Integer> mySet){
		List<Integer> myList = new ArrayList<Integer>();
		Iterator<Integer> iter = mySet.iterator();
    	while (iter.hasNext()) {
    		myList.add(iter.next());
    	}
    	return myList;
	}
	
}
