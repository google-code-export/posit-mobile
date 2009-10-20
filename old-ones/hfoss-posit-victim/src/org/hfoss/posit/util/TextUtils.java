package org.hfoss.posit.util;

import java.util.HashMap;
import java.util.Iterator;
import android.util.Log;
public class TextUtils {
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
