package org.hfoss.posit;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;

import com.google.android.maps.GeoPoint;


public class ListFindsDialog extends Dialog {

	private Context mContext;
	private DBHelper mDbHelper;
	private Cursor notesCursor;
	private static final String APP = "ListFindsDialog";
	private GeoPoint geoPoint;
	public ListFindsDialog(Context context,GeoPoint _geoPoint) {
		super(context);
		this.setTitle("All Finds");
		mContext = context;
		setContentView(R.layout.list_finds_dialog);
		geoPoint = _geoPoint;
		mDbHelper = new DBHelper(mContext);
		mDbHelper.open();
		fillData();
		mDbHelper.close();
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
    	
        //startManagingCursor(notesCursor);
        
        //String[] from = new String[]{DBHelper.KEY_ROWID, DBHelper.KEY_NAME,DBHelper.KEY_DESCRIPTION};
        String[] from = new String[]{DBHelper.KEY_ROWID, DBHelper.KEY_NAME,DBHelper.KEY_DESCRIPTION};
        int[] to = new int[]{R.id.row_id, R.id.name_id,R.id.description_id};
               
        // Now create a simple cursor adapter and set it to display
      
        SimpleCursorAdapter notes =  
        	    new SimpleCursorAdapter(mContext, R.layout.list_row_for_dialog, notesCursor, from, to);
        ListView lv = (ListView)findViewById(R.id.list); 
        lv.setAdapter(notes);
        lv.setOnItemClickListener(findsClickListener);
    }
    
    private OnItemClickListener findsClickListener = new OnItemClickListener(){

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			/**
			 * Need to pass id as recordId to the NewInstanceDialog
			 */
			//Log.i(APP,"position="+position+" id="+id);
			NewSightingDialog nSD = new NewSightingDialog(mContext,id,geoPoint);
			nSD.show();
			ListFindsDialog.this.dismiss();
			
			
		}
    	
    };
	public ListFindsDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	public ListFindsDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}

}
