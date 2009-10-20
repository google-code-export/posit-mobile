package org.hfoss.posit.web;

import java.util.Timer;
import java.util.TimerTask;

import org.hfoss.posit.PositMain;
import org.hfoss.posit.R;
import org.hfoss.posit.Utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
/**
 * Class for monitoring all the sync related activities
 * @author pgautam
 *
 */
public class SyncService extends Service{
	//The frequency at which we want to update
	private static final long UPDATE_INTERVAL = 3000;
	private static  Activity MAIN_ACTIVITY = null;
	private Timer timer = new Timer();
	private NotificationManager mNotificationManager;
	private int count=0;
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
		timer.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				getStatus();
			}
			
		},0, UPDATE_INTERVAL);
		Log.i(getClass().getSimpleName(), "Timer started");
	}

	protected void getStatus() {
		Log.i(getClass().getSimpleName(), "background task-start"+count);
		count++;
		
	}
	
	/**
	 * Shutting down the service
	 */
	private void shutdownService() {
		if (timer != null) timer.cancel();
		  Log.i(getClass().getSimpleName(), "Timer stopped!!!");
		  if (MAIN_ACTIVITY!=null) {
			  Utils.showToast(MAIN_ACTIVITY, "POSIT Service Stopped");
			  //mNotificationManager.notify(NOTIFICATION_ID, Utils.defaultNotification("Service stopped"));
		  }
	}
}
