package org.hfoss.posit;

import java.util.Calendar;
import java.util.HashMap;

import org.hfoss.posit.util.SpinnerBindings;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
/**
 * Class specifically designed for adding newInstances and be called from anywhere in 
 * the application. The values are put into this from the NewFind like lat, long, weather and it
 * adds the Name, description data to them and save to the instances database.
 * @author pgautam
 *
 */
public class NewInstanceDialog extends Dialog {
	public NewInstanceDialog(Context context) {
		super(context, android.R.style.Theme_Dialog);
		mContext = context;
		setContentView(R.layout.add_new_instance);
		mapViewItems();
		setListeners();
		setObservationTypeSpinner();
		this.setCancelable(true);
		
	}
	
	private void mapViewItems () {
		observationTypeSpinner = (Spinner) findViewById(R.id.observationTypeSpinnerDialog);
		descriptionText = (EditText)findViewById(R.id.descriptionText);
		newName = (EditText) findViewById(R.id.addNewNameDialogText);
		okButton = (Button) findViewById(R.id.addNewNameDialogOkButton);
		cancelButton = (Button) findViewById(R.id.addNewNameDialogCancelButton);
	}
	
	private void setObservationTypeSpinner() {
		SpinnerBindings.bindArrayFromResource(this.getContext(), R.array.observation_types, observationTypeSpinner);
	}
	
	private View.OnClickListener okButtonListener = new  View.OnClickListener(){

		public void onClick(View v) {
			name = newName.getText().toString();
			observationType = observationTypeSpinner.getSelectedItem().toString();
			description = descriptionText.getText().toString();
			NewInstanceDialog.this.dismiss();
			saveDBInstance();
			/**
			 * Broadcasts intent to the NewFind Activity to refresh itself.
			 */
			Intent intent = new Intent();
			intent.setAction("org.hfoss.posit.find.refreshInstance");
			getContext().sendBroadcast(intent);
			Log.i("NewInstanceDialog","intent broadcasted");
		}
		
	};

	public void saveDBInstance() {
		try {
		/*newInstanceInfo = nInt.getOutput();*/
		ContentValues newInstanceInfo = new ContentValues();
		newInstanceInfo.put(DBHelper.KEY_NAME, name);
		newInstanceInfo.put(DBHelper.KEY_LATITUDE, Double.parseDouble(latitude));
		newInstanceInfo.put(DBHelper.KEY_LONGITUDE, Double.parseDouble(longitude));
		newInstanceInfo.put(DBHelper.KEY_RECORD_ID, Id);
		newInstanceInfo.put(DBHelper.KEY_WEATHER, weather);
		newInstanceInfo.put(DBHelper.KEY_TIME, getCurrentDate());
		newInstanceInfo.put(DBHelper.KEY_DESCRIPTION, description);
		newInstanceInfo.put(DBHelper.KEY_OBSERVED, observationType);
		mDbHelper.addSighting(newInstanceInfo);
		} catch (NullPointerException e ) {
			
		}
	}
	
	private View.OnClickListener cancelButtonListener = new  View.OnClickListener(){

		public void onClick(View v) {
			
			NewInstanceDialog.this.dismiss();		
		}
		
	};


	@Override
	public void dismiss() {
		super.dismiss();
	}
	/**
	 * Used to set the extra values like the database helper object, weather and 
	 * more metadata in the future.
	 * @param db
	 * @param latVal
	 * @param longVal
	 * @param mRowId
	 * @param weatherVal
	 */
	public void setValues(DBHelper db, String latVal, String longVal, Long mRowId, String weatherVal) {
		mDbHelper = db;
		latitude = latVal;
		longitude = longVal;
		if (mRowId!=null)
		Id = mRowId;
		else 
			Id = 1;
		weather = weatherVal;
	}
	private void setListeners() {
		okButton.setOnClickListener(okButtonListener);
		cancelButton.setOnClickListener(cancelButtonListener);
	}
	
	public HashMap<String,String> getOutput() {
		HashMap<String,String> output = new HashMap<String,String>();
		output.put(DBHelper.KEY_NAME, name);
		output.put(DBHelper.KEY_OBSERVED, observationType);
		output.put(DBHelper.KEY_DESCRIPTION, description);
		success=true;
		return output;
	}
	/**
	 * Gets the current Date using the singleton Calendar classs.
	 * @return
	 */
	private String getCurrentDate() {
		Calendar c = Calendar.getInstance();
		String dateString = c.get(Calendar.YEAR) + "/" + c.get(Calendar.MONTH)
				+ "/" + c.get(Calendar.DAY_OF_MONTH) + " "
				+ c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);
		return dateString;
	}
	private DBHelper mDbHelper;
	private long Id;
	private String latitude, longitude, weather;
	private Context mContext;
	private Spinner observationTypeSpinner;
	private EditText newName,descriptionText;
	private Button okButton;
	private Button cancelButton;
	private String name,observationType,description;
	public boolean success=false;
}
