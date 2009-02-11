package org.hfoss.posit;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TabWidget;

/**
 * Gives the user preferences to change.
 * @author pgautam
 *
 */
public class Preferences extends Activity{


	@Override
	protected void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		TabWidget x = new TabWidget(this);
		setContentView(R.layout.preferences);
	}
	
}
