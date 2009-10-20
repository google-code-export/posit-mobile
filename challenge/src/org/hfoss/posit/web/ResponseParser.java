package org.hfoss.posit.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
/**
 * Class to parse JSON response from the server and convert to required formats.
 *
 * @author Prasanna Gautam
 * @author Qianqian Lin
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
		JSONArray j = new JSONArray(response);
		for (int i= 0; i < j.length(); i++){
			HashMap<String,Object> map = new HashMap<String,Object>();
			JSONObject json = j.getJSONObject(i);
			Iterator<String> iterKeys = json.keys();
			while(iterKeys.hasNext()) {
				String key = iterKeys.next();
				map.put(key, json.get(key));
			}
			findsList.add(map);
		}
		return findsList;
	}
	
}
