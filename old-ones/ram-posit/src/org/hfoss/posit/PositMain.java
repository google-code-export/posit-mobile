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
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.Menu.Item;

public class PositMain extends Activity {
	public static final int ACTIVITY_ABOUT=0;
	public static final int ACTIVITY_NEWFIND=1;
	public static final int ACTIVITY_LISTFINDS=2;
	public static final int ACTIVITY_CAMERA=3;

	private static final int ABOUT_ID = Menu.FIRST;
	private static final int NEW_ID = Menu.FIRST +1;
	private static final int LIST_ID = Menu.FIRST + 2;
	private static final int CAMERA_ID = Menu.FIRST + 3;
	
	public PositMain(){	}
	
    /** 
     * Called when the activity is first created. Its UI is specified in main.xml.
     * @param A Bundle is a mapping from Strings to Parcelable or Cloneable types.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
     }

    /** 
     * Displays an about dialog.
     */   
    private void about(){
        setContentView(R.layout.about);
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
 
    /** 
     * Snaps a picture with the phones camera..
     */ 
    private void takePicture(){
		Intent i = new Intent(this, Camera.class);
        startSubActivity(i, ACTIVITY_CAMERA);
    }
    
	/**
	 * Adds menu items to the main Android menu.
	 * @param menu is a reference to Android's main menu.
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, ABOUT_ID, R.string.menu_about);
		menu.add(0, NEW_ID, R.string.menu_newfind);
		menu.add(0, LIST_ID, R.string.menu_listfinds);
		menu.add(0, CAMERA_ID, R.string.menu_camera);
		return true;
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
		}		
		return super.onOptionsItemSelected(item);
	}
}