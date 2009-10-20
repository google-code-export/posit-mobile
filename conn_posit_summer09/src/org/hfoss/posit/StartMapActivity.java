/**
 * @author James Jackson
 */

package org.hfoss.posit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class StartMapActivity extends Activity  {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gps_start_screen);
		Button button = (Button) findViewById(R.id.start_gps);
		button.setOnClickListener(mStartGps);
	}

	private OnClickListener mStartGps = new OnClickListener() {
		public void onClick(View v) {
			startMapsApp();
		}
	};

	public void startMapsApp(){
		startActivity(new Intent(this, MapFindsActivity.class));	
	}
}