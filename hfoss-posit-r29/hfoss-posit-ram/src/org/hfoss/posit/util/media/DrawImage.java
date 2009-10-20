package org.hfoss.posit.util.media;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.hfoss.posit.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class DrawImage {
	public static void fromURL(Context context, ImageView imageview, String urlString) {
		try {
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();
		conn.connect();
		InputStream is = conn.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);
        Bitmap bm = BitmapFactory.decodeStream(bis);
        bis.close();
        is.close();
        imageview.setImageBitmap(bm);
		}
		catch (IOException e) {
			imageview.setImageDrawable(context.getResources().getDrawable(R.drawable.flag));
		}
	}
}
