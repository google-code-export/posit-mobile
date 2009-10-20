package org.hfoss.posit;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class TabMain extends TabActivity {
	public static final String TAG = "TabMain";
	
	TabHost mTabHost;
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
				.setContent(new Intent(this, PositMain.class)));

		mTabHost.addTab(mTabHost.newTabSpec(TAB_VIEW_FINDS)
				.setIndicator(getResources().getString(R.string.tt_view_finds))
				.setContent(new Intent(this, ListFindsActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec(TAB_CREATE_FIND)
				.setIndicator(getResources().getString(R.string.tt_add_finds))
				.setContent(new Intent(this, FindActivity.class).setAction(Intent.ACTION_INSERT)));
		mTabHost.addTab(mTabHost.newTabSpec(TAB_MAPS_GPS)
				.setIndicator(getResources().getString(R.string.tt_maps_gps))
				.setContent(new Intent(this, StartMapActivity.class)));

		mTabHost.setCurrentTab(0);
	}
}