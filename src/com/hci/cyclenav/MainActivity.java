package com.hci.cyclenav;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
//import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mapquest.android.maps.GeoPoint;
import com.mapquest.android.maps.MapActivity;
import com.mapquest.android.maps.MapView;
import com.mapquest.android.maps.MyLocationOverlay;
import com.mapquest.android.maps.RouteManager;
import com.mapquest.android.maps.RouteResponse;
import com.mapquest.android.maps.ServiceResponse.Info;

/* MainActivity.java
 * Layout: cycle-nav/res/layout/activity_main.xml
 * 
 * onCreate outlines a rough algorithm for map initialization:
 * 1. Get any saved instance from memory
 * 2. Set the view
 * 3. init the map
 * 5. configure the route manager
 * 6. get the user's location and localize the map
 * 
 * showGuidanceNarrative called by get_guidance_narrative onClick
 * 1. gets destination string
 * 2. gets user lat/lng
 * 3. stores them in an intent
 * 3. passes the intent and starts GuidanceNarrative activity
 * 
 * drawRoute is called by show_route onClick
 * 1. gets destination string from text field
 * 2. gets user location lat/lng
 * 3. clears the current route (if one exists)
 * 4. creates the route
 */

public class MainActivity extends MapActivity {
	public final static String DESTINATION = "com.hci.cyclenav.DESTINATION";
	public final static String USR_LAT = "com.hci.cyclenav.USR_LAT";
	public final static String USR_LNG = "com.hci.cyclenav.USR_LNG";
	protected static String porOrientation = "";
    protected static String lanOrientation = "";

	protected MapView map; // the map object
	private MyLocationOverlay myLocationOverlay; // a dot representing the user
	private RouteManager myRoute; // calculates and displays route
	private boolean routeDisplayed;
	private boolean validRoute = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setupMapView(); // make the map
		setupRoute(); // setup routeManager & err handling
		setupMyLocation(); // localize the map to the user
	}

	// called by get_guidance_narrative button onclick
	public void showGuidanceNarrative(View view) {
		if(myLocationOverlay.getMyLocation() != null) {
			// make a new intent to be passed to the GuidanceNarrative activity
			Intent intent = new Intent(this, GuidanceNarrative.class);
			EditText editText = (EditText) findViewById(R.id.location_field);
			String to = editText.getText().toString();
	
			// Get the user's location from myLocationOverlay
			double lat = myLocationOverlay.getMyLocation().getLatitude();
			double lng = myLocationOverlay.getMyLocation().getLongitude();
	
			// add the destination & user location
			intent.putExtra(DESTINATION, to);
			intent.putExtra(USR_LAT, lat);
			intent.putExtra(USR_LNG, lng);
	
			// start a new GuidanceNarrative
			startActivity(intent);
		} else {
			Toast.makeText(getApplicationContext(), "Attempting to get your location...",
					Toast.LENGTH_LONG).show();
		}
		
	}

	// moves the map view so the user's location is centered on the screen
	public void setUserFocus(View view) {
		GeoPoint currentLocation = myLocationOverlay.getMyLocation();
		map.getController().animateTo(currentLocation);
		map.getController().setZoom(14);
		map.getOverlays().add(myLocationOverlay);
	}

	// Called when the user presses the 'pin' button
	public void drawRoute(View view) {
		if(myLocationOverlay.getMyLocation() != null) {
			EditText editText = (EditText) findViewById(R.id.location_field);
			String to = editText.getText().toString();
	
			// Get the user's location from myLocationOverlay
			String from = myLocationOverlay.getMyLocation().getLatitude() + ","
					+ myLocationOverlay.getMyLocation().getLongitude();
	
			// Clear the current route ribbon (if one exists) and make a new one
			if (isRouteDisplayed())
				myRoute.clearRoute();
			
			Toast.makeText(getApplicationContext(), "Calculating route...",
					Toast.LENGTH_LONG).show();
			
			myRoute.createRoute(from, to);
			// map.getController().zoomOut();
			setDisplayed(true);
		} else {
			Toast.makeText(getApplicationContext(), "Attempting to get your location...",
					Toast.LENGTH_LONG).show();
		}
	}

	// Link the map in the layout to the mapView object
	private void setupMapView() {
		this.map = (MapView) findViewById(R.id.map);
	}

	// set up a MyLocationOverlay and execute the runnable once we have a
	// location fix
	private void setupMyLocation() {
		this.myLocationOverlay = new MyLocationOverlay(this, map);
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.runOnFirstFix(new Runnable() {
			@Override
			public void run() {
				GeoPoint currentLocation = myLocationOverlay.getMyLocation();
				map.getController().animateTo(currentLocation);
				map.getController().setZoom(14);
				map.getOverlays().add(myLocationOverlay);

				// should be set to true once navigation begins:
				myLocationOverlay.setFollowing(false); // don't follow the
														// user's location
			}
		});
	}

	// enable features of the overlay
	@Override
	protected void onResume() {
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.enableCompass();
		super.onResume();
	}

	// disable features of the overlay when in the background
	@Override
	protected void onPause() {
		super.onPause();
		myLocationOverlay.disableCompass();
		myLocationOverlay.disableMyLocation();
	}

	// setup route manager and link to mapview
	private void setupRoute() {
		String key = getString(R.string.api_key);
		myRoute = new RouteManager(this, key);
		myRoute.setMapView(map);
		myRoute.setBestFitRoute(false);

		/*
		 * Configure error handling and success feedback notifications when the
		 * user attempts to create a new route
		 * 
		 * RouteCallback throws RouteResponse objects when createRoute() is
		 * called if an error occurs, the code in onError() runs and a error
		 * Toast is generated if the route is generated successfully,
		 * onSuccess() is called
		 */
		myRoute.setRouteCallback(new RouteManager.RouteCallback() {
			@Override
			public void onError(RouteResponse routeResponse) {
				validRoute = false;
				Info info = routeResponse.info;
				int statusCode = info.statusCode;

				StringBuilder message = new StringBuilder();
				message.append("Unable to create route.\n").append("Error: ")
						.append(statusCode).append("\n").append("Message: ")
						.append(info.messages);
				Toast.makeText(getApplicationContext(), message.toString(),
						Toast.LENGTH_LONG).show();
			}

			@Override
			public void onSuccess(RouteResponse routeResponse) {
				validRoute = true;
			}
		});
	}

	/*
	 * isRouteDisplayed() and setDisplayed() are just used to check if a
	 * previous route needs to be cleared
	 */
	@Override
	public boolean isRouteDisplayed() {
		return routeDisplayed;
	}

	private void setDisplayed(boolean isDisplayed) {
		routeDisplayed = isDisplayed;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		final Context context = this;
		switch (item.getItemId()) {
		case R.id.help:
			AlertDialog myDialog;
			View alertview;
			
			AlertDialog.Builder helpBuilder = new AlertDialog.Builder(context);
			LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
			alertview = inflater.inflate(R.layout.helplayout, null);
			helpBuilder.setTitle(R.string.help);
			helpBuilder.setView(alertview)
			.setNeutralButton(R.string.helpexit, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			
			myDialog = helpBuilder.create();
			myDialog.show();
			return true;
		case R.id.action_settings:
			AlertDialog levelDialog;
			CharSequence[] items={"Portrait","Landscape"};
				AlertDialog.Builder settingDialog = new AlertDialog.Builder(this);
				settingDialog.setTitle(R.string.screen_orientation);
				settingDialog.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						switch(item){
						case 0:
							MainActivity.porOrientation="True";
							MainActivity.lanOrientation="False";
							setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
							break;
						case 1:
							MainActivity.lanOrientation="True";
							MainActivity.porOrientation="False";
							setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
							break;
						}
					dialog.dismiss();
					}
				});
			levelDialog = settingDialog.create();
			levelDialog.show();
			return true;
		}
			
		return false;
	}

}
