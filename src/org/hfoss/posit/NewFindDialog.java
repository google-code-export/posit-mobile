package org.hfoss.posit;

import java.util.Calendar;

import org.hfoss.posit.util.DataSet;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.google.android.maps.GeoPoint;

public class NewFindDialog extends Dialog {

private EditText descriptionText, identifierText, ageText, nameText;
private CheckBox taggedCheckBox;
private RadioGroup sexRadioGroup;
private RadioButton maleRadioButton, femaleRadioButton;
private ImageButton cameraButton, saveButton, viewGalleryButton;
private DBHelper mDbHelper;
private String name;
Integer latitude = null;
Integer longitude = null;
private String description = null;
private int Sex;
private Long mRowId = 1L, recordId = mRowId;
private static final String APP="NewFindDialog"; 
	private Context mContext;
	public NewFindDialog(Context context, GeoPoint geoPoint) {
		super(context);
		mContext = context;
		setContentView(R.layout.add_new_find_dialog);
		mDbHelper = new DBHelper(mContext);
		setTitle("Add a new Bird");
		mapViewItems();
		setListeners();
		latitude = geoPoint.getLatitudeE6();
		longitude = geoPoint.getLongitudeE6();
		//setObservationTypeSpinner();
		this.setCancelable(true);
		
	}

	private void openGallery() {
		Intent i = new Intent(mContext, PictureViewer.class);
		i.putExtra(DBHelper.KEY_ROWID, mRowId);
		mContext.startActivity(i);
	}

	private void takePicture() {
		savetoDB();
		Intent i = new Intent(mContext, CameraPreview2.class);
		i.putExtra(DBHelper.KEY_ROWID, mRowId);
		Log.i(APP, mRowId + "");
		mContext.startActivity(i);
	}
	private void mapViewItems(){
		mapCoreViews();
		
	}
	private void mapCoreViews() {
		try {
		
			descriptionText = (EditText) findViewById(R.id.descriptionText);
			
			identifierText = (EditText) findViewById(R.id.identifierText);
			ageText = (EditText) findViewById(R.id.ageText);

			nameText = (EditText) findViewById(R.id.nameText);
			sexRadioGroup = (RadioGroup) findViewById(R.id.sexRadioGroup);
			maleRadioButton = (RadioButton) findViewById(R.id.maleRadioButton);
			femaleRadioButton = (RadioButton) findViewById(R.id.femaleRadioButton);
			taggedCheckBox = (CheckBox) findViewById(R.id.taggedCheckBox);
			// TODO Reenable mapButton
			// mapButton = (ImageButton) findViewById(R.id.mapButton);
			/*cameraButton = (ImageButton) findViewById(R.id.cameraButton);*/
			saveButton = (ImageButton) findViewById(R.id.saveButton);
			viewGalleryButton = (ImageButton) findViewById(R.id.viewGalleryButton);

			// set the Gallery Button to be Invisible by default
			viewGalleryButton.setVisibility(View.INVISIBLE);
		} catch (NullPointerException e) {
			// ignore null pointers for now
		}
	}
	
	
	/**
	 * Sets all the listeners here.
	 */
	private void setListeners() {
		saveButton.setOnClickListener(saveButtonListener);
		// mapButton.setOnClickListener(mapButtonListener);
		/*cameraButton.setOnClickListener(cameraButtonListener);*/
		viewGalleryButton.setOnClickListener(viewGalleryButtonListener);
		sexRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.maleRadioButton)
					Sex = 0;
				else
					Sex = 1;
			}
		});

	
	}
	

	
	private View.OnClickListener saveButtonListener = new View.OnClickListener() {
		public void onClick(View view) {
			//setResult(RESULT_OK);
			
			savetoDB();
			NewFindDialog.this.dismiss();
			//finish();
		}
	};
	private View.OnClickListener cameraButtonListener = new View.OnClickListener() {
		public void onClick(View v) {
			takePicture();
		}
	};

	private View.OnClickListener viewGalleryButtonListener = new View.OnClickListener() {
		public void onClick(View v) {
			openGallery();
		}
	};

	/**
	 * Returns the id depending on which of the buttons are selected.
	 * 
	 * @return
	 */
	private int findSex() {

		/*
		 * if (sexRadioGroup.getCheckedRadioButtonId() == R.id.maleRadioButton)
		 * sex = 0; else if
		 * (sexRadioGroup.getCheckedRadioButtonId()==R.id.femaleRadioButton) sex
		 * = 1; Log.i(APP,sex+"");
		 */
		return Sex;
	}
	private String getTime(){
		Calendar c = Calendar.getInstance();
		return  c.get(Calendar.YEAR) + "/" + c.get(Calendar.MONTH)
				+ "/" + c.get(Calendar.DAY_OF_MONTH) + " "
				+ c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);
	}
	/**
	 * Saves to the database
	 */
	private void savetoDB() {
		int age=0;
		mDbHelper.open();
		description = descriptionText.getText().toString();
		String time = getTime();
		name = nameText.getText().toString();
		int sex = findSex();
		if (!ageText.getText().toString().equals("")){
		 age = Integer.parseInt(ageText.getText().toString());
		}
		
		String identifier = identifierText.getText().toString();
		// String name = (newItemName!=null)?newItemName:"Untitled";
		int tagged = taggedCheckBox.isChecked() ? 1 : 0;
		//if (mRowId == null) {
			long id = mDbHelper.createRow(description, longitude, latitude,
					time, sex, age, identifier, name, tagged);
			long newInstanceId = saveToInstanceDB(id);

			if (!DataSet.isMultipleInstances())
				mDbHelper.setDefaultInstance(id, newInstanceId);

			if (id > 0) {
				mRowId = id;

			}

		/*} else {
			mDbHelper.createRow( description, sex, age, identifier,
					name, tagged);
		}*/
		// if the application has multiple Instances enabled, then save to the
		// instance DB
		mDbHelper.close();
	}
	
	protected void finish() {
		// TODO Auto-generated method stub
		
	}
	/**
	 * clears all the info on the screen for new ones mostly
	 */
	private void clearFields() {
		identifierText.setText("");
		ageText.setText("");
		taggedCheckBox.setSelected(false);
		maleRadioButton.setSelected(false);
		femaleRadioButton.setSelected(false);
		descriptionText.setText("");
	}
	
	private long saveToInstanceDB(long recordId) {

		// TODO move everything to contentvalues instead of HashMap and then to
		// contentValues anyways
		ContentValues values = new ContentValues();
		values.put(DBHelper.KEY_LATITUDE, latitude!=null?latitude:0);
		values.put(DBHelper.KEY_LONGITUDE, longitude!=null?longitude:0);
		values.put(DBHelper.KEY_RECORD_ID, "" + recordId);
		values.put(DBHelper.KEY_NAME, name);
		values.put(DBHelper.KEY_DESCRIPTION, descriptionText.getText()
				.toString());
		values.put(DBHelper.KEY_TIME, getTime());
		if (DataSet.isMultipleInstances()) {

			values.put(DBHelper.KEY_OBSERVED, "unknown");
			values.put(DBHelper.KEY_WEATHER, "unknown");
		}
		return mDbHelper.addSighting(values);
	}
	


}
