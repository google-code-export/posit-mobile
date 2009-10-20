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
 *     Phil Fritzsche - Summer 2009 Intern
 *     James Jackson - Summer 2009 Intern
 *     Qianqian Lin - Summer 2009 Intern 
 *     Khanh Pham - Summer 2009 Intern
 ******************************************************************************/

package org.hfoss.posit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
 * @author Prasanna Gautam
 * @author Qianqian Lin
 */
public class Utils {
	public static boolean debug = true;
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
	 * Given a context and MediaStore-specific uri, this function will return the filename
	 * associated with the uri. Note: only works on items that can be found in MediaStore.
	 * @param mContext
	 * @param mUri
	 * @return Filename the URI links to
	 */
	public static String getFilenameFromMediaUri(Context mContext, Uri mUri) {
		final String[] mProjection = new String[] {
				android.provider.BaseColumns._ID,
				android.provider.MediaStore.MediaColumns.TITLE,
				android.provider.MediaStore.MediaColumns.DATA };

		Cursor mCursor= mContext.getContentResolver().query(mUri, mProjection, null, null, null);

		String mFilename = null;
		
		if (mCursor != null && mCursor.moveToFirst()) {
			mFilename = mCursor.getString(mCursor.getColumnIndexOrThrow(mProjection[2]));
		}
		
		return mFilename;
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

	public static HashMap<String,String> getMapFromCursor(Cursor c){
		HashMap<String, String> myMap = new HashMap<String, String>();
		for (int i = 0; i < c.getColumnCount(); i++) {
			myMap.put(c.getColumnName(i), c.getString(i));
		}
		return myMap;
	}

	/**
	 * Checks if the phone has any connection at all, wifi or data.  Used at startup to see if the phone should
	 * go into ad hoc mode or not.  
	 * 
	 * @param mContext the Context asking for the connection, in this case only the main activity
	 * @return whether any connection exists
	 */
	public static boolean isConnected(Context mContext) {
		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info ==  null)
			return false;
		else return true;
	}
}
