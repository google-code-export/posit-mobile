package org.hfoss.posit;

import java.net.URI;
import java.util.Vector;

import org.hfoss.posit.util.DataSet;
import org.xmlrpc.android.XMLRPCClient;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SendFiles extends Activity {
	private static final String APP = "SendFiles";
	private static String URL;
	private static final String deviceId = "android-1";
	private DBHelper dbHelper;
	private Button syncFindsButton, syncImagesButton;
	private TextView status;
	private TextView resultText;
	private XMLRPCClient client;
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		dbHelper = new DBHelper(this);
		dbHelper.open();
		DataSet.getPOSITPreferences(this);
		URL = DataSet.getServer() ;
		URI uri = URI.create(URL);
		client = new XMLRPCClient(uri);
		Log.i(APP, URL);
		setContentView(R.layout.synchronize);

		mapViewItems();
		setListeners();
		// syncImagesButton.setVisibility(View.INVISIBLE);
	}

	private void setListeners() {
		syncFindsButton.setOnClickListener(syncFindsButtonListener);
		syncImagesButton.setOnClickListener(syncImagesButtonListener);
	}

	private OnClickListener syncFindsButtonListener = new OnClickListener() {

		public void onClick(View arg0) {
			syncFinds();
		}

	};

	private OnClickListener syncImagesButtonListener = new OnClickListener() {

		public void onClick(View arg0) {
//			syncImages();
		}

	};


	private void mapViewItems() {
		syncFindsButton = (Button) findViewById(R.id.syncFindsButton);
		syncImagesButton = (Button) findViewById(R.id.syncImagesButton);
		status = (TextView)findViewById(R.id.status);
		resultText = (TextView)findViewById(R.id.resultText);
	}

	private void syncFinds() {
		Cursor c = dbHelper.fetchAllRows();
		XMLRPCClient client = new XMLRPCClient(URL);

		while (c.moveToNext()) {
			Vector v = new Vector();
			Integer rowId = c.getInt(c.getColumnIndex(DBHelper.KEY_ROWID));
			String identifier = c.getString(c
					.getColumnIndex(DBHelper.KEY_IDENTIFIER));
			String name = c.getString(c.getColumnIndex(DBHelper.KEY_NAME));
			Integer age = c.getInt(c.getColumnIndex(DBHelper.KEY_AGE));
			Integer sex = c.getInt(c.getColumnIndex(DBHelper.KEY_SEX));
			String description = c.getString(c
					.getColumnIndex(DBHelper.KEY_DESCRIPTION));
			Integer tagged = c.getInt(c.getColumnIndex(DBHelper.KEY_TAGGED));
			/* String type =c.getString(c.getColumnIndex(DBHelper.KEY_TYPE)); */
			Double longitude = c.getDouble(c
					.getColumnIndex(DBHelper.KEY_LONGITUDE));
			longitude = (!longitude.equals(null))?longitude:0;
			Double latitude = c.getDouble(c
					.getColumnIndex(DBHelper.KEY_LATITUDE));
			latitude = (!longitude.equals(null))?latitude:0;
			String time = c.getString(c.getColumnIndex(DBHelper.KEY_TIME));

			// v.add((Integer)rowId);
//			v.add((String) name);
//			v.add((String) identifier);
//			v.add((String) description); /* Description Value */
//			v.add((String) latitude.toString());
//			v.add((String) longitude.toString());
//			v.add((Integer) age);
//			v.add((Integer) sex);
//			v.add((Integer) tagged);
//			/* v.add((String)type); /Type */
//			v.add((String) time);
			//service.send(URL, "saveFind", v);
			  XMLRPCMethod method = new XMLRPCMethod("saveFind", true, new XMLRPCMethodCallback() {
					public void callFinished(Object result) {
						resultText.setText(result.toString());
					}
		        });
			  Object[] items  = {name, identifier, description, latitude.toString()
					  , longitude.toString(),age, sex,tagged,time
			  };
			  Log.i(APP,"HERE");
			  method.call(items);

		}
	}

	private void sendFinds(String url, Vector data) {

	}

	/*private void sendFile(String filePath, int recordId, String description) {
		WebServiceClient service = new WebServiceClient(this);
		// String identifier =
		// dbHelper.getIdForImage(((Integer)recordId).longValue());

		// String identifier ="4366";
		try {

			File f = new File(filePath);

			FileInputStream fIS = new FileInputStream(filePath);
			String fileName = filePath.split("/")[filePath.split("/").length - 1];
			Log.i(APP, fileName);

			byte[] buffer = new byte[(int) f.length()];
			Log.i("SendFiles", "" + f.length());
			int test = fIS.read(buffer);
			Log.i("SendFiles", test + "");
			if (description.equals(null))
				description = "untitled";
			Vector y = new Vector();
			y.add((byte[]) buffer);
			y.add((String) deviceId + "-" + fileName);
			y.add((Integer) recordId);
			y.add((String) description);
			 y.add((Integer)recordId); 
			service.send(URL, "saveImage", y);
		} catch (IOException e) {
			Log.e("SendFiles", "File Not found");
		}

	}
*/
/*	private void syncImages() {
		Cursor c = dbHelper.allImages();
		try {
			while (c.moveToNext()) {
				// String description =
				// c.getString(c.getColumnIndex(DBHelper.KEY_DESCRIPTION));
				String description = "untitled";
				sendFile(c.getString(c.getColumnIndex(DBHelper.KEY_FILENAME)),
						c.getInt(c.getColumnIndex(DBHelper.KEY_RECORD_ID)),
						description);

				 This is reminder that there could be synchronization issues 
				
				 * try{ Thread.sleep(3000L); }catch (InterruptedException e ){
				 * Log.e(APP, "Sleep not complete"); }
				 
			}
		} catch (NullPointerException e) {

		}
	}*/

	interface XMLRPCMethodCallback {
		void callFinished(Object result);
	}

	class XMLRPCMethod extends Thread {
		private String method;
		private Object[] params;
		private Handler handler;
		private XMLRPCMethodCallback callBack;
		private boolean isText;


		public XMLRPCMethod(String method, boolean isText,
				XMLRPCMethodCallback callBack) {
			this.method = method;
			this.isText = isText;
			this.callBack = callBack;
			handler = new Handler();
		}

		public void call() {
			call(null);
		}
		
		public void call(Object[] params) {
			//do the UI stuff here.
			this.params = params;
			start();
		}
		
		@Override
		public void run() {
    		try {
    			final long t0 = System.currentTimeMillis();
    			final Object result = client.call(method, params);
    			final long t1 = System.currentTimeMillis();
    			handler.post(new Runnable() {
					public void run() {
						//tests.setEnabled(true);
						status.setText("XML-RPC call took " + (t1-t0) + "ms");
						callBack.callFinished(result);
					}
    			});
    		} catch (final Exception e) {
    			handler.post(new Runnable() {
					public void run() {
						//textResult.setText("");
						//tests.setEnabled(true);
						status.setTextColor(0xffff8080);
						//status.setError("", errorDrawable);
						status.setText("Error " + e.getMessage());
						Log.d("Test", "error", e);
					}
    			});
    		}
		}
	}
}
