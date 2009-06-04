package org.hfoss.posit.web;

import java.util.HashMap;
import java.util.List;

import org.hfoss.posit.Find;
import org.hfoss.posit.MyDBHelper;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class SyncThread extends Thread {
	
	private static final String TAG = "SyncThread";
	public static final int DONE = 0;
	private Handler mHandler;
	private Context mContext;
	private List<HashMap<String, Object>> mRemoteFindsList;

	public SyncThread(Context context, Handler handler) {
		mHandler = handler;
		mContext = context;
	}
	
	public void run() {
		try {
			Communicator comm = new Communicator(mContext);
			mRemoteFindsList =  comm.getAllRemoteFinds();  // Get all server SIDs

			getNewFindsFromServer(comm, mRemoteFindsList);
			putNewFindsToServer(comm);
			getUpdatedFindsFromServer(comm, mRemoteFindsList);
			putUpdatedFindsToServer(comm, mRemoteFindsList);
		}catch (NullPointerException e) {
			Log.e(TAG, e.getMessage()+"");
		}
		mHandler.sendEmptyMessage(DONE);
	}

	/**
	 * This method retrieves all finds from the server that aren't already on the phone.
	 * @param comm
	 */
	private void getNewFindsFromServer(Communicator comm, List<HashMap<String, Object>> remoteFindsList) {
		Log.i(TAG, "Remote Finds (SIDs) From Server: " + remoteFindsList.toString());
		
		MyDBHelper mDBHelper = new MyDBHelper(mContext);  		// Get all phone SIDs
		List<Integer> phoneSIDs = mDBHelper.getAllPhoneSIDs();
		Log.i(TAG, "Phone SIDs: " + phoneSIDs.toString());
		
		// Retrieve each new find from the server
		for (int i = 0; i < remoteFindsList.size(); i++) {
			HashMap<String, Object> find = remoteFindsList.get(i);
			Integer Id = Integer.parseInt(find.get("id").toString());
		
			if (!phoneSIDs.contains(Id)) {
				int id = Id.intValue();
				ContentValues args = comm.getRemoteFindById(id);
				if (!args.equals(null)) {
					Find newFind = new Find(mContext);
					newFind.insertToDB(args, null);
					Log.i(TAG, "Inserting new find from server into DB: " + id);
				}
			}
		}
	}
	
	/**
	 * This method sends the phone's new finds to the server.
	 * @param comm
	 */
	private void putNewFindsToServer(Communicator comm){
		MyDBHelper mDBHelper = new MyDBHelper(mContext); 
		List<Integer> newFindsOnPhone = mDBHelper.getAllNewIds();
		if (newFindsOnPhone.equals(null) || newFindsOnPhone.size()==0) {
			Log.i(TAG, "No NEW finds to send to server.");
			return;
		}

		Log.i(TAG, "Sending finds to server: " + newFindsOnPhone.toString());

		for (int id: newFindsOnPhone) {
			Find find = new Find(mContext, id);
			comm.sendFind(find);
		}
	}
	
	
	/**
	 * This method retrieves finds that have been updated from the server
	 *  and puts them in the phone's DB.
	 * @param comm
	 * @param remoteFindsList
	 */
	private void getUpdatedFindsFromServer(Communicator comm, List<HashMap<String, Object>> remoteFindsList) {
		Log.i(TAG, "Remote Finds (SIDs) From Server: " + remoteFindsList.toString());
		
		MyDBHelper mDBHelper = new MyDBHelper(mContext);  // Get matching SIDs from phone
		List<Integer> phoneSIDs = mDBHelper.getFindsNeedingUpdate(remoteFindsList);
		Log.i(TAG, "Updated Phone SIDs: " + phoneSIDs.toString());
		
		for (int id: phoneSIDs) {
			Find updatedFind = new Find(mContext,id);
			comm.updateRemoteFind(updatedFind);
			Log.i(TAG, "Sent updated find to server: " + id);
		}
	}
	
	/**
	 * This method retrieves finds that have been updated on the phone
	 *  and sends them to the server.
	 * @param comm
	 * @param remoteFindsList
	 */
	private void putUpdatedFindsToServer(Communicator comm, List<HashMap<String, Object>> remoteFindsList) {
		Log.i(TAG, "Remote Finds (SIDs) From Server: " + remoteFindsList.toString());
		
		MyDBHelper mDBHelper = new MyDBHelper(mContext);  // Get matching SIDs from phone
		List<Integer> phoneSIDs = mDBHelper.getUpdatedSIDsFromPhone(remoteFindsList);
		Log.i(TAG, "Updated Phone SIDs: " + phoneSIDs.toString());
		
		for (int id: phoneSIDs) {
			Find updatedFind = new Find(mContext,id);
			comm.updateRemoteFind(updatedFind);
			Log.i(TAG, "Sent updated find to server: " + id);
		}
	}
	
}

