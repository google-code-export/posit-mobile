package org.hfoss.webServices;

import java.util.Vector;

import org.kxmlrpc.XmlRpcClient;

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
        XmlRpcClient client = new XmlRpcClient(url);
        Vector res = null;

        try {
             res = (Vector) client.execute(serverFunction, new Vector(), m_context);

        } catch (Exception e) {
        	Log.e(DEBUG_TAG + ".ping", e.getMessage() + " : " + res.toString());
        }

        return res;
   }
    public void send(String url, String serverFunction, Vector args){
    	XmlRpcClient client = new XmlRpcClient(url);
    	 try {
    	client.execute(serverFunction, args, m_context);
    	 } catch (Exception e) {
         	Log.e(DEBUG_TAG + ".ping", e.getMessage() + " : " + args.toString());
         }
    	
    }

}
