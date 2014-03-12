package com.hci.cyclenav;

import java.text.DecimalFormat;
import java.util.ArrayList;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hci.cyclenav.util.ArrowAnimation;
import com.hci.cyclenav.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.hci.cyclenav.guidance.GuidanceNode;
import com.hci.cyclenav.guidance.GuidanceRoute;
import com.hci.cyclenav.guidance.NavigationUtil;
import com.hci.cyclenav.util.SystemUiHider;
import com.mapquest.android.maps.GeoPoint;

/**
 * ArrowNavigaiton.java Layout:
 * cycle-nav/res/layout/activity_arrrow_navigation.xml
 * 
 * Turn-by-turn arrow navigation
 */

public class ArrowNavigation extends Activity {
	// Store navigation data and reference user location via locationManager
	private GuidanceRoute route;
	private LocationManager locationManager;
	private LocationListener locationListener;
	protected static String setOrientation = "True";
	protected static String noSetOrientation = "False";
	NavigationUtil navUtil = new NavigationUtil();

	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_arrow_navigation);

		// UI init & configuration
		final View[] controlsView = 
			{findViewById(R.id.menuButton)};
		final View contentView = findViewById(R.id.fullscreen_content_controls);
		setupUI(controlsView, contentView);

		// Get route from previous activity
		Intent intent = getIntent();
		ArrayList<GuidanceNode> nodes = intent
				.getParcelableArrayListExtra(GuidanceNarrative.GUIDANCE_NODES);
		route = new GuidanceRoute(nodes);

		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		// Define a listener that responds to location updates
		locationListener = arrowLocationListener();

		/**
		 * Register the listener with the Location Manager to receive location
		 * updates Params: String provider, long wait_in_ms, float
		 * delta_distance_in_meters, listener)
		 */
		Criteria criteria = new Criteria();
		String provider = locationManager.getBestProvider(criteria, false);
		locationManager
				.requestLocationUpdates(provider, 0, 0, locationListener);

		// Manually trigger an update using last known location
		Location location = locationManager.getLastKnownLocation(provider);
		if (location != null)
			locationListener.onLocationChanged(location);
	}
	
	public void onPopUpBt(View view) {
		final Context context = this;
		PopupMenu menu = new PopupMenu(this, view);
		menu.getMenuInflater().inflate(R.menu.popmenu, menu.getMenu());
		menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem item) {
				// TODO Auto-generated method stub
				switch (item.getItemId()) {
				case R.id.begin_new_route:
					Intent home = new Intent(context, MainActivity.class);
					startActivity(home);
					return true;
					
				case R.id.help:
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
						builder.setTitle(R.string.help);
				        builder.setMessage(R.string.helpTxt)
				        	.setNeutralButton(R.string.helpexit, new DialogInterface.OnClickListener() {
				        		public void onClick(DialogInterface dialog, int id) {
				        			dialog.cancel();
				        		}
					        });

				        AlertDialog alert = builder.create();
					    alert.show();
					    return true;
				}
			return false;
			}
	
		});
		menu.show();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	protected void setupUI(final View[] controls, final View contentView) {
		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							for (View controlview : controls)
								controlview
									.animate()
									.alpha(visible ? 1 : 0)
									.setDuration(1000);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							for (View controlview : controls)
								controlview.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		//findViewById(R.id.dummy_button).setOnTouchListener(
			//	mDelayHideTouchListener);

	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	// enable features of the overlay
	@Override
	protected void onResume() {
		super.onResume();
	}

	// disable features of the overlay when in the background
	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(locationListener);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.activity_arrow_navigation);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: LocationListener Implementation */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */

	protected LocationListener arrowLocationListener() {
		return new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				
				/**
				 * This method is called whenever the user location gets
				 * a new fix.  It is probably a good idea to eventually offload some
				 * of the non-UI proximity calculations to other methods
				 * either static, in navUtil or in guidanceroute would
				 * be good strategies.
				 */
				StringBuilder info = new StringBuilder();
				StringBuilder proximity = new StringBuilder();
				GuidanceNode previous = route.getCurrent();
				GuidanceNode next = route.peekNext();

				GeoPoint previousPoint = previous.getLocation();
				GeoPoint nextPoint = next.getLocation();
				
				double distance = navUtil.distance(nextPoint, location);
				
				/**
				 * ATTN : Jonathan
				 * Stub for moving to next node when the user gets within < 50ft of a turn
				 * this is how we will update the UI in correspondance to user proximity
				 * to a turn
				 */
				if(distance < .01) {
					if (route.index() < route.getNodes().size()) {
						previous = route.next();
						next = route.peekNext();
						previousPoint = previous.getLocation();
						nextPoint = next.getLocation();
					}
				}

				String distStr = navUtil.distanceStr(nextPoint, location, 0.1);
				float progress = (float) navUtil.progress(previousPoint,
						nextPoint, location);
				if (progress < 0) progress = 0;

				info.append(next.getInfo());
				proximity.append(distStr + "\n");
				//proximity.append(new DecimalFormat("#.00").format(progress * 100)
					//	+ "%\n");
				
				//ATTN JONATHAN : this is what I use to update the arrow image
				//ImageView imgView = (ImageView) findViewById(R.id.arrow_placeholder);
				//imgView.setImageResource(navUtil.getManeuverIcon(next));
				
				ArrowAnimation arrow = (ArrowAnimation) findViewById(R.id.arrowAnimation);
				arrow.setArrowType(next.getManeuverType());
				arrow.setFill(progress);

				final View beforeArrow = findViewById(R.id.text_before_arrow);
				final View afterArrow = findViewById(R.id.text_after_arrow);
				((TextView) beforeArrow).setText(info.toString());
				((TextView) afterArrow).setText(proximity.toString());
			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub 
			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub

			}
		};
	}
}
