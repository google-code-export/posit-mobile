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

import org.hfoss.posit.web.Communicator;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class ListFindsActivity extends ListActivity implements ViewBinder{

	private static final String TAG = "ListActivity";
	private DBHelper mDbHelper;

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_finds);
		mDbHelper = new DBHelper(this);
		
	}

	/**
	 * Put the items from the table into the rows.
	 */
	private void fillData() {
		mDbHelper.open();
		String[] from = new String[] {
	    		DBHelper.KEY_ID,
	    		DBHelper.KEY_NAME,
	    		DBHelper.KEY_DESCRIPTION,
	    		DBHelper.KEY_SYNCED
	    	};
		Cursor c = mDbHelper.fetchSelectedColumns(R.array.TABLE_FINDS, from);
    	if (c.getCount() == 0) return;
    	startManagingCursor(c);
        int[] to = new int[] {R.id.find_image, R.id.name_id,R.id.description_id,R.id.status};
        /* bind the item in 'from' to the views in 'from' */
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.list_row, c, from, to);
        adapter.setViewBinder(this);
        setListAdapter(adapter);
        mDbHelper.close();
	}
	
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent intent = new Intent(this, AddFindActivity.class);
        intent.setAction(Intent.ACTION_EDIT);
        
        intent.putExtra(DBHelper.KEY_ID, id);
        
        startActivity(intent);
    }
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
        fillData();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list_finds_menu, menu);
		return true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case R.id.new_find_menu_item:
				intent = new Intent (this, AddFindActivity.class);
				intent.setAction(Intent.ACTION_INSERT);
				startActivity(intent);
				break;
			case R.id.sync_finds_menu_item:
				//Utils.showToast(this, "Sync not implemented");
				syncFinds();
				fillData();
				break;
			case R.id.map_finds_menu_item:
				mDbHelper.close();
				intent = new Intent(this, MapFindsActivity.class);
				startActivity(intent);
				break;
		}
		
		return true;
	}
	
	private void checkNetwork() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		for (NetworkInfo networkInfo : netInfo) {
			Log.i(TAG, networkInfo.toString());
		}
		DhcpInfo dhcpInfo = new DhcpInfo();
		Log.i(TAG, dhcpInfo.dns1+"");
		Log.i(TAG,dhcpInfo.dns2+"");
	}
	
	/**
	 * Sync the finds with the server.
	 */
	private void syncFinds() {
		checkNetwork();
		String[] projection = new String[]{
    			DBHelper.KEY_ID,"name","identifier","latitude","longitude","description","time"
    		};
		// FIXME getAllUnsynced from DBHelper
		mDbHelper.open();
		Cursor c = mDbHelper.getAllUnsynced(R.array.TABLE_FINDS);

		Communicator comm = new Communicator();
		comm.sendFinds(this,projection, c);
		//comm.receiveFinds(this);
		mDbHelper.close();
	}
	
	/**
	 * Binds to the view Binder to show the image and if the image is synced.
	 * @author pgautam
	 *
	 */
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		switch (view.getId()) {
		case R.id.find_image:
			int rowId = cursor.getInt(cursor
					.getColumnIndexOrThrow(DBHelper.KEY_ID));
			Cursor imagesQuery = managedQuery(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] {
							BaseColumns._ID, ImageColumns.BUCKET_ID },
					ImageColumns.BUCKET_ID + "=\"posit\" AND "
							+ ImageColumns.BUCKET_DISPLAY_NAME + "=\"posit|"
							+ rowId + "\"", null, null);
			try {
				if (imagesQuery.getCount() > 0) {
					imagesQuery.moveToFirst();
					ImageView i = (ImageView) view;
					int id = imagesQuery.getInt(cursor
							.getColumnIndexOrThrow(BaseColumns._ID));
					i.setImageURI(Uri.withAppendedPath(
							MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
							"" + id));
					i.setScaleType(ImageView.ScaleType.FIT_XY);
				}
			} catch (NullPointerException e) {
				// avoid null imageQueries
			}
			return true;
			
		case R.id.status:
			int status = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.KEY_SYNCED));
			/*CheckBox cb = (CheckBox) view;
			cb.setChecked(status==1?true:false);
			cb.setClickable(false);*/
			TextView tv = (TextView) view;
			tv.setText(status==1?"Synced":"Not synced");
			return true;
		default:
			
			return false;
			
		}
		
	}
	
}
