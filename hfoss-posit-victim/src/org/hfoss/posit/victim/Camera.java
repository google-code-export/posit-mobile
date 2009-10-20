package org.hfoss.posit.victim;

import java.io.IOException;
import java.io.OutputStream;

import org.hfoss.posit.util.ImageAdapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.CameraDevice;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

/**
 * 
 * This is a simple camera that will fill the screen with a preview and take the
 * picture. Currently, it returns the bitmap in a bundle and you can retrieve it
 * using the following code in your onActivityResult:
 * 
 * <P>
 * Bitmap mBitmap = (Bitmap) extras.getParcelable("bitmap");
 * </P>
 * 
 * TODO:
 * <ul>
 * <li> A generic saving against a cursor to the _data field </li>
 * <li> Given a file name or location, save to that location </li>
 * </ul>
 * 
 */
/**
 * How this application works:
 * 
 * The camera application opens up a thread that accesses the camera device which is streaming images.
 * This thread updates the canvas which is displayed using the surfaceview. Every time the center button 
 * is pressed, it saves the file, based on the last row id of the sql database and updates the value into 
 * the database. When the user presses the back button, this same string is passed back to the calling 
 * activity in a bundle so that it can parsed and displayed or saved into the main database.
 */
public class Camera extends Activity {

	// Logging tag
	private static final String TAG = "Camera";

	// Default parameter of the camera
	static private CameraDevice.CaptureParams param = new CameraDevice.CaptureParams();

	// Static variables
	private static final String SAVING_MSG = "Saving to disk...";

	// Progress Dialog when saving as the operation can take a bit of time
	private ProgressDialog mProgressDialog;

	private Gallery gallery;
	private int  count;
	String picOutputString = null;
	private Long recordid= null;
	private DBHelper dbHelper;
	
	// the preview box
	private Preview mPreview;

	// the cursor for the database
	private Cursor mCursor;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

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


		mPreview = new Preview(this, param);
		mPreview.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		LinearLayout ll = (LinearLayout) findViewById(R.id.camera_preview);
		ll.addView(mPreview);
		// set the gallery values
		gallery = (Gallery) findViewById(R.id.gallery);

		
		gallery.setAdapter(new ImageAdapter(mCursor, this));
		//gallery.setOnItemSelectedListener(this);


		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		// Set the parameters.
		param.type = 1; // preview
		param.srcWidth = dm.widthPixels;
		param.srcHeight = dm.heightPixels;
		param.leftPixel = 0;
		param.topPixel = 0;
		param.outputWidth = dm.widthPixels;
		param.outputHeight = dm.heightPixels;
		param.dataFormat = 2; // RGB_565
	}
	
	
/*
 * 
 * resume the preview as soon as the activity resumes.
 */
	@Override
	protected void onResume() {
		// Because the CameraDevice object is not a shared resource,
		// it's very important to release it when the activity is paused.
		super.onResume();
		mPreview.resume();
	}
/*
 * Pause the preview when the activity pauses. We don't want to be burning cycles/battery :)
 */
	@Override
	protected void onPause() {
		// Start Preview again when we resume.
		super.onPause();
		mPreview.pause();
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
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage(SAVING_MSG);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.show();

			mPreview.post(mTakePicture);

			break;
		// Not really sure why this does not work by default
		case KeyEvent.KEYCODE_BACK:
			// Always kill the preview thread
			mPreview.pause();

			finish();
			break;
		}
		return false;
	}

	Bundle bundle = new Bundle();
	final Runnable mTakePicture = new Runnable() {
		public void run() {
			// Need to pause the preview thread in order to kill it and
			// restore the camera object
			mPreview.pause();

			// Actual taking and saving of the picture
			// bundle.putParcelable("bitmap", takePicture());
			takePicture();
			mProgressDialog.dismiss();
			// Set the result that we managed to save the bitmap,
			// return to the edit page

			mPreview.resume();
			// finish();
		}
	};

	/*
	 * This function takes the actual picture onto a bitmap and returns it
	 */
	private void takePicture() {

		DisplayMetrics dm = new DisplayMetrics();
		/*
		 * Get some measurements of the screen so that we can display the camera 
		 * properly in all kinds of devices
		 */
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		CameraDevice camera = CameraDevice.open();

		if (camera != null) {
			camera.setCaptureParams(param);

			// Bitmap to which we will save the picture
			Bitmap myPic = Bitmap.createBitmap(param.outputWidth,
					param.outputHeight, false);

			// Canvas so we can link the bitmap to the picture
			Canvas canvas = new Canvas(myPic);

			// Capturing the image onto the bitmap
			camera.capture(canvas);

			/*
			 * Create a new row in the pictures table
			 * and get the count value. This helps us to take a unique name for our picture file
			 */
			try {
				/*
				 * Open an output stream named pic<last-created-database-row id>.png
				 * To avoid anyone overwriting it, it is set to world readable
				 */
				OutputStream outStream = this.openFileOutput("pic" + count
						+ ".png", Context.MODE_WORLD_READABLE);
				/**
				 *	actual saving of the file
				 *	Save it to the assigned quality (currently 100, although for most practical purposes
				 *	it need not be 100)
				 */
				myPic.compress(Bitmap.CompressFormat.PNG, 100, outStream);
						
				/*
				 * Close the output stream
				 */
				outStream.close();
				dbHelper.addImage(this.getFilesDir().toString()
				+ "/pic" + count + ".png",recordid);
				mCursor.requery();
				count = dbHelper.lastImage()+1;
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			}

			// Always closing the camera
			camera.close();
			//return myPic;
		} else {
			Log.e(TAG, "Failed to open camera device");
			return;
		}
	}

	

	
	//	----------------------------------------------------------------------

	class Preview extends SurfaceView implements SurfaceHolder.Callback {

		private CameraDevice.CaptureParams parama = null;

		Preview(Context context, CameraDevice.CaptureParams param) {
			super(context);

			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			mHolder = getHolder();
			mHolder.addCallback(this);
			mHasSurface = false;
			mHolder.setSizeFromLayout();
			this.parama = param;
		}

		public void resume() {
			// We do the actual acquisition in a separate thread. Create it now.
			if (mPreviewThread == null) {
				mPreviewThread = new PreviewThread();
				mPreviewThread.param = parama;

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
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// Surface will be destroyed when we return. Stop the preview.
			mHasSurface = false;
			pause();
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			// Surface size or format has changed. This should not happen in this
			// example.
		}

		// ----------------------------------------------------------------------

		class PreviewThread extends Thread {
			PreviewThread() {
				super();
				mDone = false;
			}

			@Override
			public void run() {
				// We first open the CameraDevice and configure it.
				CameraDevice camera = CameraDevice.open();
				if (camera != null) {
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

					// canvas.drawRGB(10, 40, 100);
					// And finally unlock and post the surface.
					holder.unlockCanvasAndPost(canvas);
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
				} catch (InterruptedException ex) {
				}
			}

			private boolean mDone;

			private CameraDevice.CaptureParams param = null;
		}

		SurfaceHolder mHolder;

		private PreviewThread mPreviewThread;

		private boolean mHasSurface;
	}
}