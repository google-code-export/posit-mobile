/**
 * @author James Jackson
 * Main menu TabActivity that connects all modules of the application. This class sets up a tab
 * menu which allows the user to easily select 4 different options (GPS, Camera, About, and View)
 * that contain their own activity, and interface. 
 */

package com.hfoss.summer09.cc;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.Toast;

public class AppMain extends TabActivity {
	TabHost  mTabHost;
	Toast myToast;

	@Override   
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		callTabs();
	}

	public void callTabs(){
		gpsTab();
		cameraTab();
		viewTab();
		aboutTab();
	}

	public void gpsTab(){
		mTabHost = getTabHost();
		Intent intent = new Intent("com.hfoss.summer09.cc.SET");
		intent.setClass(this, StartGpsApp.class);
		mTabHost.addTab(mTabHost.newTabSpec("GPS")
				.setIndicator(("GPS"),
						getResources().getDrawable(R.drawable.ic_maps_web_details))
						.setContent(intent));
	}

	public void cameraTab(){
		mTabHost = getTabHost();
		Intent intent = new Intent("com.hfoss.summer09.cc.START_CAMERA_APP");
		intent.setClass(this, StartCameraApp.class);
		mTabHost.addTab(mTabHost.newTabSpec("Camera")
				.setIndicator(("Camera"),
						getResources().getDrawable(R.drawable.ic_camera_indicator_photo))
						.setContent(intent));
	}

	public void viewTab(){
		mTabHost = getTabHost();
		Intent intent = new Intent("com.hfoss.summer09.cc.VIEW_INFO");
		intent.setClass(this, ViewInfoApp.class);

		mTabHost.addTab(mTabHost.newTabSpec("View Info")
				.setIndicator(("View"),
						getResources().getDrawable(R.drawable.ic_tab_unselected_recent))
						.setContent(intent));
	}

	public void aboutTab(){
		mTabHost = getTabHost();
		Intent intent = new Intent("com.hfoss.summer09.cc.ABOUT");
		intent.setClass(this, AboutApp.class);
		mTabHost.addTab(mTabHost.newTabSpec("dialer")
				.setIndicator(("About"),
						getResources().getDrawable(R.drawable.ic_dialog_info))
						.setContent(intent));
	}
}