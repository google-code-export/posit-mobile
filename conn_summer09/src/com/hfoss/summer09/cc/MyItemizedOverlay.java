/**
 * Extension of Itemized Overlay
 * @author Phil Fritzsche
 * 
 * Overrides the ItemizedOverlay class from the android maps tree, allowing
 * us to use it to create map overlays.
 */

package com.hfoss.summer09.cc;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

@SuppressWarnings("unchecked")
public class MyItemizedOverlay extends ItemizedOverlay
{
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	
	/**
	 * Public constructor.
	 * @param defaultMarker
	 */
	public MyItemizedOverlay(Drawable defaultMarker)
	{
		super(boundCenterBottom(defaultMarker));
	}

	/**
	 * Internal call; used when populate() is called.
	 * @param i
	 * @return Overlay at index i
	 */
	@Override
	protected OverlayItem createItem(int i)
	{
		return mOverlays.get(i);
	}
	
	/**
	 * Add overlay to the array of overlays.
	 * @param overlay
	 * @return
	 */
	public void addOverlay(OverlayItem overlay)
	{
		mOverlays.add(overlay);
		populate();
	}

	/**
	 * Gets the size of the array of overlays
	 * @return mOverlays' size
	 */
	@Override
	public int size()
	{
		return mOverlays.size();
	}

}
