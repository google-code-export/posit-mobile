/*
 * File: ImageAdapter.java
 * 
 * Copyright (C) 2009 The Humanitarian FOSS Project (http://www.hfoss.org)
 * 
 * This file is part of POSIT, Portable Open Search and Identification Tool.
 *
 * POSIT is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) as published 
 * by the Free Software Foundation; either version 3.0 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU LGPL along with this program; 
 * if not visit http://www.gnu.org/licenses/lgpl.html.
 * 
 */
package org.hfoss.posit.utilities;

import java.util.List;

import org.hfoss.posit.R;
import org.hfoss.posit.R.string;
import org.hfoss.posit.R.styleable;
import org.hfoss.posit.provider.PositDbHelper;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.util.Log;
import java.util.ArrayList;



/**
 * Used for putting images in the view
 *
 */
public class ImageAdapter extends BaseAdapter {
    int mGalleryItemBackground;
	private Context mContext;
	private List<Bitmap> mBitmaps;
	private static final String TAG="ImageAdapter";
	private ArrayList<ContentValues> mList;
	
	public ImageAdapter(ArrayList<ContentValues> list, Context c) {
		mContext = c;
		mList = list;
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
    	if (mList != null)
    		return mList.size();
    	else 
    		return mBitmaps.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    /**
     * Called repeatedly to render the view of each item in the Gallery.
     * Note: rather than passing the entire Uri as a string and then
     * parsing it, you might be able to just past the Image ID and use
     * 
     * Note on memory leak: Do we really need the cursor here? Wouldn't
     * ContentValues also work?
     * 
     */
    public View getView(int position, View convertView, ViewGroup parent) {
      ImageView i = new ImageView(mContext);

     if (mList != null) {
	      if (convertView == null) {
	           ContentValues values = mList.get(position);
	           String s = values.getAsString(PositDbHelper.PHOTOS_IMAGE_URI);
           		if (s != null) {
           			i.setImageURI(Uri.parse(s));
           			Log.i(TAG, "Uri = " + s);
           		}
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