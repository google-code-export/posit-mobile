package org.hfoss.posit.web;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.hfoss.posit.Find;
import org.hfoss.posit.MyDBHelper;
import org.hfoss.posit.Utils;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
/**
 * Class for monitoring all the sync related activities
 * @author pgautam
 *
 */
public class SyncService extends Service{
	//The frequency at which we want to update
	private static long UPDATE_INTERVAL = 3000;
	private static  String TAG = "SyncService";
	private static  Activity MAIN_ACTIVITY = null;
	private Timer timer = new Timer();
	private NotificationManager mNotificationManager;
	private int count=0;
	private SharedPreferences applicationPreferences;
	private boolean notifyOnStartStop=true;
	private List<HashMap<String, Object>> remoteFindIds;
	private List<Integer> updatedFindsOnPhone;
	private List<Integer> newFindsOnServer;
	private static int NOTIFICATION_ID = 2;
	/* We wont be using any IPC communication, so we don't care about this */
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	public static  void setMainActivity (Activity activity){
		MAIN_ACTIVITY = activity;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		initialize();
		startService();
		
		if (MAIN_ACTIVITY!=null){
			if (notifyOnStartStop)
			Utils.showToast(MAIN_ACTIVITY, "POSIT Service Started");
		}
		
        
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		shutdownService();
	}
	
	private void initialize(){
		if (MAIN_ACTIVITY!= null){
			mNotificationManager = (NotificationManager) 
				MAIN_ACTIVITY.getSystemService(Context.NOTIFICATION_SERVICE);
		}
	}
	
	
	
	/**
	 * Starting the service
	 */
	private void startService(){
		readApplicationPreferences();
		timer.schedule(new TimerTask(){

			@Override
			public void run() {
//				Sync();
			}
			
		},0, UPDATE_INTERVAL);
		Log.i(getClass().getSimpleName(), "Timer started");
	}
	/**
	 * 
	 */
	private void readApplicationPreferences() {
		applicationPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		UPDATE_INTERVAL = Long.parseLong(applicationPreferences.getString("UPDATE_FREQ", "30"))*1000; //convert to milliseconds
		notifyOnStartStop = applicationPreferences.getBoolean("NOTIFY_START_STOP", false);
	}

	/**
	protected void Sync() {
		Log.i(getClass().getSimpleName(), "sync-start"+count);
		count++;
		getRemoteFinds();
		sendNewFinds();	
		//sendUpdatedFinds();
		
	}
	**/
	/**
	private void getRemoteFinds() {
		Communicator comm = new Communicator(this);
		remoteFindIds = comm.getAllRemoteFinds(); //gets all the finds Ids and Revisions
		Log.i(TAG, "From Server" +remoteFindIds.toString());
		MyDBHelper mDBHelper = new MyDBHelper(this);
		newFindsOnServer = mDBHelper.getNewAndUpdatedFindIdsOnServer(remoteFindIds);
		Log.i(TAG, "New Finds on Server" + newFindsOnServer.size()+" "+newFindsOnServer+"");
		updatedFindsOnPhone = mDBHelper.getUpdatedSIDsFromPhone(remoteFindIds);
		Log.i(TAG, "new Finds on Phone"+ updatedFindsOnPhone+"");
		
	}
	**/
	/**
	 * Shutting down the service
	 */
	private void shutdownService() {
		if (timer != null) timer.cancel();
		  Log.i(getClass().getSimpleName(), "Timer stopped!!!");
		  if (MAIN_ACTIVITY!=null) {
			  if(notifyOnStartStop)
				  Utils.showToast(MAIN_ACTIVITY, "POSIT Service Stopped");
			  //mNotificationManager.notify(NOTIFICATION_ID, Utils.defaultNotification("Service stopped"));
			  
		  }
	}
	/**
	 * Sends Unsynced Finds to the server
	 */
	private void sendNewFinds() {
		Communicator comm = new Communicator(this);
		MyDBHelper mDBHelper  = new MyDBHelper(this);
		List<Integer> findsList = mDBHelper.getAllNewIds();
		for (int id: findsList) {
			Find find = new Find(this, id);
			comm.sendFind(find);
		}
	}
	
	private void sendUpdatedFinds() {
		Communicator comm = new Communicator(this);
		if (updatedFindsOnPhone.size()>0) {
			for (int id : updatedFindsOnPhone) {
				Find find = new Find(this,id);
				comm.updateRemoteFind(find);
			}
		}
	}
}
