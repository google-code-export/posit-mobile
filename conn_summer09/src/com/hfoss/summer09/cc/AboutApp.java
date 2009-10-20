/**
 * @author James Jackson
 * About activity module.
 */

package com.hfoss.summer09.cc;

import android.app.Activity;
import android.os.Bundle;

public class AboutApp extends Activity  {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_tab_view);
	}
}
