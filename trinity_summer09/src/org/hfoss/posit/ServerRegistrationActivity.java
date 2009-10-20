package org.hfoss.posit;

import java.util.List;

import org.hfoss.posit.web.Communicator;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ServerRegistrationActivity extends Activity {
	
	private static final int BARCODE_READER = 0;
	private static final String TAG = "ServerRegistration";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.registration);
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String server = sp.getString("SERVER_ADDRESS", null);
		
		if (server != null) {
			TextView notRegisteredTv = (TextView) findViewById(R.id.phoneNotRegistered);
			TextView registeredTv = (TextView) findViewById(R.id.phoneRegistered);
			TextView serverAddress = (TextView) findViewById(R.id.serverAddress);
			
			notRegisteredTv.setVisibility(View.GONE);
			registeredTv.setVisibility(View.VISIBLE);
			
			serverAddress.setVisibility(View.VISIBLE);
			serverAddress.setText(server);
		}
		final Button registerButton = (Button)findViewById(R.id.registerButton);
        
		registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (ServerRegistrationActivity.isIntentAvailable(
            			ServerRegistrationActivity.this,"com.google.zxing.client.android.SCAN"))
        		{
        			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        			try{
        				startActivityForResult(intent, BARCODE_READER);
        			}catch(ActivityNotFoundException e)
        			{
        				Log.i(TAG, e.toString());
        			}
        		}
            }
        });
	}
	
	public static boolean isIntentAvailable(Context context, String action) {
	    final PackageManager packageManager = context.getPackageManager();
	    final Intent intent = new Intent(action);
	    List<ResolveInfo> list =
	            packageManager.queryIntentActivities(intent,
	                    PackageManager.MATCH_DEFAULT_ONLY);
	    return list.size() > 0;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(TAG, "Result code = " + resultCode);
		Log.i(TAG, "Request code = " + requestCode);
		
		if (resultCode == RESULT_CANCELED)
			return;
		switch (requestCode) {
		case BARCODE_READER:
			String value = data.getStringExtra("SCAN_RESULT");
			Log.i(TAG, value);
			
			JSONObject object;
			try {
				object = new JSONObject(value);
				String server = object.getString("server");
				String authKey = object.getString("authKey");
				Log.i(TAG, "server= "+server+", authKey= "+authKey);
				
				TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
				String imei = manager.getDeviceId();
				
				Communicator communicator = new Communicator(this);
				boolean registered = communicator.registerDevice(server, authKey, imei);
				if (registered == true) {
					SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
					Editor spEditor = sp.edit();
					
					spEditor.putString("SERVER_ADDRESS", server);
					spEditor.putString("AUTHKEY", authKey);
					spEditor.commit();
				}
				finish();
				
			} catch (JSONException e) {
				Utils.showToast(this, e.toString());
			}
			break;
		}
	}
}
