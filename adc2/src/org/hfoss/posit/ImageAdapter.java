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

import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

/**
 * Used for putting images in the view
 * @author pgautam
 *
 */
class ImageAdapter extends BaseAdapter {
    int mGalleryItemBackground;
	private Context mContext;
	private Cursor mCursor;
	private List<Bitmap> mBitmaps;

    public ImageAdapter(Cursor cursor, Context c) {
        mContext = c;
        mCursor = cursor;
        // See res/values/attrs.xml for the  defined values here for styling
        TypedArray a = mContext.obtainStyledAttributes(R.styleable.Gallery1);
        mGalleryItemBackground = a.getResourceId(
                R.styleable.Gallery1_android_galleryItemBackground, 0);
        a.recycle();
    }
    
    public ImageAdapter(Context c, List<Bitmap> bitmaps) {
    	mContext = c;
    	mBitmaps = bitmaps;
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
    		return mBitmaps.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      ImageView i = new ImageView(mContext);
      if (mCursor != null) {
	      mCursor.requery();
	      if (convertView == null) {
	           mCursor.moveToPosition(position);
           		String s = mCursor.getString(mCursor.getColumnIndexOrThrow(mContext.getString(R.string.imageUriDB)));
           		if (s != null)
           			i.setImageURI(Uri.parse(s));
                i.setScaleType(ImageView.ScaleType.FIT_XY);
                i.setLayoutParams(new Gallery.LayoutParams(136, 136));
                i.setBackgroundResource(mGalleryItemBackground);   
	      }
      } else {
    	  if (convertView == null) {
       		Bitmap bm = mBitmaps.get(position);
       		if (bm != null)
       			i.setImageBitmap(bm);
            i.setScaleType(ImageView.ScaleType.FIT_XY);
            i.setLayoutParams(new Gallery.LayoutParams(136, 136));
            i.setBackgroundResource(mGalleryItemBackground);
    	  }
      }
      return i;
    }
  }