/**
 * @author Khanh Pham
 * PictureDemo activity launches the camera hardware, allowing the user to take, 
 * and save a picture.
 */

package com.hfoss.summer09.cc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class CameraApp extends Activity {
	private SurfaceView preview = null;
	private SurfaceHolder previewHolder = null;
	private Camera camera = null;
	private LinkedBlockingQueue<Job> q = new LinkedBlockingQueue<Job>();
	long temp;
	File currentFile;
	String path; // path references the file and is used on the "tag" module
	Button save, cancel;
	ImageView imageview;
	boolean ready = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo);
		new Thread(qProcessor).start();
		preview = (SurfaceView) findViewById(R.id.preview);
		previewHolder = preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		save = (Button) findViewById(R.id.save);
		save.setVisibility(View.INVISIBLE);
		cancel = (Button) findViewById(R.id.cancel);
		cancel.setVisibility(View.INVISIBLE);
		imageview = (ImageView) findViewById(R.id.imageview);
		imageview.setVisibility(View.INVISIBLE);
		save.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				q.add(new KillJob());
				savePicture();
			}
		});

		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				imageview.setVisibility(View.INVISIBLE);
				imageview.setImageResource(R.drawable.photo);
				currentFile.delete();
				save.setVisibility(View.INVISIBLE);
				cancel.setVisibility(View.INVISIBLE);
				preview.setVisibility(View.VISIBLE);
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		q.add(new KillJob());
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// If the center key is pressed, take a picture
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			takePicture();
			return (true);
		}
		// If not, then use the onKeyDown code of the superclass.
		return (super.onKeyDown(keyCode, event));
	}

	private void takePicture() {
		camera.stopPreview();
		camera.takePicture(null, null, photoCallback);
	}

	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			camera = Camera.open();
			try {
				camera.setPreviewDisplay(previewHolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Camera.Parameters parameters = camera.getParameters();

			parameters.setPreviewSize(width, height);
			parameters.setPictureFormat(PixelFormat.JPEG);
			camera.setParameters(parameters);
			camera.startPreview();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	};

	Camera.PictureCallback photoCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			updateTime();
			SavePhotoJob photo = new SavePhotoJob(data);
			q.add(photo);
			path = "/sdcard/photo" + temp + ".jpg";
			currentFile = new File(path);
			preview.setVisibility(View.INVISIBLE);
			ready = false;
			while (ready == false) {
				if (currentFile.exists()) {
					imageview.setVisibility(View.VISIBLE);
					Uri uri = Uri.parse(path);
					imageview.setImageURI(uri);
					save.setVisibility(View.VISIBLE);
					cancel.setVisibility(View.VISIBLE);
					ready = true;
				}
			}
			camera.startPreview();
		}
	};

	Runnable qProcessor = new Runnable() {
		public void run() {
			while (true) {
				try {
					Job j = q.take();
					if (j.stopThread()) {
						finish();
					} else {
						j.process();
					}
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	};

	class Job {
		boolean stopThread() {
			return (false);
		}

		void process() {
		}
	}

	class KillJob extends Job {
		@Override
		boolean stopThread() {
			return (true);
		}
	}

	class SavePhotoJob extends Job {
		byte[] jpeg = null;

		SavePhotoJob(byte[] jpeg) {
			this.jpeg = jpeg;
		}

		@Override
		void process() {
			File photo = new File(Environment.getExternalStorageDirectory(),
					"photo" + temp + ".jpg");
			try {
				FileOutputStream fos = new FileOutputStream(photo.getPath());
				fos.write(jpeg);
				fos.close();
			} catch (java.io.IOException e) {
				Log.e("PictureDemo", "Exception in photoCallback", e);
			}
		}
	}

	private void updateTime() {
		temp = System.currentTimeMillis();
	}

	public void savePicture() {
		Intent intent = new Intent("com.hfoss.summer09.cc.SAVE_FIND");
		startActivity(intent);
	}
}