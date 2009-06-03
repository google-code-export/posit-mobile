/**
 * 
 */
package org.hfoss.posit;

import org.hfoss.posit.web.SyncThread;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * @author rmorelli
 *
 */
public class SyncActivity extends Activity {
	
	private ProgressDialog mProgressDialog;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getIntent().getAction().equals(Intent.ACTION_SYNC)) {
			syncFinds();
		}
	}

	/**
	 * This method starts the synchronization thread which handles the sync action.
	 */
	private void syncFinds() {
		mProgressDialog = ProgressDialog.show(this, "Synchronizing", "Please wait.", true,false);
		Thread syncThread = new SyncThread(this, new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what==SyncThread.DONE) {
					mProgressDialog.dismiss();
					finish();
				}
			}
		});
		syncThread.start();
	}

}
