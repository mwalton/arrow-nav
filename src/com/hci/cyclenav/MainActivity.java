package com.hci.cyclenav;

import android.content.Intent;
import android.os.Bundle;
//import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.mapquest.android.maps.GeoPoint;
import com.mapquest.android.maps.MapActivity;
import com.mapquest.android.maps.MapView;
import com.mapquest.android.maps.MyLocationOverlay;
import com.mapquest.android.maps.RouteManager;

public class MainActivity extends MapActivity {

	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
	protected MapView map;
    private MyLocationOverlay myLocationOverlay;
    private String myLocation_latLng;
    private String toLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      setupMapView();
      setupMyLocation();
      //displayRoute();
    }
    
    public void searchLocation(View view) {
    	EditText editText = (EditText) findViewById(R.id.location_field);
    	String searchText = editText.getText().toString();
    	
    	toLocation = searchText;
    	displayRoute();
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
          myLocation_latLng = myLocationOverlay.getMyLocation().getLatitude() + "," + myLocationOverlay.getMyLocation().getLongitude();
          map.getController().animateTo(currentLocation);
          map.getController().setZoom(14);
          map.getOverlays().add(myLocationOverlay);
          //displayRoute();
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


    // create a route and display on the map 
    private void displayRoute() {
      RouteManager routeManager = new RouteManager(this);
      routeManager.setMapView(map);
      routeManager.createRoute(myLocation_latLng, toLocation);
    }

    @Override
    public boolean isRouteDisplayed() {
      return true;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
