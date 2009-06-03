package org.hfoss.posit.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.hfoss.posit.Find;
import org.hfoss.posit.MyDBHelper;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class SyncThread extends Thread{
	
	private static final String TAG = "SyncThread";
	public static final int DONE = 0;
	private Handler mHandler;
	private Context mContext;
	private List<Integer> updatedFindsOnPhone = new ArrayList<Integer>();
	private List<Integer> newFindsOnServer= new ArrayList<Integer>();
	private List<HashMap<String, Object>> mRemoteFindsList;
	private List<Integer> mNewFindIdsOnPhone;
//	private List<Integer> mPhoneFindSIDs;

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
					newFind.insertToDB(args);
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


	
	/**********
	private void getFindsFromServer(Communicator comm, List<Integer> idSet) {
		if (idSet.size() == 0) {
			Log.i(TAG, "No new finds on server.");
			return;		
		}
		for (int id: idSet) {
			ContentValues args = comm.getRemoteFindById(id);
			if (!args.equals(null)) {
				Find find = new Find(mContext);
				find.insertToDB(args);
				Log.i(TAG, "Insert find to DB: " + id);

			}
		}
	}
	private void sendUpdatedFinds(Communicator comm) {
		if (updatedFindsOnPhone.size()==0)
			return;
		
		for (int id: updatedFindsOnPhone) {
			Find find = new Find(mContext,id);
			comm.updateRemoteFind(find);
		}
	}
	*********/

	/**
	 * This method gets the SIDs of Finds on the server. Then checks them against
	 *  Finds on the phone.  
	 *  TODO: This method should be modified to return the list.
	 * Side Effect:  sets the list newFindsOnServer (SIDs)
	 * Side Effect:  sets the list updatedFindsonPhone (Phone IDs)
	 * @param comm
	 */	
	
	/***
		//private void getRemoteFindIDs(Communicator comm) {
	private List<HashMap<String, Object>> remoteFindIds getRemoteFindIDs(Communicator comm) {
		List<HashMap<String, Object>> remoteFindIds = comm.getAllRemoteFindsIds(); //gets all the finds Ids and Revisions
		
		if (remoteFindIds.size()>0) {
			Log.i(TAG, "getRemoteFindIDs.remotFindIds: " +remoteFindIds.toString());
			MyDBHelper mDBHelper =new MyDBHelper(mContext);
			newFindsOnServer = mDBHelper.getNewAndUpdatedFindIdsOnServer(remoteFindIds);
			Log.i(TAG, "Finds that need synching to Server (SID) (" + newFindsOnServer.size()+") "+newFindsOnServer+"");
			updatedFindsOnPhone = mDBHelper.getUpdatedFindIdsOnPhone(remoteFindIds);
			Log.i(TAG, "Finds on phone that need synching to Server (ID) ("+ updatedFindsOnPhone.size() + ") " +updatedFindsOnPhone+"");
			getNewRemoteFinds(comm);
		}else {
			Log.i(TAG, "No Finds on Server");
		}
		return  remoteFindIds;
	}
	***/
/***
	private void getUpdatedRemoteFinds(Communicator comm) {
		
	}
***/
	
	/**
	 * This method retrieves finds from server that aren't on the phone.
	 * @param comm
	 */	
	/********
	private void getNewRemoteFinds(Communicator comm) {
		if (newFindsOnServer.size()==0) {
			Log.i(TAG, "No new finds on server.");
			return;
		}

		Log.i(TAG, "Getting Finds from server: " + newFindsOnServer.toString());

		for (int id: newFindsOnServer) {
			ContentValues args = comm.getRemoteFindById(id);
			if (!args.equals(null)) {
				Find find = new Find(mContext);
				find.insertToDB(args);
			}
		}
	}
****/
	/**
	 * Give a list of Ids (not SIDs), sends the Finds with those IDs to the server.
	 * @param comm
	 * @param toSend -- a list of Ids
	 */
	/***
	//private void sendNewFinds(Communicator comm) {
	private void sendNewFinds(Communicator comm, List<Integer> newFinds) {

//		MyDBHelper mDBHelper  = new MyDBHelper(mContext);
//		List<Integer> newFinds = mDBHelper.getAllNewIds();
		if (newFinds.equals(null) || newFinds.size()==0) {
			Log.i(TAG, "No finds to send to server.");
			return;
		}

		Log.i(TAG, "Sending finds to server: " + newFinds.toString());

		for (int id: newFinds) {
			Find find = new Find(mContext, id);
			comm.sendFind(find);
		}
	}
}
***/
