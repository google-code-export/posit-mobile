/**
 * 
 * @author Phil Fritzsche
 * Class of various useful functions.
 *
 */

package com.hfoss.summer09.cc;

import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

public class Utils
{
	/**
	 * Displays a short Toast text notification
	 * @param context
	 * @param text Text to be displayed
	 * @return
	 */
	public static void shortToast(Context context, String text)
	{
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * Displays a short Toast text notification
	 * @param context
	 * @param num int to be displayed
	 * @return
	 */
	public static void shortToast(Context context, int num)
	{
		Toast.makeText(context, num, Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * Displays a long Toast text notification
	 * @param context
	 * @param text Text to be displayed
	 * @return
	 */
	public static void longToast(Context context, String text)
	{
		Toast.makeText(context, text, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * Displays a long Toast text notification
	 * @param context
	 * @param num int to be displayed
	 * @return
	 */
	public static void longToast(Context context, int num)
	{
		Toast.makeText(context, num, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * Returns a <string, string> type hash map from a given cursor.
	 * @param c the cursor to work with
	 * @return hashmap containing string type values of the information from the given cursor
	 */
	public static HashMap<String,String> getMapFromCursor(Cursor c)
	{
		HashMap<String, String> map = new HashMap<String, String>();
		
		for (int i = 0; i < c.getColumnCount(); i++)
		{
			map.put(c.getColumnName(i), c.getString(i));
		}
		return map;
	}
}