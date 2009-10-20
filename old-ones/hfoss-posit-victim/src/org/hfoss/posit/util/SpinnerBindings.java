package org.hfoss.posit.util;

import org.hfoss.posit.victim.DBHelper;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


	public class SpinnerBindings {
		
		private final static String APP = "CursorToSpinner";
		public static void bindCursor(Context context , Cursor c, Spinner nameSpinner, String newItemName) {
			int size;
			//set the size of array based on if the new item's name has been put in yet or not
			 size = (newItemName!=null )?c.count()+1:c.count(); 
			String names[] = new String[size];
			Log.i(APP,""+c.count());
			int i =0;
			while (c.next()){
				names[i++] = c.getString(c.getColumnIndex(DBHelper.KEY_NAME));
				Log.i(APP,names[i-1]);
			}
			if (newItemName !=null) names[size-1]=newItemName;
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item,names);
			nameSpinner.setAdapter(adapter);
			nameSpinner.setSelection(size-1);
		}
		
		public static void bindArray(Context context , String[] array, Spinner nameSpinner) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item,array);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			nameSpinner.setAdapter(adapter);
			nameSpinner.setSelection(adapter.getCount()-1);
		}
		
		public static void bindArrayFromResource(Context context, int resource, Spinner nameSpinner) {
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, resource, android.R.layout.simple_spinner_item);
			Log.i(APP,"Adapter Count"+adapter.getCount());
			 adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			 nameSpinner.setAdapter(adapter);
		}
	}
