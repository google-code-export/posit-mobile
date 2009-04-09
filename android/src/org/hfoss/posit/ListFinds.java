

package org.hfoss.posit;

import org.hfoss.posit.util.DataSet;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
/**
 * 
 * @author pgautam
 *
 */
public class ListFinds extends ListActivity {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    
    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;

    private DBHelper mDbHelper;
    private Cursor notesCursor ;
    private ImageButton mapButton;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setTheme(android.R.style.Theme_Light);
        setContentView(R.layout.finds_list);
        DataSet.getPOSITPreferences(this);
        mDbHelper = new DBHelper(this);
        mDbHelper.open();
        fillData();
        mapViewItems();
        setListeners();
    }
    
    
    private void fillData() {
        // Get all of the rows from the database and create the item list
        //Cursor notesCursor = mDbHelper.fetchAllRows();
    	String[] projection = new String[] {
    		DBHelper.KEY_ROWID,
    		DBHelper.KEY_NAME	
    	};
    	notesCursor = mDbHelper.fetchAllRows();
    	if (notesCursor.getCount() == 0) return;
        startManagingCursor(notesCursor);
        
        //String[] from = new String[]{DBHelper.KEY_ROWID, DBHelper.KEY_NAME,DBHelper.KEY_DESCRIPTION};
        String[] from = new String[]{DBHelper.KEY_ROWID, DBHelper.KEY_NAME,DBHelper.KEY_DESCRIPTION};
        int[] to = new int[]{R.id.row_id, R.id.name_id,R.id.description_id};
               
        // Now create a simple cursor adapter and set it to display
      
        SimpleCursorAdapter notes =  
        	    new SimpleCursorAdapter(this, R.layout.list_row, notesCursor, from, to);
        setListAdapter(notes);
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID,0, R.string.menu_newfind);
        menu.add(0, DELETE_ID,0, R.string.menu_delete);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
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
        i.putExtra("action", "New Record");
        startActivity(i);
    }
    /*
    * Opens the map find with all the pretty bubbles with all the finds
    */
   private void openMapFinds(){
   	Intent i = new Intent(this, MapperNew.class);
   	startActivity(i);
   }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Cursor c = notesCursor;
        c.move(position);
        Log.i("ListFindLog", ""+id);
        Intent i = new Intent(this, NewFind.class);
        i.putExtra(DBHelper.KEY_ROWID, id);
        
        startActivity(i);
    }

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, String data, Bundle extras) {
        super.onActivityResult(requestCode, resultCode, data, extras);
        fillData();
    }
*/

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
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
    
	private void mapViewItems() {
		mapButton = (ImageButton) findViewById(R.id.mapButton);
	}
	
	private OnClickListener mapButtonListener = new OnClickListener() {
		public void onClick(View v) {
			openMapFinds();
		}
	};
	
	private void setListeners() {
		mapButton.setOnClickListener(mapButtonListener);
	}
}
