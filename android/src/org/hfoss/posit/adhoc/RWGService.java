package org.hfoss.posit.adhoc;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.hfoss.posit.PositMain;
import org.hfoss.posit.utilities.Utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class RWGService extends Service implements RWGConstants {
	
	private static final String TAG = "RWG";
	private static PositMain ACTIVITY = null;
	private static Process procRWG = null;
	
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}
	
	public static void setActivity(PositMain activity) {
    	ACTIVITY = activity;
    }
	
	public static boolean isRunning ()
    {
    	int procId = findProcessId("rwgexec");

		return (procId != -1);
		
    }
	
	public static int findProcessId(String command) {
		int procId = -1;
		
		Runtime r = Runtime.getRuntime();
		    	
		Process procPs = null;
		
        try {
            
            procPs = r.exec(SHELL_CMD_PS);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(procPs.getInputStream()));
            String line = null;       
            while ((line = reader.readLine())!=null)
            {
            	if (line.indexOf(command)!=-1)
            	{
            		StringTokenizer st = new StringTokenizer(line," ");
            		st.nextToken(); //proc owner
            		procId = Integer.parseInt(st.nextToken().trim());
            		break;
            	}
            }
            
        } 
        catch (Exception e) {
            Log.e(TAG, "error: " + e.getMessage(), e);
        }
        
        return procId;
	}
	
	public Process doCommand(String command, String arg1) {
		
		Runtime r = Runtime.getRuntime();
		    	
		Process child = null;
		
        try {
            if(child != null) {
            	child.destroy();
            	child = null;
            }
            
            child = r.exec(command + ' ' + arg1);
            
            
            
        } catch (Exception e) {
            Log.e(TAG, "error: " + e.getMessage(), e);
        }
        
        return child;

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
        		killRWGProcess();
                procRWG = Runtime.getRuntime().exec("su");
        os = new DataOutputStream(procRWG.getOutputStream());
        os.writeBytes("chmod 777 files"+"\n");
        os.writeBytes("cd "+POSIT_HOME+"files" + "\n");
        os.writeBytes("chmod 777 rwgexec"+"\n");
        os.writeBytes("./rwgexec -t -g "+ groupSize+ " -h 99 -l 3600 -i tiwlan0 > trace.txt"+"\n");
        os.close();
        Log.i("RWGService", "tried to open rwgexec");
        } catch (Exception e) {
                Log.d("RWG", "Unexpected error - Here is what I know: "+e.getMessage());
        }
}
	
	public void initRWG() {
		try{
			boolean binaryExists = new File(RWG_BINARY_INSTALL_PATH).exists();
			
			if (!binaryExists)
			{
				RWGBinaryInstaller installer = new RWGBinaryInstaller(); 
				installer.start(false);
			
				binaryExists = new File(RWG_BINARY_INSTALL_PATH).exists();
	    		if (binaryExists)
	    		{
	    			Utils.showToast(ACTIVITY, "Tor binary installed!");
	    			Log.i(TAG,"Binary installed!");
	    		}
	    		else
	    		{
	    			Utils.showToast(ACTIVITY, "Tor binary install FAILED!");
	    			Log.i(TAG,"Binary install failed.");
	    			return;
	    		}
			}
			else
				Log.i(TAG,"Binary already exists");
			
			Log.i(TAG,"Setting permission on RWG binary");
			doCommand(SHELL_CMD_CHMOD, CHMOD_EXE_VALUE + ' ' + RWG_BINARY_INSTALL_PATH);
			killRWGProcess();
			
			//doCommand(SHELL_CMD_RM,RWG_LOG_PATH);
			
			Log.i(TAG,"Starting tor process");
            
			runRWG();
            //procRWG = doCommand(RWG_BINARY_INSTALL_PATH,"-t -g "+ groupSize+ " -h 99 -l 3600 -i tiwlan0 > trace.txt");
		} 
		catch (Exception e) {
		
		Log.w(TAG,"unable to start Tor Process",e);
	
		e.printStackTrace();
		}
	}
	
	private void killRWGProcess () {
		//doCommand(SHELL_CMD_KILLALL, CHMOD_EXE_VALUE, TOR_BINARY_INSTALL_PATH);
		
    	if (procRWG != null)
    	{
    		Log.i(TAG,"shutting down RWG process...");
    		
    		procRWG.destroy();
    		
    		try {
    			procRWG.waitFor();
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    		
    		int exitStatus = procRWG.exitValue();
    		Log.i(TAG,"Tor exit: " + exitStatus);

    		procRWG = null;
    	}
    		
		int procId = findProcessId(RWG_BINARY_INSTALL_PATH);

		if (procId != -1)
		{
			Log.i(TAG,"Found RWG PID=" + procId + " - killing now...");
			
			doCommand(SHELL_CMD_KILLALL, procId + "");
		}
		
		/*if (ACTIVITY != null)
			((TorControlPanel)ACTIVITY).setUIState();*/	
    }
	
	public int killProcessRunning(String processName) throws Exception {
        int pid = -1;
    	Process process = null;
		process = Runtime.getRuntime().exec("ps");
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        while ((line = in.readLine()) != null) {
            if (line.contains(processName)) {
            	Log.i("LINE",line);
            	line = line.substring(line.indexOf(' '));
            	line = line.trim();
            	pid = Integer.parseInt(line.substring(0,line.indexOf(' ')));
            	Runtime.getRuntime().exec("kill -9 "+pid);
            }
        }
        in.close();
        process.waitFor();
		return pid;
    }


	@Override
	public void onDestroy() {
		try{
		killProcessRunning("./rwgexec");
        this.getSystemService(Context.WIFI_SERVICE);
                Log.i("RWGService","Stopped WiFi Service");
        }
        catch(Exception e){Log.i("endRWG()",e.toString());}
       procRWG.destroy();
	}


	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
	}


	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.i(TAG, "starting RWG");
		initRWG();
	}


	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}


	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
