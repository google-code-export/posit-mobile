/*
 * File: PositHttpUtils.java
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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.hfoss.posit.Find;
import org.hfoss.posit.utilities.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.util.Log;

public class PositHttpUtils {
	private static final String STATUS = "status";
	private static final String OK = "OK";
	private static final String TAG = "HttpUtils";
	public static List<NameValuePair> convertFindsForPost(String[]projection, Cursor c) {
		List<NameValuePair> finds= new ArrayList<NameValuePair>();
		c.moveToFirst();
		try {
		for(String item:projection) {
			finds.add(new BasicNameValuePair(item, c.getString(c.getColumnIndexOrThrow(item))));
		}
		}catch (CursorIndexOutOfBoundsException e) {
			if(Utils.debug)
				Log.e(TAG, e.getMessage());
		}
		return finds;
	}
	public static List<NameValuePair> convertFindsForPost(String[]projection, HashMap<String, String> findsMap) {
		List<NameValuePair> finds= new ArrayList<NameValuePair>();
		for(String item:projection) {
			finds.add(new BasicNameValuePair(item,findsMap.get(item)));
		}
		return finds;
	}
	public static List<NameValuePair> convertFindsForPost(Find find){
		List<NameValuePair> findList = new ArrayList<NameValuePair>();
		HashMap<String, String> findMap = find.getContentMap();
		for (String key: findMap.keySet()){
			findList.add(new BasicNameValuePair(key, findMap.get(key)));
		}
		return findList;
	}
	public static boolean isResponseOK (HashMap<String,Object> responseMap) {
		String str = (String) responseMap.get(STATUS);
		if (str.equals(OK))return true;
		return false;
	}
	/**
	 * Returns the NameValuePair objects required 
	 * @param nameValuesMap
	 * @return
	 */
	public static List<NameValuePair> getNameValuePairs (HashMap<String,String> nameValuesMap) {
		Iterator<String> iter = nameValuesMap.keySet().iterator();
		List<NameValuePair> nvp = new ArrayList<NameValuePair>();
		while (iter.hasNext()) {
			String key = iter.next();
			String value = nameValuesMap.get(key);
			nvp.add(new BasicNameValuePair(key,value));
		}
		return nvp;
	}
	
	
	
}
