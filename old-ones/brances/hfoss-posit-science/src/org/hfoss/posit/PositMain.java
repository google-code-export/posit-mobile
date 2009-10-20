/*
 * Copyright (C) 2008 HFOSS.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Prasana Gautam
 * @author R. Morelli
 */

package org.hfoss.posit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Menu.Item;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PositMain extends Activity {
	public static final int ACTIVITY_ABOUT=0;
	public static final int ACTIVITY_NEWFIND=1;
	public static final int ACTIVITY_LISTFINDS=2;
	public static final int ACTIVITY_CAMERA=3;
	public static final int ACTIVITY_SYNC = 4;
	private static final int ABOUT_ID = Menu.FIRST;
	private static final int NEW_ID = Menu.FIRST +1;
	private static final int LIST_ID = Menu.FIRST + 2;
	private static final int CAMERA_ID = Menu.FIRST + 3;
	private static final int NETCAM_ID = Menu.FIRST+4;
	private static final int SERVER_ID = Menu.FIRST+5;
	private static final int SYNC_ID = Menu.FIRST+6;
	private static final int MAPFINDS_ID = Menu.FIRST+7;
	private Dialog actionDialog;
	private Button addSightingButton, viewSightingsButton;
	public PositMain(){	}
	
    /** 
     * Called when the activity is first created. Its UI is specified in main.xml.
     * @param A Bundle is a mapping from Strings to Parcelable or Cloneable types.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        /**create tables, used when new tables need to be added**/
        /*DBHelper db = new DBHelper(this);
        db.createTables();*/
        mapViewItems();
        addListeners();
     }

    private void mapViewItems() {
    	addSightingButton = (Button)findViewById(R.id.addSightingButton);
    	viewSightingsButton = (Button)findViewById(R.id.viewSightingsButton);
    }
    
    private void addListeners() {
    	addSightingButton.setOnClickListener(new OnClickListener(){
    		public void onClick(View v) {
				newSighting();		
			}
    		
    	});
    	
    	viewSightingsButton.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			viewSightings();
    		}
    	});
    }
    
    private void viewSightings() {
    	Intent i = new Intent(this, ViewSightings.class);
    	startSubActivity(i,2); 
    }
    private void newSighting() {
    	Intent i = new Intent(this,NewSighting.class);
		startSubActivity(i,2);
    }
    /** 
     * Displays an about dialog.
     */   
    private void about(){
    	actionDialog = new Dialog(this, android.R.style.Theme_Dialog);

		
		actionDialog.setContentView(R.layout.about);
		actionDialog.setTitle("About");
		
		actionDialog.show();
    }
    
    private void error(){
    	new AlertDialog.Builder(this)
    		.setTitle("Oops!!!!!!")
    		.setIcon(android.R.drawable.padlock)
    		.setMessage("Wrong Option")
    		.show();
    }
    /** 
     * Handles new discoveries.
     */   
    private void openNewFind(){
   // 	Intent i = new Intent(this, Mapper.class);
    	Intent i = new Intent(this, NewFind.class);
    	startSubActivity(i,ACTIVITY_NEWFIND);
    }

    /** 
     * Displays a list of all discoveries from DB.
     */   
    private void openListFind(){
    	Intent i = new Intent(this, ListFinds.class);
    	startSubActivity(i,0);
    }
 
    private void openMapFinds(){
    	Intent i = new Intent(this, MapperNew.class);
    	startSubActivity(i,0);
    }
    /** 
     * Snaps a picture with the phones camera..
     */ 
    private void takePicture(){
	    	Intent i = new Intent(this, Camera.class);
        startSubActivity(i, ACTIVITY_CAMERA);
    }
    
    
    
    private void syncActivity(){
    	Intent i = new Intent (this, SendFiles.class);
    	startSubActivity(i, ACTIVITY_SYNC);
    }
	/**
	 * Adds menu items to the main Android menu.
	 * @param menu is a reference to Android's main menu.
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, ABOUT_ID, R.string.menu_about).setIcon(R.drawable.about);
		menu.add(0, NEW_ID, R.string.menu_newfind).setIcon(R.drawable.binocular);
		menu.add(0, LIST_ID, R.string.menu_listfinds).setIcon(R.drawable.notebook_32x32);
		menu.add(1, CAMERA_ID, R.string.menu_camera).setIcon(R.drawable.camera_icon_32x32);
		menu.add(1, NETCAM_ID, "Stream").setIcon(R.drawable.streaming);
		/*menu.add(1, SERVER_ID, R.string.servers).setIcon(R.drawable.db_32x32);*/
		menu.add(1, SYNC_ID, R.string.sync).setIcon(R.drawable.update);
		menu.add(1, MAPFINDS_ID, R.string.menu_mapfinds).setIcon(R.drawable.flag);
		return true;
		
	}
	/**
	 * Opens the streaming camera, will be removed in future.
	 */
	public void openNetCam() {
		Intent i = new Intent(this,GetDataFromWeb.class);
		startSubActivity(i, SYNC_ID);
	}
	
	/**
	 * Handles selected menu items.
	 * @param item is a reference to the selected menu option.
	 * @see android.app.Activity#onOptionsItemSelected(android.view.Menu.Item)
	 */
	@Override
	public boolean onOptionsItemSelected(Item item) {
		switch (item.getId()){
		case ABOUT_ID:
			about();
			break;
		case NEW_ID:
			openNewFind();
			break;
		case LIST_ID:
			openListFind();
			break;
		case CAMERA_ID:
			takePicture();
	        break;
		case SYNC_ID:
			syncActivity();
			break;
		case NETCAM_ID:
			openNetCam();
			break;
		/*case SERVER_ID:
			pingServer();
			break;*/
		case MAPFINDS_ID:
			openMapFinds();
			break;
		default:
			error();
			break;
		}		
		return super.onOptionsItemSelected(item);
	}
}