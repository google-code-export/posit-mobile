/**
 * @author Phil Fritzsche
 * @author James Jackson
 * Creates a list of available finds in the database on the phone.
 */

package com.hfoss.summer09.cc;

import java.io.File;
import java.io.IOException;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;

/*
 * (non-Javadoc)
 * Unfinished class; not properly displaying.
 */
public class ListFinds extends ListActivity implements ViewBinder, View.OnClickListener{
	public static final String TAG = "ListFinds";

	private MyDBHelper mDbHelper;
	private Cursor mCursor;
	private ContentValues tempVal;

	public static final int[] LIST_ROW_VIEWS = {	
		R.id.show_name,
		R.id.show_description,
		R.id.show_latitude,
		R.id.show_longitude,
		R.id.show_create_time,
		R.id.show_modified_time,
		R.id.show_sent_time,
		R.id.show_image,
		R.id.show_audio,
		R.id.show_video
	};

	/**
	 * Called upon activity creation. Sets content view to ListView, connects to DB and calls a cursor & adapter.
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.list);

		mDbHelper = new MyDBHelper(this);
		initData();
		mDbHelper.close();
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.show_video: 
			videoPlayBack();
		case R.id.show_audio: 
			try {
				audioPlayBack();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Initializes the cursor to point to all database information, then creates a simple cursor adapter 
	 * for the list activity, specifically focusing on the picture names in the database.
	 * @return
	 */
	private void initData() {
		String[] columns = { MyDBHelper.KEY_PIC_NAME };

		mCursor = mDbHelper.fetchAllFinds();		
		if (mCursor.getCount() == 0) { return; } //no finds available
		startManagingCursor(mCursor);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, 
				mCursor, columns, LIST_ROW_VIEWS);
		adapter.setViewBinder((ViewBinder) this);
		setListAdapter(adapter); //not displaying titles?
	}
	
	/**
	 * Plays back the specified audio file.
	 * @throws IllegalArgumentException
	 * @throws IllegalStateException
	 * @throws IOException
	 * @return
	 */
	public void audioPlayBack() throws IllegalArgumentException, IllegalStateException, IOException {
		MediaPlayer mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setDataSource(tempVal.getAsString(MyDBHelper.KEY_AUDIO));
		mMediaPlayer.prepare();
		mMediaPlayer.start();
	}

	/**
	 * Plays back the specified video file.
	 * @return
	 */
	public void videoPlayBack() {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(tempVal.getAsString(MyDBHelper.KEY_VIDEO))), "video/*");
		startActivity(intent);
	}

	/**
	 * Ref: ViewBinder Interface. Binds a specified view to a cursor column.
	 * @return true if handled by ViewBinder; false if otherwise. If false, the cursor adapter will attempt
	 * to handle the bind on its own.
	 */
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {		
		String[] proj = new String[1];
		int rowId;
		MyDBHelper myDbHelper;

		switch (view.getId()) {
		case R.id.show_image: //[FIX?]image
			proj[0] = MyDBHelper.KEY_IMAGE;
			rowId = cursor.getInt(cursor.getColumnIndexOrThrow(MyDBHelper.KEY_ID));
			myDbHelper = new MyDBHelper(this);
			tempVal = myDbHelper.fetchFindColumns(rowId, proj);
			if (tempVal != null) {
				Uri uri = Uri.parse(tempVal.getAsString(proj[0]));
				ImageView iv = (ImageView) view;

				iv.setImageURI(uri);
				iv.setScaleType(ImageView.ScaleType.FIT_XY);
				return true;
			}
		case R.id.show_video: //[FIX?]video
			proj[0] = MyDBHelper.KEY_VIDEO;
			rowId = cursor.getInt(cursor.getColumnIndexOrThrow(MyDBHelper.KEY_ID));
			mDbHelper = new MyDBHelper(this);
			tempVal = mDbHelper.fetchFindColumns(rowId, proj);
			if (tempVal != null) {
				Button b_playVideo = (Button) findViewById(R.id.show_video);
				b_playVideo.setOnClickListener(this);
				return true;
			}
		case R.id.show_audio: //[FIX?]audio
			proj[0] = MyDBHelper.KEY_AUDIO;
			rowId = cursor.getInt(cursor.getColumnIndexOrThrow(MyDBHelper.KEY_ID));
			mDbHelper = new MyDBHelper(this);
			tempVal = mDbHelper.fetchFindColumns(rowId, proj);
			if (tempVal != null) {
				Button b_playVideo = (Button) findViewById(R.id.show_audio);
				b_playVideo.setOnClickListener(this);
				return true;
			}
		}
		return false;
	}
}