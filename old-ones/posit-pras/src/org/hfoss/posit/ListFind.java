package org.hfoss.posit;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import org.hfoss.webServices.WebServiceClient;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ListFind extends ListActivity{
	private final String DEBUG_TAG = "ListFind";

	// -----------------------------------------------------------------------
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        try {
	        WebServiceClient service = new WebServiceClient(this);
	        Vector res = service.ping("157.252.154.190:8000", "info");
	        //System.out.print(res);

			// Extract the data
			ArrayList<String> records = new ArrayList<String>();

			String row;
			Vector rowVector = new Vector();
			// Yeah the brave ones do it manually!! :)
			// rowVector = (Vector)res.elementAt(1);
			// row = (String)rowVector.elementAt(3);
			// records.add(row);
			// rowVector = (Vector)res.elementAt(0);
			// row = (String)rowVector.elementAt(3);
			// records.add(row);
			//much better (i.e, less stupid way)
			for (Enumeration e = res.elements(); e.hasMoreElements();)
			{
				rowVector = (Vector)e.nextElement();
				row = (String)rowVector.elementAt(3);
				records.add(row);
			}

			// Show the records
			setListAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, records));

	    } catch (Exception e) {
	    	Log.e(DEBUG_TAG + ".ping", "oops- happy debugging!");
	    }
    }

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l,v, position, id);
	}


}
