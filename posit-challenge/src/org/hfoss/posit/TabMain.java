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

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;

/**
 * @author Phil Fritzsche
 * @author James Jackson
 */
public class TabMain extends TabActivity {
	public static final String TAG = "TabMain";
	
	static TabHost mTabHost;
	public static final String TAB_VIEW_FINDS = "tab_finds";
	public static final String TAB_CREATE_FIND = "tab_create_finds";
	public static final String TAB_VIEW_PROJECTS = "tab_view_projects";
	public static final String TAB_MAPS_GPS = "tab_maps_gps";
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.tab_main);
		initTabs();
	}
	
	private void initTabs() {		
		mTabHost = getTabHost();
		
		mTabHost.addTab(mTabHost.newTabSpec(TAB_VIEW_PROJECTS)
				.setIndicator(getResources().getString(R.string.tt_view_main))
				.setContent(new Intent(this, PositMain.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)));
		mTabHost.addTab(mTabHost.newTabSpec(TAB_VIEW_FINDS)
				.setIndicator(getResources().getString(R.string.tt_view_finds))
				.setContent(new Intent(this, ListFindsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)));
		mTabHost.addTab(mTabHost.newTabSpec(TAB_CREATE_FIND)
				.setIndicator(getResources().getString(R.string.tt_add_finds))
				.setContent(new Intent(this, FindActivity.class).setAction(Intent.ACTION_INSERT).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)));
		//mTabHost.addTab(mTabHost.newTabSpec(TAB_MAPS_GPS)
		//		.setIndicator(getResources().getString(R.string.tt_maps_gps))
		//		.setContent(new Intent(this, StartMapActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)));
		this.setDefaultTab(0);
	}
	
	public static void moveTab(int tab) {
		mTabHost.setCurrentTab(tab);
	}
	
}