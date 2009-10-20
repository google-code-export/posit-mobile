/**
 * Camera class is an Activity. When activated (onCreate) it continually
 *  inputs an image from the camera device. In the emulator the images are
 *  a moving checkerboard.  To take a picture, the user clicks the middle
 *  button of the phone device. This saves the current image into the
 *  ./files directory: /data/data/org.hfoss.posit/files/pic.png.
 *
 *  The picture file is returned to the calling application when Camera
 *  exits.
 *  @author Prasanna Gautam
 */

package org.hfoss.posit;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.hardware.CameraDevice;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;


// ----------------------------------------------------------------------

public class Camera extends Activity
{
	/**
	 * myPic stores the picture taken.
	 */
	private Bitmap myPic;
	/**
	 * fileLocation is the file name that is returned as the Camera's result.
	 */
	private String fileLocation;
	/**
	 * mPreview stores the view through the camera device.
	 */
	private Preview mPreview;


	/**
	 * This code executes when the Camera activity starts up. It basically
	 *  starts a preview thread and waits for the user to take a picture.
	 *  The preview thread repeatedly samples input form the camera device.
	 *  Making the window TRANSLUCENT allows the camera image to show on
	 *  Posit's window.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Make sure to create a TRANSLUCENT window. This is required
        // for SurfaceView to work. Eventually this'll be done by
        // the system automatically.
        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        // Create our Preview view and set it as the content of our
        // Activity
        mPreview = new Preview(this);
        setContentView(mPreview);
    }

    @Override
	protected boolean isFullscreenOpaque() {
        // Our main window is set to translucent, but we know that we will
        // fill it with opaque data. Tell the system that so it can perform
        // some important optimizations.
        return true;
    }

    @Override
	protected void onResume()
    {
        // Because the CameraDevice object is not a shared resource,
        // it's very important to release it when the activity is paused.
        super.onResume();
        mPreview.resume();
    }

    /**
     * onKeyDown() is invoked when the user clicks any button. If it's
     *  the center button, takePicture() is called. If it's the BACK
     *  button, the last picture taken is returned. All other buttons
     *  are ignored.
     */
    @Override
	public boolean onKeyDown(int KeyCode, KeyEvent arg1) {
		if (KeyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
				KeyCode == KeyEvent.KEYCODE_SPACE){
			mPreview.pause();
			takePicture();
			setResult(RESULT_OK, "pic.png", null );
			mPreview.resume();
			return true;
		} else if (KeyCode == KeyEvent.KEYCODE_BACK){
			setResult(RESULT_OK, "pic.png", null );
			//Log.i("MyLog", fileLocation);
			finish();
			return true;
		}
		return false;
	}

    @Override
	protected void onFreeze(Bundle outState) {
		// TODO Auto-generated method stub
		super.onFreeze(outState);
	}

	private int i =0;

	/**
	 * takePicture() gets a reference to the camera device and sets its
	 *  parameters, then creates a Bitmap to store the picture. It opens
	 *  an output stream and and compresses and stores the picture in the
	 *  file "pic.png".
	 * @author Prasanna Gautam
	 */
    void takePicture() {
    	CameraDevice camera = CameraDevice.open();
    	if (camera !=null) {
    		Log.i("MyLog", "inside the camera"); // Debugging
    		CameraDevice.CaptureParams param =
    			new CameraDevice.CaptureParams();
    		param.type = 1; // preview
            param.srcWidth = 1280;
            param.srcHeight = 960;
            param.leftPixel = 0;
            param.topPixel = 0;
            param.outputWidth = 320;
            param.outputHeight = 480;
            param.dataFormat = 2; // RGB_565
            camera.setCaptureParams(param);

            myPic = Bitmap.createBitmap(320,480, false);

            Canvas canvas = new Canvas (myPic);
//            try {
            	//FileOutputStream stream = super.openFileOutput("pic.png", MODE_PRIVATE);
//            	HashMap<String,Object> values = new HashMap<String,Object>();
//            	values.put(MediaStore.Images.Media.TITLE, "first photo");
//            	values.put(MediaStore.Images.Media.DESCRIPTION, "description");

//            	values= (ContentValues)values;


            	camera.capture(canvas);

            	ContentValues values = new ContentValues();
            	values.put(MediaStore.Images.Media.TITLE, "k");
            	values.put(MediaStore.Images.Media.DATE_TAKEN, "d");



            	Uri uri = getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI , values);
            	//Uri uri = getContentResolver().insert("content://media/images/" , values);

            	try{
            	OutputStream outStream = getContentResolver().openOutputStream(uri);
            	myPic.compress(Bitmap.CompressFormat.JPEG, 50, outStream);
            	outStream.close();
            	} catch (FileNotFoundException e) {
            		Log.i("MyLog", e.toString());
            	} catch (IOException e){
            		Log.i("MyLog", e.toString());
            	} catch (NullPointerException e){
            		Log.i("MyLog", e.toString());
            	}
            	//myPic.compress(CompressFormat.PNG, 50, stream);
            	//android.provider.MediaStore.Images.Media.insertImage(getContentResolver(), myPic, "ping" , "pong");
            	//Log.i("Camera",struri);

            	//getContentResolver().insert(android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
//            	stream.flush(); //closes the underlying OS resources
//            	stream.close();
//            }finally{
//            	Log.e("Camera", "Big Error!");
//            }
            if (camera !=null)
            	camera.close();
    	}
    }
    @Override
	protected void onPause()
    {
        // Start Preview again when we resume.
        super.onPause();
        mPreview.pause();
    }
}

// ----------------------------------------------------------------------

/**
 * The Preview class extends SurfaceView, a two-dimensional surface.
 * It manages the acquisition of an image through the camera device. The
 * acquisition is managed in a separate thread.
 */
class Preview extends SurfaceView implements SurfaceHolder.Callback
{
    Preview(Context context) {
        super(context);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHasSurface = false;

        // In this example, we hardcode the size of the preview. In a real
        // application this should be more dynamic. This guarantees that
        // the uderlying surface will never change size.
        mHolder.setFixedSize(320, 480);
    }

    public void resume() {
        // We do the actual acquisition in a separate thread. Create it now.
        if (mPreviewThread == null) {
            mPreviewThread = new PreviewThread();
            // If we already have a surface, just start the thread now too.
            if (mHasSurface == true) {
                mPreviewThread.start();
            }
        }
    }

    public void pause() {
        // Stop Preview.
        if (mPreviewThread != null) {
            mPreviewThread.requestExitAndWait();
            mPreviewThread = null;
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, start our main acquisition thread.
        mHasSurface = true;
        if (mPreviewThread != null) {
            mPreviewThread.start();
        }
        // Tell the system that we filled the surface in this call.
        // This is a lie to prevent the system to fill the surface for us
        // automatically.
        // THIS IS REQUIRED because other wise we'll access the Surface object
        // from 2 different threads which is not allowd (And will crash
        // currently).
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return. Stop the preview.
        mHasSurface = false;
        pause();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Surface size or format has changed. This should not happen in this
        // example.
    }

    // ----------------------------------------------------------------------

    /**
     * The PreviewThread manages the acquisition of the image from the camera.
     *  It repeatedly stores the camera view onto a canvas.  When the picture is
     *  taken (center key is pressed), this thread is paused and control of the
     *  camera device is  returned back to Camera object, which then stores the
     *  contents of the surface in a file.
     */
    class PreviewThread extends Thread
    {
        PreviewThread() {
            super();
            mDone = false;
        }

        @Override
		public void run() {
            // We first open the CameraDevice and configure it.
            CameraDevice camera = CameraDevice.open();
            if (camera != null) {
                CameraDevice.CaptureParams param = new CameraDevice.CaptureParams();
                    param.type = 1; // preview
                    param.srcWidth      = 1280;
                    param.srcHeight     = 960;
                    param.leftPixel     = 0;
                    param.topPixel      = 0;
                    param.outputWidth   = 320;
                    param.outputHeight  = 480;
                    param.dataFormat    = 2; // RGB_565
                camera.setCaptureParams(param);
            }

            // This is our main acquisition thread's loop, we go until
            // asked to quit.
            SurfaceHolder holder = mHolder;
            while (!mDone) {
                // Lock the surface, this returns a Canvas that can
                // be used to render into.
                Canvas canvas = holder.lockCanvas();

                // Capture directly into the Surface
                if (camera != null) {
                    camera.capture(canvas);
                }

                // And finally unlock and post the surface.
                holder.unlockCanvasAndPost(canvas);
//                Log.i("MyLog",holder.toString());
            }

            // Make sure to release the CameraDevice
            if (camera != null)
                camera.close();
        }

        public void requestExitAndWait() {
            // don't call this from PreviewThread thread or it a guaranteed
            // deadlock!
            mDone = true;
            try {
                join();
            } catch (InterruptedException ex) { }
        }
        private boolean mDone;
    }

            SurfaceHolder   mHolder;
    private PreviewThread   mPreviewThread;
    private boolean         mHasSurface;
}
