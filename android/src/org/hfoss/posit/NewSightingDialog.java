package org.hfoss.posit;
import java.util.Calendar;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.maps.GeoPoint;


public class NewSightingDialog extends Dialog {

	private Context mContext;
	private Long recordId;
	private DBHelper mDbHelper;
	private String name;
	private int latitude;
	private int longitude;
	private String description;
	private EditText descriptionText;
	private EditText nameText;
	private Button okButton;
	private Button cancelButton;


	public NewSightingDialog(Context context,Long _recordId,GeoPoint geoPoint) {
		super(context);
		mContext = context;
		recordId = _recordId;
		mDbHelper = new DBHelper(context);
		//mDbHelper.open();
		//String findName = mDbHelper.getNameForId(recordId);
		//setTitle("New Sighting for"+findName);
		//mDbHelper.close();
		setContentView(R.layout.add_new_instance_dialog);
		latitude = geoPoint.getLatitudeE6();
		longitude = geoPoint.getLongitudeE6();
		mapViewItems();
		setListeners();
	}
	
	private void mapViewItems(){
		descriptionText = (EditText)findViewById(R.id.descriptionText);
		nameText = (EditText)findViewById(R.id.addNewNameDialogText);
		okButton = (Button)findViewById(R.id.addNewNameDialogOkButton);
		cancelButton = (Button)findViewById(R.id.addNewNameDialogCancelButton);
	}
	
	private void setListeners(){
		okButton.setOnClickListener(okButtonListener);
		cancelButton.setOnClickListener(cancelButtonListener);
	}
	
	private View.OnClickListener okButtonListener = new View.OnClickListener() {
		public void onClick(View v) {
			saveDBInstance();
			NewSightingDialog.this.dismiss();
		}
	};

	private View.OnClickListener cancelButtonListener = new View.OnClickListener() {
		public void onClick(View v) {
			NewSightingDialog.this.dismiss();
		}
	};
	private String getTime(){
		Calendar c = Calendar.getInstance();
		return  c.get(Calendar.YEAR) + "/" + c.get(Calendar.MONTH)
				+ "/" + c.get(Calendar.DAY_OF_MONTH) + " "
				+ c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);
	}
	
	public void saveDBInstance() {
		mDbHelper.open();
		name = nameText.getText().toString();
		description = descriptionText.getText().toString();
		/*newInstanceInfo = nInt.getOutput();*/
		ContentValues newInstanceInfo = new ContentValues();
		newInstanceInfo.put(DBHelper.KEY_NAME, name);
		newInstanceInfo.put(DBHelper.KEY_LATITUDE, latitude);
		newInstanceInfo.put(DBHelper.KEY_LONGITUDE, longitude);
		newInstanceInfo.put(DBHelper.KEY_RECORD_ID, recordId);
		newInstanceInfo.put(DBHelper.KEY_WEATHER, "unknown");
		newInstanceInfo.put(DBHelper.KEY_TIME, getTime());
		newInstanceInfo.put(DBHelper.KEY_DESCRIPTION, description);
		newInstanceInfo.put(DBHelper.KEY_OBSERVED, "unknown");
		mDbHelper.addSighting(newInstanceInfo);
		mDbHelper.close();
	}
}
