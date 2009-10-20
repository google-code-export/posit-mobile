/**
 * @author James Jackson
 */

package com.hfoss.summer09.cc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ViewInfoApp extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_empty);
		getInfo();
	}

	public void getInfo() {
		Intent intent = new Intent("com.hfoss.summer09.cc.SHOW_FINDS");
		intent.setClass(this, ListFinds.class);
		startActivity(intent);
	}
}




