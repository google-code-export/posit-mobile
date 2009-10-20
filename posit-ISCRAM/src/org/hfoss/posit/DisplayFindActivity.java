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
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class DisplayFindActivity extends Activity{
	private static final String TAG = "DisplayFindActivity";
	private ArrayList<String> coreViewItems = new ArrayList<String>(); 
	private HashMap<String, View> displayFindViews;
	private long mRowId;
	private DBHelper mDBHelper;

	public DisplayFindActivity() {
		displayFindViews = new HashMap<String, View>();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		coreViewItems = Utils.getFieldsFromResource(this, R.array.TABLE_FINDS_core_fields);
		setContentView(R.layout.display_find);
		mapLayoutItems();
		mRowId = intent.getExtras().getLong(DBHelper.KEY_ID);
		
		mDBHelper = new DBHelper(this);
		mDBHelper.open();
		
		populateFields();
			
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
	}

	private void populateFields() {
		mDBHelper.fetchFind(R.array.TABLE_FINDS, getResources().getStringArray(R.array.TABLE_FINDS_core_fields)
				,mRowId,displayFindViews);
	}

	/**
	 * Adds the layout items to the addFindViews HashMap so that they're accessible
	 * the idea here is to use the same field names as the table to save and retrieve items.
	 * This assumes there are some naming conventions followed while naming the text fields
	 * the names of the fields are assumed to be <fieldName>Text. Have to add some conditions
	 * for checkboxes and stuff
	 */
	private void mapLayoutItems() {
		for (String item:coreViewItems) {
			Integer resId=getResources().getIdentifier("org.hfoss.posit:id/"+item+"Text", null, null);
			Log.w(TAG,"Id for org.hfoss.posit:id/"+item+"Text "+resId );
			if (resId != 0) {
				displayFindViews.put(item, findViewById(resId));
			}
			
		}
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.edit_find_menu_item:
			intent = new Intent (this, AddFindActivity.class);
			intent.setAction(Intent.ACTION_EDIT);
			intent.putExtra(DBHelper.KEY_ID, mRowId);
			startActivity(intent);
			break;
		case R.id.delete_find_menu_item:
			if (mDBHelper.deleteFind(R.array.TABLE_FINDS,mRowId))
				Utils.showToast(this, R.string.deleted_from_database);
			else 
				Utils.showToast(this, R.string.delete_failed);
			finish();
			break;
		default:
			return false;
		}
		return true;
	}
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.display_finds_menu, menu);
		return true;
	}
}
