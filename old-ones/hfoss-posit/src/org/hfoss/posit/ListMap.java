package org.hfoss.posit;

import java.util.ArrayList;
import java.util.List;

import org.hfoss.posit.db.PositData;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayController;
import com.google.android.maps.Point;

public class ListMap extends MapActivity implements
AdapterView.OnItemSelectedListener
{
// This is only used when we need logging
private static final String TAG = "SpotLog";

// Index to be used when accessing teh cursor
private static final int LATITUDE_INDEX = 2;

private static final int LONGITUDE_INDEX = 3;

private static final int BITMAP_INDEX = 5;

private Uri mURI;

private Cursor mCursor;

private MapView mMapView = null;

private MapController mMapController = null;

private Overlay mMapOverlay = null;

private OverlayController mMapOverlayController = null;

// Minimum & maximum latitude so we can span it
// The latitude is clamped between -80 degrees and +80 degrees inclusive
// thus we ensure that we go beyond that number
private int minLatitude = (int) (+81 * 1E6);

private int maxLatitude = (int) (-81 * 1E6);

// Minimum & maximum longitude so we can span it
// The longitude is clamped between -180 degrees and +180 degrees inclusive
// thus we ensure that we go beyond that number
private int minLongitude = (int) (+181 * 1E6);;

private int maxLongitude = (int) (-181 * 1E6);;

private static final String[] PROJECTION = new String[]{
	PositData.Photos._ID,
	PositData.Photos.NAME,
	PositData.Photos.DESCRIPTION,
	PositData.Photos.BITMAP,
	PositData.Photos.LATITUDE,
	PositData.Photos.LONGITUDE,
	PositData.Photos.CREATED_DATE,
	PositData.Photos.MODIFIED_DATE

};

@Override
protected void onCreate(Bundle icicle)
{
super.onCreate(icicle);

// MapView setup
setContentView(R.layout.spotmap);

mMapView = new MapView(this);
mMapView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.FILL_PARENT));

LinearLayout rl = (LinearLayout) findViewById(R.id.map_view);
rl.addView(mMapView);

// Get the corresponding picture from the database
// It can be 1 picture as much as all the pictures as
// defined in the manifest
final Intent intent = getIntent();
intent.setData(PositData.Photos.CONTENT_URI);
mURI = intent.getData();
mCursor = managedQuery(mURI, PROJECTION, null, null);
if (mCursor != null)
{
    setupMap();
    
    // Only when there are several pictures
    if (mURI.compareTo( PositData.Photos.CONTENT_URI) == 0)
    {
        Gallery g = (Gallery) findViewById(R.id.gallery);
        g.setAdapter(new ImageAdapter(mCursor,this));
        g.setSelectorSkin(getResources().getDrawable(
                android.R.drawable.box));
        g.setOnItemSelectedListener(this);
    }

}
}

/*
* Basic Map setup
*/
private void setupMap()
{

// Holds all the picture location as Point
List<Point> mPoints = new ArrayList<Point>();

while (mCursor.next())
{
    int latitude = mCursor.getInt(LATITUDE_INDEX);
    int longitude = mCursor.getInt(LONGITUDE_INDEX);

    // Sometimes the longitude or latitude gathering
    // did not work so skipping the point
    // doubt anybody would be at 0 0
    if (latitude != 0 && longitude != 0)
    {

        // Sets the minimum and maximum latitude so we can span and zoom
        minLatitude = (minLatitude > latitude) ? latitude : minLatitude;
        maxLatitude = (maxLatitude < latitude) ? latitude : maxLatitude;

        // Sets the minimum and maximum latitude so we can span and zoom
        minLongitude = (minLongitude > longitude) ? longitude
                : minLongitude;
        maxLongitude = (maxLongitude < longitude) ? longitude
                : maxLongitude;

        mPoints.add(new Point(latitude, longitude));
    }
}

// Get the controller
mMapController = mMapView.getController();

// Zoom to span from the list of points
mMapController.zoomToSpan((maxLatitude - minLatitude),
        (maxLongitude - minLongitude));

// Animate to the center of the list of points
mMapController.animateTo(new Point((maxLatitude + minLatitude) / 2,
        (maxLongitude + minLongitude) / 2));

// Add all the point to the overlay
mMapOverlay = new SpotMapOverlay(mPoints, this);
mMapOverlayController = mMapView.createOverlayController();

// Add the overlay to the mapview
mMapOverlayController.add(mMapOverlay, true);
}

// Will reposition the map to the photo
public void onItemSelected(AdapterView parent, View v, int position, long id)
{
Cursor tmpCursor = getContentResolver().query(mURI, PROJECTION,
        "_id=" + id, null, null);
if (tmpCursor != null)
{
    tmpCursor.first();
    int latitude = tmpCursor.getInt(LATITUDE_INDEX);
    int longitude = tmpCursor.getInt(LONGITUDE_INDEX);
    if (latitude != 0)
        mMapController.animateTo(new Point(latitude, longitude));
    else
    {
        Log.i(TAG, "GeoLocation not set for photo id "
                + tmpCursor.position());
    }
}
}

// Do nothing
public void onNothingSelected(AdapterView arg0)
{
}

// Only work for key down.. Is it because of the gallery?
public boolean onKeyDown(int keyCode, KeyEvent keyEvent)
{
switch (keyCode)
{
    case KeyEvent.KEYCODE_DPAD_DOWN:
        Intent intent = new Intent();
        // intent.setData(MSpots.MySpots.CONTENT_URI.addId(1));

        intent.setData(ContentUris.appendId(
                PositData.Photos.CONTENT_URI.buildUpon(), 1).build());
        intent.setAction(Intent.VIEW_ACTION);
        Log.i("MyLog", intent.toString());
        startActivity(intent);
        break;
}
return super.onKeyDown(keyCode, keyEvent);
}
public class ImageAdapter extends CursorAdapter
{

    private Context mContext;

    public ImageAdapter(Cursor c, Context context)
    {
        super(c, context);
        mContext = context;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        Bitmap bp = BitmapFactory
                .decodeFile(cursor.getString(BITMAP_INDEX));

        ImageView i = (ImageView) view;
        i.setImageBitmap(bp);
        i.setAdjustViewBounds(true);
        i.setLayoutParams(new Gallery.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        i.setBackground(android.R.drawable.picture_frame);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        return new ImageView(mContext);
    }

    public float getAlpha(boolean focused, int offset)
    {
        return Math.max(0.2f, 1.0f - (0.2f * Math.abs(offset)));
    }

    public float getScale(boolean focused, int offset)
    {
        return Math.max(0, offset == 0 ? 1.0f : 0.6f);
    }
}
}