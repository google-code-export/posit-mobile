package org.hfoss.posit;

import org.hfoss.posit.web.Communicator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SendCommandActivity extends Activity {
	protected static final String TAG = "CommandSend";
	private static final int confirm_exit=0;
	public Communicator comm; 
	public String searchText, command;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.commands);
		comm = new Communicator(this);
		createButtons();
	}

	private void createButtons() {
		Button search_button = (Button) findViewById(R.id.start_search);
		search_button.setOnClickListener(mSearch);
		Button command_one = (Button) findViewById(R.id.command_1);
		command_one.setOnClickListener(mCommand1);
		Button command_two = (Button) findViewById(R.id.command_2);
		command_two.setOnClickListener(mCommand2);
		Button command_three = (Button) findViewById(R.id.command_3);
		command_three.setOnClickListener(mCommand3);
		Button command_four = (Button) findViewById(R.id.command_4);
		command_four.setOnClickListener(mCommand4);
		Button submit_button = (Button) findViewById(R.id.submit_command);
		submit_button.setOnClickListener(mSubmit);
	}

	private OnClickListener mCommand1 = new OnClickListener() { public void onClick(View v) { } };

	private OnClickListener mCommand2 = new OnClickListener() { public void onClick(View v) { } };

	private OnClickListener mCommand3 = new OnClickListener() { public void onClick(View v) { } };

	private OnClickListener mCommand4 = new OnClickListener() { public void onClick(View v) { } };

	private OnClickListener mSubmit = new OnClickListener() {
		public void onClick(View v) {
			String result = null;
			EditText eText = (EditText) findViewById(R.id.search_text);
			command = eText.getText().toString();

			ConnectivityManager mgr = (ConnectivityManager) SendCommandActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (mgr.getActiveNetworkInfo() != null) {
				if (command.equals("")) {
					Utils.showToast(SendCommandActivity.this, R.string.invalid_entry);
				} else {
					result = comm.sendCommand(command);
					if (result == null || result == "") {
						Utils.showToast(SendCommandActivity.this, R.string.no_finds_returned);
					} else {
						Log.d(TAG, "RESULT FROM SERVER IS: " + result);
						Utils.showToast(SendCommandActivity.this, result);
					}
				}
			} else { 
				Utils.showLongToast(SendCommandActivity.this, R.string.no_data_connection);
			}
		}
	};

	private  OnClickListener mSearch = new OnClickListener() {
		public void onClick(View v) {
			searchFind();
		}
	};

	private void searchFind(){
		String result = null;
		EditText eText = (EditText) findViewById(R.id.search_text);

		searchText = eText.getText().toString();

		if (searchText.equals("")){
			Utils.showToast(this, R.string.invalid_entry);
			return;
		}

		ConnectivityManager mgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (mgr.getActiveNetworkInfo() != null) {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			int projectId = sp.getInt("PROJECT_ID", -1);
			result = comm.searchServerForFind(searchText, projectId);

			if (result.equals("")) {
				Utils.showToast(this, R.string.no_finds_returned);
			} else {		
				Log.d(TAG, "RESULT FROM SERVER IS " + result);
				Intent intent = new Intent(this, ListSearchResults.class );
				intent.putExtra("result", result);
				startActivity(intent);
			}
		} else { 
			Utils.showLongToast(SendCommandActivity.this, R.string.no_data_connection);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			showDialog(confirm_exit);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case confirm_exit:
			return new AlertDialog.Builder(this)
			.setIcon(R.drawable.alert_dialog_icon)
			.setTitle(R.string.exit)
			.setPositiveButton(R.string.alert_dialog_ok, 
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// User clicked OK so do some stuff 
					finish();
				}
			}).setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					setContentView(R.layout.commands);
					/* User clicked Cancel so do nothing */
				}
			}).create();

		default:
			return null;
		}
	}
}