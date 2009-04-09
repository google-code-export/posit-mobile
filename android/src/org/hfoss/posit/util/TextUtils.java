package org.hfoss.posit.util;

import java.util.HashMap;
import java.util.Iterator;
import android.util.Log;
/**
 * All the text related utilities for parsing, presenting text.
 * @author pgautam
 *
 */
public class TextUtils {
	/**
	 * Generates text based on the given hashMaps and sets delimiters.
	 * ie. if delimeter is : and hashmap is
	 * {<Key-Name>, <Value-X>,<Key-Description>, <Value-Y>}
	 * Name: X
	 * Description: Y
	 * @param args
	 * @param delimiter
	 * @return
	 */
	public static String genTextFromHashMap(HashMap<String,String> args,String delimiter) {
		Iterator<String> iter = args.keySet().iterator();
    	String result="", key=null;
    	Log.i("TextUtils",result);
    	while (iter.hasNext()) {
    		key=iter.next();
    		Log.i("TextUtils",key);
    		result = result + key+delimiter+args.get(key)+"\n";
    		Log.i("TextUtils",result);
    	}
    	Log.i("TextUtils",result);
    	return result;
	}
}
