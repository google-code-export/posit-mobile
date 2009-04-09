/* kxmlrpc - XML-RPC for J2ME
 *
 * Copyright (C) 2001  Kyle Gabhart ( kyle@gabhart.com )
 *
 * Contributors: David Johnson ( djohnsonhk@users.sourceforge.net )
 * 				   Stefan Haustein
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.kxmlrpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.http.util.ByteArrayBuffer;
import org.kxmlrpc.XmlRpcParser;
import org.kxmlrpc.XmlRpcWriter;

import android.content.Context;
import android.net.http.SslCertificate;
import android.util.Log;


/**
 * A single-threaded, reusable XML-RPC client object.
 */
public class XmlRpcClient {

    /**
     * Stores the full URL the client will connect with
     */
    String url;

    /**
     * Stores the response sent back by the server
     */
    Object result = null;

    /**
     * Turns debugging on/off
     */
    boolean debug = true;
    private static final String LOG_TAG = "XmlRPC";
    /**
     * Constructs an XML-RPC client with a specified string representing a URL.
     *
     * @param url The full URL for the XML-RPC server
     */
    public XmlRpcClient( String url ) {
        this.url = url;
    }//end KxmlRpcClient( String )

    /**
     * Construct an XML-RPC client for the specified hostname and port.
     *
     * @param hostname the name of the host server
     * @param the server's port number
     */
    public XmlRpcClient( String hostname, int port ) {
        int delim = hostname.indexOf("/");
        String context = "";
        if (delim>0) {
            context = hostname.substring(delim);
            hostname = hostname.substring(0, delim);
        }
        this.url = "http://" + hostname + ":" + port + context;
    }//end KxmlRpcClient( String, int )

    public String getURL() {
        return url;
    }//end getURL()

    public void setURL( String newUrl ) {
        url = newUrl;
    }//end setURL( String )


    /**
      * An Android specific implementation of the "execute" method
      */
     public Object execute(String method, Vector params, Context context) throws Exception {
           KXmlSerializer           xw = null;
           XmlRpcWriter             writer = null;
           XmlRpcParser             parser = null;
           RequestQueue             con = null;
           ByteArrayOutputStream    bos = null;
           ByteArrayInputStream     bis = null;

           try {
                // Prepare the arguments for posting
                bos = new ByteArrayOutputStream();
                xw = new KXmlSerializer();
                xw.setOutput(new OutputStreamWriter(bos));
                writer = new XmlRpcWriter(xw);
                writer.writeCall(method, params);
                xw.flush();

                if (debug)
                     Log.d(LOG_TAG, bos.toString());

                byte[] request = bos.toByteArray();
                bis = new ByteArrayInputStream(request);

                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "text/xml");

                XmlRpcEventHandler eventHandler = new XmlRpcEventHandler();

                // Create the connection and post the arguments
                con = new RequestQueue(context);
                con.queueRequest(url, "POST", headers, eventHandler, bis, request.length, false);
                //con.waitUntilComplete();
                con.wait(); //hack
                ByteArrayInputStream in = new ByteArrayInputStream(eventHandler.getBytes());

                // Parse response from server
                KXmlParser xp = new KXmlParser();
                xp.setInput(new InputStreamReader(in));
                parser = new XmlRpcParser(xp);
                result = parser.parseResponse();

           } catch (Exception x) {
                Log.e(LOG_TAG + ".error", x.getMessage());
           }//end try/catch/finally

           if (result instanceof Exception)
                throw (Exception) result;

           return result;
      }//end execute( String, Vector )
     /**
     * Called when the return value has been parsed.
     */
    void setParsedObject(Object parsedObject) {
        result = parsedObject;
    }//end objectCompleted( Object )

    private class XmlRpcEventHandler implements EventHandler {

		private ByteArrayBuffer m_byteArray = new ByteArrayBuffer(20);

		XmlRpcEventHandler() {
		}

		public void data(byte[] bytes, int len) {
			m_byteArray.append(bytes, 0, len);
		}

		public void endData() {
			Log.d(LOG_TAG + ".endData", new String(m_byteArray.toByteArray()));
		}

		public void status(int arg0, int arg1, int arg2, String s) {
			Log.d(LOG_TAG + ".status", "status [" + s + "]");
		}

		public void error(int i, String s) {
			Log.d(LOG_TAG + ".error", "error [" + s + "]");
		}

		public void handleSslErrorRequest(int arg0, String arg1, SslCertificate arg2) {
		}

		public void headers(Iterator arg0) {
		}

		

		public byte[] getBytes() {
			return m_byteArray.toByteArray();
		}

		public void certificate(SslCertificate arg0) {
			// TODO Auto-generated method stub
			
		}

		
	}

}//end class KXmlRpcClient