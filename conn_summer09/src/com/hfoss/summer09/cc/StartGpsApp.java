/**

 * @author James Jackson
 * 
 */

package com.hfoss.summer09.cc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.Toast;

public class StartGpsApp extends Activity  {
	TabHost  mTabHost, mTabHost2;
	Toast myToast;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gps_start_screen);
		Button button = (Button)findViewById(R.id.start_gps);
		button.setOnClickListener(mStartGps);
	}

	private OnClickListener mStartGps = new OnClickListener() {
		public void onClick(View v) {
			startApp();
		}
	};

	public void startApp(){
		Intent intent = new Intent("com.hfoss.summer09.cc.MAPS");
		startActivity(intent);	
	}
}