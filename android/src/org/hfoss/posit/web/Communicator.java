/*
 * File: Communicator.java
 * 
 * Copyright (C) 2009 The Humanitarian FOSS Project (http://www.hfoss.org)
 * 
 * This file is part of POSIT, Portable Open Search and Identification Tool.
 *
 * POSIT is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) as published 
 * by the Free Software Foundation; either version 3.0 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU LGPL along with this program; 
 * if not visit http://www.gnu.org/licenses/lgpl.html.
 * 
 */
package org.hfoss.posit.web;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.hfoss.posit.Find;
import org.hfoss.posit.R;
import org.hfoss.posit.provider.PositDbHelper;
import org.hfoss.posit.utilities.Utils;
import org.hfoss.third.Base64Coder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * The communication module for POSIT.  Handles most calls to the server to get information regarding
 * projects and finds.
 * 
 * 
 */
public class Communicator {
	private static final String COLUMN_IMEI = "imei";

	/*
	 * You should be careful with putting names for server. DO NOT always trust
	 * DNS.
	 */

	public static final String RESULT_FAIL = "false";
	private static String server;
	private static String authKey;
	private static String imei;

	private static int projectId;

	private static String TAG = "Communicator";
	private String responseString;
	private Context mContext;
	private SharedPreferences applicationPreferences;
	private HttpParams mHttpParams;
	private HttpClient mHttpClient;
	private ThreadSafeClientConnManager mConnectionManager;
	public static long mTotalTime = 0;
	private long mStart = 0;

	public Communicator(Context _context) {
		mContext = _context;
		mTotalTime = 0;
		mStart = 0;
		
		mHttpParams = new BasicHttpParams();
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", new PlainSocketFactory(), 80));
		mConnectionManager = new ThreadSafeClientConnManager(mHttpParams, registry);
		mHttpClient = new DefaultHttpClient(mConnectionManager, mHttpParams);

		PreferenceManager.setDefaultValues(mContext, R.xml.posit_preferences, false);
		applicationPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		setApplicationAttributes(applicationPreferences.getString("AUTHKEY", ""), 
				applicationPreferences.getString("SERVER_ADDRESS", server), 
				applicationPreferences.getInt("PROJECT_ID", projectId));
		TelephonyManager manager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		imei = manager.getDeviceId();

	}

	private void setApplicationAttributes(String aKey, String serverAddress, int projId){
		authKey = aKey;
		server = serverAddress;
		projectId = projId;
	}

	
	
	/**
	 * NOTE: Calls doHTTPGet
	 * 
	 * Get all open projects from the server.  Eventually, the goal is to be able to get different types
	 * of projects depending on the privileges of the user.
	 * @return a list of all the projects and their information, encoded as maps
	 * @throws JSONException 
	 */
	public ArrayList<HashMap<String,Object>> getProjects(){
		Log.i(TAG,"authkey="+authKey);
		if (authKey.equals("")) {
			Log.e(TAG, "getProjects() authKey == ");
			Utils.showToast(mContext, "Aborting Communicator:\nPhone does not have a valid authKey."
					+ "\nUse settings menu to register phone.");
			return null;
		}  
		String url = server + "/api/listOpenProjects?authKey=" + authKey;
		ArrayList<HashMap<String, Object>> list;
		responseString = doHTTPGET(url);
		if(Utils.debug)
			Log.i(TAG, responseString);
			list = new ArrayList<HashMap<String, Object>>();
			try {
				list = (ArrayList<HashMap<String, Object>>) (new ResponseParser(responseString).parse());
			} catch (JSONException e1) {
				Log.i(TAG, "getProjects JSON exception " + e1.getMessage());
				e1.printStackTrace();
				return null;
			}
		return list;
	}

	/**
	 * Registers the phone being used with the given server address, the authentication key,
	 * and the phone's imei
	 * 
	 * @param server 
	 * @param authKey
	 * @param imei
	 * @return whether the registration was successful
	 */
	public boolean registerDevice(String server, String authKey, String imei){
		  // server = "http://192.168.1.105/posit";
		String url = server + "/api/registerDevice?authKey=" +authKey 
		+ "&imei=" + imei;
		Log.i(TAG, "registerDevice URL=" + url);

		try {
			responseString = doHTTPGET(url);
		} catch (Exception e) {
			Utils.showToast(mContext, e.getMessage());
		}

		if (responseString.equals(RESULT_FAIL))
			return false;
		else return true;
	}
	
	/*
	 * TODO: This method is a little long and could be split up.
	 * Send one find to the server, including its images.
	 * @param find a reference to the Find object
	 * @param action -- either  'create' or 'update'
	 */
	public boolean sendFind (Find find, String action) {
		boolean success = false;
		String url;
		HashMap<String, String> sendMap = find.getContentMapGuid();
		//Log.i(TAG, "sendFind map = " + sendMap.toString());
		cleanupOnSend(sendMap);
		sendMap.put("imei", imei);
		String guid = sendMap.get(PositDbHelper.FINDS_GUID);

		// Create the url
		
		if (action.equals("create")) {
			url = server +"/api/createFind?authKey="+authKey;
		} else {
			url = server +"/api/updateFind?authKey="+authKey;
		}
		if(Utils.debug) {
			Log.i(TAG,"SendFind=" + sendMap.toString());			
		}
	
		// Send the find
		try {
			responseString = doHTTPPost(url, sendMap);
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
			Utils.showToast(mContext, e.getMessage());
		}
		if(Utils.debug)
			Log.i(TAG, "sendFind.ResponseString: " + responseString);

		// If the update failed return false
		if (responseString.indexOf("True") == -1) {
			Log.i(TAG, "sendFind result doesn't contain 'True'");
			return false;
		} else {
			PositDbHelper dbh = new PositDbHelper(mContext);
			long id = find.getId();
			success = dbh.markFindSynced(id);
			if (Utils.debug) Log.i(TAG, "sendfind synced " + id + " " + success);
		}
		
		if (!success)
			return false;
		
		// Otherwise send the Find's images
		
		long id = Long.parseLong(sendMap.get(PositDbHelper.FINDS_ID));
		PositDbHelper dbh = new PositDbHelper(mContext);
		ArrayList<ContentValues> photosList = dbh.getImagesListSinceUpdate(id);
		
		Log.i(TAG, "sendFind, photosList=" + photosList.toString());
		
		Iterator<ContentValues> it = photosList.listIterator();
		while (it.hasNext()) {
			ContentValues imageData = it.next();
			Uri uri = Uri.parse(imageData.getAsString(PositDbHelper.PHOTOS_IMAGE_URI));
			String base64Data = convertUriToBase64(uri);
			uri = Uri.parse(imageData.getAsString(PositDbHelper.PHOTOS_THUMBNAIL_URI));
			String base64Thumbnail = convertUriToBase64(uri);
			sendMap = new HashMap<String, String>();
			sendMap.put(COLUMN_IMEI, Utils.getIMEI(mContext));
			sendMap.put(PositDbHelper.FINDS_GUID, guid);

			sendMap.put(PositDbHelper.PHOTOS_IDENTIFIER, 
					imageData.getAsString(PositDbHelper.PHOTOS_IDENTIFIER));
			sendMap.put(PositDbHelper.FINDS_PROJECT_ID, 
					imageData.getAsString(PositDbHelper.FINDS_PROJECT_ID));
			sendMap.put(PositDbHelper.FINDS_TIME, 
					imageData.getAsString(PositDbHelper.FINDS_TIME));
			sendMap.put(PositDbHelper.PHOTOS_MIME_TYPE, 
					imageData.getAsString(PositDbHelper.PHOTOS_MIME_TYPE));
			
			sendMap.put("mime_type", "image/jpeg");
			
			sendMap.put(PositDbHelper.PHOTOS_DATA_FULL, 
					base64Data);
			sendMap.put(PositDbHelper.PHOTOS_DATA_THUMBNAIL, 
					base64Thumbnail);
			sendMedia(sendMap);
			//it.next();
		}
		
		// Update the Synced attribute.
		return true;
	}

	/**
	 * Sends an image (or sound file or video) to the server.
	 * @param identifier
	 * @param findId  the guid of the associated find
	 * @param data
	 * @param mimeType
	 */		
	public void sendMedia(HashMap<String, String> sendMap) {
		Log.i(TAG, "sendMedia, sendMap= " + sendMap);

		String url = server + "/api/attachPicture?authKey=" +authKey;

		responseString = doHTTPPost(url, sendMap);
		if(Utils.debug)
			Log.i(TAG, "sendImage.ResponseString: " + responseString);
	}
	
	/**
	 * Converts a uri to a base64 encoded String for transmission to server.
	 * @param uri
	 * @return
	 */
	private String convertUriToBase64(Uri uri) {
		ByteArrayOutputStream imageByteStream= new ByteArrayOutputStream();
		byte[] imageByteArray = null;
		Bitmap bitmap = null;

		try {
			bitmap = android.provider.MediaStore.Images.Media.getBitmap
			(mContext.getContentResolver(), uri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}			

		if (bitmap == null) {
			Log.d(TAG, "No bitmap");
		}
		// Compress bmp to jpg, write to the byte output stream
		bitmap.compress(Bitmap.CompressFormat.JPEG, 80, imageByteStream);
		// Turn the byte stream into a byte array
		imageByteArray = imageByteStream.toByteArray();
		char[] base64 = Base64Coder.encode(imageByteArray);
		String base64String = new String(base64);
		return base64String;
	}
		

	/**
	 * cleanup the item key,value pairs so that we can send the data.
	 * @param sendMap
	 */
	private void cleanupOnSend(HashMap<String, String> sendMap) {
		addRemoteIdentificationInfo(sendMap);
	}
	
	/**
	 * Add the standard values to our request. We might as well use this as initializer for our 
	 * requests.
	 * 
	 * @param sendMap
	 */
	private void addRemoteIdentificationInfo(HashMap<String, String> sendMap) {
		//sendMap.put(COLUMN_APP_KEY, appKey);
		sendMap.put(COLUMN_IMEI, Utils.getIMEI(mContext));
	}

	/**
	 * cleanup the item key,value pairs so that we can receive and save to the internal database
	 * @param rMap
	 */
	public static void cleanupOnReceive(HashMap<String,Object> rMap){
		rMap.put(PositDbHelper.FINDS_SYNCED,PositDbHelper.FIND_IS_SYNCED);
		rMap.put(PositDbHelper.FINDS_GUID, rMap.get("barcode_id"));		
		//rMap.put(PositDbHelper.FINDS_GUID, rMap.get("barcode_id"));

		rMap.put(PositDbHelper.FINDS_PROJECT_ID, projectId);
		if (rMap.containsKey("add_time")) {
			rMap.put(PositDbHelper.FINDS_TIME, rMap.get("add_time"));
			rMap.remove("add_time");
		}
		if (rMap.containsKey("images")) {
			if(Utils.debug)
				Log.d(TAG, "contains image key");
			rMap.put(PositDbHelper.PHOTOS_IMAGE_URI, rMap.get("images"));
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
		if(Utils.debug)
			Log.i(TAG, "doHTTPPost() URI = "+Uri);
		try {
			post.setURI(new URI(Uri));
		} catch (URISyntaxException e) {
			Log.e(TAG, "URISyntaxException " + e.getMessage());
			e.printStackTrace();
			return e.getMessage();
		}
		List<NameValuePair> nvp = PositHttpUtils.getNameValuePairs(sendMap);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		try {
			post.setEntity(new UrlEncodedFormEntity(nvp, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "UnsupportedEncodingException " + e.getMessage());
		}
		mStart = System.currentTimeMillis();

		try {
			responseString = mHttpClient.execute(post, responseHandler);
		} catch (ClientProtocolException e) {
				Log.e(TAG, "ClientProtocolExcpetion" + e.getMessage());
				e.printStackTrace();
				return e.getMessage();
		} catch (IOException e) {
				Log.e(TAG, "IOException " + e.getMessage());	
				e.printStackTrace();
				return e.getMessage();
		} catch (IllegalStateException e) {
				Log.e(TAG, "IllegalStateException: "+ e.getMessage());
				e.printStackTrace();
				return e.getMessage();
		} catch (Exception e) {
				Log.e(TAG, "Exception on HttpPost " + e.getMessage());
				e.printStackTrace();
				return e.getMessage();
		}
		long time = System.currentTimeMillis()-mStart;
		mTotalTime += time;
		Log.i(TAG, "TIME = "+time + " millisecs");
		
		return responseString;
	}
	/**
	 * A wrapper(does some cleanup too) for sending HTTP GET requests to the URI 
	 * 
	 * @param Uri
	 * @return the request from the remote server
	 */
	public String doHTTPGET(String Uri) {
		if (Uri==null) throw new NullPointerException("The URL has to be passed");
		String responseString = null;
		HttpGet httpGet = new HttpGet();
		
		try{
			httpGet.setURI(new URI(Uri));
		}catch (URISyntaxException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
			return "[Error]" + e.getMessage();
		}
		if (Utils.debug){
			Log.i(TAG, "doHTTPGet Uri = " + Uri);
		}
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		mStart = System.currentTimeMillis();

		try {
			responseString = mHttpClient.execute(httpGet, responseHandler);
		} catch (ClientProtocolException e) {
				Log.e(TAG, "ClientProtocolException" + e.getMessage());
				e.printStackTrace();
				return "[Error]" + e.getMessage();
		} catch (IOException e) {
				Log.e(TAG, e.getMessage());	
				e.printStackTrace();
				return "[Error]" + e.getMessage();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
			return "[Error]" + e.getMessage();
		}
		
		long time = System.currentTimeMillis()-mStart;
		mTotalTime += time;
		Log.i(TAG, "TIME = "+ time + " millisecs");

		
		if(Utils.debug)
			Log.i(TAG, "doHTTPGet Response: "+ responseString);
		return responseString;
	}
	
 
	/**
	 * Pull the remote find from the server using the guid provided.
	 * @param guid, a globally unique identifier
	 * @return an associative list of attribute/value pairs
	 */
	public ContentValues getRemoteFindById(String guid) {
		String url = server +"/api/getFind?guid=" +guid +"&authKey=" +authKey;
		HashMap<String, String> sendMap = new HashMap<String,String>();
		addRemoteIdentificationInfo(sendMap);
		sendMap.put("guid", guid+"");
		String responseString = doHTTPPost(url, sendMap);
		ContentValues cv = new ContentValues();
    
		Log.i(TAG,"getRemoteFindById = " + responseString);
		try {
			JSONObject jobj = new JSONObject(responseString);
			cv.put(PositDbHelper.FINDS_GUID, jobj.getString("barcode_id"));
			cv.put(PositDbHelper.FINDS_PROJECT_ID, jobj.getInt("project_id"));
			cv.put(PositDbHelper.FINDS_NAME, jobj.getString("name"));
			cv.put(PositDbHelper.FINDS_DESCRIPTION, jobj.getString("description"));
			cv.put(PositDbHelper.FINDS_TIME, jobj.getString("add_time"));
			cv.put(PositDbHelper.FINDS_TIME, jobj.getString("modify_time"));
			cv.put(PositDbHelper.FINDS_LATITUDE, jobj.getDouble("latitude"));
			cv.put(PositDbHelper.FINDS_LONGITUDE, jobj.getDouble("longitude"));
			cv.put(PositDbHelper.FINDS_REVISION,jobj.getInt("revision"));
			return cv;
		} catch (JSONException e) {
			Log.i(TAG, e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Get an image from the server using the guid as Key.
	 * @param guid the Find's globally unique Id
	 */
	public ArrayList<HashMap<String, String>> getRemoteFindImages(String guid) {
		ArrayList<HashMap<String, String>> imagesMap = null;
//		ArrayList<HashMap<String, String>> imagesMap = null;
		String imageUrl = server +"/api/getPicturesByFind?findId=" + guid + "&authKey=" +authKey;
		HashMap<String, String> sendMap = new HashMap<String,String>();
		Log.i(TAG, "getRemoteFindImages, sendMap=" + sendMap.toString());
		sendMap.put(PositDbHelper.FINDS_GUID, guid);
		addRemoteIdentificationInfo(sendMap);
		try {
			String imageResponseString = doHTTPPost(imageUrl, sendMap);
			Log.i(TAG, "getRemoteFindImages, response=" + imageResponseString);

			if(!imageResponseString.equals(RESULT_FAIL)) {
				JSONArray jsonArr = new JSONArray(imageResponseString);
				imagesMap = new ArrayList<HashMap<String, String>>();
//				imagesMap = new ArrayList<HashMap<String, String>>();

				for(int i = 0; i < jsonArr.length(); i++) {
					JSONObject jsonObj = jsonArr.getJSONObject(i);
					if(Utils.debug)
						Log.i(TAG, "JSON Image Response String: " + jsonObj.toString());
//					imagesMap.add((HashMap<String, String>) jsonArr.get(i));
					Iterator<String> iterKeys = jsonObj.keys();
					HashMap<String,String>map = new HashMap<String,String>();
					while(iterKeys.hasNext()) {
						String key = iterKeys.next();
						map.put(key, jsonObj.getString(key));
					}
					imagesMap.add(map); 
				}
			}
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
			e.printStackTrace();
		}	
		if (imagesMap != null && Utils.debug)
			Log.i(TAG, "getRemoteFindImages, imagesMap=" + imagesMap.toString());
		else 
			Log.i(TAG, "getRemoteFindImages, imagesMap= null");
		return imagesMap;
	}
	

	/**
	 * Checks if a given image already exists on the server.  Allows for quicker syncing to the server,
	 * as this allows the application to bypass converting from a bitmap to base64 to send to the server
	 * 
	 * @param imageId the id of the image to query
	 * @return whether the image already exists on the server
	 */
	public boolean imageExistsOnServer(int imageId) {
		HashMap<String, String> sendMap = new HashMap<String,String>();
		addRemoteIdentificationInfo(sendMap);
		String imageUrl = server +"/api/getPicture?id=" + imageId + "&authKey=" +authKey;
		String imageResponseString = doHTTPPost(imageUrl, sendMap);
		if (imageResponseString.equals(RESULT_FAIL))
			return false;
		else return true;
	}

	public String registerExpeditionPoint(double lat, double lng, int expedition) {
		String result = doHTTPGET(server+"/api/addExpeditionPoint?authKey="+authKey+"&lat="+lat+"&lng="+lng+"&expedition="+expedition);
		return result;
	}	

	public String registerExpeditionPoint(double lat, double lng,  double alt, long swath, int expedition) {
		if (Utils.debug) Log.i(TAG, "registerExpeditionPoint " + lat + " " + lng);
		HashMap<String, String> sendMap  = new HashMap<String,String>();
		addRemoteIdentificationInfo(sendMap);
		String addExpeditionUrl = server+"/api/addExpeditionPoint?authKey="+authKey;
		sendMap.put("lat", ""+lat );
		sendMap.put("lng", lng+"");
		sendMap.put("alt", ""+alt);
		sendMap.put("swath", ""+swath);
		sendMap.put("expeditionId", expedition+"");
		String addExpeditionResponseString = doHTTPPost(addExpeditionUrl, sendMap);
		if (Utils.debug){
			Log.i(TAG, "response: " + addExpeditionResponseString);
		}
		return addExpeditionResponseString;
	}

	public int registerExpeditionId(int projectId){
		HashMap<String, String> sendMap  = new HashMap<String,String>();
		addRemoteIdentificationInfo(sendMap);
		String addExpeditionUrl = server+"/api/addExpedition?authKey="+authKey;
		sendMap.put("projectId", ""+projectId );
		String addExpeditionResponseString = doHTTPPost(addExpeditionUrl, sendMap);
		if (Utils.debug){
			Log.i(TAG, "registerExpeditionId, response: " + addExpeditionResponseString);
		}
		try {
			Integer i = Integer.parseInt(addExpeditionResponseString);
			return i;
		}catch (NumberFormatException e ){
			Log.e(TAG, "Invalid response received");
			return -1;
		}
	}
}