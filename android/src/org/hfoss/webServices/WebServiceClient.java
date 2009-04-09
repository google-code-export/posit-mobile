
package org.hfoss.webServices;

import java.net.MalformedURLException;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcClient;

import android.content.Context;
import android.util.Log;
public class WebServiceClient {
	private Context m_context;
	private final String DEBUG_TAG = "WebServiceClient";

	
	
	// -----------------------------------------------------------------------
	public WebServiceClient(Context context) {
		m_context = context;
	}

	// -----------------------------------------------------------------------
    public Vector ping(String url, String serverFunction) {
        XmlRpcClient client=null;
		try {
			client = new XmlRpcClient(url);
		} catch (MalformedURLException e) {
			// TODO: handle exception
		}
        Vector res = null;
        
        try {
             res = (Vector) client.execute(serverFunction, new Vector());

        } catch (Exception e) {
        	Log.e(DEBUG_TAG + ".ping", e.getMessage() + " : " + res.toString());
        }
        Log.i(DEBUG_TAG,res.toString());
        return res;
   }
    public void send(String url, String serverFunction, Vector args){
    	XmlRpcClient client=null;
    	try {
    	client = new XmlRpcClient(url);
    	Log.i(DEBUG_TAG,args.toString());
    } catch (MalformedURLException e) {
		// TODO: handle exception
	}
    	 try {
    	client.execute(serverFunction, args);
    	 } catch (Exception e) {
         	Log.e(DEBUG_TAG + ".ping", e.getMessage() + " : " + args.toString());
         }
    	
    }

}
