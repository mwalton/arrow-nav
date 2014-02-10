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


/* MainActivity.java (at some point should be named something more descriptive)
 * Layout: cycle-nav/res/layout/activity_main.xml
 * 
 * onCreate outlines a rough algorithm for map initialization:
 * 1. Get any saved instance from memory
 * 2. Set the view
 * 3. init the map
 * 5. configure the route manager
 * 6. get the user's location and localize the map
 * 
 * Search location runs when the search button is pressed
 * 1. gets destination string from text field
 * 2. gets user location lat/lng
 * 3. clears the current route (if one exists)
 * 4. creates the route
 */

public class MainActivity extends MapActivity {
	public final static String DESTINATION = "com.example.myfirstapp.DESTINATION";
	public final static String USR_LAT = "com.example.myfirstapp.USR_LAT";
	public final static String USR_LNG = "com.example.myfirstapp.USR_LNG";
	
	protected MapView map;							//the map object
    private MyLocationOverlay myLocationOverlay;	//a dot representing the user
    private RouteManager myRoute;					//calculates and displays route
    private boolean routeDisplayed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      
      setupMapView();								//make the map
      setupRoute();									//setup routeManager & err handling
      setupMyLocation();							//localize the map to the user
    }
    
    public void beginNavigation(View view) {
    	Intent intent = new Intent(this, CycleNavigation.class);
    	EditText editText = (EditText) findViewById(R.id.location_field);
    	String to = editText.getText().toString();
    	
    	//Get the user's location from myLocationOverlay
    	double lat = myLocationOverlay.getMyLocation().getLatitude();
    	double lng = myLocationOverlay.getMyLocation().getLongitude();
    	
    	intent.putExtra(DESTINATION, to);
    	intent.putExtra(USR_LAT, lat);
    	intent.putExtra(USR_LNG, lng);
    	
    	startActivity(intent);
    }
    
    //Called when the user presses the search button
    public void searchLocation(View view) {
    	EditText editText = (EditText) findViewById(R.id.location_field);
    	String to = editText.getText().toString();
    	
    	//Get the user's location from myLocationOverlay
    	String from = myLocationOverlay.getMyLocation().getLatitude() + 
    			"," + myLocationOverlay.getMyLocation().getLongitude();
    	
    	//Clear the current route ribbon (if one exists) and make a new one
    	if (isRouteDisplayed()) myRoute.clearRoute();
    	myRoute.createRoute(from, to);
    	setDisplayed(true);
    }
    
    //Link the map in the layout to the mapView object
    private void setupMapView() {
    	this.map = (MapView) findViewById(R.id.map);
    }

    // set up a MyLocationOverlay and execute the runnable once we have a location fix 
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
          
          //should be set to true once navigation begins:
          myLocationOverlay.setFollowing(false);  //don't follow the user's location
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
        
        /*Configure error handling and success feedback notifications
         * when the user attempts to create a new route
         * 
         * RouteCallback throws RouteResponse objects when createRoute() is called
         * if an error occurs, the code in onError() runs and a error Toast is generated
         * if the route is generated successfully, onSuccess() is called
         */
        myRoute.setRouteCallback(new RouteManager.RouteCallback() {
			@Override
			public void onError(RouteResponse routeResponse) {
				Info info=routeResponse.info;
				int statusCode=info.statusCode;
				
				StringBuilder message = new StringBuilder();
				message.append("Unable to create route.\n")
					.append("Error: ").append(statusCode).append("\n")
					.append("Message: ").append(info.messages);
				Toast.makeText(getApplicationContext(), message.toString(), Toast.LENGTH_LONG).show();
			}

			@Override
			public void onSuccess(RouteResponse routeResponse) {
				Toast.makeText(getApplicationContext(), "Calculating route", Toast.LENGTH_LONG).show();
			}
		});
    }

    /* isRouteDisplayed() and setDisplayed() are just used to
    *  check if a previous route needs to be cleared
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

}
