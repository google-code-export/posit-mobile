package org.hfoss.posit;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

import com.google.android.maps.Overlay;
import com.google.android.maps.Point;

/*
 * Overlay class that will display that can either display one picture or
 * several pictures on the map.
 */
public class SpotMapOverlay extends Overlay {

	// Holds all the photos
	private List<Point> myPoints;

	// The icon to print where the photo was taken
	private BitmapDrawable camera = null;

	// 1 picture
	public SpotMapOverlay(Point photo, Context context) {
		myPoints = new ArrayList<Point>();
		myPoints.add(photo);
		camera = (BitmapDrawable) context.getResources().getDrawable(
				R.drawable.camera);
	}

	// Several pictures
	public SpotMapOverlay(List<Point> photos, Context context) {
		myPoints = photos;
		camera = (BitmapDrawable) context.getResources().getDrawable(
				R.drawable.camera);
	}

	/*
	 * Draws the camera icon onto the map where the picture are located
	 * 
	 * (non-Javadoc)
	 * 
	 * @see com.google.android.maps.Overlay#draw(android.graphics.Canvas,
	 *      com.google.android.maps.Overlay.PixelCalculator, boolean)
	 */
	@Override
	public void draw(Canvas canvas, PixelCalculator calculator, boolean shadow) {
		for (Point p : myPoints) {
			calculator.getPointXY(p, sXYCoords);
			int w = camera.getIntrinsicWidth();
			int h = camera.getIntrinsicHeight();
			camera.setBounds(sXYCoords[0] - w / 2, sXYCoords[1] - h,
					sXYCoords[0] + w / 2, sXYCoords[1]);
			camera.draw(canvas);
		}
	}
}