package org.hfoss.posit.util;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

import org.hfoss.posit.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.Log;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 * StaticOverlays over the MapViews based on the given DataPoint objects.
 * @author pgautam
 *
 */
public class StaticOverlay extends Overlay {
	private List<DataPoint> mapPoints;
	
	private String stringOverlayName = "";
	private DataPoint selectedPoint;
	private Paint	innerPaint, borderPaint, textPaint;
	private Bitmap bubbleIcon, shadowIcon;
	private Context mCtx; /*reference to the context*/
	private MapView mapView;
	private final String APP="StaticOverlay";
	private float FONT_SIZE = 12f;
	/**
	 * Initialize using single DataPoint
	 * @param latVal
	 * @param longVal
	 */
	public StaticOverlay(DataPoint dataPoint, Context ctx) {
		mapPoints = new ArrayList<DataPoint>();
		mapPoints.add(dataPoint);
		mCtx = ctx; 
		init_icons(mCtx);
	}
	/**
	 * Takes a list of DataPoints and shows it on the MapView applied/added to.
	 * @param points
	 * @param ctx
	 */
	public StaticOverlay(List<DataPoint> points,MapView mMapView, Context ctx) {
		mapPoints = points;
		mCtx= ctx;
		mapView = mMapView;
		init_icons(mCtx);
	}

	private  void init_icons(Context mCtx){
		
		bubbleIcon = BitmapFactory.decodeResource(mCtx.getResources(), R.drawable.bubble);
		shadowIcon = BitmapFactory.decodeResource(mCtx.getResources(),R.drawable.shadow);
	}
	/**
	 * Sets the name to display above the overlayed field
	 * @param name
	 */
	public void setName(String name) {
		stringOverlayName = name;
	}

	/*dummy method to be implemented later*/
	/*TODO the color should be taken from the array colors so that they can
	 * can be set by the user at the time of initialization or later. 
	 * Will be useful for mapping.
	 */
	public void setColor() {

	}

	@Override
	public void draw(Canvas canvas,  MapView v, boolean shadow) {
		for (DataPoint p : mapPoints) {
			//calculator.getPointXY(p.mPoint, sXYCoords);

			
			Paint paint = new Paint();
			paint.setStyle(Style.FILL);

			if (shadow) {
	    		//  Only offset the shadow in the y-axis as the shadow is angled so the base is at x=0; 
	    		canvas.drawBitmap(shadowIcon, sXYCoords[0], sXYCoords[1] - shadowIcon.height(),null);
	    	} else {
    			canvas.drawBitmap(bubbleIcon, sXYCoords[0] - bubbleIcon.width()/2, sXYCoords[1] - bubbleIcon.height(),null);
	    	}

			canvas.drawText(p.mName, sXYCoords[0]-9 , sXYCoords[1]+9, paint);
			
		}
		//selectedPoint = mapPoints.get(0);
		
		//drawInfoWindow(canvas, v, true,shadow);

	}

	
	@Override
	public boolean onTap(DeviceType deviceType, Point p,
			PixelCalculator calculator) {
		
		//return super.onTap(deviceType, p, calculator);
		boolean isRemovePriorPopup = selectedPoint != null;  
		Log.i(APP,"Tapped");
		//  Next test whether a new popup should be displayed
		selectedPoint = getHitMapLocation(calculator,p);
		if ( isRemovePriorPopup || selectedPoint != null) {
			Log.i(APP,"Invalidate");
			mapView.invalidate();
		}		
		
		//  Lastly return true if we handled this onTap()
		return selectedPoint != null;
	}
//Idea from http://blog.pocketjourney.com/2008/03/19/tutorial-2-mapview-google-map-hit-testing-for-display-of-popup-windows/
	private DataPoint getHitMapLocation(MapView calculator, Point	tapPoint) {

		DataPoint hitPoint = null;
		int[] screenCoords = new int[2];
		RectF hitTestRectangle = new RectF();
		ListIterator<DataPoint> testIterator = mapPoints.listIterator();
		int i = 0;
		while (testIterator.hasNext()){
			DataPoint testPoint = testIterator.next();
			calculator.getPointXY(testPoint.getPoint(), screenCoords);
			hitTestRectangle.set(-bubbleIcon.getWidth()/2,-bubbleIcon.getHeight(),bubbleIcon.getWidth()/2,0);
			Log.i(APP,"Point Lat"+testPoint.getPoint().getLatitudeE6()+" Long"+testPoint.getPoint().getLongitudeE6());
			/*hitTestRectangle.set(-camera.getIntrinsicWidth()/2,-camera.getIntrinsicHeight(),camera.getIntrinsicWidth(),0);*/
			hitTestRectangle.offset(screenCoords[0], screenCoords[1]);
			/*Log.i(APP,camera.getIntrinsicWidth()/2+" "+camera.getIntrinsicHeight());*/
			Log.i(APP,"0->"+screenCoords[0]+" 1->" +screenCoords[1]);
			Log.i(APP,hitTestRectangle.left+" "+hitTestRectangle.top+" "+hitTestRectangle.right+" "+hitTestRectangle.bottom);
			calculator.getPointXY(tapPoint, screenCoords);
			if (hitTestRectangle.contains(screenCoords[0],screenCoords[1]))
			
			{
				hitPoint = testPoint;
				Log.i(APP,"HIT!!");
				break;
			}
			
		}
		//hitPoint = mapPoints.get(0);
		tapPoint = null;
		return hitPoint;
	}

	private void drawInfoWindow(Canvas canvas, MapView v,
			boolean shadow) {
		if (selectedPoint != null) {
			// First determine the screen coordinates of the selected
			// MapLocation
			int[] selDestinationOffset = new int[2];
//			calculator.getPointXY(selectedPoint.getPoint(),
//					selDestinationOffset);
			Point p=null;
			v.getProjection().toPixels(selectedPoint.getPoint(),p);
			// Setup the info window with the right size & location
			int INFO_WINDOW_WIDTH = 125;
			int INFO_WINDOW_HEIGHT = 25;
			RectF infoWindowRect = new RectF(0, 0, INFO_WINDOW_WIDTH,
					INFO_WINDOW_HEIGHT);
			int infoWindowOffsetX = selDestinationOffset[0] - INFO_WINDOW_WIDTH
					/ 2;
			int infoWindowOffsetY = selDestinationOffset[1]
					- INFO_WINDOW_HEIGHT - 32;
			infoWindowRect.offset(infoWindowOffsetX, infoWindowOffsetY);

			// Draw inner info window
			canvas.drawRoundRect(infoWindowRect, 5, 5, getInnerPaint());

			// Draw border for info window
			canvas.drawRoundRect(infoWindowRect, 5, 5, getBorderPaint());

			// Draw the MapLocation's name
			int TEXT_OFFSET_X = 10;
			int TEXT_OFFSET_Y = 15;
			String printString = "Name:" + selectedPoint.mName;
					/*+ "\nDescription:" + selectedPoint.mDescription;*/
			canvas.drawText(printString, infoWindowOffsetX + TEXT_OFFSET_X,
					infoWindowOffsetY + TEXT_OFFSET_Y, getTextPaint());
		}
	}

	
	
	private void drawInfoWindow(Canvas canvas, MapView v,boolean text,
			boolean shadow) {
		if (selectedPoint != null) {
			// First determine the screen coordinates of the selected
			// MapLocation
			String iText = TextUtils.genTextFromHashMap(selectedPoint.getMaps(),":");

			Log.i(APP,iText);
			Pattern linesPattern = Pattern.compile("\\n");
			/*WordWrap w = new WordWrap(iText,12);
			String x = "";
			while (w.hasNext()) {
				x+=w.next();
				Log.i(APP,x);
			}*/
			//WordWrap w = WordWrap();
			String x =WordWrap.wrap(iText, 25);
			String[] lines = linesPattern.split(x);
			
			int[] selDestinationOffset = new int[2];
			
			calculator.getPointXY(selectedPoint.getPoint(),
					selDestinationOffset);

			// Setup the info window with the right size & location
			int INFO_WINDOW_WIDTH = 165;
			int INFO_WINDOW_HEIGHT = lines.length*(int)FONT_SIZE+15;
			RectF infoWindowRect = new RectF(0, 0, INFO_WINDOW_WIDTH,
					INFO_WINDOW_HEIGHT);
			int infoWindowOffsetX = selDestinationOffset[0] - INFO_WINDOW_WIDTH
					/ 2;
			int infoWindowOffsetY = selDestinationOffset[1]
					- INFO_WINDOW_HEIGHT - 32;
			infoWindowRect.offset(infoWindowOffsetX, infoWindowOffsetY);

			// Draw inner info window
			canvas.drawRoundRect(infoWindowRect, 5, 5, getInnerPaint());

			// Draw border for info window
			canvas.drawRoundRect(infoWindowRect, 5, 5, getBorderPaint());

			// Draw the MapLocation's name
			int TEXT_OFFSET_X = 10;
			int TEXT_OFFSET_Y = 15;
			/*String printString = "Name:" + selectedPoint.mName
					+ "\nDescription:" + selectedPoint.mDescription;*/
			for (String printString:lines) {
				canvas.drawText(printString, infoWindowOffsetX + TEXT_OFFSET_X,
					infoWindowOffsetY + TEXT_OFFSET_Y, getTextPaint());
				TEXT_OFFSET_Y+=15;
			}
		}
	}

	public Paint getInnerPaint() {
		if ( innerPaint == null) {
			innerPaint = new Paint();
			innerPaint.setARGB(225, 75, 75, 75); //gray
			innerPaint.setAntiAlias(true);
		}
		return innerPaint;
	}

	public Paint getBorderPaint() {
		if ( borderPaint == null) {
			borderPaint = new Paint();
			borderPaint.setARGB(255, 255, 255, 255);
			borderPaint.setAntiAlias(true);
			borderPaint.setStyle(Style.STROKE);
			borderPaint.setStrokeWidth(2);
		}
		return borderPaint;
	}

	public Paint getTextPaint() {
		if ( textPaint == null) {
			textPaint = new Paint();
			textPaint.setARGB(255, 255, 255, 255);
			textPaint.setTextSize(FONT_SIZE);
			textPaint.setAntiAlias(true);
		}
		return textPaint;
	}
}
