package org.hfoss.posit.util;

import org.hfoss.posit.DBHelper;

import android.R;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Everything that needs to be bound to a spinner goes here.
 * @author pgautam
 *
 */
	public class SpinnerBindings {
		
		private final static String APP = "CursorToSpinner";
		/**
		 * Binds a cursor and it's values to the given Spinner
		 * @param context
		 * @param c
		 * @param nameSpinner
		 * @param newItemName
		 */
		public static void bindCursor(Context context , Cursor c, Spinner nameSpinner, String newItemName) {
			int size;
			//set the size of array based on if the new item's name has been put in yet or not
			 size = (newItemName!=null )?c.getCount()+1:c.getCount(); 
			String names[] = new String[size];
			Log.i(APP,""+c.getCount());
			int i =0;
			while (c.moveToNext()){
				names[i++] = c.getString(c.getColumnIndex(DBHelper.KEY_NAME));
				Log.i(APP,names[i-1]);
			}
			if (newItemName !=null) names[size-1]=newItemName;
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item,names);
			nameSpinner.setAdapter(adapter);
			nameSpinner.setSelection(size-1);
		}
		/**
		 * Binds cursor to spinner, differently. 
		 * @param context
		 * @param c
		 * @param nameSpinner
		 * @param newItemName
		 * @param bindTo
		 */
		public static void bindCursor(Context context , Cursor c, Spinner nameSpinner, String newItemName, String bindTo) {
			int size;
			//set the size of array based on if the new item's name has been put in yet or not
			 size = (newItemName!=null )?c.getCount()+1:c.getCount(); 
			String names[] = new String[size];
			Log.i(APP,""+c.getCount());
			int i =0;
			while (c.moveToNext()){
				names[i++] = c.getString(c.getColumnIndex(bindTo));
				Log.i(APP,names[i-1]);
			}
			if (newItemName !=null) names[size-1]=newItemName;
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item,names);
			nameSpinner.setAdapter(adapter);
			nameSpinner.setSelection(size-1);
		}
		/**
		 * Binds array to a spinner.
		 * @param context
		 * @param array
		 * @param nameSpinner
		 */
		public static void bindArray(Context context , String[] array, Spinner nameSpinner) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item,array);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
			nameSpinner.setAdapter(adapter);
			nameSpinner.setSelection(adapter.getCount()-1);
		}
		
		public static void bindArrayFromResource(Context context, int resource, Spinner nameSpinner) {
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, resource, android.R.layout.simple_spinner_item);
			Log.i(APP,"Adapter Count"+adapter.getCount());
			 adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
			 nameSpinner.setAdapter(adapter);
		}
	}
