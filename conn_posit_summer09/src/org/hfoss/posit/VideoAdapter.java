package org.hfoss.posit;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.VideoView;


public class VideoAdapter extends BaseAdapter {
	int mGalleryItemBackground;
	private Context mContext;
	private Cursor mCursor;
	private List<File> mVideos;

	public VideoAdapter(Cursor cursor, Context c) {
		mContext = c;
		mCursor = cursor;
		// See res/values/attrs.xml for the  defined values here for styling
		TypedArray a = mContext.obtainStyledAttributes(R.styleable.Gallery1);
		mGalleryItemBackground = a.getResourceId(
				R.styleable.Gallery1_android_galleryItemBackground, 0);
		a.recycle();
	}

	public VideoAdapter(Context c, List<File> videos) {
		mContext = c;
		mVideos = videos;
		// See res/values/attrs.xml for the  defined values here for styling
		TypedArray a = mContext.obtainStyledAttributes(R.styleable.Gallery1);
		mGalleryItemBackground = a.getResourceId(
				R.styleable.Gallery1_android_galleryItemBackground, 0);
		a.recycle();
	}

	public int getCount() {
		if (mCursor != null)
			return mCursor.getCount();
		else 
			return mVideos.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		VideoView view = new VideoView(mContext);
		if (mCursor != null) {
			mCursor.requery();
			if (convertView == null) {
				mCursor.moveToPosition(position);
				String s = mCursor.getString(mCursor.getColumnIndexOrThrow(mContext.getString(R.string.videoUriDB)));
				if (s != null)
					view.setVideoURI(Uri.parse(s));
				view.setLayoutParams(new Gallery.LayoutParams(136, 136));
				view.setBackgroundResource(mGalleryItemBackground);   
			}
		} else {
			if (convertView == null) {
				File video = mVideos.get(position);
				if (video != null)
					view.setVideoPath(video.getPath());
				view.setLayoutParams(new Gallery.LayoutParams(136, 136));
				view.setBackgroundResource(mGalleryItemBackground);

			}
		}
		return view;
	}


}
