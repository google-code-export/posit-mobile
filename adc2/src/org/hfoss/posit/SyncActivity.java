/*******************************************************************************
 * Copyright (c) 2009 .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Prasanna Gautam - initial API and implementation
 *     Ralph Morelli - Supervisor
 *     Trishan deLanerolle - Director
 *     Antonio Alcorn - Summer 2009 Intern
 *     Gong Chen - Summer 2009 Intern
 *     Chris Fei - Summer 2009 Intern
 *     Phil Fritzsche - Summer 2009 Intern
 *     James Jackson - Summer 2009 Intern
 *     Qianqian Lin - Summer 2009 Intern 
 *     Khanh Pham - Summer 2009 Intern
 ******************************************************************************/

package org.hfoss.posit;

import org.hfoss.posit.web.SyncThread;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * @author rmorelli
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
				} else if(msg.what==SyncThread.NETWORK_ERR) {
					showNetworkError();
					return;
				}
			}
		});
		syncThread.start();
	}
	
	private void showNetworkError() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("POSIT can't find the network.")
               .setCancelable(false)
               .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                   public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        finish();
                   }
               });
        final AlertDialog alert = builder.create();
        alert.show();
	}
}
