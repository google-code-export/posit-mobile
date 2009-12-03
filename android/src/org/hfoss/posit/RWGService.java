package org.hfoss.posit;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
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
    	int procId = findProcessId(RWG_BINARY_INSTALL_PATH);

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
			
			Log.i(TAG,"Setting permission on Tor binary");
			doCommand(SHELL_CMD_CHMOD, CHMOD_EXE_VALUE + ' ' + RWG_BINARY_INSTALL_PATH);
			killRWGProcess();
			
			doCommand(SHELL_CMD_RM,RWG_LOG_PATH);
			
			Log.i(TAG,"Starting tor process");
			procRWG = doCommand(RWG_BINARY_INSTALL_PATH,"");
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


	@Override
	public void onDestroy() {
		super.onDestroy();
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
