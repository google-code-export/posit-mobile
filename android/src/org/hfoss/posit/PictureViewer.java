

package org.hfoss.posit;

import org.hfoss.posit.util.ImageAdapter;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery.LayoutParams;
import android.widget.ViewSwitcher.ViewFactory;

/**
 * Essentially a gallery that shows the pictures listed on the database based on their Ids.
 * It uses an ImageAdapter Gallery to show the files.
 * @author pgautam
 *
 */
public class PictureViewer extends Activity implements
        AdapterView.OnItemSelectedListener, ViewSwitcher.ViewFactory, AdapterView.OnItemClickListener {
private Long recordid;
private DBHelper dbHelper;
private Cursor c;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        dbHelper = new DBHelper(this);
    	dbHelper.open();
    	
    
        setContentView(R.layout.image_gallery);
        
        recordid = icicle != null ? icicle.getLong(DBHelper.KEY_ROWID) : null;
		if (recordid == null){
		Bundle extras = getIntent().getExtras();
		recordid = extras != null ? extras.getLong(DBHelper.KEY_ROWID): 0;
		}
		c = dbHelper.findImages(recordid);
        mSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
        mSwitcher.setFactory(this);
        mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in));
        mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out));

        Gallery g = (Gallery) findViewById(R.id.gallery);
        g.setAdapter(new ImageAdapter(c,this));
        g.setOnItemSelectedListener(this);
        g.setOnItemClickListener(this);
       /* g.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView parent, View v, int position,
					long id) {
				Log.i("test","it works");
				openEditDialog();
			}      	
     });*/
        
    }


    public void onItemSelected(AdapterView parent, View v, int position, long id) {
    	Drawable d = Drawable.createFromPath(c.getString(c.getColumnIndex(DBHelper.KEY_FILENAME)));
    	mSwitcher.setImageDrawable(d);
    }
    
    
    
	
	
	public void onItemClick(AdapterView parent, View v, int position,
			long id) {
		Log.i("test","true");
		openEditDialog();
		Drawable d = Drawable.createFromPath(c.getString(c.getColumnIndex(DBHelper.KEY_FILENAME)));
    	mSwitcher.setImageDrawable(d);

	}


	private void openEditDialog() {
			NewInstanceDialog nInt = new NewInstanceDialog(this);
			nInt.show();
			
		
	}

	public void onNothingSelected(AdapterView parent) {
    }

    public View makeView() {
        ImageView i = new ImageView(this);
        i.setBackgroundColor(0xFF000000);
        i.setScaleType(ImageView.ScaleType.FIT_CENTER);
        i.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
        return i;
    }

    private ImageSwitcher mSwitcher;


}
