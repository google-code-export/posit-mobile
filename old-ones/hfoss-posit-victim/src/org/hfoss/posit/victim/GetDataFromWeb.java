package org.hfoss.posit.victim;



import java.net.URLConnection;

import org.hfoss.posit.util.Streamer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;

public class  GetDataFromWeb extends Activity {
	private Paint paint;
	private Preview mPreview;
	private URLConnection ucon;

	    @Override
		protected void onCreate(Bundle icicle) {
	        super.onCreate(icicle);
	        
	        // Hide the window title.
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	    
	        // Create our Preview view and set it as the content of our
	        // Activity
	        mPreview = new Preview(this);
	        setContentView(mPreview);
	    }
	    
	    @Override
		protected void onResume() {
	        // Because the CameraDevice object is not a shared resource,
	        // it's very important to release it when the activity is paused.
	        super.onResume();
	        mPreview.resume();
	    }

	    @Override
		protected void onPause() {
	        // Start Preview again when we resume.
	        super.onPause();
	        mPreview.pause();
	    }
	}

	// ----------------------------------------------------------------------

	class Preview extends SurfaceView implements SurfaceHolder.Callback {
	    SurfaceHolder  mHolder;
	    private PreviewThread mPreviewThread;
	    private boolean mHasSurface;
	    
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
	        mHolder.setFixedSize(320, 240);
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

	    class PreviewThread extends Thread {
	        private boolean mDone;
	    	

	        PreviewThread() {
	            super();
	            mDone = false;
	        }

	        @Override
			public void run() {
	            // We first open the CameraDevice and configure it.
	            Streamer streamcamera =  new Streamer();
//	            if (camera != null) {
//	                CameraDevice.CaptureParams param = new CameraDevice.CaptureParams();
//	                    param.type = 1; // preview
//	                    param.srcWidth      = 1280;
//	                    param.srcHeight     = 960;
//	                    param.leftPixel     = 0;
//	                    param.topPixel      = 0;
//	                    param.outputWidth   = 320;
//	                    param.outputHeight  = 240;
//	                    param.dataFormat    = 2; // RGB_565
//	                camera.setCaptureParams(param);
//	            }

	            // This is our main acquisition thread's loop, we go until
	            // asked to quit.
	            SurfaceHolder holder = mHolder;
	            while (!mDone) {
	                // Lock the surface, this returns a Canvas that can
	                // be used to render into.
	                Canvas canvas = holder.lockCanvas();

	                // Capture directly into the Surface
//	                if (camera != null) {
//	                    camera.capture(canvas);
//	                }
	                streamcamera.capture(canvas);
	                // And finally unlock and post the surface.
	                holder.unlockCanvasAndPost(canvas);
	            }

	            // Make sure to release the CameraDevice
	            /*if (camera != null)
	                camera.close();*/
	        }
	        
	        public void requestExitAndWait() {
	            // don't call this from PreviewThread thread or it a guaranteed
	            // deadlock!
	            mDone = true;
	            try {
	                join();
	            } catch (InterruptedException ex) { }
	        }
	    }
	}

//     @Override
//     public void onCreate(Bundle icicle) {
//          super.onCreate(icicle);
//
//          /* We will show the data we read in a TextView. */
//          //TextView tv = new TextView(this);
//          //Canvas canvas = new Canvas();
//          //Bitmap bitmap ;
//          //BitmapDrawable bitmapd = null;
//          //SurfaceView sV = new SurfaceView(this);
//          //View sV = new View(this);
//          //setContentView(sV);
//           String myString = null;
//     	 try {
//			URL myURL = new URL(
//					"http://157.252.16.250:8090/?action=snapshot");
//			URLConnection ucon = myURL.openConnection();
//			Log.i("GetData", ucon.getContentType());
//			
//			InputStream is = ucon.getInputStream();
//			BufferedInputStream bis = new BufferedInputStream(is);
//
//			
//			Drawable d = Drawable.createFromStream(bis, "src");
//			bis.close();
//			is.close();
//			
//			d.draw(canvas);
//
//			sV.setBackground(d);
//			
//			
//
//		} catch (Exception e) {
//			/* On any Error we want to display it. */
//			myString = e.getMessage();
//		}
//		
//		
//
// }

//     private void getStream(){
//    	 try {
//				/* Define the URL we want to load data from. */
//				//               URL myURL = new URL(
//				//                         "http://www.anddev.org/images/tut/basic/getdatafromtheweb/loadme.txt");
//				URL myURL = new URL(
//						"http://157.252.16.250:8090/?action=snapshot");
//				/* Open a connection to that URL. */
//				URLConnection ucon = myURL.openConnection();
//				Log.i("GetData", ucon.getContentType());
//				/* Define InputStreams to read
//				 * from the URLConnection. */
//				InputStream is = ucon.getInputStream();
//				BufferedInputStream bis = new BufferedInputStream(is);
//
//				//bitmap = BitmapFactory.decodeStream(is);
//				/* Read bytes to the Buffer until
//				 * there is nothing more to read(-1). */
//				//               ByteArrayBuffer baf = new ByteArrayBuffer(50*1024);
//				//               int current = 0;
//				//               while((current = bis.read()) != -1){
//				//                    baf.append((byte)current);
//				//               }
//				/* Convert the Bytes read to a String. */
//				//myString = new String(baf.toByteArray());
//				//bitmap = BitmapFactory.decodeStream(bis);
//				Drawable d = Drawable.createFromStream(bis, "src");
//				bis.close();
//				is.close();
//				//bitmap = Bitmap.createBitmap(640, 480, null);
//				
//				d.draw(canvas);
//
//				sV.setBackground(d);
//				//for (int i = 0; i <=1000000; i++);
//				//     canvas.drawBitmap(bitmap, 0,0, null);
//				//bitmapd= new BitmapDrawable(is);
//				//bitmapd
//				
//
//			} catch (Exception e) {
//				/* On any Error we want to display it. */
//				myString = e.getMessage();
//			}
//       }
//     }
