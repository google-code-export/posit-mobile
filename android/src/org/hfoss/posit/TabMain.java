/*******************************************************************************
 * Copyright (c) 2009 .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package org.hfoss.posit;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;

public class TabMain extends TabActivity implements TabHost.OnTabChangeListener{
	public static final String TAG = "TabMain";
	
	static TabHost mTabHost;
	public static final String TAB_VIEW_FINDS = "tab_finds";
	public static final String TAB_CREATE_FIND = "tab_create_finds";
	public static final String TAB_MAIN = "tab_main";
	public static final String TAB_MAPS_GPS = "tab_maps_gps";
	
	/**
	 * This is the onCreate() for the Tab interface
	 * We initialize the tabs here
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.tab_main);
		initTabs();
	}
	
	/**
	 * Calls a setup method for each of the tabs
	 */
	private void initTabs() {
		mTabHost = getTabHost();
		mTabHost.setOnTabChangedListener(this);
		setupMainTab();
		setupViewFindsTab();
		setupAddFindTab();
		//mTabHost.addTab(mTabHost.newTabSpec(TAB_MAPS_GPS)
		//		.setIndicator(getResources().getString(R.string.tt_maps_gps))
		//		.setContent(new Intent(this, StartMapActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)));
		this.setDefaultTab(0);
	}
	
	/**
	 * Sets up the main tab. This displays PositMain
	 */
	private void setupMainTab() {
			// Force the class since overriding tab entries doesn't work
		Intent intent = new Intent();
		intent.setClass(this, PositMain.class);
		mTabHost.addTab(mTabHost.newTabSpec(TAB_MAIN)
			.setIndicator(getString(R.string.tt_view_main))
			.setContent(intent));
	}
	
	/**
	 * Sets up the List Finds tab.  This displays ListFindsActivity
	 */
	private void setupViewFindsTab() {
		// Force the class since overriding tab entries doesn't work
		Intent intent = new Intent();
		intent.setClass(this, ListFindsActivity.class);
		mTabHost.addTab(mTabHost.newTabSpec(TAB_VIEW_FINDS)
			.setIndicator(getString(R.string.tt_view_finds))
			.setContent(intent));
	}
	
	/**
	 * Sets up the Add Find tab.  This displays FindActivity.
	 */
	private void setupAddFindTab() {
		// Force the class since overriding tab entries doesn't work
		Intent intent = new Intent();
		intent.setClass(this, FindActivity.class);
		intent.setAction(Intent.ACTION_INSERT);
		mTabHost.addTab(mTabHost.newTabSpec(TAB_CREATE_FIND)
			.setIndicator(getString(R.string.tt_add_finds))
			.setContent(intent));
	}
	
	/**
	 * Changes which tab appears on the device.
	 * Main => tab=0
	 * List Finds => tab=1
	 * Add Find => tab=2
	 * @param tab is the index of the tab to which we change
	 */
	public static void moveTab(int tab) {
		mTabHost.setCurrentTab(tab);
	}
	
	/**
	 * This isn't currently used. It was used at one point for experimentation.
	 */
	public void onTabChanged(String tabId) {
		// Because we're using Activities as our tab children, we trigger
		// onWindowFocusChanged() to let them know when they're active.  This may
		// seem to duplicate the purpose of onResume(), but it's needed because
		// onResume() can't reliably check if a keyguard is active.
		Activity activity = getLocalActivityManager().getActivity(tabId);
		if (activity != null) {
			activity.onWindowFocusChanged(true);
		}
	}
}