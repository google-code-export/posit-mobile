package org.hfoss.posit.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.hfoss.posit.Find;
import org.hfoss.posit.Utils;

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
