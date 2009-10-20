/**
 * Maps Module
 * @author Khanh Pham
 * @author Phil Fritzche
 * @author James Jackson 
 * 
 * This file creates a map view and accesses the GPS object. It allows
 * the user to view their current location and enable GPS tracking of their
 * phone.
 */

package com.hfoss.summer09.cc;

import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class Maps extends MapActivity
{
	private static final String TAG = "Maps";

	//create overlay variables
	List<Overlay> mapOverlays;
	Drawable drawable;
	MyItemizedOverlay mItemizedOverlay;
	
	//create options menu variables
	public static final int MAIN_MENU_GROUP_ID = 0;
	public static final int MY_LOC = Menu.FIRST;
	public static final int ADD_MARK = Menu.FIRST + 1;
	public static final int MAP_TYPE_MENU = Menu.FIRST + 2;

	//create submenu [maptype] menu variables
	private static final int SUB_MENU_GROUP_ID = 1;
	public static final int MAP_TYPE_GROUP_ID = 0;
	public static final int MAP_VIEW = Menu.FIRST + 3;
	public static final int SAT_VIEW = Menu.FIRST + 4;
	public static final int TRAFFIC_VIEW = Menu.FIRST + 5;

	//creation of map variables
	private MapView mapView;
	private MapController mapCont;
	private boolean trav_on = false;
	private boolean satv_on = false;

	//helpful boolean parameters
	private boolean follow = false;
	private boolean go;

	//creation of location overlay
	private MMyLocationOverlay mLocOverlay;

	//creation of gps variables
	private GpsUtils gpsUtils;

	//creation of BUTONZ variables
	private ImageButton gpsFollow;

	//creation of timer variables
	private int pauseTimer = 5000;

	/**
	 * Called when the activity is first started.
	 * @return
	 */
	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.gps_maps);

		initMap();
		gpsUtils = new GpsUtils();
		gpsUtils.initGps(this, GpsUtils.QUICK_UPDATE);
		initLocOverlay();
		initItemizedOverlay();
		createGpsFollowBtn();
	}

	/**
	 * Called when the application is resumed. Enables location
	 * overlay and adds the gps listener.
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
		gpsUtils.enableListener();
		mLocOverlay.enableMyLocation();
	}

	/**
	 * Called when the application is paused. Disables location
	 * overlay and removes the gps listener.
	 * @return
	 */
	@Override
	protected void onPause()
	{
		super.onPause();
		gpsUtils.disableListener();
		mLocOverlay.disableMyLocation();
	}

	/**
	 * Called when the application is stopped.
	 * @return
	 */
	@Override
	protected void onStop()
	{
		super.onStop();
	}

	/**
	 * Called to create the options menu.
	 * @param menu The menu to create
	 * @return super.onCreateOptionsMenu(menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		populateOptionsMenu(menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Called whenever an item in the options menu is selected.
	 * @param item Selected item
	 * @return super.onOptionsItemSelected(item)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		applyOptionsMenuChoice(item);
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Necessary function, telling the system whether or not a route will be displayed.
	 * @return True if a route is to be displayed, false if not.
	 */
	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}

	/**
	 * Initialize the required map-related variables.
	 * @param
	 * @return
	 */
	protected void initMap()
	{
		mapView = (MapView) findViewById(R.id.map_view);
		mapCont = mapView.getController();
		mapView.setBuiltInZoomControls(true);
		mapView.setSatellite(true);
	}

	/**
	 * Initialize the location overlay.
	 * @param
	 * @return
	 */
	protected void initLocOverlay()
	{
		mLocOverlay = new MMyLocationOverlay(this, mapView);
		mapView.getOverlays().add(mLocOverlay);
		mLocOverlay.enableCompass();
		mLocOverlay.enableMyLocation();
	}
	
	/**
	 * Initialize the itemized overlay.
	 * @param
	 * @return
	 */
	protected void initItemizedOverlay()
	{
		mapOverlays = mapView.getOverlays();
		drawable = this.getResources().getDrawable(R.drawable.map_pin);
		mItemizedOverlay = new MyItemizedOverlay(drawable);
	}

	/**
	 * Initialize a button to turn on/off GPS tracking of the phone, and animate to the current
	 * location of the phone if follow is turned on. If no location is available, it posts an
	 * error and 
	 * @param
	 * @return
	 */
	protected void createGpsFollowBtn()
	{
		gpsFollow = (ImageButton) findViewById(R.id.b_gps_follow_u);
		gpsFollow.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{ 
				follow = !follow;
				if (follow)
				{
					if (gpsUtils.getCurrentGp() != null)
					{
						mapCont.animateTo(gpsUtils.getCurrentGp()); 
					}
					else if (!checkNullGp())
					{
						follow = false;
					}
				}
				setBtnBackground(v, follow);
			}
		}); 
	}

	/**
	 * Checks to see if a geopoint currently exists; primarily used when the application is started
	 * to see if it can animate to the user's location without a null pointer exception. If no
	 * geopoint is available, it pauses 5 seconds and checks again. If no point is still found,
	 * returns false and displays a notification to the user.
	 * @return Returns true if a current geopoint has been found, or false if not.
	 */
	public boolean checkNullGp()
	{
		go = false;
		if (gpsUtils.getCurrentGp() == null)
		{
			{
				Utils.longToast(mapView.getContext(), R.string.finding_pos);

				Thread gpsFindTimer = new Thread() 
				{
					public void run() 
					{
						Looper.prepare();
						try
						{
							while(0 < pauseTimer) 
							{
								sleep(100); 
								pauseTimer -= 100; 
							}	
						}
						catch (Exception e)
						{
							Log.e(TAG, e.toString());
						}
						if (gpsUtils.getCurrentGp() != null)
						{
							mapCont.animateTo(gpsUtils.getCurrentGp());
							go = true;
						}
						else
						{
							Utils.shortToast(mapView.getContext(), R.string.no_pos_avail);
						}
						Looper.loop();
					}
				};
				gpsFindTimer.start();
			}
		}
		return go;
	}

	/**
	 * Called to fill in the options menu.
	 * @param menu
	 * @return
	 */
	public void populateOptionsMenu(Menu menu)
	{
		MenuItem m_myLoc = menu.add(MAIN_MENU_GROUP_ID, MY_LOC, 0, R.string.my_loc);
		{
			m_myLoc.setIcon(R.drawable.icon_gps_unpressed);
		}
		MenuItem m_addMark = menu.add(MAIN_MENU_GROUP_ID, ADD_MARK, 0, R.string.add_mark);
		{
			m_addMark.setIcon(R.drawable.icon_menu_goto);
		}

		createMapTypeSubMenu(menu);
	}

	/**
	 * Called when an item from the options menu is selected.
	 * @param item Menu item selected
	 * @return Returns true if menu item is selected, false if no items are selected.
	 */
	public boolean applyOptionsMenuChoice(MenuItem item)
	{
		switch (item.getItemId())
		{
		case MY_LOC: return gotoMyLoc();
		case ADD_MARK: return addMarker();
		case MAP_VIEW: return setMapView(item);
		case SAT_VIEW: return setSatView(item);
		case TRAFFIC_VIEW: return setTrafficView(item);
		}
		return false;
	}
	
	/**
	 * Turns on map view.
	 * @param item Selected item
	 * @return true
	 */
	public boolean setMapView(MenuItem item)
	{
		Log.e(TAG, "MAP_VIEW");
		item.setChecked(true);
		satv_on = false;
		trav_on = false;
		mapView.setTraffic(trav_on);
		mapView.setSatellite(satv_on);
		return true;
	}
	
	/**
	 * Turns on satellite view.
	 * @param item Selected item
	 * @return true
	 */
	public boolean setSatView(MenuItem item)
	{
		Log.e(TAG, "SAT_VIEW");
		item.setChecked(true);
		satv_on = true;
		trav_on = false;
		mapView.setTraffic(trav_on);
		mapView.setSatellite(satv_on);
		return true;
	}
	
	/**
	 * Turns on traffic view.
	 * @param item Selected item
	 * @return true
	 */
	public boolean setTrafficView(MenuItem item)
	{
		Log.e(TAG, "TRAFFIC_VIEW");
		item.setChecked(true);
		trav_on = true;
		satv_on = false;
		mapView.setSatellite(satv_on);
		mapView.setTraffic(trav_on);
		return true;
	}

	/**
	 * Creates a submenu with options relating to changing the type of map view and
	 * adds it to the options menu.
	 * @param menu The menu to add the submenu to.
	 * @return
	 */
	public void createMapTypeSubMenu(Menu menu)
	{
		SubMenu mapTypeMenu = menu.addSubMenu(SUB_MENU_GROUP_ID, MAP_TYPE_MENU, 0, R.string.sm_mt_title);
		mapTypeMenu.add(MAP_TYPE_GROUP_ID, MAP_VIEW, 0, R.string.sm_mt_map);
		mapTypeMenu.add(MAP_TYPE_GROUP_ID, SAT_VIEW, 0, R.string.sm_mt_sat).setChecked(true);
		mapTypeMenu.add(MAP_TYPE_GROUP_ID, TRAFFIC_VIEW, 0, R.string.sm_mt_tra);
		mapTypeMenu.setGroupCheckable(MAP_TYPE_GROUP_ID, true, true);

		mapTypeMenu.setIcon(R.drawable.icon_map_view);
	}

	/**
	 * Called when the user wants to go to their current location. If the geopoint is null,
	 * calls the checkNullGp function.
	 * @return
	 */
	public boolean gotoMyLoc()
	{
		if (gpsUtils.getCurrentGp() != null)
		{
			mapCont.animateTo(gpsUtils.getCurrentGp());
		}
		else
		{
			if (checkNullGp())
			{
				mapCont.animateTo(gpsUtils.getCurrentGp());
			}
		}
		return true;
	}
	
	/**
	 * Called to add a marker based on the location inputted by the user.
	 * @return true
	 */
	public boolean addMarker()
	{
		GeoPoint point = new GeoPoint(19240000,-99120000);
		OverlayItem overlayItem = new OverlayItem(point, "", "");
		mItemizedOverlay.addOverlay(overlayItem);
		mapOverlays.add(mItemizedOverlay);
		return true;
	}

	/**
	 * Function to change the location following button's image, based on whether or not it is enabled.
	 * @param v Button View
	 * @param follow Variable denoting whether or not the button is enabled or disabled
	 * @return
	 */
	protected void setBtnBackground(View v, boolean follow)
	{
		if (follow)
		{
			v.setBackgroundResource(R.drawable.icon_gps_pressed);
		}
		else
		{
			v.setBackgroundResource(R.drawable.icon_gps_unpressed);
		}
	}
}