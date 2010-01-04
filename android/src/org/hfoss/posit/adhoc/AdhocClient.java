package org.hfoss.posit.adhoc;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.hfoss.posit.Find;
import org.hfoss.posit.PositMain;
import org.hfoss.posit.R;
import org.hfoss.posit.utilities.Utils;
import org.hfoss.third.CoreTask;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

public class AdhocClient {
    /** Called when the activity is first created. */
    
	private Context mContext;
	private ProgressDialog mProgressDialog;
	private String status;
	private CoreTask coretask = null;
	private BufferedReader br;
	private BufferedWriter bw;
	private String incoming ="";
	private boolean ret = true;
	private char[] buff;
	private final String TAG = "AdhocClient";
	private Thread netStartUpThread;
	private Thread netHandleIncomingThread;
	
	private Intent rwgService = null;
	
	// Need handler for callbacks to the UI thread
    final Handler messHandler = new Handler();
    // Create runnable for posting
    final Runnable messUpdate = new Runnable() {
        public void run() {
        	try{
        		if(AdhocClientActivity.sendView!=null) {
        			File f = new File("/data/rwg/results.txt");
        			if(f.createNewFile()||f.exists()) {
	        			FileWriter results = new FileWriter(f,true);
	        			results.append("Received ("+Utils.getTimestamp()+"): "+ incoming+"\n");
	        			results.flush();
	        			results.close();
        			}
        		AdhocClientActivity.sendView.setText("Received: " + incoming + "\n" 
        				+ AdhocClientActivity.sendView.getText());
        		}
        	}catch(Exception e){
        		System.out.println(e.getClass().toString());
        	}
        }
    };
    
    // Need handler for callbacks to the UI thread
    final Handler statusHandler = new Handler();
    // Create runnable for posting
    final Runnable statusUpdate = new Runnable() {
        public void run() {
        	try{
        		if(AdhocClientActivity.sendView!=null)
        		AdhocClientActivity.info.setText(status);
        	}catch(Exception e){
        		System.out.println(e.getClass().toString());
        	}
        }
    };

    public AdhocClient(Context c) {
		/*
		 * The Adhoc client needs several binaries installed to run. We check each time.
		 */
        Log.i(TAG, "Creating AdhocClient");
        mContext = c;
    	mProgressDialog = ProgressDialog.show(mContext, "Please wait", "Enabling Random-Walk Gossip-Based Manycast", true,true);
		coretask = new CoreTask();
		coretask.setPath(mContext.getApplicationContext().getFilesDir().getParent());
		this.checkDirs();
		boolean filesetOutdated = coretask.filesetOutdated();
        if (binariesExists() == false || filesetOutdated) {
        	if (coretask.hasRootPermission()) {
                Log.i(TAG, "Installing Binaries");
        		installBinaries();
        	}
        	else {
        		this.openNotRootDialog();
        	}
        }
    
       Log.i(TAG, "Starting network thread...");
        this.netStartUpThread = new Thread(new NetworkStart());
        this.netStartUpThread.start();
        Log.i(TAG, "Starting HandleIncoming thread...");
        this.netHandleIncomingThread = new Thread(new HandleIncoming());
        this.netHandleIncomingThread.start();
        Log.i(TAG, "Done starting network services!");
	}

	 public boolean binariesExists() {
	    	File file = new File(this.coretask.DATA_FILE_PATH+"/bin/tether");
	    	if (file.exists()) {
	    		return true;
	    	}
	    	return false;
    }
    
    public void installBinaries() {
    	List<String> filenames = new ArrayList<String>();
    	// tether
    	this.copyBinary(this.coretask.DATA_FILE_PATH+"/bin/netcontrol", R.raw.netcontrol);
    	filenames.add("netcontrol");
    	// dnsmasq
    	this.copyBinary(this.coretask.DATA_FILE_PATH+"/bin/dnsmasq", R.raw.dnsmasq);
    	//filenames.add("dnsmasq");
    	try {
			this.coretask.chmodBin(filenames);
		} catch (Exception e) {
			displayToastMessage("Unable to change permission on binary files!");
		}
    	// dnsmasq.conf
    	this.copyBinary(this.coretask.DATA_FILE_PATH+"/conf/dnsmasq.conf", R.raw.dnsmasq_conf);
    	// tiwlan.ini
    	this.copyBinary(this.coretask.DATA_FILE_PATH+"/conf/tiwlan.ini", R.raw.tiwlan_ini);
    	this.displayToastMessage("Binaries and config-files installed!");
    }
    
    private void copyBinary(String filename, int resource) {
    	File outFile = new File(filename);
    	InputStream is = mContext.getResources().openRawResource(resource);
    	byte buf[]=new byte[1024];
        int len;
        try {
        	OutputStream out = new FileOutputStream(outFile);
        	while((len = is.read(buf))>0) {
				out.write(buf,0,len);
			}
        	out.close();
        	is.close();
		} catch (IOException e) {
			this.displayToastMessage("Couldn't install file - "+filename+"!");
		}
    }

    public void displayToastMessage(String message) {
		Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
	}
    
    private void openNotRootDialog() {
		LayoutInflater li = LayoutInflater.from(mContext);
        View view = li.inflate(R.layout.norootview, null); 
		new AlertDialog.Builder(mContext)
        .setTitle("Not Root!")
        .setView(view)
        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                //        Log.d(MSG_TAG, "Close pressed");
                        //this.application.finish();
                }
        })
        .setNeutralButton("Override", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                 //   Log.d(MSG_TAG, "Override pressed");
                    installBinaries();
                }
        })
        .show();
   	}
    
    public void enableAdhocClient() {
		if (this.coretask.runRootCommand(this.coretask.DATA_FILE_PATH+"/bin/netcontrol start_client "+this.coretask.DATA_FILE_PATH)) {
    	}
    }
    
    public void disableAdhocClient() {
		if (this.coretask.runRootCommand(this.coretask.DATA_FILE_PATH+"/bin/netcontrol stop_client "+this.coretask.DATA_FILE_PATH)) {
    	}
    }

    public void disableWifi() {
    	boolean done=false;
    	while (!done && !Thread.currentThread().isInterrupted()) {
    		AdhocClientActivity.wifiManager.setWifiEnabled(false);
			// Waiting for interface-shutdown
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// nothing
			}
			if (AdhocClientActivity.wifiManager.isWifiEnabled())
				done=false;
			else
				done=true;
    	}
    }
    
    public void enableWifi() {
    	
    	/*if(!this.wifiManager.isWifiEnabled())
    		this.wifiManager.setWifiEnabled(true);
    	*/
    	
    	Log.i(TAG,"Enabling wifi...");
    	
    	boolean done=false;
    	while (!done&& !Thread.currentThread().isInterrupted()) {
    		WifiManager wm;
    		if(AdhocClientActivity.wifiManager==null)
    			wm = PositMain.wifiManager;
    		else
    			wm = AdhocClientActivity.wifiManager;
    		wm.setWifiEnabled(true);
			// Waiting for interface-shutdown
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// nothing
			}
			if (wm.isWifiEnabled())
				done=true;
			else
				done=false;
    	}
        Log.i(TAG, "Enabled Wifi");

    }
    
    private void checkDirs() {
    	File dir = new File(this.coretask.DATA_FILE_PATH);
    	if (dir.exists() == false) {
    			this.displayToastMessage("Application data-dir does not exist!");
    	}
    	else {
	    	dir = new File(this.coretask.DATA_FILE_PATH+"/bin");
	    	if (dir.exists() == false) {
	    		if (!dir.mkdir()) {
	    			this.displayToastMessage("Couldn't create bin-directory!");
	    		}
	    	}
	    	dir = new File(this.coretask.DATA_FILE_PATH+"/var");
	    	if (dir.exists() == false) {
	    		if (!dir.mkdir()) {
	    			this.displayToastMessage("Couldn't create var-directory!");
	    		}
	    	}
	    	dir = new File(this.coretask.DATA_FILE_PATH+"/conf");
	    	if (dir.exists() == false) {
	    		if (!dir.mkdir()) {
	    			this.displayToastMessage("Couldn't create conf-directory!");
	    		}
	    	}   			
    	}
    }
      
    class NetworkStart implements Runnable {
        // @Override
       public void run() {	
      	 Log.i(TAG, "Starting NetworkStart");
    	 enableWifi();
    	 status = "Wifi enabled, starting AdhocClient. Plz Wait";
    	 statusHandler.post(statusUpdate);
    	 
    	 //disableAdhocClient();
      	 //Log.i(TAG, "Enabling AdhocClient");
      	 //enableAdhocClient();
      	 //Log.i(TAG, "Enabled AdhocClient");

    		    	 
    	 status = "Wifi & AdhocClient enabled, Start the protocol (./data/rwg/rwgexec -i tiwlan0)";
    	 statusHandler.post(statusUpdate);
    	 
    	 Log.i(TAG, "Starting rwgexec");
    	 if (!RWGService.isRunning())
			{
    		 	Log.i(TAG, "NOT RUNNING");
		        rwgService = new Intent(mContext, RWGService.class);
		        //rwgService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        RWGService.setActivity((PositMain)mContext);

				mContext.startService(rwgService);
			      
			}
    	 try{
    	    	Thread.sleep(2000);
    	    	}catch(Exception e){
    	    		System.out.println(e.getClass().toString());
    	    	}
    	 Log.i(TAG, "AFTER SERVICE IS STARTED");
    	 
    	 
    	 
    	/* if(coretask.runRootCommand("./data/rwg/rwgexec -t -l 3 -i tiwlan0"))
    		 System.out.println("Does this work?");	 */
    	 
    	 
    	/*
    	 * Communication is done with 2 pipes, input and output. These must be in the
    	 * current working directory when rwgexec is started.
    	 * 
    	 *  The pipes are named from the point of view of rwg. Thus, the sense of the
    	 *  pipes is switched from the point of view of our app using them. This is why
    	 *  we read from the output pipe and write to the input pipe.
    	 */
    	// open Output Pipe
    	 
    	while(!RWGService.isRunning());
    	
    	Log.i("ADHOC_CLIENT","It's running!");
		try{
			Log.i("ADHOCCLIENT", "DOING INPUT PIPE");
			FileOutputStream fos = new FileOutputStream("/data/data/org.hfoss.posit/files/input");
			Log.i("ADHOCCLIENT", "1");
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			Log.i("ADHOCCLIENT", "2");
			PrintWriter pw = new PrintWriter(bos);
			Log.i("ADHOCCLIENT", "3");
			bw = new BufferedWriter(pw);
			Log.i("ADHOCCLIENT", "DID INPUT PIPE");
		}catch(Exception e2){
			Log.i("ADHOCCLIENT", e2.toString(), e2);
			System.out.println(e2.getClass().toString());
		}
    	 
    	try{
    		 FileInputStream fis = new FileInputStream("/data/data/org.hfoss.posit/files/output");
    		 InputStreamReader isr = new InputStreamReader(fis,"ASCII");
    		 br = new BufferedReader(isr);
    		 Log.i("ADHOCCLIENT", "DID OUTPUT PIPE");
	    }catch(Exception e1){
	    	Log.i("ADHOCCLIENT", e1.toString(), e1);
	    }
	    
	    Log.i("ADHOCCLIENT", "GETS TO NETWORK CONFIG DONE");
	    status = "Network config done";
    	statusHandler.post(statusUpdate);
    	Log.i("ADHOCCLIENT", "AFTER NET CONFIG DONE");
	    
    	try{
    	Thread.sleep(2000);
    	}catch(Exception e){
    		System.out.println(e.getClass().toString());
    	}
    	
    	status = "Random-Walk Gossip-Based Manycast Active";
    	statusHandler.post(statusUpdate);
    	mProgressDialog.dismiss();
    	 return;     	
        }
    }
    
    class HandleIncoming implements Runnable {
       // @Override
    	 public void run() {
          	Log.i(TAG, "Starting HandleIncoming");

    		boolean pipeOpen = false;
    		buff = new char[1];
    		
    		while(!pipeOpen){
    				try{	
	    				if(br.ready()){
	    					pipeOpen = true;
	    					break;
	    				}
    				} catch(Exception e1) {

    				}
    				try{
    					Thread.sleep(1000);
    				} catch(Exception e2) {} 
    		}
 
        	Looper.prepare();
        	while (!Thread.currentThread().isInterrupted() && ret) { //
        		
        		try{
        		/*	
        		  if(br.ready()){
        		  	incoming = br.readLine();
        			messHandler.post(messUpdate); 
        			}
        		 */
    			
        			if(br.ready()){
        				incoming = "";
        			
        				while(br.read(buff,0,1) > 0){
        					if(buff[0] == '\r')
        		 				break;
        					incoming = incoming + Character.toString(buff[0]);
        				}
        				
     					messHandler.post(messUpdate);
     					parseAndSave(incoming);
        			}
        			
    			Thread.sleep(300);
        			
        		}catch(Exception e){
        			System.out.println(e.getClass().toString());
        		}
        	}  	
        	return;
    	 }
    }
    
    private void parseAndSave(String incoming) {
    	Log.i(TAG, "Parsing string:"+incoming);
    	int index = incoming.indexOf("{");
    	if(index!=-1)
    		incoming = incoming.substring(index);
    	Log.i(TAG, "Parsing string:"+incoming);
		try {
			JSONObject obj = new JSONObject(incoming);
			ContentValues content = new ContentValues();
			
			double longitude = Double.parseDouble(obj.getString("findLong"));
			content.put(mContext.getString(R.string.longitudeDB), longitude);
			double latitude = Double.parseDouble(obj.getString("findLat"));
			content.put(mContext.getString(R.string.latitudeDB), latitude);
			long findId = obj.getLong("findId");
			content.put(mContext.getString(R.string.idDB), findId);
			int projectId = obj.getInt("projectId");
			String name = obj.getString("name");
			content.put(mContext.getString(R.string.nameDB), name);
			String description = obj.getString("description");
			content.put(mContext.getString(R.string.descriptionDB), description);
			content.put(mContext.getString(R.string.projectId), projectId);
			content.put(mContext.getString(R.string.adhocDB), 1);
			content.put("sid", findId);
			
			Log.i(TAG, content.toString());
			Find find = new Find(mContext);
			find.insertToDB(content, null);
			
		} catch (JSONException e) {
			Log.e("JSONError", e.toString());
		} catch (NumberFormatException e) {
			Log.e(TAG, e.toString());
		}
	}   

	public void send(String sendMessage) {
		try{
			Log.i(TAG, "sending message:" + sendMessage);
			File f = new File("/data/rwg/results.txt");
			if(f.createNewFile()||f.exists()) {
				FileWriter results = new FileWriter(f,true);
		        results.append("Sent ("+Utils.getTimestamp()+"): "+sendMessage+"\n");
		        results.flush();
		        results.close();
			}

    		bw.write(sendMessage);
    		bw.flush();
    	}catch(Exception e){
    		Log.e(TAG,e.getClass().toString());
    	}
		
	}
	
	public void end() {
		netStartUpThread.interrupt();
		netHandleIncomingThread.interrupt();
		ret = false;
		if (rwgService == null)
			rwgService = new Intent(mContext, RWGService.class);
		
		
		 RWGService.setActivity((PositMain)mContext);
		
		mContext.stopService(rwgService);
	}   
}


