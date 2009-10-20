/*******************************************************************************
 * Copyright (c) 2009 .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Prasanna Gautam - initial API and implementation
 *     Ralph Morelli - Supervisor
 *     Trishan deLanerolle - Director
 *     Antonio Alcorn - Summer 2009 Intern
 *     Gong Chen - Summer 2009 Intern
 *     Chris Fei - Summer 2009 Intern
 *     Qianqian Lin - Summer 2009 Intern 
 ******************************************************************************/
package org.hfoss.posit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;

public class ImageViewActivity extends Activity {

	
	private int mPosition;
	private Find mFind = null;
	public static final int CONFIRM_DELETE_DIALOG = 0;
	private Cursor mCursor;
	private static final String TAG = "ImageViewActivity";

	private ImageView mIV;

	private Bitmap mBm;
	
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	
	GestureDetector gestureDetector = new GestureDetector(new MyGestureDetector());
    OnTouchListener gestureListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (gestureDetector.onTouchEvent(event)) {
                return true;
            }
            return false;
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		
		mBm = (Bitmap)intent.getExtras().get("bitmap");
		if (mBm != null) {
			setContentView(R.layout.image_view);
			mIV = (ImageView)findViewById(R.id.photo_big);
			mIV.setImageBitmap(mBm);
		}
		else {
			String action = intent.getAction();
			setResult(RESULT_OK,intent);
			if(action.equals(this.getString(R.string.delete_find_image))) {
				showDialog(CONFIRM_DELETE_DIALOG);
			}
			
			mPosition = intent.getIntExtra("position",-1);
			mFind = new Find(this, intent.getLongExtra("findId",-1));
			mCursor = mFind.getImages();
			mCursor.moveToPosition(mPosition);
			
			Uri data = mFind.getImageUriByPosition(mFind.getId(), mPosition);
	
			setContentView(R.layout.image_view);
			mIV = (ImageView)findViewById(R.id.photo_big);
			mIV.setImageURI(data);
			
	
			if(mPosition>0) {
				final Button leftButton = (Button)findViewById(R.id.photo_left);
				leftButton.setVisibility(0);
		        leftButton.setOnClickListener(new View.OnClickListener() {
		            public void onClick(View v) {
	
	                	Intent intent = new Intent(ImageViewActivity.this,ImageViewActivity.class);
	                	intent.setAction(Intent.ACTION_VIEW);
	                	intent.putExtra("position",mPosition-1);
	                	intent.putExtra("findId", mFind.getId());
	                	finish();
	                	startActivity(intent);
	
		            }
		        });
			}
	
			if(mPosition<mCursor.getCount()-1) {
		        final Button rightButton = (Button)findViewById(R.id.photo_right);
		        rightButton.setVisibility(0);
		        rightButton.setOnClickListener(new View.OnClickListener() {
		            public void onClick(View v) {	       
	            		Intent intent = new Intent(ImageViewActivity.this,ImageViewActivity.class);
	                	intent.setAction(Intent.ACTION_VIEW);
	                	intent.putExtra("position",mPosition+1);
	                	intent.putExtra("findId", mFind.getId());
	                	finish();
	                	startActivity(intent);        
		            }
		        });
			}
		}
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.media_view_menu, menu);
		return true;
	} // onCreateOptionsMenu()

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.delete_item:
			showDialog(CONFIRM_DELETE_DIALOG);
			break;
		default: return false;
		}
		return true;
	}


	@Override
	protected Dialog onCreateDialog(int id) {
		
		final Intent intent = new Intent(ImageViewActivity.this, FindActivity.class);
		intent.putExtra(MyDBHelper.KEY_ID, mFind.getId());
		intent.setAction(Intent.ACTION_EDIT);
		setResult(RESULT_OK,intent);
		setResult(RESULT_OK);
		
		switch(id) {
		case CONFIRM_DELETE_DIALOG:
        	return new AlertDialog.Builder(this)
            .setIcon(R.drawable.alert_dialog_icon)
            .setTitle(R.string.alert_dialog_2)
            .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // User clicked OK so do some stuff 
                	if (mFind.deleteImageByPosition(mPosition)) { // Assumes find was instantiated in onCreate        		
                		Utils.showToast(ImageViewActivity.this, R.string.deleted_from_database);	
                		finishActivity(ListFindsActivity.FIND_FROM_LIST);
                		finish();
                		//FindActivity fa = FindActivity.newInstance();
                		startActivityForResult(intent,FindActivity.STATE_EDIT);
                	}
                	else 
                		Utils.showToast(ImageViewActivity.this, R.string.delete_failed);  
                }
            }
            )
            .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	
                    /* User clicked Cancel so do nothing */
                }
            })
            .create();
        	default: return null;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if(keyCode==KeyEvent.KEYCODE_BACK && mBm==null){
			final Intent intent = new Intent(ImageViewActivity.this, FindActivity.class);
			intent.putExtra(MyDBHelper.KEY_ID, mFind.getId());
			intent.setAction(Intent.ACTION_EDIT);
			setResult(RESULT_OK,intent);
			setResult(RESULT_OK);
			
			startActivityForResult(intent,FindActivity.STATE_EDIT);
			finish();
			return true;
		}
	return super.onKeyDown(keyCode, event);
	}
	
	class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        	if (mCursor != null) {
	        	try {
	                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
	                    return false;
	                // Right to left fling
	                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	                        Log.d(TAG, "Right to left fling");
	                        if(mPosition<mCursor.getCount()-1) {
	                        	Intent intent = new Intent(ImageViewActivity.this,ImageViewActivity.class);
	                        	intent.setAction(Intent.ACTION_VIEW);
	                        	intent.putExtra("position",mPosition+1);
	                        	intent.putExtra("findId", mFind.getId());
	                        	finish();
	                        	startActivity(intent);
	                        }
	
	                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	
	                        Log.d(TAG, "Left to right fling");
	                        if(mPosition>0) {
	                        	Intent intent = new Intent(ImageViewActivity.this,ImageViewActivity.class);
	                        	intent.setAction(Intent.ACTION_VIEW);
	                        	intent.putExtra("position",mPosition-1);
	                        	intent.putExtra("findId", mFind.getId());
	                        	finishActivity(ListFindsActivity.FIND_FROM_LIST);
	                        	finish();
	                        	startActivity(intent);
	                        }
	                	}
	            }
	             catch (Exception e) {
	                Utils.showToast(ImageViewActivity.this, e.toString());
	            }
        	}
            return false;
        
        }
	}
	@Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event))
                return true;
            else
                return false;
    }


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(TAG,"ImageView onActivtyResult()");
		if (resultCode == RESULT_CANCELED)
			return;
		switch (requestCode) {
		case ListFindsActivity.FIND_FROM_LIST:
			Log.i(TAG,"finish old activity, start new");
			//finish();
			startActivity(data);
		}
	} 
	
}