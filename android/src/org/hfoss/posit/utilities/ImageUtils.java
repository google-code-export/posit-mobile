/*
 * File: ImageUtils.java
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
package org.hfoss.posit.utilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Images.Media;
import android.util.Log;

public class ImageUtils {
	public static final String TAG = "ImageUtils";
	private static final int THUMBNAIL_TARGET_SIZE = 320;
	private static Uri mThumbUri = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
	private static Uri mImagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	/* 
	 * Save the Image to MediaStore 
	 */
	public static void saveImageFromCamera(Context mContext,long mRowId, Intent data) {
		Bitmap x = (Bitmap) data.getExtras().get("data");
		ContentValues values = new ContentValues();
		values.put(MediaColumns.TITLE, "title");
		values.put(ImageColumns.BUCKET_DISPLAY_NAME,"posit|"+mRowId);
		values.put(ImageColumns.BUCKET_ID, "posit");
		values.put(ImageColumns.IS_PRIVATE, 0);
		values.put(MediaColumns.MIME_TYPE, "image/jpeg");
		Uri uri = mContext.getContentResolver().insert(mImagesUri, values);
		OutputStream outstream;
		
		try {
			outstream = mContext.getContentResolver().openOutputStream(uri);
		
			x.compress(Bitmap.CompressFormat.JPEG, 70, outstream);
			outstream.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.toString());
		}catch (IOException e){
			Log.e(TAG, e.toString());
		}
		
		int imageId = Integer.parseInt(uri.toString()
				.substring(Media.EXTERNAL_CONTENT_URI.toString().length()+1));	
		
		int width = x.getWidth();
		int height = x.getHeight();
		int newWidth = THUMBNAIL_TARGET_SIZE;
		int newHeight = THUMBNAIL_TARGET_SIZE;
		
		float scaleWidth = ((float)newWidth)/width;
		float scaleHeight = ((float)newHeight)/height;
		
		Matrix matrix = new Matrix();
		matrix.setScale(scaleWidth, scaleHeight);
		Bitmap thumbnailImage = Bitmap.createBitmap(x, 0, 0,width,height,matrix,true);
		
		values = new ContentValues(4);
		values.put(Images.Thumbnails.KIND, Images.Thumbnails.MINI_KIND);
        values.put(Images.Thumbnails.IMAGE_ID, imageId);
        values.put(Images.Thumbnails.HEIGHT, height);
        values.put(Images.Thumbnails.WIDTH, width);
        uri = mContext.getContentResolver().insert(mThumbUri, values);
		
		try {
			outstream = mContext.getContentResolver().openOutputStream(uri);
			thumbnailImage.compress(Bitmap.CompressFormat.JPEG, 70, outstream);
			outstream.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.toString());
		}catch (IOException e){
			Log.e(TAG, e.toString());
		}
	}
}
