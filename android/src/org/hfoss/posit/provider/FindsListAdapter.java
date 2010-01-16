/*
 * File: ListViewAdapter.java
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

package org.hfoss.posit.provider;
import android.content.ContentValues;
import android.content.Context;

import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hfoss.posit.R;


public class FindsListAdapter extends BaseAdapter {
	private static final String TAG = "ListActivity";

    private Context context;
    private ArrayList<HashMap<String,String>>  mList;
    private View mView;

    public FindsListAdapter(Context context, ArrayList<HashMap<String,String>> list, View v) { 
        this.context = context;
        this.mList = list;
        this.mView = v;
    }

    public int getCount() {                        
        return mList.size();
    }

    public Object getItem(int position) {     
        return mList.get(position);
    }

    public long getItemId(int position) {  
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) { 
        HashMap map = mList.get(position);
        return new ListAdapterView(this.context, map, mView);
    }
    
    class ListAdapterView extends LinearLayout {        
		private String[] columns = PositDbHelper.list_row_data;
		private int [] views = PositDbHelper.list_row_views;
    	private View mView;
    	private LinearLayout.LayoutParams  params;
    	private long mId;

    	Context context;
        public ListAdapterView(Context context, HashMap<String,String> map, View view) {
        	super(context);
        	this.context = context;
        	this.mView = view;
        	
        	mId =  Long.parseLong(map.get(PositDbHelper.FINDS_ID).toString());

        	if (view != null) {
            	Log.i(TAG, "Resource= " + mView.getResources());
        	}
       //	Log.i(TAG, "Resource= " + mView.getResources().getResourceName(R.id.latitudeText));
           	this.setOrientation(VERTICAL);
        	params = 
                new LinearLayout.LayoutParams(100, LayoutParams.WRAP_CONTENT);
            params.setMargins(1, 1, 1, 1);

        	for (int k = 0; k < views.length; k++) {
        		setViewValue(views[k], columns[k], map);
        	}
        }

        public boolean setViewValue(int viewId, String data, HashMap map) {
        	View view = null;
        	TextView tv = null; // = (TextView) view;
        	//    		long findIden = cursor.getLong(cursor.getColumnIndexOrThrow(PositDbHelper.FINDS_ID));
        	switch (viewId) {
          	case R.id.row_id:
           		//tv  = new TextView(context);
            	tv = (TextView) mView.findViewById(R.id.row_id);
    			tv.setText( map.get(PositDbHelper.FINDS_ID).toString() );
    		//	tv.setTextSize(14f);
    			addView(tv, params); 
        		return true;
          	case R.id.barcode_id:
           		tv  = new TextView(context);
    			tv.setText( map.get(PositDbHelper.FINDS_GUID).toString() );
    			tv.setTextSize(14f);
    			addView(tv, params); 
        		return true;
          	case R.id.name_id:
           		tv  = new TextView(context);
    			tv.setText( map.get(PositDbHelper.FINDS_NAME).toString() );
    			tv.setTextSize(14f);
    			addView(tv, params); 
        		return true;
          	case R.id.description_id:
           		tv  = new TextView(context);
    			tv.setText( map.get(PositDbHelper.FINDS_DESCRIPTION).toString() );
    			tv.setTextSize(14f);
    			addView(tv, params); 
        		return true;     
           	case R.id.latitude_id:
           		tv = new TextView(context);
    			tv.setText( map.get(PositDbHelper.FINDS_LATITUDE).toString() );
    			tv.setTextSize(14f);
    			addView(tv, params); 
        		return true;
        	case R.id.longitude_id:
          		tv = new TextView(context);
    			tv.setText( map.get(PositDbHelper.FINDS_LONGITUDE).toString() );
    			tv.setTextSize(14f);
    			addView(tv, params); 
         		return true;
        	case R.id.status:
         		tv = new TextView(context);
           		int status = Integer.parseInt(map.get(PositDbHelper.FINDS_SYNCED).toString());
           		tv.setText( status==1?"Synced  ":"Not synced  " );
    			tv.setTextSize(14f);
    			addView(tv, params); 
        		return true;
        	case R.id.num_photos:
        		tv = new TextView(context);
        		tv.setText("-1 photos");
        		tv.setTextSize(14f);
    			addView(tv, params); 
        		return true;
        	case R.id.find_image:
        		Log.i(TAG,"setViewValue case find_image=" );
        		PositDbHelper myDbHelper = new PositDbHelper(context);
        		ContentValues values = myDbHelper.getImages(mId);
        		ImageView iv = new ImageView(context);
        		if (values != null && values.containsKey(PositDbHelper.PHOTOS_IMAGE_URI)) {
        			String strUri = values.getAsString(PositDbHelper.PHOTOS_IMAGE_URI);
        			Log.i(TAG,"setViewValue strUri=" + strUri);
        			if (strUri != null) {
        				Log.i(TAG,"setViewValue strUri=" + strUri);
        				Uri iUri = Uri.parse(strUri);
        				iv.setImageURI(iUri);
        				iv.setScaleType(ImageView.ScaleType.FIT_XY);
        			} else {
        				iv.setImageResource(R.drawable.person_icon);
        				iv.setScaleType(ImageView.ScaleType.FIT_XY);
        			}
        			addView(iv, params); 
        		} else {
        			iv.setImageResource(R.drawable.person_icon);
        			iv.setScaleType(ImageView.ScaleType.FIT_XY);
        			addView(iv, params); 

        		}
        		Log.i(TAG,"setViewValue case find_image finished");
        		return true;
         	default:
        		return false;
        	}
        }

    }

}