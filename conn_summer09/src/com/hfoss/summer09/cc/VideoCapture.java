/**
 * @author Khanh Pham
 */

package com.hfoss.summer09.cc;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class VideoCapture extends Activity implements View.OnClickListener, SurfaceHolder.Callback, MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener{

	private SurfaceHolder mSurfaceHolder = null;
	private SurfaceView mVideoPreview;
	private MediaRecorder mMediaRecorder;
	private Button save, goback, review, cancel;
	private ImageButton shutter;
	private TextView recording;
	private File temp = new File("/sdcard/._temp.3gp");
	private File currentVideo = new File("/sdcard/._currentVideo.3gp");
	private static final int VIDEO_WIDTH = 176;
	private static final int VIDEO_HEIGHT = 144;
	private static final int VIDEO_FRAME_RATE = 20;
	private static final int MAX_VIDEO_LENGTH = 30000; //30 seconds
	private static final String TAG = "Video";
	private int camera_state;
	private static final int NOT_STARTED = 1, RECORDING =2;
	private static final long LOW_STORAGE_THRESHOLD = 512L * 1024L;
	
	
	
	/**
	 * Sets up 
	 * - video preview
	 * - buttons to capture, play, save or cancel
	 */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_app);
		camera_state = NOT_STARTED;
		mVideoPreview = (SurfaceView) findViewById(R.id.camera_preview);
		SurfaceHolder holder = mVideoPreview.getHolder();
		holder.setFixedSize(400, 300);  
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		review = (Button) findViewById(R.id.reviewbutton);
		review.setOnClickListener(this);
		save = (Button) findViewById(R.id.savebutton);
		save.setOnClickListener(this); 
		goback = (Button) findViewById(R.id.gobackbutton);
		goback.setOnClickListener(this);
		cancel = (Button) findViewById(R.id.cancelbutton);
		cancel.setOnClickListener(this);
		recording = (TextView) findViewById(R.id.recording);
		toggleVisibility(3);
		toggleVisibility(5);
		shutter = (ImageButton) findViewById(R.id.shutter_button);
		shutter.setOnClickListener(this);

	}


	public void onPause()
	{
		super.onPause();
		release();
	}

	/**
	 * Setting up the video camera parameters, such as audio/video sources, frame rate
	 * preview display.
	 */
	
	private void initialize(){
		mMediaRecorder = new MediaRecorder();
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mMediaRecorder.setOutputFile("/sdcard/._temp.3gp");
		mMediaRecorder.setVideoSize(VIDEO_WIDTH, VIDEO_HEIGHT);
		mMediaRecorder.setVideoFrameRate(VIDEO_FRAME_RATE);
		mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mMediaRecorder.setMaxDuration(MAX_VIDEO_LENGTH);
		mMediaRecorder.setMaxFileSize(getAvailableStorage()- LOW_STORAGE_THRESHOLD);
		mMediaRecorder.setOnErrorListener(this);
		mMediaRecorder.setOnInfoListener(this);
		mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
		try {
			mMediaRecorder.prepare();
		} catch (IOException exception) {
			Log.e(TAG, "prepare failed for " + temp.getName());
			Log.e(TAG, exception.toString());
			release();
		}
	}

	/**
	 * Starts the video recorder
	 * Called when the ImageButton "shutter" is clicked. 
	 */

	private void record(){	
		try {
			mMediaRecorder.start();
		} catch (RuntimeException e) {
			Log.e(TAG, "Could not start media recorder. ", e);
			return;
		}
	}

	/**
	 * Stops the camera and exports the video
	 * Called when the ImageButton "shutter" is clicked again
	 */
	private void stop(){
		mMediaRecorder.stop();
	}

	/**
	 * Releases the media recorder
	 * Called when 
	 * - Recording is done
	 * - There's an error initializing the camera
	 */
	private void release(){
		Log.v(TAG, "Releasing media recorder.");
		if (mMediaRecorder != null) {
			mMediaRecorder.reset();
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
	}	

	/**
	 * Implementing onClickListener
	 * Assigns tasks for 4 buttons
	 * - Review: Plays the video just recorded
	 * - Go back: Display the camera preview
	 * - Save: Rename the default file to a more specific name based on when the video was recorded
	 * - Shutter: If the recorder has not started, then record the video; If the recorder is already running, then stop the recorder
	 * Also calls the toggleVisibility() function  
	 */
	
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.reviewbutton: 
			Intent intent = new Intent();  
			intent.setAction(android.content.Intent.ACTION_VIEW);  
			intent.setDataAndType(Uri.fromFile(currentVideo), "video/*");  
			startActivity(intent);
			toggleVisibility(1);
			break;
		case R.id.gobackbutton:
			toggleVisibility(3);
			toggleVisibility(4);
			release();
			initialize();
			break;	        	
		case R.id.savebutton:
			currentVideo.renameTo(new File("/sdcard/video"+System.currentTimeMillis()+".3gp"));
			cleanUpTemp();
			finish();
			break;
		case R.id.cancelbutton:
			cleanUpTemp();
			finish();
		case R.id.shutter_button:
			if (camera_state == NOT_STARTED){
				record();
				camera_state = RECORDING;
				recording.setVisibility(View.VISIBLE);
			}
			else if (camera_state == RECORDING){
				stop();
				resetSetup();	
			}
			break;

		}
	}
	/**
	 * Implements the surfaceHolder.Callback interface
	 */
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}
	/**
	 * Implements the surfaceHolder.Callback interface. When the surface is created, 
	 * initialize the camera and the preview.
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		mSurfaceHolder = holder;	
		initialize();
	}
	/**
	 * Implements the surfaceHolder.Callback interface
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		mSurfaceHolder = null;
	}

	/**
	 * Implements the MediaRecorder.onErrorListener interface.
	 * Logs errors that occur 
	 */
	public void onError(MediaRecorder mr, int what, int extra) {
		if (what == MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN){
			Log.d(TAG, "Error occured: Video Module" );
			release();
			finish();
		}
	}

	/**
	 * Implements the MediaRecorder.infoListener interface.
	 * When the recording reaches its max duration or the storage is low, display a warning 
	 */
	
	public void onInfo(MediaRecorder mr, int what, int extra) {

		if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
			Toast.makeText(VideoCapture.this, R.string.max_duration, 5000).show();
		}

		else if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED){
			Toast.makeText(VideoCapture.this, R.string.max_filesize, 5000).show();

		}
		resetSetup();

	}
	/**
	 * Returns the available storage of the SD card
	 * @return long 
	 */
	

	private long getAvailableStorage(){
		String storageDirectory = Environment.getExternalStorageDirectory().toString();
		StatFs stat = new StatFs(storageDirectory);
		return ((long)stat.getAvailableBlocks() * (long)stat.getBlockSize());
	}
	
	/**
	 * Called when the recording is finished (users click on the shutter button or the recorder reaches its preset limit)
	 */

	private void resetSetup(){
		camera_state = NOT_STARTED;
		temp.renameTo(currentVideo);
		toggleVisibility(1);
		toggleVisibility(2);
		toggleVisibility(5);
	}

	/**
	 * Changes the visibility of elements on the layout
	 * @param group: from 1 to 6, 
	 */
	private void toggleVisibility(int group){
		switch(group){
		case 1:
			save.setVisibility(View.VISIBLE);
			goback.setVisibility(View.VISIBLE);
			review.setVisibility(View.VISIBLE);
			cancel.setVisibility(View.VISIBLE);
			break;
		case 2:
			mVideoPreview.setVisibility(View.INVISIBLE);
			shutter.setVisibility(View.INVISIBLE);
			break;
		case 3:
			save.setVisibility(View.INVISIBLE);
			goback.setVisibility(View.INVISIBLE);
			review.setVisibility(View.INVISIBLE);
			cancel.setVisibility(View.INVISIBLE);
			break;
		case 4:
			mVideoPreview.setVisibility(View.VISIBLE);
			shutter.setVisibility(View.VISIBLE);
			break;
		case 5:
			recording.setVisibility(View.INVISIBLE);
			break;
		case 6:
			recording.setVisibility(View.VISIBLE);
			break;

		}
	}

	/**
	 * Called when the user quits the program
	 */
	private void cleanUpTemp(){
		if (temp.exists())
			temp.delete();

		if (currentVideo.exists())
			currentVideo.delete();
	}
}