/*******************************************************************************
 * Copyright (c) 2009 .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package org.hfoss.posit.utilities;

import java.util.ArrayList;

import org.hfoss.posit.FindActivity;
import org.hfoss.posit.provider.MyDBHelper;

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

	/**
	 * @param defaultMarker
	 */
	public MyItemizedOverlay(Drawable defaultMarker, Context c) {
		super(boundCenterBottom(defaultMarker));
		mContext = c;
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
		Intent intent = new Intent(mContext, FindActivity.class);
		intent.setAction(Intent.ACTION_EDIT);
		long itemId = Long.parseLong(mOverlays.get(pIndex).getTitle());
		Log.i(TAG, "itemID= " + itemId);

		intent.putExtra(MyDBHelper.COLUMN_IDENTIFIER, itemId); // Pass the RowID to FindActivity
		mContext.startActivity(intent);
		return true;
	}
}
