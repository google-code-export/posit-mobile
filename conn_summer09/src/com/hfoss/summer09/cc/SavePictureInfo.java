/**
 * @author Phil Fritzsche
 * @author James Jackson
 * Activity for saving a picture.
 */

package com.hfoss.summer09.cc;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SavePictureInfo extends Activity {
	EditText picName;
	EditText picDesc;
	EditText contactName;
	EditText contactEmail;
	EditText contactPhone;

	MyDBHelper mDbHelper;
	Cursor mCursor;

	Find find;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_save_view);
		mDbHelper = new MyDBHelper(this);
		find = new Find(this);

		initEditText();
		initButtons();
	}

	public void initEditText() {
		picName = (EditText) this.findViewById(R.id.pic_name);
		picDesc = (EditText) this.findViewById(R.id.pic_description);
		contactName = (EditText) this.findViewById(R.id.contact_name);
		contactPhone = (EditText) this.findViewById(R.id.contact_number);
		contactEmail = (EditText) this.findViewById(R.id.contact_email);
	}

	public void initButtons() {
		Button button = (Button)findViewById(R.id.discardButton);
		button.setOnClickListener(mCancelButton);
		button = (Button)findViewById(R.id.saveButton);
		button.setOnClickListener(mSaveButton);
		button = (Button)findViewById(R.id.videoButton);
		button.setOnClickListener(mVideoTag);
		button = (Button)findViewById(R.id.audioButton);
		button.setOnClickListener(mAudioTag);
	}

	private OnClickListener mCancelButton = new OnClickListener() {
		public void onClick(View v) {  
			Intent intent = new Intent("android.intent.action.MAIN");
			startActivity(intent);
		}
	};

	private OnClickListener mSaveButton = new OnClickListener() {
		public void onClick(View v) {
			saveInfo();
		}
	};

	private OnClickListener mVideoTag = new OnClickListener() {
		public void onClick(View v) {
			startVideoTag();
		}    	
	};

	private OnClickListener mAudioTag = new OnClickListener() {
		public void onClick(View v) {
			startAudioTag();
		}	
	};

	public void startAudioTag(){
		Intent intent = new Intent("com.hfoss.summer09.cc.AUDIO_APP");
		intent.setClass(this, AudioCapture.class);
		startActivity(intent);
	}

	public void startVideoTag(){
		Intent intent = new Intent("com.hfoss.summer09.cc.VIDEO_APP");
		intent.setClass(this, VideoCapture.class);
		startActivity(intent);
	}

	public void saveInfo() {
		ContentValues values= new ContentValues();
		values.put(MyDBHelper.KEY_PIC_NAME, picName.getText().toString());
		values.put(MyDBHelper.KEY_DESCRIPTION, picDesc.getText().toString());
		values.put(MyDBHelper.KEY_CONTACT_NAME, contactName.getText().toString());
		values.put(MyDBHelper.KEY_EMAIL, contactEmail.getText().toString());
		values.put(MyDBHelper.KEY_PHONE, contactPhone.getText().toString());

		find.addToDb(values);
		Intent intent = new Intent();
		intent.setClass(this, AppMain.class);
		startActivity(intent);
	}
}