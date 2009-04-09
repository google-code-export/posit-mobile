package org.hfoss.posit.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewAnimator;
/**
 * Shows the location dialog with the buttons.
 * @author pgautam
 *
 */
public class LocationDialog extends Dialog {

	/*
	 * Static and final variable for general sentences
	 */
	private final static String TITLE = "Locating you!";

	private final static String GETTING_PROVIDER_MSG = "Getting location providers";

	private final static String SELECT_PROVIDER_MSG = "Select the location provider from the list";

	private final static String LOCATING_MSG = "Locating you";

	// Timeout for the gps - 1 minute
	private final static long GPS_TIMEOUT = 60000;

	// Need handler for callbacks to the UI thread
	final Handler mHandler = new Handler();

	// This list will contain all available location providers
	private List<LocationProvider> mLocationProviderList = new ArrayList<LocationProvider>();

	// The current location variable
	private Location mLocation;

	private LocationManager mLocationManager;

	private LocationProvider mLocationProvider;

	// the criteria, can be null
	private Criteria mCriteria = null;

	private Context mContext = null;

	// The message to sent back to the calling activity
	// The hash key is 'location'
	private Message mMessage;

	// The dialog view defined below
	private LocationDialogView mLocationDialogView;

	// Default constructor, the criteria can be null. The message needs to be
	// attached to a handler.
	public LocationDialog(Context context, Criteria criteria, Message message,
			OnCancelListener cancelListener) {
		super(context, true, cancelListener);

		this.setTitle(TITLE);

		mContext = context;
		mCriteria = criteria;
		mMessage = message;

		mLocationDialogView = new LocationDialogView(mContext);
		setContentView(mLocationDialogView);
	}

	/*
	 * Calling show will show the dialog but also launch the locating device.
	 * this is the default function.
	 *
	 * (non-Javadoc)
	 *
	 * @see android.app.Dialog#show()
	 */
	public void show() {
		// This will show the dialog in the UI
		super.show();
		// Start the threading and long running threads.
		acquiringLocationList();
	}

	/*
	 * First post back when we receive a list of the location provider. This
	 * will show the next view when we call setLocationList.
	 */
	final Runnable mUpdateLocationManagerList = new Runnable() {
		public void run() {
			// Stop the progress bar - this is not really visible
			mLocationDialogView.isComputing(false);

			// Sets the location list (in the view it is a spinner) and call
			// the next view
			mLocationDialogView.setLocationList(mLocationProviderList);
		}
	};

	/*
	 * Send the location back to the calling Activity via a Message this will
	 * also dismiss the dialog
	 */
	final Runnable mUpdateLocation = new Runnable() {
		public void run() {
			// stop the progress bar
			mLocationDialogView.isComputing(false);

			// Create the hashmap that will be sent back to the calling activity
			// The hashmap is composed of a String - the key - and the location
			// itself as Location.
			HashMap<String, Location> location = new HashMap<String, Location>();

			// Sets the location key and value to the hashmap
			location.put("location", mLocation);

			// Attach the hashmap to the message
			mMessage.setData(location);

			// Send it to the handler in the calling activity
			mMessage.sendToTarget();

			// Dismiss the dialog, everything went fine.
			dismiss();
		}
	};

	/*
	 * Actual method that will acquire the position and send it back to the
	 * calling activity. Actually it is the handler that will do so.
	 */
	protected void getPosition() {
		// set the progress bar
		mLocationDialogView.isComputing(true);
		Thread t = new Thread() {
			public void run() {
				// Time when we start acquiring the location
				Long now = Long.valueOf(System.currentTimeMillis());

				mLocation = mLocationManager
						.getLastKnownLocation(mLocationProviderList.get(
								mLocationDialogView
										.getSelectedLocationProvider())
								.getName());

				// Hack to ensure the location is defined correctly
				// Will have to work on this
				// I also tried the availability of a location provider but
				// it did not work
				// This seems the only way to ensure the value are real values
				while (mLocation.getLatitude() == 0.0) {
					// Break if it is longer then the time out
					if (Long.valueOf(System.currentTimeMillis()) - now > GPS_TIMEOUT)
						break;
					mLocation = mLocationManager
							.getLastKnownLocation(mLocationProvider.getName());
					try {
						// Sleep for 1 second
						Thread.sleep(1000);
					} catch (Exception e) {
						Log.e("ThreadError", "Can not sleep for 1 second");
					}
				}
				mHandler.post(mUpdateLocation);
			}
		};
		t.start();
	}

	/*
	 * Method that will acquire the location list. I was not really sure if I
	 * should use another thread but I noticed - especially at first boot - that
	 * the location provider takes some time to initialize.
	 */
	protected void acquiringLocationList() {
		// Set the progress bar
		mLocationDialogView.isComputing(true);
		Thread t = new Thread() {
			public void run() {
				mLocationManager = (LocationManager) mContext
						.getSystemService(Context.LOCATION_SERVICE);
				
				mLocationProviderList = (mCriteria == null) ? mLocationManager
						.getProviders() : mLocationManager.getProviders();
				mHandler.post(mUpdateLocationManagerList);
			}
		};
		t.start();
	}

	/*
	 * Dialog View. This view is composed of 3 views and 2 buttons. There is 1
	 * cancel button as this is cancellable. If the cancel button is clicked, it
	 * calls cancel() from the dialog interface. Similarly if you click the back
	 * button, the cancel is called (this is done automatically).
	 *
	 * The locate button is only used in the third view. When clicked it locates
	 * the phone accordingly to the location provider chosen.
	 *
	 * The first and third view just holds a message and a progress bar as it
	 * does not need any user input. The second view lets the user choose the
	 * location provider he wants to use.
	 *
	 * The location provider list comes from the parent class.
	 */
	private class LocationDialogView extends LinearLayout {

		private Context mContext;

		// First View Progress bar
		private ProgressBar mProgressBar;

		// Third View Progress bar
		// Could not reuse the above one
		private ProgressBar mProgressBar2;

		// The view animator holds the 3 views so we can easily switch from one
		// to the other
		private ViewAnimator mViewAnimator;

		// Message diplayed when getting the list of provider
		// this is define in the parent class by:
		// GETTING_PROVIDER_MSG
		private TextView gettingLocationProvider;

		// Message to display when the user has to choose
		// between the location providers. This is define
		// in the parent class:
		// SELECT_PROVIDER_MSG
		private TextView selectLocationList;

		// Message for when the phone is being located
		// this is define in the parent class with:
		// LOCATING_MSG
		private TextView locatingPhone;

		// The spinner that will let the user choose the location
		// provider he wants to use
		private Spinner locationProviderList;

		// The locate button used in the third view to locate
		// the phone
		private Button locate;

		// Cancel button which is attached to a cancel listener
		// from the dialog interface
		private Button cancel;

		// Convenience variable as I use it quite frequently
		private final LinearLayout.LayoutParams ll_w_w = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);

		private final LinearLayout.LayoutParams ll_f_w = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);

		public LocationDialogView(Context context) {
			super(context);
			mContext = context;
			setLayoutParams(ll_w_w);
			setOrientation(LinearLayout.VERTICAL);
			initiateViewAnimator();
			initiateButtons();
		}

		/*
		 * Mainly UI initialization for the view animator
		 *
		 * we have 3 views: 1. First view is for getting the location providers
		 * list (no user input) 2. Second view list the location providers (user
		 * input to choose the lp) 3. Third view is the one locating the phone
		 * (no user input)
		 */
		private void initiateViewAnimator() {
			mViewAnimator = new ViewAnimator(mContext);
			mViewAnimator.setLayoutParams(ll_w_w);

			// Setup animation
			mViewAnimator.setInAnimation(AnimationUtils.loadAnimation(mContext,
					android.R.anim.fade_in));
			mViewAnimator.setOutAnimation(AnimationUtils.loadAnimation(
					mContext, android.R.anim.fade_out));

			// First view
			//
			LinearLayout firstView = new LinearLayout(mContext);
			firstView.setOrientation(LinearLayout.HORIZONTAL);

			mProgressBar = new ProgressBar(mContext);
			firstView.addView(mProgressBar, ll_w_w);

			gettingLocationProvider = new TextView(mContext);
			gettingLocationProvider
					.setText(LocationDialog.GETTING_PROVIDER_MSG);
			gettingLocationProvider.setPadding(10, 10, 0, 0);
			firstView.addView(gettingLocationProvider, ll_w_w);

			mViewAnimator.addView(firstView, ll_w_w);

			// Second View with Location Provider Listing
			//
			LinearLayout secondView = new LinearLayout(mContext);
			secondView.setOrientation(LinearLayout.VERTICAL);

			selectLocationList = new TextView(mContext);
			selectLocationList.setText(LocationDialog.SELECT_PROVIDER_MSG);
			secondView.addView(selectLocationList, ll_f_w);

			locationProviderList = new Spinner(mContext);
			secondView.addView(locationProviderList, ll_f_w);

			mViewAnimator.addView(secondView, ll_w_w);

			// Third view Locating the phone
			//
			LinearLayout thirdView = new LinearLayout(mContext);
			thirdView.setOrientation(LinearLayout.HORIZONTAL);

			// Only using 1 progress bar
			mProgressBar2 = new ProgressBar(mContext);
			thirdView.addView(mProgressBar2, ll_w_w);

			locatingPhone = new TextView(mContext);
			locatingPhone.setText(LocationDialog.LOCATING_MSG);
			locatingPhone.setPadding(10, 10, 0, 0);
			thirdView.addView(locatingPhone, ll_w_w);

			mViewAnimator.addView(thirdView, ll_w_w);

			// Add the view animator to the overall LinearLayout
			addView(mViewAnimator);
		}

		/*
		 * Initiate teh buttons
		 */
		private void initiateButtons() {
			locate = new Button(mContext);
			locate.setText("locate");

			// At first the button should not be enable
			locate.setEnabled(false);
			locate.setFocusable(false);

			locate.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					// Show the third view
					mViewAnimator.showNext();
					// final call to get the position of the phone
					getPosition();
				}
			});

			// Cancel button
			cancel = new Button(mContext);
			cancel.setText("cancel");

			cancel.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					cancel();
				}
			});

			LinearLayout buttons = new LinearLayout(mContext);
			buttons.setOrientation(LinearLayout.HORIZONTAL);
			buttons.setPadding(0, 10, 0, 5);

			buttons.addView(locate, ll_w_w);
			buttons.addView(cancel, ll_w_w);

			addView(buttons, ll_f_w);

		}

		/*
		 * Set the list of location provider to the spinner. This is called by
		 * the parent class. If we receive a list of location providers, then
		 * switch to the second view and initiate the spinner with the list of
		 * location provider.
		 */
		private void setLocationList(List<LocationProvider> locationList) {

			// Create the adapter from the location providers list
			LocationListAdapter adapter = new LocationListAdapter(mContext,
					locationList);
			// Set the adapter to the spinner
			locationProviderList.setAdapter(adapter);
			// Show the second view which will let the user choose his location
			// provider
			mViewAnimator.showNext();

			// We can click on locate now
			locate.setEnabled(true);
			locate.setFocusable(true);

			// Ensure the view is invalidated otherwise it stays unfocused
			this.invalidate();
		}

		/*
		 * conveniance method to enable or diable the progress bar here we have
		 * 2 progress bar. In any case we will only have 1 at a time
		 */
		public void isComputing(boolean progress) {
			mProgressBar.setIndeterminate(progress);
			mProgressBar2.setIndeterminate(progress);
		}

		/*
		 * Return the index of the location Provider
		 */
		public int getSelectedLocationProvider() {
			return locationProviderList.getSelectedItemPosition();
		}

		/*
		 * Simple adapter that will take a list of Location providers as list
		 * adapter
		 */
		private class LocationListAdapter extends BaseAdapter {

			private List<LocationProvider> mLocProList = new ArrayList<LocationProvider>();

			private Context mContext;

			public LocationListAdapter(Context context,
					List<LocationProvider> locProList) {
				mLocProList = locProList;
				mContext = context;
			}

			public int getCount() {
				return mLocProList.size();
			}

			public Object getItem(int position) {
				return mLocProList.get(position);
			}

			public long getItemId(int position) {
				return position;
			}

			/*
			 * Will set the name of the provider to a text view (non-Javadoc)
			 *
			 * @see android.widget.Adapter#getView(int, android.view.View,
			 *      android.view.ViewGroup)
			 */
			public View getView(int position, View convertView, ViewGroup parent) {
				LocationProvider locPro = mLocProList.get(position);

				TextView mTV = new TextView(mContext);
				mTV.setText(locPro.getName());
				mTV.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT));

				return mTV;
			}
		}

	}
}