package org.hfoss.posit;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.hfoss.third.CoreTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class RWGService extends Service {

	private Process mProcess;
	private CoreTask mCoreTask;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		try {
			Log.i("RWGService", "onCreate()");
			mCoreTask = new CoreTask();
			runRWG();
		}
		catch(Exception e) {
			Log.i("RWGService", e.toString());
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("SERVICE", "onDestroy()");
		endRWG();
	}

	private void runRWG() throws IOException {
		// Get the user's group size setting
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		int groupSize = Integer.parseInt(sp.getString("ADHOC_GROUPSIZE", "3"));
		
		Log.i("Adhoc", "Set groupSize to "+ groupSize);
		
		Log.i("RWGService", "runRWG()");
        DataOutputStream os = null;
        
        
        
        /*
         * We need to run rwgexec as root, so we get a su shell.
         * The pipes need to be in the current working directory when rwgexec is
         * started, so we cd to /data/rwg first.
         */
        
		try {
			mCoreTask.killProcessRunning("./rwgexec");
			mProcess = Runtime.getRuntime().exec("su");
	        os = new DataOutputStream(mProcess.getOutputStream());
	        os.writeBytes("cd data/rwg"+"\n");
	        os.writeBytes("chmod 777 rwgexec"+"\n");
	        os.writeBytes("./rwgexec -t -g "+ groupSize+ " -h 99 -l 3600 -i tiwlan0 > trace.txt"+"\n");
	        os.close();
	        Log.i("RWGService", "tried to open rwg");
		} catch (Exception e) {
			Log.d("RWG", "Unexpected error - Here is what I know: "+e.getMessage());
		}
	}
	
	private void endRWG() {
		try {
			/*int pid2;
			Log.i("pid = ",(pid2=mCoreTask.isProcessRunning("./rwgexec"))+"");
			File f = new File("/data/rwg/pid");
			Scanner sc = new Scanner(f);
			String pid = sc.next();
			Log.i("equal",(Integer.parseInt(pid)==pid2)+"");
			Runtime.getRuntime().exec("kill -9 "+pid);
			sc.close();
			f.delete();*/
			mCoreTask.killProcessRunning("./rwgexec");
	        this.getSystemService(Context.WIFI_SERVICE);
			Log.i("RWGService","Stopped WiFi Service");
		}
		catch(Exception e){Log.i("endRWG()",e.toString());}
		mProcess.destroy();
	}
		
}