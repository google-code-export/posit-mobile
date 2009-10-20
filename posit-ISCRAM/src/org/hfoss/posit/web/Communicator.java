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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.hfoss.posit.DBHelper;
import org.hfoss.posit.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
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
	private static final String SERVER = "http://dev.posit-project.org/";
	private static final String SAVE_FINDS_URL = SERVER + "finds.php?q=save";
	private static final String RECEIVE_FINDS_URL = SERVER
			+ "finds.php?q=get_finds";
	private static final String APP_KEY = "f8c31e98eae272b17f08498d99497184";
	private HttpClient httpClient = new DefaultHttpClient();;
	private HttpPost httpPost = new HttpPost();
	private String TAG = "Communicator";
	private String responseString;

	public Communicator() {

	}

	private void setURI(String Uri) {
		try {
			httpPost.setURI(new URI(Uri));
		} catch (URISyntaxException e1) {
			Log.e(TAG, e1.getMessage());
		}
	}

	public void sendFinds(Context mContext, String[] projection, Cursor c) {
		if (c.getCount() <= 0)
			return;
		List<NameValuePair> finds = PositHttpUtils.convertFindsForPost(projection, c);
		setURI(SAVE_FINDS_URL);

		for (NameValuePair nameValuePair : finds) {
			if (nameValuePair.getName().equals("time")) {
				String value = nameValuePair.getValue();
				finds.add(new BasicNameValuePair("find_time", value));
				finds.remove(nameValuePair);
			}
		}
		finds.remove(0);// removing _id so that we are not sending unecessary
						// data to server.
		finds.add(new BasicNameValuePair("appkey", APP_KEY));
		finds.add(new BasicNameValuePair("imei", 100 + ""));
		
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
	 * @param mContext
	 */
	public void receiveFinds(Context mContext) {
			HashMap<String, String> sendMap = new HashMap<String,String>();
			sendMap.put("appkey",APP_KEY);
			try {
				responseString = doHTTPPost(mContext, RECEIVE_FINDS_URL, sendMap);
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
	 * @param rMap
	 */
	private void cleanupOnSend(HashMap<String,Object> rMap) {
		rMap.put("find_time", rMap.get("time"));
		rMap.remove("time");
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
	 * @param mContext the application
	 * @param Uri the URL to send to/receive from
	 * @param sendMap the hashMap of data to send to the server as POST data
	 * @return the response from the URL
	 */
	private String doHTTPPost(Context mContext, String Uri, HashMap<String,String> sendMap) {
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
			responseString = httpClient.execute(httpPost, responseHandler);
		
		
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());	
		} 
		return responseString;
	}
}