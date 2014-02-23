package com.hci.cyclenav.guidance;

import com.mapquest.android.maps.GeoPoint;

import android.location.Location;

public class NavigationUtil {
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	
	public NavigationUtil() {}
	
	/**
    * Convenience methods for computing distance
    */
	
	//get the 'progress' or percent complete (a value between 0 - 1)
	double progress(GeoPoint from, GeoPoint to, GeoPoint usrLoc) {
		double totalDistance = distance(from, to);
		double distanceRemaining = distance(usrLoc, to);
		return (totalDistance - distanceRemaining) / totalDistance;
	}
	
	//get the distance in miles between two GeoPoints
	double distance(GeoPoint pnt1, GeoPoint pnt2) {
		double lat1 = pnt1.getLatitude();
		double lng1 = pnt1.getLongitude();
		double lat2 = pnt2.getLatitude();
		double lng2 = pnt2.getLongitude();
	
		double theta = lng1 - lng2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
	      
		return dist;
	}
	

    // This function converts decimal degrees to radians
    private double deg2rad(double deg) {
    	return (deg * Math.PI / 180.0);
    }

    //This function converts radians to decimal degrees
    private double rad2deg(double rad) {
    	return (rad * 180.0 / Math.PI);
    }
	
	/**
    * LocationListener accuracy verification methods
    */

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}

}
