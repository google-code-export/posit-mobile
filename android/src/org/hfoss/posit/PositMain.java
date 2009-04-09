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

import org.hfoss.posit.util.DataSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * <h2>About PositMain</h2>
 * This Activity is the Launcher for all the other activities that POSIT launches, basically, the mothership for everything. \
 *   
 * @author pgautam
 *
 */

public class PositMain extends Activity {
	/**
	 * Some constants used for launching subActivities. Will be removed when optimizing
	 * the only point of these is to make sure the opening Activity opens as subclass but we
	 * just need to ensure that it's greater than 0 for that. Since we aren't reading any 
	 * files back (where these values are indispensable for understanding the result codes)
	 * Keeping this, case we add some functions that need the onResult function
	 * <b>Refactoring Notes:</b>Maybe I can use the same Id for the menus too. Call me frugal but that saves memory!. 
	 */
	public static final int ACTIVITY_ABOUT=0;
	public static final int ACTIVITY_NEWFIND=1;
	public static final int ACTIVITY_LISTFINDS=2;
	public static final int ACTIVITY_CAMERA=3;
	public static final int ACTIVITY_SYNC = 4;
	/**
	 * Vestiges of the first codes that I wrote, something tells me I need to change this
	 */
	private static final int ABOUT_ID = Menu.FIRST+8;
	private static final int NEW_ID = Menu.FIRST +1;
	private static final int LIST_ID = Menu.FIRST + 2;
	private static final int CAMERA_ID = Menu.FIRST + 5;
	private static final int NETCAM_ID = Menu.FIRST+6;
	private static final int SERVER_ID = Menu.FIRST+7;
	private static final int SYNC_ID = Menu.FIRST+3;
	private static final int MAPFINDS_ID = Menu.FIRST+4;
	private static final String APP="PositMain";
	private Dialog actionDialog; /* Opens the dialog*/
	
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
//        SharedPreferences p = getSharedPreferences("Application Preferences",MODE_PRIVATE);
//        boolean preferences = p.getBoolean("Preferences", false);
//        if (!preferences) 
        DataSet.getPOSITPreferences(this);
        ((TextView)findViewById(R.id.applicationTypeLabel)).setText("Application:"+DataSet.getType());
        ((TextView)findViewById(R.id.customizedByLabel)).setText("Customized by: "+DataSet.getCustomizedBy());
     }
    /**
     * This maps to the different view Items that we have or <b> might have </b> 
     */
    private void mapViewItems() {
    	/*addSightingButton = (Button)findViewById(R.id.addSightingButton);
    	viewSightingsButton = (Button)findViewById(R.id.viewSightingsButton);*/
    }
    
    /**
     * Link those mapped Views to the <b>listeners</b>
     */
    private void addListeners() {
    	/*addSightingButton.setOnClickListener(new OnClickListener(){
    		public void onClick(View v) {
				newSighting();		
			}
    		
    	});
    	
    	viewSightingsButton.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			viewSightings();
    		}
    	});*/
    }
    
    /*private void viewSightings() {
    	Intent i = new Intent(this, ViewSightings.class);
    	startActivity(i); 
    }
    private void newSighting() {
    	Intent i = new Intent(this,NewSighting.class);
		startActivity(i);
    }*/
    /** 
     * Displays an about dialog.
     */   
    private void about(){
    	actionDialog = new Dialog(this, android.R.style.Theme_Dialog);

		
		actionDialog.setContentView(R.layout.about);
		actionDialog.setTitle(R.string.about);
		
		actionDialog.show();
    }
    
    
    /**
     * Aite! hit an error! A friendly warning for error, depending on your definition.
     */
    private void error(){
    	new AlertDialog.Builder(this)
    		.setTitle("Oops!!!!!!")
    		.setIcon(android.R.drawable.ic_secure)
    		.setMessage("Wrong Option")
    		.show();
    }
    /** 
     * Handles new discoveries.
     */   
    private void openNewFind(){
   // 	Intent i = new Intent(this, Mapper.class);
    	Intent i = new Intent(this, NewFind.class);
    	i.putExtra("action", "New Record");
    	startActivity(i);
    }

    /** 
     * Displays a list of all discoveries from DB.
     */   
    private void openListFind(){
    	Intent i = new Intent(this, ListFinds.class);
    	startActivity(i);
    }
    /**
     * Opens the map find with all the pretty bubbles with all the finds
     */
    private void openMapFinds(){
    	Intent i = new Intent(this, MapTrackingVersion.class);
    	startActivity(i);
    }
    /** 
     * Snaps a picture/s with the phones camera.. 
     */ 
    private void takePicture(){
	    	Intent i = new Intent(this, CameraPreview2.class);
        startActivity(i);
    }
    
    
    /**
     * opens the sendFiles class  
     * so that the user can send the files over the net
     * TODO add validation.. this one should repeat in the file too :) 
     */
    private void syncActivity(){
    	Intent i = new Intent (this, SendFiles.class);
    	startActivity(i);
    }
    
  
	/**
	 * Adds menu items to the main Android menu.
	 * @param menu is a reference to Android's main menu.
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(1, MAPFINDS_ID, 0,"Explore").setIcon(R.drawable.flag);
		menu.add(0, ABOUT_ID, 0, R.string.menu_about).setIcon(R.drawable.about);
		menu.add(0, NEW_ID, 0, getString(R.string.menu_newfind).replace("Find", DataSet.getRecordName())).setIcon(R.drawable.binocular);
		if (DataSet.isMultipleInstances())
		menu.add(0, LIST_ID,0, getString(R.string.menu_listfinds).replace("Finds", DataSet.getInstanceName())+"s").setIcon(R.drawable.notebook_32x32);
		else 
			menu.add(0, LIST_ID, 0,getString(R.string.menu_listfinds).replace("Finds", DataSet.getRecordName())+"s").setIcon(R.drawable.notebook_32x32);
		menu.add(1, CAMERA_ID,0,  R.string.menu_camera).setIcon(R.drawable.camera_icon_32x32);
		menu.add(1, NETCAM_ID,0, "ViewPictures").setIcon(R.drawable.streaming);
		/*menu.add(1, SERVER_ID, R.string.servers).setIcon(R.drawable.db_32x32);*/
		menu.add(1, SYNC_ID, 0, R.string.sync).setIcon(R.drawable.update);

		return true;
		
	}
	/**
	 * Opens the streaming camera, will be removed in future.
	 */
	public void openNetCam() {
		Intent i = new Intent(this,PictureViewer.class);
		startActivity(i);
	}
	
	/**
	 * Handles selected menu items.
	 * @param item is a reference to the selected menu option.
	 * @see android.app.Activity#onOptionsItemSelected(android.view.Menu.Item)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
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