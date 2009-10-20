/**
 * @author Khanh Pham
 * Audio Activity for recording and playing back audio.
 */

package com.hfoss.summer09.cc;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class AudioCapture extends Activity implements View.OnClickListener, OnInfoListener,  MediaRecorder.OnErrorListener{
	private MediaRecorder mMediaRecorder;
	private MediaPlayer mMediaPlayer;
	private Button record, playback, save, skip;
	private ImageButton circle;
	private File temp = new File("/sdcard/audio_temp.3gp");
	private int recorder_status, playback_status;
	private static final int MAX_DURATION = 60000; //miliseconds
	private static final int MAX_FILE_SIZE = 10000000;
	private static final int NOT_STARTED = 1, STARTED =2;
	private static final String TAG = "Audio Capture";
	/** Called when the activity is first created. */
	@Override

	
	/**
	 * Sets up 
	 * - buttons to capture, play, save or cancel
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audiotag);
		record = (Button) findViewById(R.id.record);
		record.setOnClickListener(this);
		playback = (Button) findViewById(R.id.playback);
		playback.setOnClickListener(this); 
		save = (Button) findViewById(R.id.save);
		save.setOnClickListener(this);
		skip = (Button) findViewById(R.id.skip);
		skip.setOnClickListener(this);
		circle = (ImageButton) findViewById(R.id.circle);
		circle.setVisibility(View.INVISIBLE);
		recorder_status = NOT_STARTED;
		playback_status = NOT_STARTED;
		mMediaPlayer = new MediaPlayer();
	}

	
	/**
	 * Implementing onClickListener
	 * Assigns tasks for 4 buttons
	 * - Record: starts recording audio
	 * - Playback: Starts/Stops playback
	 * - Save: Renames the default file to a more specific name based on when the video was recorded
	 * - Skip: removes the temp file
	 */
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.record: 
			if (recorder_status == NOT_STARTED){
				initialize();
				record();
			}
			else if (recorder_status == STARTED){
				stopRecording();
			}
			break;
		case R.id.playback:
			if (playback_status == NOT_STARTED){
				try {
					playback();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if (playback_status == STARTED){
				stopPlayback();

			}
			break;       	
		case R.id.save:
			temp.renameTo(new File("/sdcard/audio"+System.currentTimeMillis()+".3gp"));
			if (temp.exists()){
				temp.delete();
			}
			finish();
			break;
		case R.id.skip:
			if (temp.exists()){
				temp.delete();
			}
			finish();
			break;        	
		}
	}
	
	/**
	 * Setting up parameters for the audio recorder
	 */

	private void initialize(){
		mMediaRecorder = new MediaRecorder();
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mMediaRecorder.setOutputFile(temp.getPath());
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mMediaRecorder.setMaxDuration(MAX_DURATION);
		mMediaRecorder.setMaxFileSize(MAX_FILE_SIZE);
		mMediaRecorder.setOnInfoListener(this);
		try {
			mMediaRecorder.prepare();
		} catch (IOException exception) {
			Log.e(TAG, "prepare failed for " + temp.getName());
			Log.e(TAG, exception.toString());
			release();
		}
	}

	/**
	 * Releases the media recorder
	 * Called when 
	 * - Users quit the program
	 * - Errors occur
	 */
	private void release(){
		Log.v(TAG, "Releasing media recorder.");
		if (mMediaRecorder != null) {
			mMediaRecorder.reset();
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
	}
	
	/**+
	 * Called by the playback button
	 * Plays the audio file that is just recorded
	 * @throws Exception
	 * @throws IllegalStateException
	 * @throws IOException
	 */

	private void playback() throws Exception, IllegalStateException, IOException{
		playback_status = STARTED;
		playback.setText("STOP PLAYBACK");
		mMediaPlayer.setDataSource(temp.getPath());
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener(){
			public void onCompletion(MediaPlayer player) {
				playback_status = NOT_STARTED;
				playback.setText("Playback");
			}
		});
		mMediaPlayer.prepare();
		mMediaPlayer.start();
	}

	
	/**
	 * Called by the record button
	 * Stops recording and displays buttons
	 */
	private void stopRecording(){
		showButtons();
		mMediaRecorder.stop();
		circle.setVisibility(View.INVISIBLE);
		release();
		record.setText("Record");
		recorder_status = NOT_STARTED;
	}

	/**
	 * Called by the playback button
	 * Stops the playback
	 */
	private void stopPlayback(){
		mMediaPlayer.stop();
		playback_status = NOT_STARTED;
		playback.setText("Playback");

	}
	
	/**
	 * Called by the record button
	 * Starts the recording
	 * Hides playback, skip and save buttons
	 */
	private void record(){	
		hideButtons();
		circle.setVisibility(View.VISIBLE);
		record.setText("STOP RECORDING");
		recorder_status = STARTED;
		try {
			mMediaRecorder.start();   // Recording is now started
		} catch (RuntimeException e) {
			Log.e(TAG, "Could not start media recorder. ", e);
			return;
		}
	}

	/**
	 * Called when the record button is clicked
	 * Hides playback, save and skip buttons
	 */
	private void hideButtons(){

		playback.setVisibility(View.INVISIBLE);
		save.setVisibility(View.INVISIBLE);
		skip.setVisibility(View.INVISIBLE);
	}

	/**
	 * Called when the recording is finished
	 * Shows playback, save and skip buttons
	 */
	private void showButtons(){
		playback.setVisibility(View.VISIBLE);
		save.setVisibility(View.VISIBLE);
		skip.setVisibility(View.VISIBLE);	
	}
	
	/**
	 * Implementing onInfoListerner
	 * If the recorder reaches its max duration or if the storage is full,
	 * set up the "after recording" layout 
	 */

	public void onInfo(MediaRecorder mr, int what, int extra) {
		if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED || what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED){	
			showButtons();
			circle.setVisibility(View.INVISIBLE);
			release();
			record.setText("Record");
			recorder_status = NOT_STARTED;	
		}

		else {
			release();
			Log.d(TAG, "UNKNOWN ERROR at AudioCapture");
		}
	}

	
	/**
	 * Implementing onErrorListerner
	 * Logs the errors that occur
	 */
	public void onError(MediaRecorder mr, int what, int extra) {
		if (what == MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN) {
			release();
			Log.d(TAG, "UNKNOWN ERROR at AudioCapture");
		}
	}
}