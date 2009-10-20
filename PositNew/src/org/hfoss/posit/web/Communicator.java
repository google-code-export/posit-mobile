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
import org.hfoss.posit.DBHelper;
import org.hfoss.posit.Find;
import org.hfoss.posit.R;
import org.hfoss.posit.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
	/*
	 * You should be careful with putting names for server. DO NOT always trust
	 * DNS.
	 */
	private static String SERVER = "http://dev.posit-project.org";
	private static String SEND_FIND_URL = SERVER + "/finds.php?q=save";
	private static String PROJECTS_LIST_URL = SERVER + "/projectslist_phone.php";
	private static String RECEIVE_FINDS_URL = SERVER
			+ "finds.php?q=get_finds";
	private static String REGISTER_DEVICE_URL = SERVER +"/registerdevice.php";
	private static String GET_FIND_IDS_URL = SERVER+"/finds.php?q=get_ids";
	private static String UPDATE_FIND_URL = SERVER+"/finds.php?q=edit";
	private static String APP_KEY = "f8c31e98eae272b17f08498d99497184";
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
		setApplicationAttributes(applicationPreferences.getString("APP_KEY", APP_KEY), 
				applicationPreferences.getString("SERVER_ADDRESS", SERVER));
	}
	
	private void setApplicationAttributes(String appKey, String serverAddress){
		APP_KEY = appKey;
		SERVER = serverAddress;
		setFindAndReceiveAdresses(SERVER);
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

	public ArrayList<HashMap<String,Object>> getProjects(){
		responseString = doHTTPGET(PROJECTS_LIST_URL);
		ArrayList<HashMap<String,Object>> list= new ArrayList<HashMap<String,Object>>();
		try {
			JSONArray j = new JSONArray(responseString);
			for (int i= 0; i < j.length(); i++){
				list.add((new ResponseParser(j.getString(i)).parse()));
			}
			
			Log.i(TAG, j.toString(1));
		} catch (JSONException e) {
			Log.e(TAG,e.getMessage());
			
		}
		return list;
	}
	
	public boolean registerDevice(String appKey, String imei, String deviceName){
	
		HashMap<String, String> sendMap= new HashMap<String, String>();
		sendMap.put("app_key", appKey);
		sendMap.put("imei", imei);
		sendMap.put("name",deviceName);
		responseString = doHTTPPost(REGISTER_DEVICE_URL, sendMap);
		Log.i(TAG,responseString);
		HashMap<String, Object> responseMap = null;
		try {
			 responseMap = (new ResponseParser(responseString)).parse();
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
		}
		return PositHttpUtils.isResponseOK(responseMap);
	}
	/*
	 * Set the URL for sending the request
	 */
	private void setURI(String Uri) {
		try {
			httpPost.setURI(new URI(Uri));
		} catch (URISyntaxException e1) {
			Log.e(TAG, e1.getMessage());
		}
	}
	/*
	 * Send one find to the server.
	 */
	public void sendFind (Find find) {
		setURI(SEND_FIND_URL);
		HashMap<String, String> sendMap = find.getContentMap();
		cleanupOnSend(sendMap);
		responseString = doHTTPPost(SEND_FIND_URL, sendMap);
		Log.i(TAG, responseString);
		try {
			HashMap<String, Object> responseMap = (new ResponseParser(
					responseString)).parse();
			cleanupOnReceive(responseMap);
			Log.i(TAG, responseMap.toString());
			if (PositHttpUtils.isResponseOK(responseMap)){
				find.setSyncStatus(true);
				find.setServerId((Integer)responseMap.get(DBHelper.KEY_SID));
				find.setRevision((Integer)responseMap.get(DBHelper.KEY_REVISION));
			}
				
		} catch (JSONException e) {
			Log.e(TAG,e.getMessage());
		}catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	/*
	 * Send one find to the server.
	 */
	public void updateFind (Find find) {
		setURI(UPDATE_FIND_URL);
		HashMap<String, String> sendMap = find.getContentMap();
		cleanupOnSend(sendMap);
		responseString = doHTTPPost(UPDATE_FIND_URL, sendMap);
		Log.i(TAG, responseString);
		try {
			HashMap<String, Object> responseMap = (new ResponseParser(
					responseString)).parse();
			cleanupOnReceive(responseMap);
			Log.i(TAG, responseMap.toString());
			if (PositHttpUtils.isResponseOK(responseMap)){
				find.setSyncStatus(true);
				find.setServerId((Integer)responseMap.get(DBHelper.KEY_SID));
				find.setRevision((Integer)responseMap.get(DBHelper.KEY_REVISION));
			}
				
		} catch (JSONException e) {
			Log.e(TAG,e.getMessage());
		}catch (Exception e ) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	/*
	 * Send the finds to the designated server
	 */
	public void sendFinds(String[] projection, Cursor c) {
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
		finds.add(new BasicNameValuePair("appkey", APP_KEY));
		finds.add(new BasicNameValuePair("imei", Utils.getIMEI(mContext)));
		
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
			Log.i(TAG, responseString);
			if (PositHttpUtils.isResponseOK(responseMap)) {
				int server_find_id = (Integer) responseMap.get("id");
				long rowId = c
						.getLong(c.getColumnIndexOrThrow(DBHelper.KEY_ID));
				DBHelper mDBHelper = new DBHelper(mContext);
				mDBHelper.open();
				mDBHelper.setServerId(R.array.TABLE_FINDS, rowId,
						server_find_id);
				mDBHelper.close();
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
	public void receiveFinds() {
			HashMap<String, String> sendMap = new HashMap<String,String>();
			sendMap.put("appkey",APP_KEY);
			try {
				responseString = doHTTPPost(RECEIVE_FINDS_URL, sendMap);
			Log.i(TAG, responseString);
			HashMap<String, Object> responseMap = (new ResponseParser(
					responseString)).parse();
			
			if (PositHttpUtils.isResponseOK(responseMap)) {
				DBHelper mDBHelper = new DBHelper(mContext);
				mDBHelper.open();
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
	}
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
	sendMap.put("appkey", APP_KEY);
	sendMap.put("imei", Utils.getIMEI(mContext));
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
	private void cleanupOnReceive(HashMap<String,Object> rMap){
		rMap.put("sid", rMap.get("id")); //set the id from the server as sid
		rMap.remove("id");
		rMap.put("time", rMap.get("find_time"));
		rMap.remove("find_time");
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

		HttpClient httpClient = new DefaultHttpClient();;
		try {
			post.setURI(new URI(Uri));
		} catch (URISyntaxException e1) {
			Log.e(TAG, e1.getMessage());
		}
		List<NameValuePair> nvp = PositHttpUtils.getNameValuePairs(sendMap);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		try {
			post.setEntity(new UrlEncodedFormEntity(nvp, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.getMessage());
		}
		try {
			responseString = httpClient.execute(post, responseHandler);
		
		
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.getMessage());
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
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());	
		} 
		return responseString;
	}
	/**
	 * Get all the remote finds 
	 * @return a HashMap of the Id and Revision of all the finds in the server
	 */
	public List<HashMap<String,Object>> getAllRemoteFindsIds() {
		List<HashMap<String,Object>> findsMap = new ArrayList<HashMap<String,Object>>();
		HashMap<String, String> sendMap = new HashMap<String,String>();
		addRemoteIdentificationInfo(sendMap);
		String responseString = doHTTPPost(GET_FIND_IDS_URL, sendMap);
		Log.i(TAG, responseString);
		try {
			HashMap<String, Object> responseMap = (new ResponseParser(
					responseString).parse());
			if (PositHttpUtils.isResponseOK(responseMap)) {
				JSONArray responseArray = new JSONArray(responseMap.get("find_ids").toString());
				for (int i = 0; i < responseArray.length(); i++) {
					findsMap
						.add((new ResponseParser(responseArray.getString(i)).parse()));
				}
			}
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
		} 
		return findsMap;
	}
}