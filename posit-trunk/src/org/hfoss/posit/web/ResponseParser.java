/*
 * File: ResponseParser.java
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
/**
 * Class to parse JSON response from the server and convert to required formats.
 *
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
