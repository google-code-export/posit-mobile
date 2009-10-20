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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.hfoss.posit.web.ResponseParser;
import org.json.JSONException;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

/**
 * Activity to list any video clips associated with a specific find as a list adapter.
 * Clicking on any video in the list causes it to be played back.
 * @author Phil Fritzsche
 * @author James Jackson
 * @author Khanh Pham
 */
public class ListSearchResults extends ListActivity /*implements OnCreateContextMenuListener*/ {

	private static final String TAG = "ShowSearchedFind";

	private static final int REBUILT_LIST = 1;

	private List<HashMap<String, Object>> resultList;
	public static final int FIND_FROM_LIST = 0;
	public SimpleAdapter adapter;

	/** 
	 * Called when the program receives search results back from the server.
	 * @param savedInstanceState contains the Activity's previously frozen state.  In this case it is unused.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		resultList = new LinkedList<HashMap<String, Object>>();
		fillData();	
	}

	/** 
	 * This method is called when the activity is ready to start 
	 *  interacting with the user. It is at the top of the Activity
	 *  stack.
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		fillData();
	}

	/**
	 * Called when the system is about to resume some other activity.
	 *  It can be used to save state, if necessary.  In this case
	 *  we close the cursor to the DB to prevent memory leaks.
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onPause(){
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * Puts the items from the passed in list into the rows of the view.
	 */
	private void fillData() {
		Log.i(TAG, "filldata: populating the list");

		resultList = null;

		String result = getIntent().getStringExtra("result");
		ResponseParser rp = new ResponseParser(result);
		try {
			resultList = rp.parse();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		int mListSize = resultList.size();

		if (mListSize == 0) { //no video clips
			Utils.showToast(this, R.string.no_finds_returned);
			return;
		}

		String[] columns = { MyDBHelper.KEY_NAME, MyDBHelper.KEY_DESCRIPTION };
		int[] views = { R.id.name_search_id, R.id.description_search_id };

		// SimpleAdapter binds the data in 'columns' to the views in 'views'
		// It repeatedly calls ViewBinder.setViewValue() (see below) for each column
		adapter = new SimpleAdapter(this, resultList, R.layout.list_search_row, columns, views);
		setListAdapter(adapter);

		final ListView list = getListView();
		list.setFocusable(true);
		list.setOnCreateContextMenuListener(this);	
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_CANCELED) { return; }

		switch (requestCode) {
		case REBUILT_LIST:
			setResult(RESULT_OK, data);
			break;
		}
	}

	/**
	 * This method executes when the user clicks on one of the videos in the
	 *   list. It starts an intent to view the video.
	 *   @param listView is the ListView that was clicked on 
	 *   @param view is the View within the ListView
	 *   @param position is the View's position in the ListView
	 *   @param id is the video clip's RowID
	 */
	@Override
	protected void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);
		Intent intent = new Intent(this, FindActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		long keyId= Long.parseLong((String) resultList.get((int) id).get("id"));
		intent.putExtra(MyDBHelper.KEY_FIND_ID, keyId);
		Log.d("RESULT IS", "==="+keyId);
		startActivityForResult(intent, FIND_FROM_LIST);
		FindActivity.SAVE_CHECK=false;
	}
}