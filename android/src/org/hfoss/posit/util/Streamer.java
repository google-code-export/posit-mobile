package org.hfoss.posit.util;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
/**
 * The class for streaming videos from my server
 * @author pgautam
 *
 */
public class Streamer {
	private URL myURL;
	private URLConnection ucon;
	private final Rect bounds = new Rect(0, 0, 320, 240);
	private Paint paint = new Paint();
	private static final int CONNECT_TIMEOUT = 1000;
	private static final int SOCKET_TIMEOUT = 1000;
	
	public Streamer() {
		try {
			myURL = new URL("http://157.252.16.250:8090/?action=stream");
			paint.setFilterBitmap(true);
			paint.setAntiAlias(true);
		} catch (Exception e) {
		}

	}

	public boolean capture(Canvas canvas) {
		if (canvas == null)
			throw new IllegalArgumentException("null canvas");
		try {
			int response = -1;
			Bitmap bitmap = null;
			InputStream is = null;
			try {
				ucon = myURL.openConnection();
				if (!(ucon instanceof HttpURLConnection))
					throw new IOException("Not an HTTP connection.");
				HttpURLConnection httpConn = (HttpURLConnection) ucon;
				httpConn.setAllowUserInteraction(false);
				httpConn.setConnectTimeout(CONNECT_TIMEOUT);
				httpConn.setReadTimeout(SOCKET_TIMEOUT);
				httpConn.setRequestMethod("GET");

				httpConn.connect();

				Log.i("GetData", httpConn.getContentType());
				response = httpConn.getResponseCode();
				if (response == HttpURLConnection.HTTP_OK) {
					Log.i("GetDataFromWeb", "" + response);
					is = httpConn.getInputStream();
					bitmap = BitmapFactory.decodeStream(is);

				}
			} finally {
				if (is != null)
					try {
						is.close();
					} catch (IOException e) {
						/* ignore */
					}

			}

			if (bitmap == null)
				throw new IOException("Response Code: " + response);

			if (bounds.right == bitmap.getWidth()
					&& bounds.bottom == bitmap.getHeight()) {
				canvas.drawBitmap(bitmap, 0, 0, null);
			} else {
				Rect dest;

				dest = bounds;

				canvas.drawBitmap(bitmap, null, dest, paint);
			}
		} catch (IOException e) {
		}

		return true;
	}

}