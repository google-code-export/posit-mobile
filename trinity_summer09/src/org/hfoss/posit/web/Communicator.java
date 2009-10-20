package org.hfoss.posit.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.hfoss.posit.Find;
import org.hfoss.posit.MyDBHelper;
import org.hfoss.posit.R;
import org.hfoss.posit.Utils;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * The communication module for POSIT.
 * 
 * @author pgautam
 * 
 */
public class Communicator {
	private static final String KEY_NAME = "name";
	private static final String KEY_IMEI = "imei";
	private static final String KEY_APP_KEY = "app_key";
	/*
	 * You should be careful with putting names for server. DO NOT always trust
	 * DNS.
	 */
	private static String server = "http://www.cs.trincoll.edu/~cfei/positweb/api/";
	private static String SEND_FIND_URL = server + "createFind?authKey=test";
	private static String PROJECTS_LIST_URL = server + "listOpenProjects?authKey=test";
	private static String RECEIVE_FINDS_URL = server
			+ "getFind/";
	private static String REGISTER_DEVICE_URL = server +"/registerdevice.php";
	private static String GET_FIND_IDS_URL = server+"listFinds?projectId=1&authKey=test";
	private static String UPDATE_FIND_URL = server+"/finds.php?q=edit";
	private static String authKey;
	private HttpClient httpClient = new DefaultHttpClient();;
	private HttpPost httpPost = new HttpPost();
	private String TAG = "Communicator";
	private String responseString;
	private Context mContext;
	private SharedPreferences applicationPreferences;

	public Communicator(Context _context) {
		mContext = _context;
		PreferenceManager.setDefaultValues(mContext, R.xml.posit_preferences, false);
		applicationPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		setApplicationAttributes(applicationPreferences.getString("AUTHKEY", ""), 
				applicationPreferences.getString("SERVER_ADDRESS", server));
	}
	
	private void setApplicationAttributes(String authKey, String serverAddress){
		this.authKey = authKey;
		server = serverAddress;
		//setFindAndReceiveAdresses(server);
	}
	/**
	 * Sets all the standard addresses based on the server address.
	 * @param server2
	 */
	private void setFindAndReceiveAdresses(String server2) {
		SEND_FIND_URL = server2 + "/finds.php?q=save";
		RECEIVE_FINDS_URL = server2+ "/finds.php?q=get_finds";
		PROJECTS_LIST_URL = server2 + "/projectslist_phone.php";
		REGISTER_DEVICE_URL = server2 +"/registerdevice.php";
		GET_FIND_IDS_URL = server2+"/finds.php?q=get_ids";
		UPDATE_FIND_URL = server2+"/finds.php?q=edit";
	}
	/**
	 * Get all projects from the server
	 * @return
	 */
	public ArrayList<HashMap<String,Object>> getProjects(){
		responseString = doHTTPGET(PROJECTS_LIST_URL);
		ArrayList<HashMap<String,Object>> list= new ArrayList<HashMap<String,Object>>();
		try {
			JSONArray j = new JSONArray(responseString);
			for (int i= 0; i < j.length(); i++){
				list.add((new ResponseParser(responseString).parse().get(0)));
			}
			
			Log.i(TAG, "JSONArray: " + j.toString(1));
		} catch (JSONException e) {
			Log.e(TAG,"JSON Exception" + e.getMessage());
			
		}
		return list;
	}
	
	public boolean registerDevice(String server, String authKey, String imei){
		String url = server + "/api/registerDevice?authKey=" +authKey 
		+ "&imei=" + imei;

		responseString = doHTTPGET(url);
		Log.i(TAG,"Registerdevice: " + responseString);

		if (responseString.equals("false"))
			return false;
		else return true;
	}
	/*
	 * Set the URL for sending the request
	 */
	private void setURI(String Uri) {
		try {
			httpPost.setURI(new URI(Uri));
		} catch (URISyntaxException e1) {
			Log.e(TAG, "URISyntaxException" + e1.getMessage());
		}
	}
	/*
	 * Send one find to the server.
	 */
	public void sendFind (Find find) {
		HashMap<String, String> sendMap = find.getContentMap();
		cleanupOnSend(sendMap);
		Log.i("FIND STATS",sendMap.get("_id"));
		Log.i("FIND STATS",sendMap.get("name"));
		Log.i("FIND STATS",sendMap.get("description"));
		Log.i("FIND STATS",sendMap.get("latitude"));
		Log.i("FIND STATS",sendMap.get("longitude"));
		Log.i("FIND STATS",sendMap.get("projectId"));
		//SEND_FIND_URL += "&id="+sendMap.get("_id")
		responseString = doHTTPPost(SEND_FIND_URL, sendMap);
		Log.i(TAG, "sendFind.ResponseString: " + responseString);
		try {
			HashMap<String, Object> responseMap = (new ResponseParser(responseString)).parse().get(0);
			cleanupOnReceive(responseMap);
			Log.i(TAG, "sendFind.ResponseMap: " + responseMap.toString());
				//this is for easiness, it would be more efficient to do in one query
				
			find.setServerId(1);
				//find.setRevision(Integer.parseInt(responseMap.get(MyDBHelper.KEY_REVISION).toString()));
			find.setSyncStatus(true);
			ContentValues cv = new ContentValues();
			cv.put("sid", "1");
			cv.put("synced", "1");
			find.updateToDB(cv);
				
		} catch (JSONException e) {
			Log.e(TAG,e.getMessage());
		}catch (Exception e) {
			Log.e(TAG, e.getStackTrace().toString());
		}
	}
	
	/*
	 * Send one find to the server.
	 */
	/*public void updateRemoteFind (Find find) {
		HashMap<String, String> sendMap = find.getContentMap();
		cleanupOnSend(sendMap);
		sendMap.put("id",sendMap.get(MyDBHelper.KEY_SID));
		sendMap.remove(MyDBHelper.KEY_SID);
		sendMap.remove(MyDBHelper.KEY_REVISION);
		sendMap.remove(MyDBHelper.KEY_ID);
		Log.i(TAG, "updateRemoteFind.sendMap " + sendMap.toString());
		responseString = doHTTPPost(UPDATE_FIND_URL, sendMap);
		Log.i(TAG, "updateRemoteFind.responseString: " + responseString);
		try {
			HashMap<String, Object> responseMap = (new ResponseParser(
					responseString)).parse();
			cleanupOnReceive(responseMap);
			Log.i(TAG, "updateRemoteFind.responseMap: " + responseMap.toString());
			if (PositHttpUtils.isResponseOK(responseMap)){
				find.setServerId(Integer.parseInt(responseMap.get(MyDBHelper.KEY_SID).toString()));
				find.setRevision(Integer.parseInt(responseMap.get(MyDBHelper.KEY_REVISION).toString()));
				find.setSyncStatus(true);
			}
				
		} catch (JSONException e) {
			Log.e(TAG,"JSONException " + e.getMessage());
		}catch (Exception e ) {
			Log.e(TAG, e.getStackTrace().toString());
		}
	}*/
	
	/*
	 * Send the finds to the designated server
	 */
	/*public void sendFinds(String[] projection, Cursor c) {
		if (c.getCount() <= 0)
			return;
		List<NameValuePair> finds = PositHttpUtils.convertFindsForPost(projection, c);
		setURI(SEND_FIND_URL);

		for (NameValuePair nameValuePair : finds) {
			if (nameValuePair.getName().equals("time")) {
				String value = nameValuePair.getValue();
				finds.add(new BasicNameValuePair("find_time", value));
				finds.remove(nameValuePair);
			}
		}
		finds.remove(0);// removing _id so that we are not sending unecessary
						// data to server.
		// add the app key and phone's IMEI code
		finds.add(new BasicNameValuePair(KEY_APP_KEY, appKey));
		finds.add(new BasicNameValuePair(KEY_IMEI, Utils.getIMEI(mContext)));
		
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(finds, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		ResponseHandler<String> responseHandler = new BasicResponseHandler();

		try {

			responseString = httpClient.execute(httpPost, responseHandler);
			HashMap<String, Object> responseMap = (new ResponseParser(
					responseString)).parse();
			Log.i(TAG, "sendFinds.responseString: " + responseString);
			cleanupOnReceive(responseMap);
			Log.i(TAG, "sendFinds.responseMap: " + responseMap.toString());
			if (PositHttpUtils.isResponseOK(responseMap)) {
				int server_find_id = (Integer) responseMap.get("id");
				long rowId = c
						.getLong(c.getColumnIndexOrThrow(MyDBHelper.KEY_ID));
				MyDBHelper mDBHelper = new MyDBHelper(mContext);
				mDBHelper.setServerId(R.array.TABLE_FINDS, rowId,
						server_find_id);
			}
			
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}
	/**
	 * Get the finds from the server. The assumption here is that the server is sending
	 * data as 
	 * [0:[array],1:[array].......,status:(OK/Error)]
	 * TODO figure out the correct type casting
	 */
	/*public void receiveFinds() {
			HashMap<String, String> sendMap = new HashMap<String,String>();
			sendMap.put(KEY_APP_KEY,appKey);
			try {
				responseString = doHTTPPost(RECEIVE_FINDS_URL, sendMap);
			Log.i(TAG, "receiveFinds.responseString: " + responseString);
			HashMap<String, Object> responseMap = (new ResponseParser(
					responseString)).parse();
			
			if (PositHttpUtils.isResponseOK(responseMap)) {
				MyDBHelper mDBHelper = new MyDBHelper(mContext);
				JSONArray finds = (JSONArray) responseMap.get("finds");
				for (int i = 0; i < finds.length(); i++){
					HashMap<String,Object> rMap = new ResponseParser(
							finds.getString(i)).parse();
					cleanupOnReceive(rMap); //cleanup for received data
					mDBHelper.addRemoteFind(rMap);
				}

				mDBHelper.close();
			}
			Log.i(TAG, responseString);
			}  catch (JSONException e) {
				Log.e(TAG, e.getMessage());
			}
	}*/
/*
 * Cleanup procedures for sending and receiving. These will get more complicated over time.
 */
	/**
	 * cleanup the item key,value pairs so that we can send the data.
	 * @param sendMap
	 */
	private void cleanupOnSend(HashMap<String, String> sendMap) {
		sendMap.put("find_time", sendMap.get("time"));
		sendMap.remove("time");
		sendMap.put("projectId", 1+"");
		addRemoteIdentificationInfo(sendMap);
		latLongHack(sendMap);
	}
/**
 * Add the standard values to our request. We might as well use this as initializer for our 
 * requests.
 * 
 * @param sendMap
 */
private void addRemoteIdentificationInfo(HashMap<String, String> sendMap) {
	//sendMap.put(KEY_APP_KEY, appKey);
	sendMap.put(KEY_IMEI, Utils.getIMEI(mContext));
}
	private void latLongHack(HashMap<String, String> sendMap) {
		if (sendMap.get("latitude").toString().equals("")||
				sendMap.get("latitude").toString().equals(""))
			sendMap.put("latitude","-1");
		if (sendMap.get("longitude").toString().equals("")||
				sendMap.get("longitude").toString().equals(""))
			sendMap.put("longitude","-1");
			
	
	}

	/**
	 * cleanup the item key,value pairs so that we can receive and save to the internal database
	 * @param rMap
	 */
	public static void cleanupOnReceive(HashMap<String,Object> rMap){
		rMap.put(MyDBHelper.KEY_SYNCED,1);
		rMap.put("identifier", rMap.get("id"));
		rMap.put("sid", rMap.get("id")); //set the id from the server as sid
		rMap.remove("id");
		if (rMap.containsKey("add_time")) {
			rMap.put("time", rMap.get("add_time"));
			rMap.remove("add_time");
		}
		if (rMap.containsKey("images")) {
			rMap.put("imageUri", rMap.get("images"));
			rMap.remove("images");
		}
	}
	
	
	/**
	 * Sends a HttpPost request to the given URL. Any JSON 
	 * @param Uri the URL to send to/receive from
	 * @param sendMap the hashMap of data to send to the server as POST data
	 * @return the response from the URL
	 */
	private String doHTTPPost(String Uri, HashMap<String,String> sendMap) {
		if (Uri==null) throw new NullPointerException("The URL has to be passed");
		String responseString=null;
		HttpPost post = new HttpPost();

		HttpClient httpClient = new DefaultHttpClient();
		Log.i("doHTTPPost()","URI = "+Uri);
		try {
			post.setURI(new URI(Uri));
			Log.i("doHTTPPost()","POST URI = "+post.getURI().toString());
			Log.i("doHTTPPost()","GOT HERE 1");
		} catch (URISyntaxException e1) {
			Log.e(TAG, "URISyntaxException " + e1.getMessage());
		}
		List<NameValuePair> nvp = PositHttpUtils.getNameValuePairs(sendMap);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		Log.i("doHTTPPost()","GOT HERE 2");
		try {
			post.setEntity(new UrlEncodedFormEntity(nvp, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "UnsupportedEncodingException " + e.getMessage());
		}
		try {
			Log.i("doHTTPPost()","GOT HERE 4");
			responseString = httpClient.execute(post, responseHandler);
			Log.i("doHTTPPost()","GOT HERE 5");
		
		} catch (ClientProtocolException e) {
			Log.e(TAG, "ClientProtocolExcpetion" + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());	
		} 
		return responseString;
	}
	/**
	 * A wrapper(does some cleanup too) for sending HTTP GET requests to the URI 
	 * 
	 * @param Uri
	 * @return the request from the remote server
	 */
	private String doHTTPGET(String Uri)
	{
		if (Uri==null) throw new NullPointerException("The URL has to be passed");
		String responseString = null;
		HttpGet httpGet = new HttpGet();
		HttpClient httpClient = new DefaultHttpClient();
		Log.e(TAG, Uri);
		try{
			httpGet.setURI(new URI(Uri));
		}catch (URISyntaxException e1) {
			Log.e(TAG, e1.getMessage());
		}
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		try {
			responseString = httpClient.execute(httpGet, responseHandler);
		} catch (ClientProtocolException e) {
			Log.e(TAG, "ClientProtocolException" + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());	
		} 
		return responseString;
	}
	/**
	 * Get all the remote finds 
	 * @return a HashMap of the Id and Revision of all the finds in the server
	 */
	public List<HashMap<String,Object>> getAllRemoteFinds() {
		//this is a List of all of the finds
		//each find is represented by a HashMap
		List<HashMap<String,Object>> findsMap = new ArrayList<HashMap<String,Object>>();
		HashMap<String, String> sendMap = new HashMap<String,String>();
		addRemoteIdentificationInfo(sendMap);
		Log.i(TAG, "url = "+GET_FIND_IDS_URL);
		
		//responseString is the raw json string returned through php
		String responseString = doHTTPPost(GET_FIND_IDS_URL, sendMap);
		
		Log.i(TAG, "getAllRemoteFindsIds.responseString: " + responseString);
		try {
			//responseMap is a HashMap of 
			findsMap = (new ResponseParser(responseString).parse());
			Log.i("getAllRemoteFinds()","GOT HERE 1");
			Log.i("getAllRemoteFinds()","GOT HERE 2");
			Log.i("getAllRemoteFinds()",findsMap.size()+"");
			//findsMap = new ResponseParser(responseString).responseMapToList();
			/*JSONArray responseArray = new JSONArray(responseMap.get("find_ids").toString());
				if (responseArray.length()>0) {
					Log.i("getAllRemoteFinds()","GOT HERE 3");
					for (int i = 0; i < responseArray.length(); i++) {
						findsMap
						.add((new ResponseParser(responseArray.getString(i)).parse()));
					}
				}*/
			
		} catch (JSONException e) {
			Log.e(TAG, "JSONException" +  e.getMessage());
		} 
		return findsMap;
	}
	
	/**
	 * Get all the remote finds 
	 * @return a HashMap of the Id and Revision of all the finds in the server
	 */
	public List<HashMap<String,Object>> getAllRemoteImages() {
		//this is a List of all of the finds
		//each find is represented by a HashMap
		List<HashMap<String,Object>> findsMap = new ArrayList<HashMap<String,Object>>();
		HashMap<String, String> sendMap = new HashMap<String,String>();
		addRemoteIdentificationInfo(sendMap);
		Log.i(TAG, "url = "+GET_FIND_IDS_URL);
		
		//responseString is the raw json string returned through php
		String responseString = doHTTPPost(GET_FIND_IDS_URL, sendMap);
		
		Log.i(TAG, "getAllRemoteFindsIds.responseString: " + responseString);
		try {
			//responseMap is a HashMap of 
			findsMap = (new ResponseParser(responseString).parse());
			Log.i("getAllRemoteFinds()","GOT HERE 1");
			Log.i("getAllRemoteFinds()","GOT HERE 2");
			Log.i("getAllRemoteFinds()",findsMap.size()+"");
			//findsMap = new ResponseParser(responseString).responseMapToList();
			/*JSONArray responseArray = new JSONArray(responseMap.get("find_ids").toString());
				if (responseArray.length()>0) {
					Log.i("getAllRemoteFinds()","GOT HERE 3");
					for (int i = 0; i < responseArray.length(); i++) {
						findsMap
						.add((new ResponseParser(responseArray.getString(i)).parse()));
					}
				}*/
			
		} catch (JSONException e) {
			Log.e(TAG, "JSONException" +  e.getMessage());
		} 
		return findsMap;
	}
	/**
	 * Pull the remote find from the server using the id provided
	 * @param remoteFindId
	 * @return
	 */
	public ContentValues getRemoteFindById(int remoteFindId) {
		HashMap<String, String> sendMap = new HashMap<String,String>();
		addRemoteIdentificationInfo(sendMap);
		sendMap.put("id", remoteFindId+"");
		String responseString = doHTTPPost(RECEIVE_FINDS_URL+remoteFindId, sendMap);
		try {
			HashMap<String, Object> responseMap = (new ResponseParser(responseString).parse()).get(0);
			//JSONArray finds = new JSONArray(responseMap.get("finds").toString());
			/*if (finds.length()>0) {
				HashMap<String,Object> rMap = new ResponseParser(finds.getString(0)).parse().get(0);
				
				return MyDBHelper.getContentValuesFromMap(rMap);
			}*/
			responseMap.put("_id", remoteFindId+"");
			cleanupOnReceive(responseMap);
			return MyDBHelper.getContentValuesFromMap(responseMap);
		}catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		return null;
	}
	
}