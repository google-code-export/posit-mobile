package org.hfoss.posit.web;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import org.apache.http.protocol.HTTP;
import org.hfoss.posit.Find;
import org.hfoss.posit.MyDBHelper;
import org.hfoss.posit.R;
import org.hfoss.posit.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * The communication module for POSIT.  Handles most calls to the server to get information regarding
 * projects and finds.
 * 
 * @author Prasanna Gautam
 * @author Chris Fei
 * @author Qianqian Lin
 * 
 */
public class Communicator {
	private static final String COLUMN_IMEI = "imei";
	/*
	 * You should be careful with putting names for server. DO NOT always trust
	 * DNS.
	 */

	private static String server;
	private static String authKey;

	private static int projectId;

	private static String TAG = "Communicator";
	private String responseString;
	private Context mContext;
	private SharedPreferences applicationPreferences;

	public Communicator(Context _context) {
		mContext = _context;
		PreferenceManager.setDefaultValues(mContext, R.xml.posit_preferences, false);
		applicationPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		setApplicationAttributes(applicationPreferences.getString("AUTHKEY", ""), 
				applicationPreferences.getString("SERVER_ADDRESS", server), 
				applicationPreferences.getInt("PROJECT_ID", projectId));
		Log.i(TAG, "authkey=" +authKey+", server="+server);

	}

	private void setApplicationAttributes(String aKey, String serverAddress, int projId){
		authKey = aKey;
		server = serverAddress;
		projectId = projId;
	}

	/**
	 * Get all open projects from the server.  Eventually, the goal is to be able to get different types
	 * of projects depending on the privileges of the user.
	 * @return a list of all the projects and their information, encoded as maps
	 * @throws JSONException 
	 */
	public ArrayList<HashMap<String,Object>> getProjects(){
		String url = server + "/api/listOpenProjects?authKey=" + authKey;
		responseString = doHTTPGET(url);
		Log.i(TAG, responseString);

		ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		try {
			list = (ArrayList<HashMap<String, Object>>) (new ResponseParser(responseString).parse());
		} catch (JSONException e1) {
			e1.printStackTrace();
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
		String url = server + "/api/registerDevice?authKey=" +authKey 
		+ "&imei=" + imei;

		responseString = doHTTPGET(url);
		Log.i(TAG,"Registerdevice: " + responseString);

		if (responseString.equals("false"))
			return false;
		else return true;
	}
	/*
	 * Send one find to the server.
	 */
	public void sendFind (Find find) {
		HashMap<String, String> sendMap = find.getContentMap();
		cleanupOnSend(sendMap);
		String url = server +"/api/createFind?authKey="+authKey;
		Log.i("FIND STATS",sendMap.get("_id"));
		Log.i("FIND STATS",sendMap.get("name"));
		Log.i("FIND STATS",sendMap.get("description"));
		Log.i("FIND STATS",sendMap.get("latitude"));
		Log.i("FIND STATS",sendMap.get("longitude"));
		Log.i("FIND STATS",sendMap.get("projectId"));
		Log.i("FIND STATS",sendMap.get("revision"));
		//SEND_FIND_URL += "&id="+sendMap.get("_id")
		responseString = doHTTPPost(url, sendMap);
		Log.i(TAG, "sendFind.ResponseString: " + responseString);
		try {
			//this is for easiness, it would be more efficient to do in one query

			//find.setServerId(1);
			//find.setRevision(Integer.parseInt(responseMap.get(MyDBHelper.COLUMN_REVISION).toString()));
			//find.setSyncStatus(true);
			ContentValues cv = new ContentValues();
			cv.put("synced", "1");
			cv.put("sid", sendMap.get("identifier"));
			find.updateToDB(cv);
			//find.delete();

		}
		catch (Exception e) {
			Log.e(TAG, e.getStackTrace().toString());
		}
	}

	public void sendMedia(int identifier, int findId, String data, String mimeType) {
		HashMap<String, String> sendMap = new HashMap<String, String>();
		String url = null;

		if (mimeType == "image/jpeg") {
			url = server + "/api/attachPicture?authKey=" +authKey;
			sendMap.put("id", ""+identifier);
			sendMap.put("findId", ""+findId);
			sendMap.put("dataFull", data);
			sendMap.put("mimeType", mimeType);

			responseString = doHTTPPost(url, sendMap);
			Log.i(TAG, "sendImage.ResponseString: " + responseString);
		}
	}

	/**
	 * Function to send any media file [audio, video, image] to the server at [server + /api/upload.php].
	 * @param identifier
	 * @param findId
	 * @param mUri
	 * @param mimeType
	 */
	public void sendMediaFileToServer(int identifier, int findId, Uri mUri, String mimeType) {
		String filename = Utils.getFilenameFromMediaUri(mContext, mUri);
		String uploadUrl = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		String contentDisposition = "Content-Disposition: form-data; ";
		int ch;

		File file;
		FileInputStream fileInputStream;
		URL mUrl;

		if (mimeType == "video/3gpp") {
			uploadUrl = server + "/api/attachVideo?authKey=" + authKey;
		} else if (mimeType == "audio/3gpp") {
			uploadUrl = server + "/api/attachAudio?authKey=" + authKey;
		} else {
			return;
		}

		// string to establish form data; name=\"file\" cannot change, as this is important for server recognition of the file
		String dosFile = contentDisposition + "name=\"file\"; filename=\"" + filename +"\"" + lineEnd;
		String dosIdent = contentDisposition + "type=\"text\"; name=\"id\";" + lineEnd;
		String dosFindId = contentDisposition + "type=\"text\"; name=\"findId\";" + lineEnd;
		String dosMimeType = contentDisposition + "type=\"text\"; name=\"mimeType\";" + lineEnd;

		if (filename == null) { return; }

		file = new File(filename);

		try {
			fileInputStream = new FileInputStream(file);
			mUrl = new URL(uploadUrl);

			// establish connection and set appropriate variables
			HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

			DataOutputStream dos = new DataOutputStream( conn.getOutputStream() );

			// begin form write
			dos.writeBytes(twoHyphens + boundary + lineEnd);

			// begin file write
			dos.writeBytes(dosFile);
			dos.writeBytes(lineEnd);

			// create a buffer of maximum size
			int bytesAvailable = fileInputStream.available();
			int maxBufferSize = 1024;
			int bufferSize = Math.min(bytesAvailable, maxBufferSize);
			byte[] buffer = new byte[bufferSize];

			// read file from file input stream
			int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			// write file to data output stream
			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			//end file write

			//send related information
			writeFormData(dos, dosIdent, String.valueOf(identifier));	//identifier
			writeFormData(dos, dosFindId, String.valueOf(findId));		//find id
			writeFormData(dos, dosMimeType, mimeType);

			//end form write
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// close streams
			fileInputStream.close();
			dos.flush();

			// retrieve the response from server
			InputStream is = conn.getInputStream();

			StringBuffer b = new StringBuffer();
			while( ( ch = is.read() ) != -1 ) {
				b.append( (char) ch );
			}
			dos.close();
			Log.i(TAG, "Response String: " + b);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch(Exception e) {
			Log.e(TAG, e.toString());
			return;
		}
	}

	private void writeFormData(DataOutputStream dos, String dosString, String dataOut) throws IOException {
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";

		dos.writeBytes(dosString);
		dos.writeBytes(lineEnd);
		dos.writeBytes(dataOut);
		dos.writeBytes(lineEnd);
		dos.writeBytes(twoHyphens + boundary + lineEnd);
	}

	public void updateFind (Find find) {
		HashMap<String, String> sendMap = find.getContentMap();
		cleanupOnSend(sendMap);
		String url = server +"/api/updateFind?authKey="+authKey;
		Log.i("FIND STATS",sendMap.get("_id"));
		Log.i("FIND STATS",sendMap.get("name"));
		Log.i("FIND STATS",sendMap.get("description"));
		Log.i("FIND STATS",sendMap.get("latitude"));
		Log.i("FIND STATS",sendMap.get("longitude"));
		Log.i("FIND STATS",sendMap.get("projectId"));
		Log.i("FIND STATS",sendMap.get("revision"));
		//SEND_FIND_URL += "&id="+sendMap.get("_id")
		responseString = doHTTPPost(url, sendMap);
		Log.i(TAG, "sendFind.ResponseString: " + responseString);
		try {
			//this is for easiness, it would be more efficient to do in one query

			//find.setServerId(1);
			//find.setRevision(Integer.parseInt(responseMap.get(MyDBHelper.COLUMN_REVISION).toString()));
			//find.setSyncStatus(true);
			ContentValues cv = new ContentValues();
			cv.put("synced", "1");
			cv.put("sid", sendMap.get("identifier"));
			find.updateToDB(cv);
			//find.delete();

		}
		catch (Exception e) {
			Log.e(TAG, e.getStackTrace().toString());
		}
	}

	/**
	 * cleanup the item key,value pairs so that we can send the data.
	 * @param sendMap
	 */
	private void cleanupOnSend(HashMap<String, String> sendMap) {
		sendMap.put("find_time", sendMap.get("time"));
		sendMap.remove("time");
		sendMap.put("projectId", projectId+"");
		sendMap.put("id",sendMap.get("identifier"));
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
		//sendMap.put(COLUMN_APP_KEY, appKey);
		sendMap.put(COLUMN_IMEI, Utils.getIMEI(mContext));
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
		rMap.put(MyDBHelper.COLUMN_SYNCED,1);
		rMap.put("identifier", rMap.get("id"));
		rMap.put("sid", rMap.get("id")); //set the id from the server as sid
		rMap.remove("id");
		rMap.put("projectId", projectId);
		if (rMap.containsKey("add_time")) {
			rMap.put("time", rMap.get("add_time"));
			rMap.remove("add_time");
		}
		if (rMap.containsKey("images")) {
			Log.d(TAG, "contains image key");
			rMap.put(MyDBHelper.COLUMN_IMAGE_URI, rMap.get("images"));
			rMap.remove("images");
		}
		if (rMap.containsKey("videos")) {
			Log.d(TAG, "contains video key");
			rMap.put(MyDBHelper.COLUMN_VIDEO_URI, rMap.get("videos"));
			rMap.remove("videos");
		}
		if (rMap.containsKey("audioClips")) {
			Log.d(TAG, "contains audio key");
			rMap.put(MyDBHelper.COLUMN_AUDIO_URI, rMap.get("audioClips"));
			rMap.remove("audioClips");
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
		} catch (IllegalStateException e) {
			Log.e(TAG, "IllegalStateException: "+ e.getMessage());
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
		String findUrl = server +"/api/listFinds?projectId="+projectId+"&authKey="+authKey;

		//this is a List of all of the finds
		//each find is represented by a HashMap
		List<HashMap<String,Object>> findsMap = new ArrayList<HashMap<String,Object>>();
		HashMap<String, String> sendMap = new HashMap<String,String>();
		addRemoteIdentificationInfo(sendMap);

		//responseString is the raw json string returned through php
		String responseString = doHTTPPost(findUrl, sendMap);

		try {
			findsMap = (ArrayList<HashMap<String, Object>>) (new ResponseParser(responseString).parse());

			Iterator<HashMap<String, Object>> it = findsMap.iterator();

			while (it.hasNext()) {
				ArrayList<HashMap<String, Object>> imagesMap = new ArrayList<HashMap<String, Object>>();
				ArrayList<HashMap<String, Object>> videosMap = new ArrayList<HashMap<String, Object>>();
				ArrayList<HashMap<String, Object>> audioClipsMap = new ArrayList<HashMap<String, Object>>();

				HashMap<String, Object> map = it.next();
				JSONArray imageIds = (JSONArray) map.get("images");
				Log.d(TAG, "image ids jsonarray: " + imageIds.toString());

				for (int i=0; i<imageIds.length(); i++) {
					int imageId = imageIds.getInt(i);
					String imageUrl = server +"/api/getPicture?id=" + imageId + "&authKey=" +authKey;
					String imageResponseString = doHTTPPost(imageUrl, sendMap);
					Log.i(TAG, "Image Response String: " + imageResponseString);

					JSONObject json = new JSONObject(imageResponseString);
					Log.i(TAG, "JSON Image Response String: " + json.toString());

					HashMap<String,Object> imageMap = new HashMap<String,Object>();
					Iterator<String> iterKeys = json.keys();
					while(iterKeys.hasNext()) {
						String key = iterKeys.next();
						imageMap.put(key, json.get(key));
					}
					imagesMap.add(imageMap);
				}

				JSONArray videoIds = (JSONArray) map.get("videos");
				Log.d(TAG, "video ids jsonarray: " + videoIds.toString());

				for (int i = 0; i < videoIds.length(); i++) {
					int videoId = videoIds.getInt(i);
					String videoUrl = server + "/api/getVideo?id=" + videoId + "&authKey=" + authKey;
					String videoResponseString = doHTTPPost(videoUrl, sendMap);
					Log.i(TAG, "Video ResponseString: " + videoResponseString);

					JSONObject json = new JSONObject(videoResponseString);
					Log.i(TAG, "JSON videoResponseString: " + json.toString());

					HashMap<String,Object> videoMap = new HashMap<String, Object>();
					Iterator<String> iterKeys = json.keys();
					while (iterKeys.hasNext()) {
						String key = iterKeys.next();
						videoMap.put(key, json.get(key));
					}
					videosMap.add(videoMap);
				}

				JSONArray audioIds = (JSONArray) map.get("audioClips");

				for (int i = 0; i< audioIds.length(); i++) {
					int audioId = audioIds.getInt(i);
					String audioUrl = server + "/api/getAudio?id=" + audioId + "&authKey=" + authKey;
					String audioResponseString = doHTTPPost(audioUrl, sendMap);
					Log.i(TAG, "Audio ResponseString:" + audioResponseString);

					JSONObject json = new JSONObject(audioResponseString);
					Log.i(TAG, "JSON audioResponseString: " + json.toString());

					HashMap<String, Object> audioMap = new HashMap<String, Object>();
					Iterator<String> iterKeys = json.keys();
					while (iterKeys.hasNext()) {
						String key = iterKeys.next();
						audioMap.put(key, json.get(key));
					}
					audioClipsMap.add(audioMap);
				}

				map.put("images", imagesMap);
				map.put("videos", videosMap);
				map.put("audioClips", audioClipsMap);
			}
		} catch (JSONException e) {
			Log.e(TAG, "JSONException" +  e.getMessage());
		} 
		Log.i("THE FINDS", findsMap.toString());
		return findsMap;
	}

	public void updateFindFromServer(Find find) {
		ContentValues vals = getRemoteFindById(find.getId());
	}
	/**
	 * Pull the remote find from the server using the id provided
	 * @param remoteFindId
	 * @return
	 */
	public ContentValues getRemoteFindById(long remoteFindId) {
		String url = server +"/api/getFind?id=" +remoteFindId +"&authKey=" +authKey;
		HashMap<String, String> sendMap = new HashMap<String,String>();
		addRemoteIdentificationInfo(sendMap);
		sendMap.put("id", remoteFindId+"");
		String responseString = doHTTPPost(url, sendMap);
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

		Log.i("TEST for Image On Server Response", imageResponseString);
		Log.i("TEST for Image On Server Response Length", ""+imageResponseString.length());
		if (imageResponseString.equals("false"))
			return false;
		else return true;
	}
}