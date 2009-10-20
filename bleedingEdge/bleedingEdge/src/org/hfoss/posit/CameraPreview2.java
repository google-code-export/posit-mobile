/* 
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hfoss.posit;

import java.io.IOException;
import java.io.OutputStream;

import org.hfoss.posit.util.ImageAdapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;


// ----------------------------------------------------------------------

public class CameraPreview2 extends Activity {    
    /* (non-Javadoc)
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	private Preview2 mPreview;
	private static final String SAVING_MSG = "Saving to disk...";
	private final String LOG_TAG = "CameraPreview2";
	// Progress Dialog when saving as the operation can take a bit of time
	private ProgressDialog mProgressDialog;
	private Gallery gallery;
	private int  count;
	String picOutputString = null;
	private Long recordid= null;
	private DBHelper dbHelper;
	
	// the cursor for the database
	private Cursor mCursor;
    @Override
	protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gallery);
		dbHelper = new DBHelper(this);
		dbHelper.open();
		

		// Create our Preview view and set it as the content of our
		// Activity
		//Intent intent = getIntent();
		recordid = icicle != null ? icicle.getLong(DBHelper.KEY_ROWID) : null;
		if (recordid == null){
		Bundle extras = getIntent().getExtras();
		recordid = extras != null ? extras.getLong(DBHelper.KEY_ROWID): 0;
		}

		
		Log.i("NewCamera",""+recordid);
		mCursor = dbHelper.findImages(recordid);
		count = dbHelper.lastImage()+1;
		// set the layout


		mPreview = new Preview2(this);
		mPreview.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		LinearLayout ll = (LinearLayout) findViewById(R.id.camera_preview);
		ll.addView(mPreview);
		// set the gallery values
		gallery = (Gallery) findViewById(R.id.gallery);
       gallery.setVisibility(View.INVISIBLE);
		// Create our Preview view and set it as the content of our activity.
        /*mPreview = new Preview2(this);
       
        setContentView(mPreview);*/
		//gallery.setAdapter(new ImageAdapter(mCursor, this));
        
    }
    
    /*
     * Checks for key events
     * Pressing the center key saves the picture to the file system and updates the database 
     * with the location of the file, pressing back returns the application back to the activity that
     * called it and returns the list of pictures for the main application to process.
     */
    	@Override
    	public boolean onKeyDown(int keyCode, KeyEvent event) {
    		switch (keyCode) {
    		case KeyEvent.KEYCODE_DPAD_CENTER:
    			// Progress bar operation
//    			mProgressDialog = new ProgressDialog(this);
//    			mProgressDialog.setMessage(SAVING_MSG);
//    			mProgressDialog.setIndeterminate(true);
//    			mProgressDialog.show();

    			mPreview.takePicture();
    			
    			break;
    		// Not really sure why this does not work by default
    		case KeyEvent.KEYCODE_BACK:
    			// Always kill the preview thread

    			finish();
    			break;
    		}
    		return false;
    	}
    	// ----------------------------------------------------------------------

    	class Preview2 extends SurfaceView implements SurfaceHolder.Callback {
    	    SurfaceHolder mHolder;
    	    Camera mCamera;
    	    
    	    Preview2(Context context) {
    	        super(context);
    	        
    	        // Install a SurfaceHolder.Callback so we get notified when the
    	        // underlying surface is created and destroyed.
    	        mHolder = getHolder();
    	        mHolder.addCallback(this);
    	        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    	    }

    	    public void surfaceCreated(SurfaceHolder holder) {
    	        // The Surface has been created, acquire the camera and tell it where
    	        // to draw.
    	        mCamera = Camera.open();
    	        mCamera.setPreviewDisplay(holder);
    	        
    	    }

    	    public void surfaceDestroyed(SurfaceHolder holder) {
    	        // Surface will be destroyed when we return, so stop the preview.
    	        // Because the CameraDevice object is not a shared resource, it's very
    	        // important to release it when the activity is paused.
    	        mCamera.stopPreview();
    	        mCamera = null;
    	    }

    	    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    	        // Now that the size is known, set up the camera parameters and begin
    	        // the preview.
    	        Camera.Parameters parameters = mCamera.getParameters();
    	        parameters.setPreviewSize(w, h);
    	        mCamera.setParameters(parameters);
    	        mCamera.startPreview();
    	    }
    	    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
    	        public void onPictureTaken(byte[] data, Camera c) {
    	            //Log.e(LOG_TAG, "PICTURE CALLBACK: data.length = " + data.length);
    	        	try {
    	        	final OutputStream outStream = openFileOutput("pic"+count+".jpg",MODE_WORLD_READABLE);
					
						outStream.write(data);
						outStream.close();
						dbHelper.addImage(CameraPreview2.this.getFilesDir()
		    					+ "/pic" + count + ".jpg",recordid);
		    					mCursor.requery();
		    					count = dbHelper.lastImage()+1;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    	            mCamera.startPreview();
    	        }
    	    };
    	    public void takePicture() {
    	    	mCamera.stopPreview();
    	    	mCamera.takePicture(null, null,mPictureCallback);
    	    }
    	}

}

