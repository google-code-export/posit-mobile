package org.hfoss.posit.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
/**
 * Class to parse JSON response from the server and convert to required formats.
 * @author pgautam
 *
 */
public class ResponseParser {
	String response=null;

	public ResponseParser(String response) {
		this.response = response;
	}
	/**
	 * Parses the response and returns the HashMap equivalent for the program to use.
	 * @return
	 * @throws JSONException
	 */
	public List<HashMap<String, Object>> parse() throws JSONException  {
		if (response.equals(null)) throw new NullPointerException("Pass a response first");
		List<HashMap<String, Object>> findsList = new ArrayList<HashMap<String,Object>>();
		HashMap<String,Object> map;
		while(response.indexOf("{")!=-1) {
			map = new HashMap<String,Object>();
			JSONObject json = new JSONObject(response.substring(response.indexOf("{"),response.indexOf("}")+1));
			response = response.substring(response.indexOf("}")+1);
			Iterator<String> iterKeys = json.keys();
			while(iterKeys.hasNext()) {
				String key = iterKeys.next();
				//Log.i("parse()","key = "+key);
				//Log.i("parse()","val = "+json.get(key));
				map.put(key, json.get(key));
			}
			findsList.add(map);
		}
		return findsList;
	}
	
}
