/**
 * @author James Jackson
 * StartCameraApp activity presents a view to the user that contains a brief description of 
 * what Camera Application is all about before the user decides to start the application
 */

package com.hfoss.summer09.cc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class StartCameraApp extends Activity  {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_tab_view);
		Button button = (Button)findViewById(R.id.start_picture);
		button.setOnClickListener(mStartPicture);
	}

	private OnClickListener mStartPicture = new OnClickListener() {
		public void onClick(View v) {
			startApp();
		}
	};

	public void startApp() {
		Intent intent = new Intent("com.hfoss.summer09.cc.START_CAMERA");
		startActivity(intent);	
	}
}