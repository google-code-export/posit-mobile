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
 */

package org.hfoss.posit.victim;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Menu.Item;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ListFinds extends ListActivity {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    
    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;

    private DBHelper mDbHelper;
    private Cursor notesCursor ;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.finds_list);
        mDbHelper = new DBHelper(this);
        mDbHelper.open();
        fillData();

    }
    
    
    private void fillData() {
        // Get all of the rows from the database and create the item list
        //Cursor notesCursor = mDbHelper.fetchAllRows();
    	String[] projection = new String[] {
    		DBHelper.KEY_ROWID,
    		DBHelper.KEY_NAME	
    	};
    	notesCursor = mDbHelper.fetchSelectedColumns(projection);
        startManagingCursor(notesCursor);
        
        // Create an array to specify the fields we want to display in the list (only TITLE)
  //      String[] from = new String[]{DBHelper.KEY_ROWID, DBHelper.KEY_DESCRIPTION,
 //      		DBHelper.KEY_LONGITUDE, DBHelper.KEY_LATITUDE};
        String[] from = new String[]{DBHelper.KEY_ROWID, DBHelper.KEY_NAME};
       
        // and an array of the fields we want to bind those fields to (in this case just text1)
 //       int[] to = new int[]{R.id.row_id, R.id.description, R.id.longitude, R.id.latitude};
     // int[] to = new int[]{R.id.row_id};
        int[] to = new int[]{R.id.row_id, R.id.description_id};
               
        // Now create a simple cursor adapter and set it to display
      
        SimpleCursorAdapter notes =  
        	    new SimpleCursorAdapter(this, R.layout.list_row, notesCursor, from, to);
        setListAdapter(notes);
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, R.string.menu_newfind);
        menu.add(0, DELETE_ID, R.string.menu_delete);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, Item item) {
        switch(item.getId()) {
        case INSERT_ID:
            createRecord();
            return true;
        case DELETE_ID:
            mDbHelper.deleteRow(getListView().getSelectedItemId());
            fillData();
            return true;
        }
       
        return super.onMenuItemSelected(featureId, item);
    }

    private void createRecord() {
        Intent i = new Intent(this, NewFind.class);
        startSubActivity(i, ACTIVITY_CREATE);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Cursor c = notesCursor;
        c.moveTo(position);
        Log.i("ListFindLog", ""+id);
        Intent i = new Intent(this, NewFind.class);
        i.putExtra(DBHelper.KEY_ROWID, id);
        startSubActivity(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, String data, Bundle extras) {
        super.onActivityResult(requestCode, resultCode, data, extras);
        fillData();
    }


	@Override
	public boolean onKeyDown(int arg0, KeyEvent arg1) {
		
		switch (arg0){
		case KeyEvent.KEYCODE_BACK:
			finish();
			break;
		}
		return false;
	}
    
}
