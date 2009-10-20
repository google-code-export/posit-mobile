package org.hfoss.posit.web;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
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
	public HashMap<String, Object> parse() throws JSONException  {
		if (response.equals(null)) throw new NullPointerException("Pass a response first");
		HashMap<String, Object> map = new HashMap<String, Object>();
		JSONObject json = new JSONObject(response);
		Iterator<String> iterKeys = json.keys();
		
		while(iterKeys.hasNext()) {
			String key = iterKeys.next();
			map.put(key, json.get(key));
		}
		return map;
	}
	
}
