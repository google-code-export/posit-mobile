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

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * Activity to list any video clips associated with a specific find as a list adapter.
 * Clicking on any video in the list causes it to be played back.
 * @author Phil Fritzsche
 * @author Khanh Pham
 */
public class ListVideosActivity extends ListActivity implements OnCreateContextMenuListener {

	private static final String TAG = "ListVideoClipsActivity";

	private static final int CONFIRM_DELETE_DIALOG = 0;
	private static final int REBUILT_LIST = 1;

	private MyDBHelper mDbHelper;
	private Cursor mCursor;  // Used for DB accesses
	private int mState;

	private LinkedList<HashMap<String, String>> mTempVideoMaps;

	/** 
	 * This method is invoked when the user clicks on the view video button on the view
	 * find screen. It creates a DBHelper and calls fillData() to fetch data from the DB, or from the
	 * list passed in as an extra from the intent call.
	 * @param savedInstanceState contains the Activity's previously frozen state.  In this case it is unused.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTempVideoMaps = new LinkedList<HashMap<String, String>>();
		mState = getIntent().getIntExtra("type", 0);
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
		mTempVideoMaps.clear();
		switch (mState) {
		case FindActivity.STATE_EDIT:
			fillDataFromDb();
			break;
		case FindActivity.STATE_INSERT:
			fillDataFromList();
			break;
		}
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
		if (mCursor != null) {
			stopManagingCursor(mCursor);
			mCursor.close();
		}
		if (mDbHelper != null) {
			mDbHelper.close(); // NOTE WELL: Can't close while managing cursor
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mCursor != null) {
			stopManagingCursor(mCursor);
			mCursor.close();
		}
		if (mDbHelper != null) {
			mDbHelper.close(); // NOTE WELL: Can't close while managing cursor
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mCursor != null) {
			stopManagingCursor(mCursor);
			mCursor.close();
		}
		if (mDbHelper != null) {
			mDbHelper.close(); // NOTE WELL: Can't close while managing cursor
		}
	}

	/**
	 * Puts the items from the DB table into the rows of the view. Note that
	 *  once you start managing a Cursor, you cannot close the DB without 
	 *  causing an error.
	 */
	private void fillDataFromDb() {
		Log.i(TAG, "filldata: refilling the data");

		mDbHelper = new MyDBHelper(this);

		String[] columns = { MyDBHelper.KEY_VIDEO_URI };
		int[] views = { R.id.video_uri_id };

		mCursor = mDbHelper.getVideosCursor((int) getIntent().getLongExtra("rowId", -1));		
		Log.i(TAG, "rowId from videos cursor " + getIntent().getLongExtra("rowId", -1));
		if (mCursor.getCount() == 0) { // No video clips
			setContentView(R.layout.list_videos);
			return;
		}
		startManagingCursor(mCursor); // NOTE: Can't close DB while managing cursor

		// CursorAdapter binds the data in 'columns' to the views in 'views' 
		// It repeatedly calls ViewBinder.setViewValue() (see below) for each column
		SimpleCursorAdapter adapter = 
			new SimpleCursorAdapter(this, R.layout.list_row_video, mCursor, columns, views);
		setListAdapter(adapter); 

		final ListView list = getListView();
		list.setFocusable(true);
		list.setOnCreateContextMenuListener(this);
		//registerForContextMenu(this.getListView());

		mDbHelper.close();
	}

	/**
	 * Puts the items from the passed in list into the rows of the view.
	 */
	private void fillDataFromList() {
		Log.i(TAG, "filldata: refilling the data from list");

		int mListSize = getIntent().getIntExtra("length", 0);

		if (mListSize == 0) { //no video clips
			setContentView(R.layout.list_videos);
			return;
		} else {
			populateMapList(mListSize);
		}

		String[] columns = { MyDBHelper.KEY_VIDEO_URI };
		int[] views = { R.id.video_uri_id };

		// SimpleAdapter binds the data in 'columns' to the views in 'views'
		// It repeatedly calls ViewBinder.setViewValue() (see below) for each column
		SimpleAdapter adapter =
			new SimpleAdapter(this, mTempVideoMaps, R.layout.list_row_video, columns, views);
		setListAdapter(adapter);

		final ListView list = getListView();
		list.setFocusable(true);
		list.setOnCreateContextMenuListener(this);
		//registerForContextMenu(this.getListView());
	}

	/**
	 * Populates a list from the intent extras to be used for the SimpleAdapter.
	 */
	private void populateMapList(int length) {	
		Log.i(TAG, "populating list of hash maps");
		for (int i = 0; i < length; i++) {
			Log.i(TAG, "inserting #" + i + " video into mTempVideoMaps");
			Log.i(TAG, "uri: " + getIntent().getStringExtra(String.valueOf(i)));
			Log.i(TAG, "=====================");
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(MyDBHelper.KEY_VIDEO_URI, getIntent().getStringExtra(String.valueOf(i)));
			mTempVideoMaps.add(map);
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_CANCELED) {
			return;
		}

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
		switch (mState) {
		case FindActivity.STATE_EDIT:
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			Log.i(TAG, "rowId from intent extra: " + getIntent().getLongExtra("rowId", -1));
			Uri uri = mDbHelper.getVideoUriByPosition((int) getIntent().getLongExtra("rowId", -1), position);
			Log.i(TAG, "uri = " + uri.toString());
			if (uri != null) {
				intent.setData(uri);
				try {
					startActivity(intent); 
				} catch(ActivityNotFoundException e) {
					Log.e(TAG, e.toString());
				} catch(Error e) {
					Log.e(TAG, e.toString());
				}
			}
			break;

		case FindActivity.STATE_INSERT:
			intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			HashMap<String, String> data = (HashMap<String, String>) listView.getItemAtPosition(position);
			uri = Uri.parse(data.get(MyDBHelper.KEY_VIDEO_URI).toString());
			if (uri != null) {
				intent.setData(uri);
				startActivity(intent); 
			}
			break;
		} //switch
	}

	/**
	 * This method creates the menus for this activity.
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list_videos_menu, menu);
		return true;
	}

	/**
	 * Called when an item in the video list is clicked and held.
	 * Generating a menu by inflating the "individual_item_selected_menu.xml" file
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.individual_item_selected_menu, menu);
		menu.setHeaderTitle(this.getResources().getString(R.string.alert_dialog_delete_video));
	}

	/** 
	 * This method is invoked when a menu item is selected. It starts
	 *   the appropriate Activity.
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		int numberOfVideosLeft;

		switch (item.getItemId()) {

		case R.id.delete_all_videos: //options menu call; delete all videos
			Log.d(TAG, "delete all videos");
			if (mState == FindActivity.STATE_EDIT) {
				mDbHelper.close();
			}
			showDialog(CONFIRM_DELETE_DIALOG);
			break;

		case R.id.cancel_delete_video_audio: //context menu call; cancel deletion of specific file
			// User hit cancel, so do nothing
			return true;

		case R.id.confirm_delete_video_audio: //context menu call; delete specific file
			switch (mState) {
			case FindActivity.STATE_EDIT:
				MyDBHelper mDbHelper = new MyDBHelper(ListVideosActivity.this);
				if (mDbHelper.deleteVideo(info.id)) {
					mDbHelper.close();
					Utils.showToast(ListVideosActivity.this, R.string.deleted_from_database);
				}
				else {
					mDbHelper.close();
					Utils.showToast(ListVideosActivity.this, R.string.delete_failed);
				}
				numberOfVideosLeft = mDbHelper.getVideosCursor((int) getIntent().getLongExtra("rowId", -1)).getCount();
				if (numberOfVideosLeft == 0){
					finish();
					Intent intent = new Intent(ListVideosActivity.this, ListVideosActivity.class);
					intent.putExtra("rowId", getIntent().getLongExtra("rowId", -1));
					intent.putExtra("type", mState);
					intent.putExtra("length", mTempVideoMaps.size());
					startActivity(intent);
					return true;
				}

			case FindActivity.STATE_INSERT:
				//delete file
				File file = new File(Utils.getFilenameFromMediaUri(this,
						Uri.parse(mTempVideoMaps.get(info.position).get(MyDBHelper.KEY_VIDEO_URI))));
				file.delete();

				//remove file from list
				mTempVideoMaps.remove(info.position);

				//create intent to be set as result
				final Intent resultIntent = new Intent(ListVideosActivity.this, FindActivity.class);
				resultIntent.putExtra("length", mTempVideoMaps.size());
				for (int i = 0; i < mTempVideoMaps.size(); i++) {
					resultIntent.putExtra(String.valueOf(i), mTempVideoMaps.get(i).get(MyDBHelper.KEY_VIDEO_URI).toString());
				}
				resultIntent.setAction(Intent.ACTION_DEFAULT);
				setResult(RESULT_OK, resultIntent);

				//NOTE: unsure if the extra intents are necessary; after you setResult to a specific intent,
				//will that result change if the intent is changed after setting the result? If it is not,
				//can probably just use the same intent so as not to create unnecessary objects
				//[Will find out soon]

				//finish current activity
				finish();

				//create new intent to be passed onto recreation of this class
				Intent intent = new Intent(ListVideosActivity.this, ListVideosActivity.class);
				intent.putExtra("rowId", getIntent().getLongExtra("rowId", -1));
				intent.putExtra("type", mState);
				intent.putExtra("length", mTempVideoMaps.size());
				for (int i = 0; i < mTempVideoMaps.size(); i++) {
					intent.putExtra(String.valueOf(i), mTempVideoMaps.get(i).get(MyDBHelper.KEY_VIDEO_URI).toString());
				}

				//start new activity
				startActivityForResult(intent, REBUILT_LIST);
				break;
			}
		}
		return true;
	}

	/**
	 * This method is invoked by showDialog() when a dialog window is created. It displays
	 *  the appropriate dialog box, currently a dialog to confirm that the user wants to 
	 *  delete all the finds.
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {

		case CONFIRM_DELETE_DIALOG:
			return new AlertDialog.Builder(this)
			.setIcon(R.drawable.alert_dialog_icon)
			.setTitle(R.string.alert_dialog_video)
			.setPositiveButton(R.string.alert_dialog_ok, 
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// User clicked OK so delete video clips
					switch (mState) {
					case FindActivity.STATE_EDIT:
						MyDBHelper mDbHelper = new MyDBHelper(ListVideosActivity.this);
						if (mDbHelper.deleteVideos(mCursor)) {
							mDbHelper.close();
							Utils.showToast(ListVideosActivity.this, R.string.deleted_from_database);
						} else {
							mDbHelper.close();
							Utils.showToast(ListVideosActivity.this, R.string.delete_failed);
							dialog.cancel();
						}
						finish();
						Intent intent = new Intent(ListVideosActivity.this, ListVideosActivity.class);
						intent.putExtra("rowId", getIntent().getLongExtra("rowId", -1));
						intent.putExtra("type", mState);
						intent.putExtra("length", mTempVideoMaps.size());
						startActivity(intent);
						break;
					case FindActivity.STATE_INSERT:
						while (!mTempVideoMaps.isEmpty()) {
							String filename = Utils.getFilenameFromMediaUri(ListVideosActivity.this,
									Uri.parse(mTempVideoMaps.getFirst().get(MyDBHelper.KEY_VIDEO_URI).toString()));
							File file = new File(filename);
							file.delete();
							mTempVideoMaps.removeFirst();
						}
						mTempVideoMaps.clear();

						//create intent to be set as result
						final Intent resultIntent = new Intent(ListVideosActivity.this, FindActivity.class);
						resultIntent.putExtra("length", mTempVideoMaps.size());
						resultIntent.setAction(Intent.ACTION_DEFAULT);
						setResult(RESULT_OK, resultIntent);

						//finish current activity
						finish();

						//create new intent to be passed onto recreation of this class
						intent = new Intent(ListVideosActivity.this, ListVideosActivity.class);
						intent.putExtra("rowId", getIntent().getLongExtra("rowId", -1));
						intent.putExtra("type", mState);
						intent.putExtra("length", mTempVideoMaps.size());

						//start new activity
						startActivity(intent);
						break;
					}
				}
			})

			.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					/* User clicked Cancel so do nothing */
				}
			})
			.create();

		default:
			return null;

		} // switch
	}
}