package org.hfoss.webServices;

import java.util.Vector;
import android.content.Context;
import android.util.Log;

import org.kxmlrpc.XmlRpcClient;

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

}
