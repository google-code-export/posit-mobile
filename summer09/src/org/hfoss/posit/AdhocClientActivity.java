package org.hfoss.posit;

import java.io.BufferedWriter;
import java.io.FileWriter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AdhocClientActivity extends Activity{
    private static final String TAG = "AdhocClientActivity";
	/** Called when the activity is first created. */
    
    public static TextView sendView;
	public static TextView info;
	public static WifiManager wifiManager;
	public static AdhocClient adhocClient;
	
	private Button sendButton;
	private Button exitButton;
	private EditText inputField;
	private String oldText = "";
	

	@Override
    public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "entering onCreate()...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client);

        
        this.sendButton = (Button)this.findViewById(R.id.Button01);
        this.exitButton = (Button)this.findViewById(R.id.Button02);
        info = (TextView)this.findViewById(R.id.TextView00);
        sendView = (TextView)this.findViewById(R.id.TextView01);
        this.inputField = (EditText)this.findViewById(R.id.EditText01);
        sendView.setMovementMethod(new ScrollingMovementMethod());
        sendButton.setOnClickListener(new SendClicker());
        exitButton.setOnClickListener(new ExitClicker());
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        
        
        if(savedInstanceState == null || (savedInstanceState.containsKey("adhocActive") && !savedInstanceState.getBoolean("adhocActive"))) {
        	Log.i(TAG, "savedInstanceState contains adhocActive...");
	        
	        Log.i(TAG, "getting WifiManager system service..."); 
	       
	        adhocClient = new AdhocClient(this);
        }
        
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Editor edit = sp.edit();
        edit.putBoolean("IS_ADHOC", true);
        edit.commit();
	}
	
	
	/*
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i("YO","onDestroy()");
		this.stopService(new Intent(this,RWGService.class));
	}*/



	class SendClicker implements Button.OnClickListener{
		public void onClick(View v){
			/*Gets new instance, with time that is up to date...*/
			String newText =  inputField.getText().toString();
        	oldText = "sent: " + newText + "\n" + sendView.getText().toString();
        	inputField.setText("");
        	sendView.setText(oldText);
          	adhocClient.send(newText);
		}	
	}

	class ExitClicker implements Button.OnClickListener{
		public void onClick(View v){
			adhocClient.end();
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(AdhocClientActivity.this);
	        Editor edit = sp.edit();
	        edit.putBoolean("IS_ADHOC", false);
	        edit.commit();
	        AdhocClientActivity.this.stopService(new Intent(AdhocClientActivity.this,RWGService.class));
			finish();
		}	
	}

    public void displayToastMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putBoolean("adhocActive", true);
		super.onSaveInstanceState(outState);
	}    
}


