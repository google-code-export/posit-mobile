package org.hfoss.posit;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

/**
 * Used for putting images in the view
 * @author pgautam
 *
 */
class ImageAdapter extends BaseAdapter {
    int mGalleryItemBackground;
	private Context mContext;
	private Cursor cursor;

    public ImageAdapter(Cursor _cursor, Context c) {
        mContext = c;
        cursor = _cursor;
        // See res/values/attrs.xml for the  defined values here for styling
        TypedArray a = mContext.obtainStyledAttributes(R.styleable.Gallery1);
        mGalleryItemBackground = a.getResourceId(
                R.styleable.Gallery1_android_galleryItemBackground, 0);
        a.recycle();
    }

    public int getCount() {
      return cursor.getCount();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      ImageView i = new ImageView(mContext);
      cursor.requery();
      if (convertView == null) {
    	  //gets the location of the image and the ID 
           cursor.moveToPosition(position);
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID));
                Cursor c= mContext.getContentResolver().query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, null,
                		MediaStore.Images.Thumbnails.IMAGE_ID+"="+id, null, null);
                
                
           		i.setImageURI(Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, ""+id));
                i.setScaleType(ImageView.ScaleType.FIT_XY);
                i.setLayoutParams(new Gallery.LayoutParams(136, 136));
                i.setBackgroundResource(mGalleryItemBackground);
               
      }
           return i;
    }
  }