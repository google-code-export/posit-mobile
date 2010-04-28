/*
 * File: MyItemizedOverlay.java
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

import java.util.ArrayList;

import org.hfoss.posit.FindActivity;
import org.hfoss.posit.provider.PositDbHelper;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class MyItemizedOverlay extends ItemizedOverlay {
	private static final String TAG = "ItemizedOverlay";

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;
	private boolean isTappable;

	/**
	 * @param defaultMarker
	 */
	public MyItemizedOverlay(Drawable defaultMarker, Context c, boolean isTappable) {
		super(boundCenterBottom(defaultMarker));
		mContext = c;
		this.isTappable = isTappable;
	}

	/* (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#createItem(int)
	 */
	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	/* (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#size()
	 */
	@Override
	public int size() {
		return mOverlays.size();
	}

	public void addOverlay(OverlayItem overlay) {
		mOverlays.add(overlay);
		populate();
	}

	/**
	 * Called when the user clicks on one of the Find icons
	 *   in the map. It shows a description of the Find
	 * @param pIndex is the Find's index in the ArrayList
	 */
	@Override
	protected boolean onTap(int pIndex) {
		// show the description
		// Toast.makeText(mContext, mOverlays.get(pIndex).getSnippet(), Toast.LENGTH_LONG).show();
		if (!isTappable)
			return false;
		Intent intent = new Intent(mContext, FindActivity.class);
		intent.setAction(Intent.ACTION_EDIT);
		long itemId = Long.parseLong(mOverlays.get(pIndex).getTitle());
		Log.i(TAG, "itemID= " + itemId);

		intent.putExtra(PositDbHelper.FINDS_ID, itemId); // Pass the RowID to FindActivity
//		intent.putExtra(PositDbHelper.FINDS_GUID, itemId); // Pass the RowID to FindActivity
		mContext.startActivity(intent);
		return true;
	}
}
