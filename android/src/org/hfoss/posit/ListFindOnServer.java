package org.hfoss.posit;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import org.hfoss.posit.util.DataSet;
import org.hfoss.webServices.WebServiceClient;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
/**
 * Shows all the finds on the server if it's reported.
 * @author pgautam
 *
 */
public class ListFindOnServer extends ListActivity{
	private final String DEBUG_TAG = "ListFindOnServer";

	// -----------------------------------------------------------------------
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        try {
	        WebServiceClient service = new WebServiceClient(this);
	        Vector res = service.ping(DataSet.getServer()+"/xml_rpc", "test");
	        //System.out.print(res);
	        
			// Extract the data
			ArrayList<String> records = new ArrayList<String>();

			String row;
			Vector rowVector = new Vector();
			
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
	    	Log.e(DEBUG_TAG + ".ping", "oops- happy debugging!"+e.getMessage());
	    }
    }

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l,v, position, id);
	}

	// Yeah the brave ones do it manually!! :)
	// rowVector = (Vector)res.elementAt(1);
	// row = (String)rowVector.elementAt(3);
	// records.add(row);
	// rowVector = (Vector)res.elementAt(0);
	// row = (String)rowVector.elementAt(3);
	// records.add(row);
	//much better (i.e, less stupid way)
}
