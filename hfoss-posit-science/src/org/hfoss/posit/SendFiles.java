package org.hfoss.posit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;

import org.hfoss.webServices.WebServiceClient;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SendFiles extends Activity{
	private static final String APP = "SendFiles";
	private static final String URL = "157.252.16.161:8000";
	private static final String deviceId = "android-1";
	private DBHelper dbHelper;
	private Button syncFindsButton, syncImagesButton; 
	
	public void onCreate(Bundle icicle){
		super.onCreate(icicle);
	
		dbHelper = new DBHelper(this);
		dbHelper.open();
		setContentView(R.layout.synchronize);
		
		mapViewItems();
		setListeners();
		//syncImagesButton.setVisibility(View.INVISIBLE);
	}
	
	private void setListeners(){
		syncFindsButton.setOnClickListener(syncFindsButtonListener);
		syncImagesButton.setOnClickListener(syncImagesButtonListener);
	}
	
	
	private OnClickListener syncFindsButtonListener = new OnClickListener(){

		public void onClick(View arg0) {
			syncFinds();
		}
		
	};
	
	private OnClickListener syncImagesButtonListener = new OnClickListener(){

		public void onClick(View arg0) {
			syncImages();
		}
		
	};
	
	
	private void mapViewItems(){
		syncFindsButton = (Button) findViewById(R.id.syncFindsButton);
		syncImagesButton = (Button) findViewById(R.id.syncImagesButton);
	}
	
	private void syncFinds(){
		Cursor c = dbHelper.fetchAllRows();
		WebServiceClient service = new WebServiceClient(this);
		while (c.next()){
		Vector v = new Vector();
		Integer rowId =c.getInt(c.getColumnIndex(DBHelper.KEY_ROWID));
		String identifier = c.getString(c.getColumnIndex(DBHelper.KEY_IDENTIFIER));
		String name = c.getString(c.getColumnIndex(DBHelper.KEY_NAME));
		Integer age = c.getInt(c.getColumnIndex(DBHelper.KEY_AGE));
		Integer sex = c.getInt(c.getColumnIndex(DBHelper.KEY_SEX));
		String description = c.getString(c.getColumnIndex(DBHelper.KEY_DESCRIPTION));
		Integer tagged = c.getInt(c.getColumnIndex(DBHelper.KEY_TAGGED));
		/*String type =c.getString(c.getColumnIndex(DBHelper.KEY_TYPE));*/
		Double longitude = c.getDouble(c.getColumnIndex(DBHelper.KEY_LONGITUDE));
		Double latitude = c.getDouble(c.getColumnIndex(DBHelper.KEY_LATITUDE));
		String time = c.getString(c.getColumnIndex(DBHelper.KEY_TIME));
		/*v.add((Integer)rowId);*/
		v.add((String)identifier);
		v.add((String)name);
		v.add((Integer)age);
		v.add((Integer)sex);
		v.add((Integer)tagged);
		v.add((String)description); /*Description Value*/
		/*v.add((String)type); /*Type*/ 
		v.add((String)latitude.toString());
		v.add((String)longitude.toString());
		v.add((String)time);
		service.send(URL, "saveFinds", v);
		}
	}
	
	private void sendFinds (String url, Vector data){
		
		
	}
	
	private void sendFile(String filePath, int recordId) {
		WebServiceClient service = new WebServiceClient(this);
		//String identifier = dbHelper.getIdForImage(((Integer)recordId).longValue());
		
		//String identifier ="4366";
		try {

			File f = new File(filePath);
			
			FileInputStream fIS = new FileInputStream(
					filePath);
			String fileName = filePath.split("/")[filePath.split("/").length-1];
			Log.i(APP,fileName);
		
			byte[] buffer = new byte[(int) f.length()];
			Log.i("SendFiles", "" + f.length());
			int test = fIS.read(buffer);
			Log.i("SendFiles", test + "");
			Vector y = new Vector();
			y.add((byte[]) buffer);
			y.add((String)""+recordId);
			y.add((String)deviceId+"-"+fileName);
			/*y.add((Integer)recordId);*/
			service.send(URL, "savefile", y);
		} catch (IOException e) {
			Log.e("SendFiles", "File Not found");
		}

	}
	
	private void syncImages() {
		Cursor c = dbHelper.allImages();
		while (c.next()) {
			sendFile(c.getString(c.getColumnIndex(DBHelper.KEY_FILENAME)), c
					.getInt(c.getColumnIndex(DBHelper.KEY_RECORD_ID)));

			/*This is reminder that there could be synchronization issues*/
			/*
			 * try{ Thread.sleep(3000L); }catch (InterruptedException e ){
			 * Log.e(APP, "Sleep not complete"); }
			 */
		}
	}
}
