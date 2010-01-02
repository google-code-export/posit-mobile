/*
 * File: SyncActivity.java
 * 
 * Copyright (C) 2009 The Humanitarian FOSS Project (http://www.hfoss.org)
 * 
 * This file is part of POSIT, Portable Open Search and Identification Tool.
 *
 * POSIT is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) as published 
 * by the Free Software Foundation; either version 3.0 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU LGPL along with this program; 
 * if not visit http://www.gnu.org/licenses/lgpl.html.
 * 
 */
package org.hfoss.posit;

import org.hfoss.posit.utilities.Utils;
import org.hfoss.posit.web.SyncThread;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class SyncActivity extends Activity {

	private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getIntent().getAction().equals(Intent.ACTION_SYNC)) {
			{
				if (!Utils.isNetworkAvailable(this)) {
					Utils.showToast(this, "Sync Error: No Network Available");
					finish();
				} else
					syncFinds();

			}
		}
	}

	/**
	 * This method starts the synchronization thread which handles the sync
	 * action.
	 */
	private void syncFinds() {
		mProgressDialog = ProgressDialog.show(this, "Synchronizing",
				"Please wait.", true, false);
		Thread syncThread = new SyncThread(this, new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == SyncThread.DONE) {
					mProgressDialog.dismiss();
					finish();
				} else if (msg.what == SyncThread.NONETWORK) {
					Utils.showToast(mProgressDialog.getContext(),
							"Sync Error:No Network Available");
					mProgressDialog.dismiss();
					finish();
				} else if (msg.what == SyncThread.SYNCERROR) {
					Utils.showToast(mProgressDialog.getContext(),
							"Sync Error: An unknown error has occurred");
					mProgressDialog.dismiss();
					finish();
				}
			}
		});
		syncThread.start();

	}
}
