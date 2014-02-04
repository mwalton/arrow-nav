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

public class MainActivity extends MapActivity {

	protected MapView map;
    private MyLocationOverlay myLocationOverlay;
    private RouteManager myRoute;
    private boolean routeOverlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      
      setupMapView();
      setupRoute();
      setupMyLocation();
    }
    
    
    public void searchLocation(View view) {
    	EditText editText = (EditText) findViewById(R.id.location_field);
    	String searchText = editText.getText().toString();
    	
    	String from = myLocationOverlay.getMyLocation().getLatitude() + 
    			"," + myLocationOverlay.getMyLocation().getLongitude();
    	String to = searchText;
    	if (isRouteDisplayed()) myRoute.clearRoute();
    	myRoute.createRoute(from, to);
    	setDisplayed(true);
    }

    // set map
    private void setupMapView() {
      this.map = (MapView) findViewById(R.id.map);
      //map.setBuiltInZoomControls(true);
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
          //myLocationOverlay.setFollowing(true);
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
				//STUB
			}
		});
    }

    @Override
    public boolean isRouteDisplayed() {
      return routeOverlay;
    }
    
    private void setDisplayed(boolean isDisplayed) {
    	routeOverlay = isDisplayed;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
