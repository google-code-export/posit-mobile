package org.hfoss.posit;

import android.app.Dialog;
import android.content.Context;

/**
 * 
 * @author pgautam
 *	Class ActionDialog for showing the action stuff.
 * Just a transparent dialog for printing things on. 
 * TODO Make it more general so that we can add more stuff and not have to spawn new dialogs 
 * 
 */
public class ActionDialog extends Dialog{
	public ActionDialog(Context context){
		
		//generates a transparent dialog.
		super(context, R.style.Theme_Transparent);

	}
//TODO enable to disable the back button for the actionDialog
	/* (non-Javadoc)
	 * @see android.app.Dialog#onKeyDown(int, android.view.KeyEvent)
	 */
//	@Override

	
	
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK){
//			return true;
//			// disabling the back button for now.. :)
//		}
//		return false;
//	}
	 
	
	
}